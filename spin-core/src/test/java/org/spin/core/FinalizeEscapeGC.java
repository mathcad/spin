package org.spin.core;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

/**
 * 1 对象可以在被GC时自我拯救
 * 2 这种自救机会只有一次，因为一个对象的finalize()方法最多只会被系统自动调用一次
 * Created by 明明如月 on 2017-05-24.
 */
public class FinalizeEscapeGC {
    private static FinalizeEscapeGC SAVE_HOOK = null;
    private void isAlive(){
        System.out.println("yes, i am still alive :)");
    }
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        System.out.println("finalize method executed!");
        FinalizeEscapeGC.SAVE_HOOK = this;
    }
 
    public static void main(String[] args) throws InterruptedException {
        SAVE_HOOK = new FinalizeEscapeGC();
        //对象第一次拯救自己
        SoftReference<FinalizeEscapeGC> wr = new SoftReference<>(SAVE_HOOK);
        SAVE_HOOK = null;
        System.gc();
        //因为finalize 方法优先级很低，所以暂停0.5秒等待它
        Thread.sleep(500);
        if(SAVE_HOOK != null){
            SAVE_HOOK.isAlive();
        }else{
            System.out.println("no, i am dead :(");
        }

        //----------下面代码完全相同但是自救失败---------
        //对象第2次拯救自己
        SAVE_HOOK = null;
        System.gc();
        //因为finalize 方法优先级很低，所以暂停0.5秒等待它
        Thread.sleep(500);
        if(SAVE_HOOK != null){
            SAVE_HOOK.isAlive();
        }else{
            System.out.println("no, i am dead :(");
        }
    }
}
