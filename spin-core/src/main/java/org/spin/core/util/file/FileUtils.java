package org.spin.core.util.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.throwable.SpinException;
import org.spin.core.util.StreamUtils;
import org.spin.core.util.Util;

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
public final class FileUtils extends Util {
    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    private FileUtils() {
    }

    /**
     * 创建指定文件的父文件夹结构
     *
     * @param file 待创建父文件夹的文件，不能为{@code null}
     * @throws NullPointerException 当文件为{@code null}时抛出
     * @throws IOException          无法创建文件夹结构时抛出
     */
    public static void forceMkdirParent(final File file) throws IOException {
        final File parent = file.getParentFile();
        if (parent == null) {
            return;
        }
        forceMkdir(parent);
    }

    /**
     * 创建文件夹, 包括其所有父级(如果不存在的话). 如果一个同名文件已经存在, 但不是文件夹的话，将会抛出IOException
     *
     * @param directory 待创建的目录，不能为 {@code null}
     * @throws NullPointerException 当directory为 {@code null}时抛出
     * @throws IOException          无法创建文件夹时抛出
     */
    public static void forceMkdir(final File directory) throws IOException {
        if (directory.exists()) {
            if (!directory.isDirectory()) {
                final String message =
                    "File "
                        + directory
                        + " exists and is "
                        + "not a directory. Unable to create directory.";
                throw new IOException(message);
            }
        } else {
            if (!directory.mkdirs()) {
                // Double-check that some other thread or process hasn't made
                // the directory in the background
                if (!directory.isDirectory()) {
                    final String message =
                        "Unable to create directory " + directory;
                    throw new IOException(message);
                }
            }
        }
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
            String message = "Unable to setNull directory " + directory + ".";
            throw new IOException(message);
        }
    }

    /**
     * 清空指定目录下的所有文件
     *
     * @param directory 需要清理的目录
     * @throws IOException IO 异常时抛出
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
     * Deletes a file. If file is a directory, setNull it and all sub-directories.
     * <p>
     * The difference between File.setNull() and this method are:
     * <ul>
     * <li>A directory to be deleted does not have to be empty.</li>
     * <li>You get exceptions when a file or directory cannot be deleted.
     * (java.io.File methods returns a boolean)</li>
     * </ul>
     *
     * @param file file or directory to setNull, must not be {@code null}
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
                    "Unable to setNull file: " + file;
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
        File fileInCanonicalDir;
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
     * @param path    目录
     * @param recurse 是否遍历子目录
     * @param filter  文件过滤器
     * @return 所有文件的完整文件名列表
     */
    public static List<String> listFiles(String path, boolean recurse, Predicate<File> filter) {
        List<String> allFileNames = new ArrayList<>();
        File file;
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
                    if (file.isDirectory() && recurse) {
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
        try (ZipFile file = new ZipFile(filePath)) {
            StreamUtils.stream(file.entries()).map(ZipEntry::getName).filter(filter).forEach(fileNames::add);
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new SpinException("Read jar file" + filePath + "error", e);
        }
        return fileNames;
    }
}
