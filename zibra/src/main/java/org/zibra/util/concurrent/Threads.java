package org.zibra.util.concurrent;

public final class Threads {

    private static final ThreadGroup rootThreadGroup;
    private static final Thread mainThread;
    private static volatile boolean enableShutdownHandler = true;
    private static volatile Runnable defaultHandler = null;

    static {
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        ThreadGroup parentThreadGroup;
        while ((parentThreadGroup = threadGroup.getParent()) != null) {
            threadGroup = parentThreadGroup;
        }
        rootThreadGroup = threadGroup;

        Thread thread = Thread.currentThread();
        Thread[] threads = findAllThreads();
        for (Thread t : threads) {
            if (t.getId() == 1) {
                thread = t;
            }
        }
        mainThread = thread;
    }

    public static Thread[] findAllThreads() {
        int estimatedSize = rootThreadGroup.activeCount() * 2;
        Thread[] slackList = new Thread[estimatedSize];
        int actualSize = rootThreadGroup.enumerate(slackList);
        Thread[] list = new Thread[actualSize];
        System.arraycopy(slackList, 0, list, 0, actualSize);
        return list;
    }

    public static Thread getMainThread() {
        return mainThread;
    }

    public static ThreadGroup getRootThreadGroup() {
        return rootThreadGroup;
    }

    public static synchronized void registerShutdownHandler(final Runnable handler) {
        if (enableShutdownHandler) {
            if (defaultHandler == null) {
                defaultHandler = handler;
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override
                    public void run() {
                        defaultHandler.run();
                    }
                });
            } else {
                final Runnable oldHandler = defaultHandler;
                defaultHandler = () -> {
                    oldHandler.run();
                    handler.run();
                };
            }
        }
    }

    public static void run() {
        defaultHandler.run();
    }

    public static void disabledShutdownHandler() {
        enableShutdownHandler = false;
    }
}
