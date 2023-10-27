package concurrency;

import java.util.concurrent.CountDownLatch;

public class Monitor {
    static public Monitor instance;
    private int concurrency = 0;
    private int maxConcurrency = 1;
    private CountDownLatch countDownLatch;

    public static Monitor getInstance() {
        if (instance == null) {
            instance = new Monitor();
        }
        return instance;
    }

    public void w() {
        try {
            concurrency++;
            if (concurrency == maxConcurrency) {
                this.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void n(Runnable r) {
        this.notify();
        concurrency--;
        countDownLatch.countDown();
    }

    public int getMaxConcurrency() {
        return maxConcurrency;
    }

    public void setMaxConcurrency(int maxConcurrency) {
        this.maxConcurrency = maxConcurrency;
    }

    public CountDownLatch getCountDownLatch() {
        return countDownLatch;
    }

    public void setCountDownLatch(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }

}
