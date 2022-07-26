package org.spin.core.util.ftp;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.ErrorCode;
import org.spin.core.throwable.SpinException;
import org.spin.core.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * FTP客户端连接
 * <p>
 * 非线程安全，避免多线程对同一连接同时进行操作<br>
 * 一个连接只能<strong>顺序地依次执行</strong>一组FTP动作。
 * 同时使用同一连接进行FTP操作，或者在上一操作尚未完成的情况下进行下一操作，可能导致无法预计的问题
 * </p>
 * <p>Created by xuweinan on 2016/8/22.</p>
 *
 * @author xuweinan
 * @version V1.1
 */
public class FtpConnection implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(FtpConnection.class);
    private static final Pattern PROTOCOL_PATTERN = Pattern.compile("^(ftp[s]?)://(.+:.+@)?([^:]+)(:\\d{2,5})?$", Pattern.CASE_INSENSITIVE);

    private String name;
    private final boolean secure;
    private final String protocol;
    private final String userName;
    private final String password;
    private final String host;
    private final int port;

    private Charset serverCharset = Charset.forName("GBK");

    private final FTPClient client;

    /**
     * 创建FTP客户端
     *
     * @param url      标准FTP连接地址，形如：ftp://用户名:密码@服务器ip地址
     * @param protocol 如果是ftps连接，需指明加密协议
     * @return FTP连接
     */
    public static FtpConnection ofUrl(String url, String protocol) {
        Matcher matcher = PROTOCOL_PATTERN.matcher(url);
        if (!matcher.matches()) {
            throw new SpinException("FTP连接URL格式错误");
        }

        String token = matcher.group(2);
        String u = null;
        String pwd = null;
        if (StringUtils.isNotEmpty(token)) {
            int idx = token.indexOf(':');
            u = token.substring(0, idx);
            pwd = token.substring(idx + 1, token.length() - 1);
        }
        String h = matcher.group(3).toLowerCase();
        String p = matcher.group(4);
        return new FtpConnection(StringUtils.trimToSpec(protocol, "TLS"), u, pwd, h, StringUtils.isEmpty(p) ? 21 : Integer.parseInt(p.substring(1)));
    }

    /**
     * 创建FTP客户端
     *
     * @param host 主机
     */
    public FtpConnection(String host) {
        this(null, null, null, host, 21);
    }

    /**
     * 创建FTP客户端
     *
     * @param host 主机
     * @param port 端口
     */
    public FtpConnection(String host, int port) {
        this(null, null, null, host, port);
    }

    /**
     * 创建FTP客户端
     *
     * @param userName 用户名
     * @param password 密码
     * @param host     主机
     */
    public FtpConnection(String userName, String password, String host) {
        this(null, userName, password, host, 21);
    }

    /**
     * 创建FTP客户端
     *
     * @param userName 用户名
     * @param password 密码
     * @param host     主机
     * @param port     端口
     */
    public FtpConnection(String userName, String password, String host, int port) {
        this(null, userName, password, host, port);
    }

    /**
     * 创建FTP客户端
     *
     * @param protocol 加密协议
     * @param userName 用户名
     * @param password 密码
     * @param host     主机
     * @param port     端口
     */
    public FtpConnection(String protocol, String userName, String password, String host, int port) {
        secure = StringUtils.isNotEmpty(protocol);
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.userName = userName;
        this.password = password;
        client = secure ? new FTPSClient(protocol) : new FTPClient();
    }

    /**
     * 执行FTP指令
     *
     * @param command 指令文本
     * @param args    参数
     * @return 状态码
     */
    public int sendCommand(String command, String args) {
        this.connect();
        try {
            return client.sendCommand(command, args);
        } catch (IOException e) {
            logger.error("执行命令失败: {}", client.getReplyString(), e);
        }
        return -1;
    }

    /**
     * 执行FTP指令
     *
     * @param command 指令文本
     * @return 状态码
     */
    public int sendCommand(String command) {
        return sendCommand(command, null);
    }

    /**
     * 列出指定远程目录下所有文件(及目录)名称，目录名称以"/"结尾
     *
     * @param remotePath 远程目录
     * @param filter     过滤器
     * @return 文件名列表
     */
    public List<String> listFileNames(String remotePath, FTPFileFilter filter) {
        this.connect();
        try {
            return Arrays.stream(client.listFiles(remotePath, filter)).map(f -> f.isDirectory() ? f.getName() + "/" : f.getName())
                .filter(n -> !"./".equals(n) && !"../".equals(n)).collect(Collectors.toList());
        } catch (IOException e) {
            throw new SpinException(ErrorCode.NETWORK_EXCEPTION, "无法列出远程目录下的文件", e);
        }
    }

    /**
     * 列出指定远程目录及子目录下所有文件(及目录)名称，目录名称以"/"结尾
     *
     * @param remotePath 远程目录
     * @param filter     过滤器
     * @return 文件名列表
     */
    public List<String> listAllFileNames(String remotePath, FTPFileFilter filter) {
        this.connect();
        Queue<String> dirs = new LinkedList<>();
        List<String> fileNames = new LinkedList<>();
        dirs.add(remotePath);
        String path;
        FTPFile[] ftpFiles;
        while (!dirs.isEmpty()) {
            path = dirs.poll();
            try {
                ftpFiles = client.listFiles(path, filter);
                for (FTPFile f : ftpFiles) {
                    if (!".".equals(f.getName()) && "..".equals(f.getName())) {
                        if (f.isDirectory()) {
                            dirs.add(path + "/" + f.getName());
                            fileNames.add(f.getName() + "/");
                        } else {
                            fileNames.add(f.getName());
                        }
                    }
                }

            } catch (IOException e) {
                throw new SpinException(ErrorCode.NETWORK_EXCEPTION, "无法列出远程目录下的文件", e);
            }
        }
        return fileNames;
    }

    /**
     * 下载文件
     *
     * @param remotePath 远程路径
     * @param fileName   文件名
     * @param localPath  本地存储路径
     * @return 是否成功
     */
    public boolean retrieveFile(String remotePath, String fileName, String localPath) {
        this.connect();
        boolean flag = false;
        try {
            client.changeWorkingDirectory(remotePath);
            File localFile = new File(localPath + File.separator + fileName);
            logger.info(localPath + File.separator + fileName);
            FileOutputStream is = new FileOutputStream(localFile);
            flag = client.retrieveFile(fileName, is);
            is.close();
        } catch (IOException e) {
            logger.error("下载文件失败: {}", client.getReplyString(), e);
        }
        return flag;
    }

    /**
     * 下载文件
     *
     * @param fullPath  远程文件全名
     * @param localPath 本地存储路径
     * @return 是否成功
     */
    public boolean retrieveFile(String fullPath, String localPath) {
        this.connect();
        int lastIdx = fullPath.lastIndexOf('/') + 1;
        if (lastIdx <= 0) {
            lastIdx = fullPath.lastIndexOf('\\') + 1;
        }
        String dir = fullPath.substring(0, lastIdx);
        String fileName = fullPath.substring(lastIdx);
        return retrieveFile(dir, fileName, localPath);
    }

    /**
     * 下载文件到输出流
     *
     * @param fullPath 远程文件全名
     * @param os       输出流
     * @return 是否成功
     */
    public boolean retrieveFile(String fullPath, OutputStream os) {
        this.connect();
        boolean flag = false;
        FTPFile[] fs;
        try {
            int lastIdx = fullPath.lastIndexOf('/') + 1;
            if (lastIdx <= 0) {
                lastIdx = fullPath.lastIndexOf('\\') + 1;
            }

            String dir = fullPath.substring(0, lastIdx);
            String fileName = fullPath.substring(lastIdx);
            // 进到FTP服务器目录
            client.changeWorkingDirectory(dir);
            fs = client.listFiles();
            for (FTPFile ff : fs) {
                if (ff.getName().equals(fileName)) {
                    flag = client.retrieveFile(ff.getName(), os);
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("下载文件失败: " + fullPath, e);
        }
        return flag;
    }

    /**
     * 上传文件
     *
     * @param path          远程路径
     * @param filename      文件名
     * @param localFilePath 本地路径
     * @return 是否成功
     */
    public boolean storeFile(String path, String filename, String localFilePath) {
        this.connect();
        boolean flag = false;
        try (FileInputStream in = new FileInputStream(new File(localFilePath))) {
            if (StringUtils.isNotEmpty(path)) {
                client.changeWorkingDirectory(path);
            }
            flag = client.storeFile(filename, in);
        } catch (Exception e) {
            logger.error("上传文件失败", e);
        }
        return flag;
    }

    /**
     * 从输入流中读取并上传文件
     *
     * @param path     远程路径
     * @param filename 文件名
     * @param local    本地输入流
     * @return 是否成功
     */
    public boolean storeFile(String path, String filename, InputStream local) {
        this.connect();
        boolean flag = false;
        try {
            client.changeWorkingDirectory(path);
            flag = client.storeFile(filename, local);
            local.close();

        } catch (Exception e) {
            logger.error("上传文件失败", e);
        }
        return flag;
    }

    /**
     * 创建目录
     * <p>只创建当前指定目录，如果父文目录不存在，会创建失败</p>
     *
     * @param path 远程路径
     * @return 是否成功
     */
    public boolean mkDir(String path) {
        this.connect();
        String p = StringUtils.replace(path, "\\", "/");
        try {
            return client.makeDirectory(p);
        } catch (Exception e) {
            logger.error("创建文件夹失败", e);
        }
        return false;
    }

    /**
     * 创建目录
     * <p>创建当前指定目录，如果父文目录不存在，会一并创建</p>
     *
     * @param path 远程路径
     * @return 是否成功
     */
    public boolean mkAllDir(String path) {
        this.connect();
        String p = StringUtils.replace(path, "\\", "/");
        int i = p.indexOf('/');
        String t;
        while (i > 0) {
            t = p.substring(0, i);
            i = p.indexOf('/', i + 1);
            try {
                return client.makeDirectory(t);
            } catch (Exception e) {
                logger.error("创建文件夹失败", e);
            }
        }
        try {
            return client.makeDirectory(p);
        } catch (Exception e) {
            logger.error("创建文件夹失败", e);
        }
        return false;
    }

    /**
     * 关闭连接
     */
    @Override
    public void close() {
        try {
            if (isAvailable()) {
                client.logout();
            }
        } catch (Exception e) {
            logger.error("登出FTP服务器失败", e);
        } finally {
            logger.info("关闭FTP服务器连接");
            try {
                if (client.isConnected()) {
                    client.disconnect();
                }
            } catch (Exception e) {
                logger.error("关闭FTP服务器连接异常", e);
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSecure() {
        return secure;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public Charset getServerCharset() {
        return serverCharset;
    }

    public void setServerCharset(Charset serverCharset) {
        this.serverCharset = serverCharset;
        close();
        connect();
    }

    public boolean isAvailable() {
        if (client.isConnected()) {
            try {
                return FTPReply.isPositiveCompletion(client.sendCommand("NOOP"));
            } catch (IOException e) {
                return false;
            }
        }
        return false;
    }

    private void connect() {
        if (isAvailable()) {
            return;
        }
        if (!client.isConnected()) {
            client.setDefaultPort(port);

            try {
                client.connect(host);
            } catch (IOException e) {
                throw new SpinException(ErrorCode.NETWORK_EXCEPTION, "FTP连接失败: " + host + ":" + port);
            }
        }
        if (StringUtils.isNotEmpty(userName)) {
            try {
                if (!client.login(userName, password)) {
                    String replyString = client.getReplyString();
                    try {
                        client.disconnect();
                    } catch (IOException e) {
                        throw new SpinException(ErrorCode.NETWORK_EXCEPTION, "FTP访问被拒绝，断开连接失败");
                    }
                    throw new SpinException(ErrorCode.NETWORK_EXCEPTION, "FTP登录失败: " + host + ":" + port + " [" + replyString + "]");
                }
            } catch (IOException e) {
                throw new SpinException(ErrorCode.NETWORK_EXCEPTION, "FTP登录失败: " + host + ":" + port, e);

            }
        }

        client.setDataTimeout(30000);
        // 开启服务器对UTF-8的支持，如果服务器支持就用UTF-8编码，否则就使用默认编码.
        try {
            if (FTPReply.isPositiveCompletion(client.sendCommand("OPTS UTF8", "ON"))) {
                serverCharset = StandardCharsets.UTF_8;
            }
        } catch (IOException e) {
            // ignore
        }
        client.setControlEncoding(serverCharset.name());
        client.enterLocalPassiveMode();
        try {
            if (!client.setFileType(FTPClient.BINARY_FILE_TYPE)) {
                throw new SpinException("无法设置FileType到BIN模式");
            }
        } catch (IOException e) {
            throw new SpinException("无法设置FileType到BIN模式", e);
        }
    }
}
