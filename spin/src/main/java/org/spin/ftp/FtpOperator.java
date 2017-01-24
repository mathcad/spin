package org.spin.ftp;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPListParseEngine;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.spin.throwable.SimplifiedException;
import org.spin.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
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

        String protocal = matcher.group(1).toLowerCase();
        String token = matcher.group(2);
        String host = matcher.group(3).toLowerCase();
        String port = matcher.group(4);

        String key = protocal + token + host + port;
        FtpOperator ftp = ftpClients.get(key);
        if (null == ftp) {
            ftp = new FtpOperator();
            ftp.key = key;
            ftp.protocal = protocal;
            if (protocal.equals("ftp"))
                ftp.client = new FTPClient();
            else if (protocal.equals("ftps"))
                ftp.client = new FTPSClient("SSL");
            ftp.host = host;
            if (StringUtils.isNotEmpty(port)) {
                ftp.port = Integer.parseInt(port.substring(1));
            }
            if (StringUtils.isNotEmpty(token)) {
                int idx = token.indexOf(':');
                ftp.userName = token.substring(0, idx);
                ftp.password = token.substring(idx + 1, token.length() - 1);
            }
            ftp.conn();
            ftpClients.put(key, ftp);
        }
        return ftp;
    }

//    public boolean connectServer() {
//        boolean flag = true;
//        FTPClient ftpClient = threadFtpClient.get();
//
//        if (ftpClient == null) {
//            int reply;
//            try {
//                ftpClient = new FTPClient();
//                ftpClient.setDefaultPort(port);
//                ftpClient.connect(ip);
//                if (!ftpClient.login(userName, password)) {
//                    throw new SimplifiedException("ftp登录出错");
//                }
//                reply = ftpClient.getReplyCode();
//
//                if (!FTPReply.isPositiveCompletion(reply)) {
//                    ftpClient.disconnect();
//                    logger.error("FTP server refused connection  " + ip + ":" + port);
//                    flag = false;
//                }
//
//                ftpClient.setDataTimeout(30000);
//                ftpClient.setControlEncoding("UTF-8");
//                ftpClient.enterLocalPassiveMode();
//                ftpClient.setFileTransferMode(FTPClient.STREAM_TRANSFER_MODE);
//
//                if (!ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE))
//                    throw new SimplifiedException("错误的FileType");
//
//                threadFtpClient.set(ftpClient);
//            } catch (SocketException e) {
//                e.printStackTrace();
//                throw new SimplifiedException("登录ftp服务器 " + ip + " 失败,连接超时！");
//            } catch (IOException e) {
//                e.printStackTrace();
//                throw new SimplifiedException("登录ftp服务器 " + ip + " 失败，FTP服务器无法打开！");
//            }
//        }
//
//        return flag;
//    }

    public static FtpOperator connect(String userName, String password, String host, int port) {
        this.userName = userName;
        this.password = password;
        this.host = host;
        this.port = port;
    }

    public int sendCommands(String command) {
        try {
            return client.sendCommand(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
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
            e.printStackTrace();
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

    public void downFile(String remotePath, String fileName, String localPath) {
        FTPFile[] fs;
        try {
            logger.info(remotePath);
            client.changeWorkingDirectory(remotePath);//转移到FTP服务器目录
            fs = client.listFiles();
            for (FTPFile ff : fs) {
                if (ff.getName().equals(fileName)) {
                    File localFile = new File(localPath + File.separator + ff.getName());
                    logger.info(localPath + File.separator + ff.getName());
                    FileOutputStream is = new FileOutputStream(localFile);
                    client.retrieveFile(ff.getName(), is);
                    is.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void downFile(String allPath, OutputStream os) {
        FTPFile[] fs;
        try {
            int lastIdx = allPath.lastIndexOf("/") + 1;
            if (lastIdx <= 0)
                lastIdx = allPath.lastIndexOf("\\") + 1;

            String dir = allPath.substring(0, lastIdx);
            String fileName = allPath.substring(lastIdx);
            client.changeWorkingDirectory(dir);//进到FTP服务器目录
            fs = client.listFiles();
            for (FTPFile ff : fs) {
                if (ff.getName().equals(fileName)) {
                    client.retrieveFile(ff.getName(), os);
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("下载ftp文件出错" + allPath, e);
            throw new SimplifiedException("下载ftp文件出错");
        }
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
            e.printStackTrace();
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
        client.connect(host);
        if (StringUtils.isNotEmpty(userName)) {
            if (!client.login(userName, password)) {
                throw new SimplifiedException("FTP登录失败");
            }
        }

        int reply = client.getReplyCode();

        if (!FTPReply.isPositiveCompletion(reply)) {
            client.disconnect();
            throw new SimplifiedException("FTP server refused connection  " + host + ":" + port);
        }

        client.setDataTimeout(30000);
        client.setControlEncoding("UTF-8");
        client.enterLocalPassiveMode();
        client.setFileTransferMode(FTPClient.STREAM_TRANSFER_MODE);

        if (!client.setFileType(FTPClient.BINARY_FILE_TYPE))
            throw new SimplifiedException("错误的FileType");

    }
}