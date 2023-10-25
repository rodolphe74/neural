package concurrency;

public class Monitor {
    static public Monitor instance;
    private int concurrency = 0;
    private int maxConcurrency = 1;

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

    public void n() {
        this.notify();
        concurrency--;
    }

    public int getMaxConcurrency() {
        return maxConcurrency;
    }

    public void setMaxConcurrency(int maxConcurrency) {
        this.maxConcurrency = maxConcurrency;
    }
}
