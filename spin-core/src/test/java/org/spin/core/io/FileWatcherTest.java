package org.spin.core.io;

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
        fileWatcher.registerWatcher(new File("C:\\Users\\Mathcat\\feign-resolve.properties"), false, action -> {
            System.out.println(action.getKind().name());
            System.out.println(action.getFile().getAbsolutePath());
            System.out.println("-------------------------");
        });

        fileWatcher.registerWatcher(new File("C:\\Users\\Mathcat\\feign-resolve.properties"), false, action -> {
            System.out.println(action.getKind().name());
            System.out.println(action.getFile().getAbsolutePath());
            System.out.println("-------------------------");
        });


        Thread.sleep(60_000L);
        fileWatcher.close();
        Thread.sleep(60_000L);
    }
}
