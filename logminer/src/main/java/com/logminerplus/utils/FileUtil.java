package com.logminerplus.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

public class FileUtil {
	
	private static Logger logger = Logger.getLogger(FileUtil.class.getName());
	
	public static Set<String> sets = new HashSet<String>();  
	  
    public static void main(String[] args) {  
        //refreshFileList("G:\\Music");  
        //moveFolder("G:\\music\\周杰伦", "E:\\Kugou");
        getRootPath();
    }
    
    public static String getRootPath() {
    	FileUtil util = new FileUtil();
    	String classPath = util.getClass().getClassLoader().getResource("").getPath();
    	String rootPath  = "";
    	//windows下
    	if("\\".equals(File.separator)) {
    		rootPath = classPath.substring(1, classPath.length());
    		rootPath = rootPath.replace("/", "\\");
    	}
    	//linux下
    	if("/".equals(File.separator)){
    		rootPath = classPath.replace("\\", "/");
    	}
    	return rootPath;
    }
	
	/**
	 * 解决文件路径有中文和空格的问题
	 * @param path
	 * @return uriPath
	 */
	public static String getURIPath(String path) throws Exception {
		String uriPath = null;
		try {
			FileUtil util = new FileUtil();
			uriPath = util.getClass().getClassLoader().getResource(path).toURI().getPath();
		} catch (URISyntaxException e) {
			e.printStackTrace();
			throw new Exception("URI路径转换异常：", e);
		}
		return uriPath;
	}
	
	@SuppressWarnings("unused")
	public static String getFileToString(String absolutePath) throws Exception {
		StringBuffer sb = new StringBuffer();
		BufferedReader reader = null;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(new File(absolutePath));
			InputStreamReader isr = new InputStreamReader(fis, "gbk");
			reader = new BufferedReader(isr);
			int line =1;
			String tempString = null;
			while ((tempString = reader.readLine()) != null) {
				sb.append(tempString+"\r\n");
	            line++;
	        }
		} catch (Exception e) {
			throw new Exception("读取文件异常：", e);
		} finally {
			if(fis!=null) {
				fis.close();
			}
			if(reader!=null) {
				reader.close();
			}
		}
		return sb.toString();
	}
	
	/** 
     * 过滤MP3文件 
     *  
     * @param strPath 
     */  
    public static void refreshFileList(String strPath) {  
        File dir = new File(strPath);  
        File[] files = dir.listFiles();  
        if (files == null) {  
            return;  
        }  
        for (int i = 0; i < files.length; i++) {  
            if (files[i].isDirectory()) {  
                refreshFileList(files[i].getAbsolutePath());  
            } else {  
                String strFilePath = files[i].getAbsolutePath().toLowerCase();  
                String strName = files[i].getName();  
                if (strName.endsWith(".mp3")) {  
                    boolean bFlag = sets.add(strName);  
                    if (bFlag == Boolean.FALSE) {  
                        // 删除重复文件  
                        removeFile(strFilePath);  
                    }  
                }  
                // System.out.println("FILE_PATH:" + strFilePath + "|strName:" +  
                // strName);  
            }  
        }  
    }  
  
    /** 
     * 创建文件夹 
     *  
     * @param strFilePath 
     *            文件夹路径 
     */  
    public boolean mkdirFolder(String strFilePath) {  
        boolean bFlag = false;  
        try {  
            File file = new File(strFilePath.toString());  
            if (!file.exists()) {  
                bFlag = file.mkdir();  
            }  
        } catch (Exception e) {  
            logger.error("新建目录操作出错" + e.getLocalizedMessage());  
            e.printStackTrace();  
        }  
        return bFlag;  
    }  
  
    public boolean createFile(String strFilePath, String strFileContent) {  
        boolean bFlag = false;  
        try {  
            File file = new File(strFilePath.toString());  
            if (!file.exists()) {  
                bFlag = file.createNewFile();  
            }  
            if (bFlag == Boolean.TRUE) {  
                FileWriter fw = new FileWriter(file);  
                PrintWriter pw = new PrintWriter(fw);  
                pw.println(strFileContent.toString());  
                pw.close();  
            }  
        } catch (Exception e) {  
            logger.error("新建文件操作出错" + e.getLocalizedMessage());  
            e.printStackTrace();  
        }  
        return bFlag;  
    }  
  
    /** 
     * 删除文件 
     *  
     * @param strFilePath 
     * @return 
     */  
    public static boolean removeFile(String strFilePath) {  
        boolean result = false;  
        if (strFilePath == null || "".equals(strFilePath)) {  
            return result;  
        }  
        File file = new File(strFilePath);  
        if (file.isFile() && file.exists()) {  
            result = file.delete();  
            if (result == Boolean.TRUE) {
                logger.debug("[REMOVE_FILE:" + strFilePath + " Deleted Successfully!]");  
            } else {  
                logger.debug("[REMOVE_FILE:" + strFilePath + " Deleted Error!]");  
            }  
        }  
        return result;  
    }  
  
    /** 
     * 删除文件夹(包括文件夹中的文件内容，文件夹) 
     *  
     * @param strFolderPath 
     * @return 
     */  
    public static boolean removeFolder(String strFolderPath) {  
        boolean bFlag = false;  
        try {  
            if (strFolderPath == null || "".equals(strFolderPath)) {  
                return bFlag;  
            }  
            File file = new File(strFolderPath.toString());  
            bFlag = file.delete();  
            if (bFlag == Boolean.TRUE) {  
                logger.debug("[REMOE_FOLDER:" + file.getPath() + "删除成功!]");  
            } else {  
                logger.debug("[REMOE_FOLDER:" + file.getPath() + "删除失败]");  
            }  
        } catch (Exception e) {  
            logger.error("FLOADER_PATH:" + strFolderPath + "删除文件夹失败!");  
            e.printStackTrace();  
        }  
        return bFlag;  
    }  
  
    /** 
     * 移除所有文件 
     *  
     * @param strPath 
     */  
    public static void removeAllFile(String strPath) {  
        File file = new File(strPath);  
        if (!file.exists()) {  
            return;  
        }  
        if (!file.isDirectory()) {  
            return;  
        }  
        String[] fileList = file.list();  
        File tempFile = null;  
        for (int i = 0; i < fileList.length; i++) {  
            if (strPath.endsWith(File.separator)) {  
                tempFile = new File(strPath + fileList[i]);  
            } else {  
                tempFile = new File(strPath + File.separator + fileList[i]);  
            }  
            if (tempFile.isFile()) {  
                tempFile.delete();  
            }  
            if (tempFile.isDirectory()) {  
                removeAllFile(strPath + "/" + fileList[i]);// 下删除文件夹里面的文件  
                removeFolder(strPath + "/" + fileList[i]);// 删除文件夹  
            }  
        }  
    }  
  
    public static void copyFile(String oldPath, String newPath) throws Exception {  
        try {  
            int bytesum = 0;  
            int byteread = 0;  
            File oldfile = new File(oldPath);  
            if (oldfile.exists()) { // 文件存在时  
                InputStream inStream = new FileInputStream(oldPath); // 读入原文件  
                FileOutputStream fs = new FileOutputStream(newPath);  
                byte[] buffer = new byte[1444];  
                while ((byteread = inStream.read(buffer)) != -1) {  
                    bytesum += byteread; // 字节数 文件大小  
                    fs.write(buffer, 0, byteread);
                }  
                inStream.close();
                fs.close();
                logger.debug("[COPY_FILE  :" + oldfile.getPath() + " Copy File Successful!]");
            }  
        } catch (Exception e) { 
            throw new Exception("Copy File Error", e);
        }  
    }  
  
    public static void copyFolder(String oldPath, String newPath) {  
        try {  
            (new File(newPath)).mkdirs(); // 如果文件夹不存在 则建立新文件夹  
            File a = new File(oldPath);  
            String[] file = a.list();  
            File temp = null;  
            for (int i = 0; i < file.length; i++) {  
                if (oldPath.endsWith(File.separator)) {  
                    temp = new File(oldPath + file[i]);  
                } else {  
                    temp = new File(oldPath + File.separator + file[i]);  
                }  
                if (temp.isFile()) {  
                    FileInputStream input = new FileInputStream(temp);  
                    FileOutputStream output = new FileOutputStream(newPath  
                            + "/ " + (temp.getName()).toString());  
                    byte[] b = new byte[1024 * 5];  
                    int len;  
                    while ((len = input.read(b)) != -1) {  
                        output.write(b, 0, len);  
                    }  
                    output.flush();  
                    output.close();  
                    input.close();  
                    logger.debug("[COPY_FILE  :" + temp.getPath() + " Copy File Successful!]");  
                }  
                if (temp.isDirectory()) {// 如果是子文件夹  
                    copyFolder(oldPath + "/ " + file[i], newPath + "/ "  
                            + file[i]);  
                }  
            }  
        } catch (Exception e) {  
        	logger.error("CopyFolder Error", e);
            e.printStackTrace();  
        }  
    }  
  
    public static void moveFile(String oldPath, String newPath) throws Exception {  
        copyFile(oldPath, newPath);  
        removeFile(oldPath);
    }  
  
    public static void moveFolder(String oldPath, String newPath) {  
        copyFolder(oldPath, newPath);  
        removeFolder(oldPath);  
    }
    
}