package org.spin.core.util.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.StreamUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * 文件工具类
 * <p>Created by xuweinan on 2017/12/14.</p>
 *
 * @author xuweinan
 */
public abstract class FileUtils {
    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    private FileUtils() {
    }

    /**
     * 删除文件夹
     *
     * @param directory 待删除文件夹
     * @throws IOException 删除不成功时抛出
     */
    public static void deleteDirectory(File directory) throws IOException {
        if (!directory.exists()) {
            return;
        }

        if (!isSymlink(directory)) {
            cleanDirectory(directory);
        }

        if (!directory.delete()) {
            String message = "Unable to delete directory " + directory + ".";
            throw new IOException(message);
        }
    }

    /**
     * Cleans a directory without deleting it.
     *
     * @param directory directory to clean
     * @throws IOException in case cleaning is unsuccessful
     */
    public static void cleanDirectory(File directory) throws IOException {
        if (!directory.exists()) {
            String message = directory + " does not exist";
            throw new IllegalArgumentException(message);
        }

        if (!directory.isDirectory()) {
            String message = directory + " is not a directory";
            throw new IllegalArgumentException(message);
        }

        File[] files = directory.listFiles();
        if (files == null) {  // null if security restricted
            throw new IOException("Failed to list contents of " + directory);
        }

        IOException exception = null;
        for (File file : files) {
            try {
                forceDelete(file);
            } catch (IOException ioe) {
                exception = ioe;
            }
        }

        if (null != exception) {
            throw exception;
        }
    }

    //-----------------------------------------------------------------------

    /**
     * Deletes a file. If file is a directory, delete it and all sub-directories.
     * <p>
     * The difference between File.delete() and this method are:
     * <ul>
     * <li>A directory to be deleted does not have to be empty.</li>
     * <li>You get exceptions when a file or directory cannot be deleted.
     * (java.io.File methods returns a boolean)</li>
     * </ul>
     *
     * @param file file or directory to delete, must not be {@code null}
     * @throws NullPointerException  if the directory is {@code null}
     * @throws FileNotFoundException if the file was not found
     * @throws IOException           in case deletion is unsuccessful
     */
    public static void forceDelete(File file) throws IOException {
        if (file.isDirectory()) {
            deleteDirectory(file);
        } else {
            boolean filePresent = file.exists();
            if (!file.delete()) {
                if (!filePresent) {
                    throw new FileNotFoundException("File does not exist: " + file);
                }
                String message =
                    "Unable to delete file: " + file;
                throw new IOException(message);
            }
        }
    }

    /**
     * 当虚拟机正常结束时删除指定文件
     * <p>如果是目录，会同时删除其中所有内容</p>
     *
     * @param file 要删除的文件
     * @throws IOException 存在文件删除不成功时抛出
     */
    public static void forceDeleteOnExit(File file) throws IOException {
        if (file.isDirectory()) {
            deleteDirectoryOnExit(file);
        } else {
            file.deleteOnExit();
        }
    }

    /**
     * 当虚拟机正常结束时删除指定目录
     *
     * @param directory 要删除的目录
     * @throws IOException 存在文件删除不成功时抛出
     */
    private static void deleteDirectoryOnExit(File directory) throws IOException {
        if (!directory.exists()) {
            return;
        }

        directory.deleteOnExit();
        if (!isSymlink(directory)) {
            cleanDirectoryOnExit(directory);
        }
    }

    /**
     * 当虚拟机正常结束时清空指定目录
     * <p>如果目录不存在，或传入的file不是目录，什么也不做</p>
     *
     * @param directory 需要清空的目录
     * @throws IOException 存在文件删除不成功时抛出
     */
    private static void cleanDirectoryOnExit(File directory) throws IOException {
        if (null == directory) {
            return;
        }
        if (!directory.exists() || !directory.isDirectory()) {
            return;
        }

        File[] files = directory.listFiles();
        if (files == null) {  // null if security restricted
            throw new IOException("Failed to list contents of " + directory);
        }

        IOException exception = null;
        for (File file : files) {
            try {
                forceDeleteOnExit(file);
            } catch (IOException ioe) {
                exception = ioe;
            }
        }

        if (null != exception) {
            throw exception;
        }
    }


    /**
     * 判断当前文件是符号连接还是真实文件
     * <p>
     * 仅取决于当前文件本身是否是符号连接，当file是目录时，即使目录中含有符号连接，只要file本身是实际文件，也不会返回true
     * <p>
     *
     * @param file 待检查的文件
     * @return 当前文件为符号连接，返回true
     * @throws IOException 发生IO异常
     */
    public static boolean isSymlink(File file) throws IOException {
        if (file == null) {
            throw new NullPointerException("File must not be null");
        }
        //FilenameUtils.isSystemWindows()
        if (File.separatorChar == '\\') {
            return false;
        }
        File fileInCanonicalDir = null;
        if (file.getParent() == null) {
            fileInCanonicalDir = file;
        } else {
            File canonicalDir = file.getParentFile().getCanonicalFile();
            fileInCanonicalDir = new File(canonicalDir, file.getName());
        }

        return !fileInCanonicalDir.getCanonicalFile().equals(fileInCanonicalDir.getAbsoluteFile());
    }

    /**
     * 列出当前目录下所有文件(非递归算法)
     *
     * @param path      目录
     * @param travelSub 是否遍历子目录
     * @param filter    文件过滤器
     * @return 所有文件的完整文件名列表
     */
    public static List<String> listFiles(String path, boolean travelSub, Predicate<File> filter) {
        List<String> allFileNames = new ArrayList<>();
        File file = null;
        Deque<File> directories = new LinkedList<>();
        file = new File(path);
        if (!file.exists() || !file.isDirectory()) {
            return allFileNames;
        }
        directories.push(file);
        File[] childFiles;
        while (!directories.isEmpty()) {
            file = directories.pop();
            childFiles = file.listFiles();
            if (childFiles != null && childFiles.length != 0) {
                for (int i = 0; i < childFiles.length; i++) {
                    file = childFiles[i];
                    if (file.isDirectory() && travelSub) {
                        directories.push(file);
                    } else if (filter.test(file)) {
                        allFileNames.add(file.getPath());
                    }
                }
            }
        }
        return allFileNames;
    }

    public static List<String> listFilesFromJarOrZip(String filePath, Predicate<String> filter) {
        List<String> fileNames = new ArrayList<>();
        try (ZipFile file = new ZipFile(filePath)){
            StreamUtils.enumerationAsStream(file.entries()).map(ZipEntry::getName).filter(filter).forEach(fileNames::add);
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new SimplifiedException("Read jar file" + filePath + "error", e);
        }
        return fileNames;
    }
}
