package org.spin.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.file.FileUtils;

import java.io.IOException;
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
     * @param jarPath      jar文件路径
     * @param packageName  包名
     * @param childPackage 是否遍历子包
     * @return 类的完整名称
     */
    public static List<String> getClassNameByJar(String jarPath, String packageName, boolean childPackage) {
        List<String> classNames = new ArrayList<>();
        String[] jarInfo = jarPath.split("!");
        String jarFilePath = jarInfo[0].substring(jarInfo[0].indexOf("/"));
        String clsPath = (jarInfo.length == 3 ? jarInfo[1] : "/").substring(1);
        String packagePath = (jarInfo.length == 3 ? (clsPath + jarInfo[2]) : jarInfo[1].substring(1));
        try {
            JarFile jarFile = new JarFile(jarFilePath);
            Stream<String> stream = StreamUtils.enumerationAsStream(jarFile.entries()).map(JarEntry::getName).filter(entryName -> entryName.endsWith(".class"));
            if (childPackage) {
                stream.filter(n -> n.startsWith(packagePath))
                    .forEach(n -> classNames.add(n.replace('/', '.').substring(clsPath.length() + 1, n.length() - 6)));
            } else {
                stream.forEach(n -> {
                    int index = n.lastIndexOf('/');
                    String myPackagePath;
                    myPackagePath = (index != -1) ? n.substring(0, index) : n;
                    if (myPackagePath.equals(packagePath)) {
                        classNames.add(n.replace('/', '.').substring(clsPath.length() + 1, n.length() - 6));
                    }
                });
            }
            jarFile.close();
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new SimplifiedException("Read jar file" + jarFilePath + "error", e);
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
