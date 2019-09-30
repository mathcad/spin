package org.spin.core.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.ErrorCode;
import org.spin.core.collection.Tuple;
import org.spin.core.collection.Tuple4;
import org.spin.core.function.serializable.BiConsumer;
import org.spin.core.throwable.SpinException;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

/**
 * 文件监控
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/3/19</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class FileWatcher implements Closeable, AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(FileWatcher.class);
    private static final AtomicInteger COUNTER = new AtomicInteger();
    private static final String THREAD_NAME_PREFIX = "FILE-WATCHER-";

    private final WatchService watchService;
    private final Map<WatchKey, Tuple4<Path, Path, Boolean, BiConsumer<Kind, File>>> keys;
    private final Thread workThread;
    private final String id;

    private volatile boolean run = true;

    public FileWatcher() {
        try {
            this.watchService = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            throw new SpinException(ErrorCode.IO_FAIL, "创建WatchService失败", e);
        }
        this.keys = new HashMap<>();
        this.id = THREAD_NAME_PREFIX + COUNTER.getAndIncrement();
        workThread = new Thread(this::working, id);
    }

    public void registWatcher(File file, boolean recursion, BiConsumer<Kind, File> fileActionCallback) {
        checkState();
        Path originPath = null;
        if (!file.isDirectory()) {
            if (recursion) {
                throw new SpinException(ErrorCode.IO_FAIL, "监控的文件(非目录)时, 无法递归监控所有子目录");
            }
            originPath = Paths.get(file.getAbsolutePath());
            file = file.getParentFile();
            if (null == file) {
                throw new SpinException(ErrorCode.IO_FAIL, "监控的文件没有上级目录(监控非目录时, 文件必须有上级目录)");
            }
        }
        Path path = Paths.get(file.getAbsolutePath());

        if (recursion) {
            try {
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        WatchKey key = dir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                keys.clear();
                throw new SpinException(ErrorCode.IO_FAIL, "递归子目录失败", e);
            }
        } else {
            WatchKey key;
            try {
                key = path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
            } catch (IOException e) {
                throw new SpinException(ErrorCode.IO_FAIL, id + "注册文件监视器失败", e);
            }
            keys.put(key, Tuple.of(path, originPath, false, fileActionCallback));
        }
    }

    /**
     * 注册监听器
     */
    public void registWatcher(File watchFile, boolean recursion, FileActionCallback fileActionCallback) {
        registWatcher(watchFile, recursion, ((kind, file) -> {
            if (kind.name().equals(StandardWatchEventKinds.ENTRY_DELETE.name())) {
                fileActionCallback.onDelete(file);
            } else if (kind.name().equals(StandardWatchEventKinds.ENTRY_CREATE.name())) {
                fileActionCallback.onCreate(file);
            } else if (kind.name().equals(StandardWatchEventKinds.ENTRY_MODIFY.name())) {
                fileActionCallback.onModify(file);
            }
        }));
    }

    public FileWatcher start() {
        checkState();
        this.workThread.start();
        return this;
    }

    @Override
    public void close() {
        if (run) {
            logger.info("关闭文件监视器实例: {}", id);
            this.run = false;
            try {
                watchService.close();
            } catch (IOException e) {
                workThread.interrupt();
            }
        }
    }

    private void checkState() {
        if (!this.run) {
            throw new SpinException("当前监视器实例已经终止");
        }
    }

    private void working() {
        while (run) {
            WatchKey key;
            try {
                key = watchService.take();
            } catch (Exception ignore) {
                logger.info("当前监视器实例已经终止: {}", id);
                return;
            }
            Tuple4<Path, Path, Boolean, BiConsumer<Kind, File>> tuple = keys.get(key);
            if (tuple == null) {
                System.err.println("操作未识别");
                continue;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                Kind kind = event.kind();

                // 事件可能丢失或遗弃
                if (kind == StandardWatchEventKinds.OVERFLOW) {
                    continue;
                }

                // 目录内的变化可能是文件或者目录
                Path name = (Path) event.context();
                Path child = tuple.c1.resolve(name);
                File file = child.toFile();

                try {
                    if (null == tuple.c2 || tuple.c2.equals(child)) {
                        tuple.c4.accept(kind, file);
                    }
                } catch (Exception e) {
                    logger.warn("文件监视回调异常", e);
                }

                // if directory is created, and watching recursively, then
                // register it and its sub-directories
                if (tuple.c3 && (kind.name().equals(StandardWatchEventKinds.ENTRY_CREATE.name()))) {
                    if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                        registWatcher(file, true, tuple.c4);
                    }
                }
            }

            boolean valid = key.reset();
            if (!valid) {
                // 移除不可访问的目录
                // 因为有可能目录被移除，就会无法访问
                keys.remove(key);
                // 如果待监控的目录都不存在了，就中断执行
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }

    /**
     * 文件变更回调
     */
    public interface FileActionCallback {

        /**
         * 文件被删除时触发
         *
         * @param file 目标文件
         */
        default void onDelete(File file) {
        }

        /**
         * 文件被修改时触发
         *
         * @param file 目标文件
         */
        default void onModify(File file) {
        }

        /**
         * 文件被创建时触发
         *
         * @param file 目标文件
         */
        default void onCreate(File file) {
        }
    }
}


