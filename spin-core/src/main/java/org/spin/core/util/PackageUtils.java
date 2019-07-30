package org.spin.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.throwable.SpinException;
import org.spin.core.util.file.FileUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

/**
 * Package相关的工具类
 *
 * @author xuweinan
 * @version V1.0
 */
public abstract class PackageUtils {
    private static final Logger logger = LoggerFactory.getLogger(PackageUtils.class);

    private PackageUtils() {
    }

    /**
     * 获取某包下（包括该包的所有子包）所有类
     *
     * @param packageName 包名
     * @return 类的完整名称
     */
    public static List<String> getClassName(String packageName) {
        return getClassName(packageName, true);
    }

    /**
     * 获取某包下所有类
     *
     * @param packageName  包名
     * @param childPackage 是否遍历子包
     * @return 类的完整名称
     */
    public static List<String> getClassName(String packageName, boolean childPackage) {
        List<String> fileNames = null;
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        String packagePath = packageName.replace(".", "/");
        URL url = loader.getResource(packagePath);
        if (url != null) {
            String type = url.getProtocol();
            if ("file".equals(type)) {
                fileNames = getClassNameByPath(url.getPath(), packageName, childPackage);
            } else if ("jar".equals(type)) {
                fileNames = getClassNameByJar(url.getPath(), packageName, childPackage);
            }
        } else {
            fileNames = getClassNameByJars(((URLClassLoader) loader).getURLs(), packagePath, childPackage);
        }
        return fileNames;
    }

    /**
     * 获取指定目录下，指定包中的所有类
     *
     * @param filePath     搜索目录
     * @param packageName  包名
     * @param childPackage 是否遍历子包
     * @return 类的完整名称
     */
    public static List<String> getClassNameByPath(String filePath, String packageName, boolean childPackage) {
        List<String> classFiles = FileUtils.listFiles(filePath, childPackage, file -> file.getName().endsWith(".class") && file.getName().indexOf('$') == -1);
        String classFile;
        if (classFiles.isEmpty()) {
            return classFiles;
        }
        for (int i = 0,
             idx = StringUtils.replace(classFiles.get(0), SystemUtils.FILE_SEPARATOR, ".").indexOf(packageName);
             i < classFiles.size(); i++) {
            classFile = classFiles.get(i);
            classFiles.set(i, StringUtils.replace(classFile, SystemUtils.FILE_SEPARATOR, ".").substring(idx, classFile.length() - 6));
        }
        return classFiles;
    }

    /**
     * 从jar获取某包下所有类（支持spring boot的fat jar）
     *
     * @param jarPath      jar路径
     * @param packageName  包名
     * @param childPackage 是否遍历子包
     * @return 类的完整名称
     */
    public static List<String> getClassNameByJar(String jarPath, String packageName, boolean childPackage) {
        List<String> classNames = new ArrayList<>();
        String[] jarInfo = jarPath.split("!");
        String jarFilePath = jarInfo[0].substring(jarInfo[0].indexOf('/'));
        String contextPath = (jarInfo.length == 3 ? jarInfo[1] : "/").substring(1);
        boolean isJarInJar = contextPath.endsWith(".jar");
        String packagePath = (isJarInJar || StringUtils.isEmpty(contextPath) ? jarInfo[jarInfo.length - 1].substring(1) : (contextPath + jarInfo[2]));
        JarFile jarFile = null;
        try {
            jarFile = new JarFile(jarFilePath);
            if (contextPath.endsWith(".jar")) {
                JarEntry jarEntry = StreamUtils.enumerationAsStream(jarFile.entries()).filter(entry -> entry.getName().endsWith(contextPath)).findFirst().orElse(null);
                if (null != jarEntry && !jarEntry.isDirectory()) {
                    String fileName = SystemUtils.getJavaIoTmpDir() + SystemUtils.FILE_SEPARATOR + RandomStringUtils.randomAlphanumeric(16) + ".jar";
                    try (OutputStream os = new FileOutputStream(fileName); InputStream is = jarFile.getInputStream(jarEntry)) {
                        byte[] tmp = new byte[4096];
                        int len = 0;
                        do {
                            os.write(tmp, 0, len);
                            len = is.read(tmp);
                        }
                        while (len != -1);
                        jarFile.close();
                        jarFile = new JarFile(fileName);
                    }
                }
            }
            Stream<String> stream = StreamUtils.enumerationAsStream(jarFile.entries()).map(JarEntry::getName).filter(entryName -> entryName.endsWith(".class") && entryName.indexOf('$') == -1);
            if (childPackage) {
                stream.filter(n -> n.startsWith(packagePath))
                    .forEach(n -> classNames.add(n.replace('/', '.').substring(isJarInJar ? 0 : contextPath.length() + 1, n.length() - 6)));
            } else {
                stream.forEach(n -> {
                    int index = n.lastIndexOf('/');
                    String myPackagePath;
                    myPackagePath = (index != -1) ? n.substring(0, index) : n;
                    if (myPackagePath.equals(packagePath)) {
                        classNames.add(n.replace('/', '.').substring(0, n.length() - 6));
                    }
                });
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new SpinException("Read jar file" + jarFilePath + "error", e);
        } finally {
            if (null != jarFile) {
                try {
                    jarFile.close();
                } catch (IOException ignore) {
                }
            }
        }
        return classNames;
    }

    /**
     * 从所有jar中搜索该包，并获取该包下所有类
     *
     * @param urls         URL集合
     * @param packageName  包路径
     * @param childPackage 是否遍历子包
     * @return 类的完整名称
     */
    public static List<String> getClassNameByJars(URL[] urls, String packageName, boolean childPackage) {
        List<String> myClassName = new ArrayList<>();
        Optional.ofNullable(urls).ifPresent(u -> Arrays.stream(u).map(URL::getPath).filter(p -> !p.endsWith("classes/"))
            .map(urlPath -> urlPath + "!/" + packageName)
            .forEach(jarPath -> myClassName.addAll(getClassNameByJar(jarPath, packageName, childPackage))));
        return myClassName;
    }
}
