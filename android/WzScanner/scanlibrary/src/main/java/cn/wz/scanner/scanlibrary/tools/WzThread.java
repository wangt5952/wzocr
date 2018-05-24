package cn.wz.scanner.scanlibrary.tools;

import java.util.concurrent.CountDownLatch;

/**
 * 解码线程公共基类.
 */
public class WzThread extends Thread {
    /** 线程控制. */
    private CountDownLatch cdl;

    /*
     * 线程控制Setter And Getter.
     */
    public CountDownLatch getCdl() {
        return cdl;
    }

    public void setCdl(CountDownLatch cdl) {
        this.cdl = cdl;
    }
}
