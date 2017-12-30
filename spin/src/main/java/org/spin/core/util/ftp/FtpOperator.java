package org.spin.core.util.ftp;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPListParseEngine;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.ErrorCode;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.EnumUtils;
import org.spin.core.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * FTP工具类
 * <p>非线程安全</p>
 * <p>Created by xuweinan on 2016/8/22.</p>
 *
 * @author xuweinan
 * @version V1.1
 */
public class FtpOperator implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(FtpOperator.class);
    private static final Pattern protocalPattern = Pattern.compile("^(ftp[s]?)://(.+:.+@)?([^:]+)(:\\d{2,5})?$", Pattern.CASE_INSENSITIVE);

    private String name;
    private Protocal protocal;
    private String userName;
    private String password;
    private String host;
    private int port = 21;

    private String localCharset = System.getProperty("sun.jnu.encoding");
    private String serverCharset = "ISO-8859-1";

    private FTPClient client;

    /**
     * 创建FTP客户端
     *
     * @param url 标准FTP连接地址，形如：ftp://用户名:密码@服务器ip地址
     */
    public FtpOperator(String url) {
        Matcher matcher = protocalPattern.matcher(url);
        if (!matcher.matches())
            throw new SimplifiedException("FTP连接URL格式错误");

        Protocal protocal = EnumUtils.getEnum(Protocal.class, matcher.group(1).toLowerCase());
        String token = matcher.group(2);
        String userName = null;
        String password = null;
        if (StringUtils.isNotEmpty(token)) {
            int idx = token.indexOf(':');
            userName = token.substring(0, idx);
            password = token.substring(idx + 1, token.length() - 1);
        }
        String host = matcher.group(3).toLowerCase();
        String port = matcher.group(4);

        Protocal p = null == protocal ? Protocal.FTP : protocal;
        this.protocal = p;
        this.host = host;
        this.port = StringUtils.isEmpty(port) ? 21 : Integer.parseInt(port.substring(1));
        this.userName = userName;
        this.password = password;
        switch (p) {
            case FTP:
                this.client = new FTPClient();
                break;
            case FTPS:
                this.client = new FTPSClient("SSL");
                break;
        }
    }

    /**
     * 创建FTP客户端
     *
     * @param protocal 协议
     * @param userName 用户名
     * @param password 密码
     * @param host     主机
     */
    public FtpOperator(Protocal protocal, String userName, String password, String host) {
        this(protocal, userName, password, host, 21);
    }

    /**
     * 创建FTP客户端
     *
     * @param userName 用户名
     * @param password 密码
     * @param host     主机
     */
    public FtpOperator(String userName, String password, String host) {
        this(Protocal.FTP, userName, password, host, 21);
    }

    /**
     * 创建FTP客户端
     *
     * @param protocal 协议
     * @param userName 用户名
     * @param password 密码
     * @param host     主机
     * @param port     端口
     */
    public FtpOperator(Protocal protocal, String userName, String password, String host, int port) {
        Protocal p = null == protocal ? Protocal.FTP : protocal;
        this.protocal = p;
        this.host = host;
        this.port = port;
        this.userName = userName;
        this.password = password;
        switch (p) {
            case FTP:
                this.client = new FTPClient();
                break;
            case FTPS:
                this.client = new FTPSClient("SSL");
                break;
        }
    }

    /**
     * 执行FTP指令
     *
     * @param command 指令文本
     * @return 状态码
     */
    public int sendCommands(String command, String args) {
        this.connect();
        try {
            return client.sendCommand(command, args);
        } catch (IOException e) {
            logger.error("执行命令失败: {}", client.getReplyString(), e);
        }
        return -1;
    }

    /**
     * 列出指定路径下所有远程文件
     *
     * @param path 远程路径
     */
    public void listRemoteAllFiles(String path) {
        this.connect();
        try {
            FTPListParseEngine f = client.initiateListParsing(path);

            while (f.hasNext()) {
                FTPFile[] files = f.getNext(5);
                for (FTPFile file : files) {
                    if (file.isDirectory() && !file.getName().equals(".") && !file.getName().equals("..")) {
                        logger.info(File.separator + file.getName());
                        listRemoteAllFiles(path + File.separator + file.getName());
                    } else if (!file.getName().equals(".") && !file.getName().equals("..")) {
                        logger.info(file.getName());
                    }
                }
            }
        } catch (IOException e) {
            logger.error("列出文件失败: ", e);
        }
    }

    public void disFile(FTPFile file, String path) {
        this.connect();
        if (file.isDirectory() && !file.getName().equals(".") && !file.getName().equals("..")) {
            logger.info(File.separator + file.getName());
            listRemoteAllFiles(path + File.separator + file.getName());
        } else if (!file.getName().equals(".") && !file.getName().equals("..")) {
            logger.info(file.getName());
        }
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
        int lastIdx = fullPath.lastIndexOf("/") + 1;
        if (lastIdx <= 0)
            lastIdx = fullPath.lastIndexOf("\\") + 1;
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
            int lastIdx = fullPath.lastIndexOf("/") + 1;
            if (lastIdx <= 0)
                lastIdx = fullPath.lastIndexOf("\\") + 1;

            String dir = fullPath.substring(0, lastIdx);
            String fileName = fullPath.substring(lastIdx);
            client.changeWorkingDirectory(dir);//进到FTP服务器目录
            fs = client.listFiles();
            for (FTPFile ff : fs) {
                if (ff.getName().equals(fileName)) {
                    flag = client.retrieveFile(ff.getName(), os);
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("下载文件失败", fullPath, e);
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
        try {
            FileInputStream in = new FileInputStream(new File(localFilePath));
            if (StringUtils.isNotEmpty(path))
                client.changeWorkingDirectory(path);
            flag = client.storeFile(filename, in);
            in.close();
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
            if (client != null && client.isConnected()) {
                client.logout();
            }
        } catch (Exception e) {
            logger.error("登出ftp服务器失败", e);
        } finally {
            logger.info("关闭ftp服务器连接");
            try {
                if (client != null) {
                    client.disconnect();
                }
            } catch (Exception e) {
                logger.info("关闭ftp服务器连接异常", e);
            }
        }
    }

    private void connect() {
        if (null == client) {
            throw new SimplifiedException("FTP客户端未初始化");
        }
        if (client.isConnected()) {
            return;
        }
        client.setDefaultPort(port);

        try {
            client.connect(host);
        } catch (IOException e) {
            throw new SimplifiedException(ErrorCode.NETWORK_EXCEPTION, "FTP连接失败");
        }
        if (StringUtils.isNotEmpty(userName)) {
            try {
                if (!client.login(userName, password)) {
                    throw new SimplifiedException(ErrorCode.NETWORK_EXCEPTION, "FTP登录失败");
                }
            } catch (IOException e) {
                throw new SimplifiedException(ErrorCode.NETWORK_EXCEPTION, "FTP登录失败");
            }
        }

        int reply = client.getReplyCode();

        if (!FTPReply.isPositiveCompletion(reply)) {
            try {
                client.disconnect();
            } catch (IOException e) {
                throw new SimplifiedException(ErrorCode.NETWORK_EXCEPTION, "FTP访问被拒绝，断开连接失败");
            }
            throw new SimplifiedException(ErrorCode.NETWORK_EXCEPTION, "FTP访问被拒绝: " + host + ":" + port);
        }

        client.setDataTimeout(30000);
        // 开启服务器对UTF-8的支持，如果服务器支持就用UTF-8编码，否则就使用本地编码.
        try {
            if (FTPReply.isPositiveCompletion(client.sendCommand("OPTS UTF8", "ON"))) {
                localCharset = "UTF-8";
            }
        } catch (IOException e) {
        }
        client.setControlEncoding(localCharset);
        client.enterLocalPassiveMode();
        try {
            if (!client.setFileType(FTPClient.BINARY_FILE_TYPE))
                throw new SimplifiedException("无法设置FileType到BIN模式");
        } catch (IOException e) {
            throw new SimplifiedException("无法设置FileType到BIN模式");
        }
    }
}
