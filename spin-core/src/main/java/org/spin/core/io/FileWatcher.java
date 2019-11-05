package org.spin.core.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.ErrorCode;
import org.spin.core.collection.Tuple;
import org.spin.core.collection.Tuple4;
import org.spin.core.function.serializable.Consumer;
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
public class FileWatcher implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(FileWatcher.class);
    private static final AtomicInteger COUNTER = new AtomicInteger();
    private static final String THREAD_NAME_PREFIX = "FILE-WATCHER-";

    private final WatchService watchService;
    private final Map<WatchKey, Tuple4<Path, Path, Boolean, Consumer<FileAction>>> keys;
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

    public void registWatcher(File file, boolean recursion, Consumer<FileAction> fileActionCallback) {
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
                        keys.put(key, Tuple.of(dir, null, true, fileActionCallback));
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
     *
     * @param watchFile          监视的文件
     * @param recursion          是否递归监视子文件夹
     * @param fileActionCallback 文件监视回调
     */
    public void registWatcher(File watchFile, boolean recursion, FileActionCallback fileActionCallback) {
        registWatcher(watchFile, recursion, (action -> {
            if (action.isDelete()) {
                fileActionCallback.onDelete(action.file);
            } else if (action.isCreate()) {
                fileActionCallback.onCreate(action.file);
            } else if (action.isModify()) {
                fileActionCallback.onModify(action.file);
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
            Tuple4<Path, Path, Boolean, Consumer<FileAction>> tuple = keys.get(key);
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
                        tuple.c4.accept(new FileAction(kind, file));
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
            }
        }
    }

    public static class FileAction {
        private final Kind kind;
        private final File file;

        public FileAction(Kind kind, File file) {
            this.kind = kind;
            this.file = file;
        }

        public Kind getKind() {
            return kind;
        }

        public File getFile() {
            return file;
        }

        public boolean isDelete() {
            return kind != null && kind.name().equals(StandardWatchEventKinds.ENTRY_DELETE.name());
        }

        public boolean isModify() {
            return kind != null && kind.name().equals(StandardWatchEventKinds.ENTRY_MODIFY.name());
        }

        public boolean isCreate() {
            return kind != null && kind.name().equals(StandardWatchEventKinds.ENTRY_CREATE.name());
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


