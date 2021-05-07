package org.spin.core;

/**
 * <p>Created by xuweinan on 2017/6/23.</p>
 *
 * @author xuweinan
 */
public class CallableAndFuture {
    public static void main(String[] args) {
        SyncThread syncThread = new SyncThread();
        Thread thread1 = new Thread(new SyncThread(), "SyncThread1");
        Thread thread2 = new Thread(new SyncThread(), "SyncThread2");
        thread1.start();
        thread2.start();
    }


}

class SyncThread implements Runnable {
    private static int count;
    private static String lock = "lock";

    public SyncThread() {
        count = 0;
    }

    public void run() {
        synchronized (lock) {
            System.out.println(Thread.currentThread().getName() + "enter ");
            try {
                System.out.println(Thread.currentThread().getName() + "release ");
                lock.wait(10 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + "exit ");
        }
    }
}
