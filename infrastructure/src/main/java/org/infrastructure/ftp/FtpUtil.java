package org.infrastructure.ftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPListParseEngine;
import org.apache.commons.net.ftp.FTPReply;
import org.infrastructure.throwable.BizException;
import org.infrastructure.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ftp文件上传
 *  
* @author zhou
* @contact 电话: 18963752887, QQ: 251915460
* @create 2015年3月25日 下午9:26:51 
* @version V1.0
 */
public class FtpUtil {
	static final Logger logger = LoggerFactory.getLogger(FtpUtil.class);
	
	public  String userName;
	public  String password;
	public  String ip;
	public int port;
	
	private static ThreadLocal<FTPClient> threadFtpClient=new ThreadLocal<FTPClient>(){ 
		
	};


	public FtpUtil() {
	}
		

	public boolean connectServer() {
		boolean flag = true;
		 FTPClient ftpClient = threadFtpClient.get();
		 
		if (ftpClient == null) {
			int reply;
			try {
				ftpClient = new FTPClient();
				ftpClient.setDefaultPort(port);
				ftpClient.connect(ip);
				if(!ftpClient.login(userName, password)){
					throw new BizException("ftp登录出错");
				}
				reply = ftpClient.getReplyCode();

				if (!FTPReply.isPositiveCompletion(reply)) {
					ftpClient.disconnect();
					logger.error("FTP server refused connection  " + ip +":" + port);
					flag = false;
				}
				
				ftpClient.setDataTimeout(30000);
				ftpClient.setControlEncoding("UTF-8"); //文件名乱码,默认ISO8859-1，不支持中文
				ftpClient.enterLocalPassiveMode(); 
				ftpClient.setFileTransferMode(FTPClient.STREAM_TRANSFER_MODE);
//				if(!ftpClient.setFileTransferMode(FTPClient.STREAM_TRANSFER_MODE))
//					throw new BizException("错误的setFileTransferMode");
				
				if(!ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE))
					throw new BizException("错误的FileType");
				
				threadFtpClient.set(ftpClient);
			} catch (SocketException e) {
				flag = false;
				e.printStackTrace();
				throw new BizException("登录ftp服务器 " + ip + " 失败,连接超时！");
			} catch (IOException e) {
				flag = false;
				e.printStackTrace();
				throw new BizException("登录ftp服务器 " + ip + " 失败，FTP服务器无法打开！");
			}
		}
		
		return flag;
	}
	
	public FtpUtil(String userName,String password,String ip,int port) {
		this.userName = userName;
		this.password = password;
		this.ip = ip;
		this.port = port;
	}

	public int sendCommands(String command){
		try {
			FTPClient ftpClient = threadFtpClient.get();
			return ftpClient.sendCommand(command);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public  void listRemoteAllFiles(String path) {
		try {
			FTPClient ftpClient = threadFtpClient.get();
			FTPListParseEngine f = ftpClient.initiateListParsing(path);

			while (f.hasNext()) {
				FTPFile[] files = f.getNext(5); 
				for(FTPFile file:files){
					disFile(file,path);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void disFile(FTPFile file,String path){
		if(file.isDirectory() && !file.getName().equals(".")&& !file.getName().equals("..")){
			logger.info(File.separator + file.getName());
			listRemoteAllFiles(path+ File.separator +file.getName());
		}else if(!file.getName().equals(".")&& !file.getName().equals("..")){
			logger.info(file.getName());
		}
	}

	public void downFile(String remotePath,String fileName,String localPath) {
		FTPClient ftpClient = threadFtpClient.get();
		FTPFile[] fs;
		try {
			logger.info(remotePath);
			ftpClient.changeWorkingDirectory(remotePath);//转移到FTP服务器目录  
			fs = ftpClient.listFiles();
			for(FTPFile ff:fs){  
				if(ff.getName().equals(fileName)){  
					File localFile = new File(localPath+ File.separator +ff.getName());  
					logger.info(localPath+ File.separator +ff.getName());
					FileOutputStream is = new FileOutputStream(localFile);   
					ftpClient.retrieveFile(ff.getName(), is);
					is.close();  
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}  
	}
	
	public void downFile(String allPath,OutputStream os) {
		FTPClient ftpClient = threadFtpClient.get();
		FTPFile[] fs;
		try {
			int lastIdx = allPath.lastIndexOf("/")+1;
			if(lastIdx <=0)
				lastIdx = allPath.lastIndexOf("\\")+1;
			
			String dir = allPath.substring(0,lastIdx);
			String fileName = allPath.substring(lastIdx);
			ftpClient.changeWorkingDirectory(dir);//进到FTP服务器目录  
			fs = ftpClient.listFiles();
			for(FTPFile ff:fs){  
				if(ff.getName().equals(fileName)){
					ftpClient.retrieveFile(ff.getName(), os); 
					break;
				}
			}
		} catch (Exception e) {
			logger.error("下载ftp文件出错" + allPath,e);
			throw new BizException("下载ftp文件出错");
		}  
	}

	public boolean upFile(String path, String filename, String localFilePath){
		boolean flag=false;
		try {
			FTPClient ftpClient = threadFtpClient.get();
			FileInputStream in=new FileInputStream(new File(localFilePath));
			if(StringUtils.isNotEmpty(path))
				ftpClient.changeWorkingDirectory(path);
			flag = ftpClient.storeFile(filename, in);
			in.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}  
		return flag;
	}
	
	public boolean upFile(String path, String filename, InputStream local){
		boolean flag=false;
		try {
			FTPClient ftpClient = threadFtpClient.get();
			ftpClient.changeWorkingDirectory(path);
			flag = ftpClient.storeFile(filename, local);
			local.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}  
		return flag;
	}

	public boolean mkdir(String path){
		try {
			FTPClient ftpClient = threadFtpClient.get();
			return ftpClient.makeDirectory(path);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 关闭连接
	 * 
	* @version 1.0
	 */
	public void closeConnect() {
		try {
			FTPClient ftpClient = threadFtpClient.get();
			if (ftpClient != null) {
				ftpClient.logout();
				ftpClient.disconnect();
				ftpClient = null;
				threadFtpClient.remove();
				logger.info("关闭ftp服务器");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}