package org.spin.core.io;

import org.junit.jupiter.api.Test;

import java.io.File;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/9/30</p>
 *
 * @author xuweinan
 * @version 1.0
 */
class FileWatcherTest {

//    @Test
    void registWatcher() throws InterruptedException {
        FileWatcher fileWatcher = new FileWatcher().start();
        fileWatcher.registWatcher(new File("C:\\Users\\Mathcat\\feign-resolve.properties"), false, (kind, file) -> {
            System.out.println(kind.name());
            System.out.println(file.getAbsolutePath());
            System.out.println("-------------------------");
        });

        fileWatcher.registWatcher(new File("C:\\Users\\Mathcat\\feign-resolve.properties"), false, (kind, file) -> {
            System.out.println(kind.name());
            System.out.println(file.getAbsolutePath());
            System.out.println("-------------------------");
        });


        Thread.sleep(60_000L);
        fileWatcher.close();
        Thread.sleep(60_000L);
    }
}
