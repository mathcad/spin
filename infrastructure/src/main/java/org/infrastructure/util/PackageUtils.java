package org.infrastructure.util;

import org.infrastructure.throwable.SimplifiedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

/**
 * 通用的枚举类型转换
 *
 * @author xuweinan
 * @version V1.0
 */
public abstract class PackageUtils {
    private static final Logger logger = LoggerFactory.getLogger(PackageUtils.class);

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
                fileNames = getClassNameByFile(url.getPath(), childPackage);
            } else if ("jar".equals(type)) {
                fileNames = getClassNameByJar(url.getPath(), childPackage);
            }
        } else {
            fileNames = getClassNameByJars(((URLClassLoader) loader).getURLs(), packagePath, childPackage);
        }
        return fileNames;
    }

    /**
     * 从项目文件获取某包下所有类
     *
     * @param filePath     文件路径
     * @param childPackage 是否遍历子包
     * @return 类的完整名称
     */
    private static List<String> getClassNameByFile(String filePath, boolean childPackage) {
        List<String> myClassName = new ArrayList<>();
        File file = new File(filePath);
        File[] childFiles = file.listFiles();
        if (childFiles != null && childFiles.length != 0) {
            Arrays.stream(childFiles).forEach(childFile -> {
                String childFilePath = childFile.getPath();
                if (childFile.isDirectory() && childPackage) {
                    myClassName.addAll(getClassNameByFile(childFilePath, true));
                } else if (childFilePath.endsWith(".class")) {
                    childFilePath = childFilePath.substring(childFilePath.indexOf("\\classes") + 9, childFilePath.lastIndexOf('.'));
                    childFilePath = childFilePath.replace("\\", ".");
                    myClassName.add(childFilePath);
                }
            });
        }
        return myClassName;
    }

    /**
     * 从jar获取某包下所有类
     *
     * @param jarPath      jar文件路径
     * @param childPackage 是否遍历子包
     * @return 类的完整名称
     */
    private static List<String> getClassNameByJar(String jarPath, boolean childPackage) {
        List<String> myClassName = new ArrayList<>();
        String[] jarInfo = jarPath.split("!");
        String jarFilePath = jarInfo[0].substring(jarInfo[0].indexOf("/"));
        String packagePath = jarInfo[1].substring(1);
        try {
            JarFile jarFile = new JarFile(jarFilePath);
            Stream<String> stream = StreamUtils.enumerationAsStream(jarFile.entries()).map(JarEntry::getName).filter(entryName -> entryName.endsWith(".class"));
            if (childPackage)
                stream.filter(n -> n.startsWith(packagePath)).forEach(n -> myClassName.add(n.replace("/", ".").substring(0, n.lastIndexOf('.'))));
            else
                stream.forEach(n -> {
                    int index = n.lastIndexOf('/');
                    String myPackagePath;
                    myPackagePath = (index != -1) ? n.substring(0, index) : n;
                    if (myPackagePath.equals(packagePath)) {
                        myClassName.add(n.replace("/", ".").substring(0, n.lastIndexOf('.')));
                    }
                });
            jarFile.close();
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new SimplifiedException("Read jar file" + jarFilePath + "error", e);
        }
        return myClassName;
    }

    /**
     * 从所有jar中搜索该包，并获取该包下所有类
     *
     * @param urls         URL集合
     * @param packagePath  包路径
     * @param childPackage 是否遍历子包
     * @return 类的完整名称
     */
    private static List<String> getClassNameByJars(URL[] urls, String packagePath, boolean childPackage) {
        List<String> myClassName = new ArrayList<>();
        Optional.ofNullable(urls).ifPresent(u -> Arrays.stream(u).map(URL::getPath).filter(p -> !p.endsWith("classes/"))
                .map(urlPath -> urlPath + "!/" + packagePath)
                .forEach(jarPath -> myClassName.addAll(getClassNameByJar(jarPath, childPackage))));
//        if (urls != null) {
//            for (URL url : urls) {
//                String urlPath = url.getPath();
//                // 不必搜索classes文件夹
//                if (urlPath.endsWith("classes/")) {
//                    continue;
//                }
//                String jarPath = urlPath + "!/" + packagePath;
//                myClassName.addAll(getClassNameByJar(jarPath, childPackage));
//            }
//        }
        return myClassName;
    }
}