package org.spin.ftp;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPListParseEngine;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.sys.ErrorCode;
import org.spin.throwable.SimplifiedException;
import org.spin.util.EnumUtils;
import org.spin.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * FTP工具类
 *
 * @version V1.0
 */
public class FtpOperator {
    static final Logger logger = LoggerFactory.getLogger(FtpOperator.class);

    private String key;
    private String protocal;
    private String userName;
    private String password;
    private String host;
    private int port = 21;
    private FTPClient client;

    public enum Protocal {
        FTP("ftp"), FTPS("ftps");
        private String value;

        Protocal(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    private static final Map<String, FtpOperator> ftpClients = new ConcurrentHashMap<>();
    private static final Pattern protocalPattern = Pattern.compile("^(ftp[s]?)://(.+:.+@)?([^:]+)(:\\d{2,5})?$", Pattern.CASE_INSENSITIVE);

    private FtpOperator() {
    }

    /**
     * 创建连接
     *
     * @param url 标准FTP连接地址，形如：ftp://用户名:密码@服务器ip地址
     */
    public static FtpOperator connect(String url) {
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

        return connect(protocal, userName, password, host, StringUtils.isEmpty(port) ? 21 : Integer.parseInt(port.substring(1)));
    }

    public static FtpOperator connect(Protocal protocal, String userName, String password, String host) {
        return connect(protocal, userName, password, host, 21);
    }

    public static FtpOperator connect(String userName, String password, String host) {
        return connect(Protocal.FTP, userName, password, host, 21);
    }

    public static FtpOperator connect(Protocal protocal, String userName, String password, String host, int port) {
        Protocal p = protocal;
        if (null == protocal)
            p = Protocal.FTP;
        String key = p.getValue() + userName + host + port;
        FtpOperator ftp = ftpClients.get(key);
        if (null == ftp) {
            ftp = new FtpOperator();
            ftp.key = key;
            ftp.protocal = "ftp";
            if (p.equals(Protocal.FTP))
                ftp.client = new FTPClient();
            else if (p.equals(Protocal.FTPS))
                ftp.client = new FTPSClient("SSL");
            ftp.host = host;
            ftp.port = port;
            ftp.userName = userName;
            ftp.password = password;
            ftp.conn();
            ftpClients.put(key, ftp);
        }
        return ftp;
    }

    public int sendCommands(String command) {
        try {
            return client.sendCommand(command);
        } catch (IOException e) {
            logger.error("执行命令失败: {}", client.getReplyString(), e);
        }
        return -1;
    }

    public void listRemoteAllFiles(String path) {
        try {
            FTPListParseEngine f = client.initiateListParsing(path);

            while (f.hasNext()) {
                FTPFile[] files = f.getNext(5);
                for (FTPFile file : files) {
                    disFile(file, path);
                }
            }
        } catch (IOException e) {
            logger.error("列出文件失败: ", e);
        }
    }

    public void disFile(FTPFile file, String path) {
        if (file.isDirectory() && !file.getName().equals(".") && !file.getName().equals("..")) {
            logger.info(File.separator + file.getName());
            listRemoteAllFiles(path + File.separator + file.getName());
        } else if (!file.getName().equals(".") && !file.getName().equals("..")) {
            logger.info(file.getName());
        }
    }

    public boolean downFile(String remotePath, String fileName, String localPath) {
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

    public boolean downFile(String fullPath, String localPath) {
        int lastIdx = fullPath.lastIndexOf("/") + 1;
        if (lastIdx <= 0)
            lastIdx = fullPath.lastIndexOf("\\") + 1;
        String dir = fullPath.substring(0, lastIdx);
        String fileName = fullPath.substring(lastIdx);
        return downFile(dir, fileName, localPath);
    }

    public boolean downFile(String fullPath, OutputStream os) {
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

    public boolean upFile(String path, String filename, String localFilePath) {
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

    public boolean upFile(String path, String filename, InputStream local) {
        boolean flag = false;
        try {
            client.changeWorkingDirectory(path);
            flag = client.storeFile(filename, local);
            local.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    public boolean mkdir(String path) {
        try {
            return client.makeDirectory(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 关闭连接
     */
    public void closeConnect() {
        try {
            if (client != null) {
                client.logout();
                client.disconnect();
                ftpClients.remove(key);
                logger.info("关闭ftp服务器");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void conn() {
        if (null == client)
            throw new SimplifiedException("FTP客户端未初始化");
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
        client.setControlEncoding("UTF-8");
        client.enterLocalPassiveMode();
        try {
            if (!client.setFileType(FTPClient.BINARY_FILE_TYPE))
                throw new SimplifiedException("无法设置FileType到BIN模式");
        } catch (IOException e) {
            throw new SimplifiedException("无法设置FileType到BIN模式");
        }
    }
}