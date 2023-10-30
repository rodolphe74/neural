package concurrency;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class Pool {

    final public static int MAX_CONCURRENCY = 64;
    private List<Task> tasks = new ArrayList<>();
    private Monitor monitor;

    public Pool() {
        monitor = new Monitor();
    }

    public Pool(int concurrency) {
        monitor = new Monitor();
        monitor.setMaxConcurrency(concurrency < MAX_CONCURRENCY ? concurrency : MAX_CONCURRENCY);
    }

    public void setMaxConcurrency(int concurrency) {
        monitor.setMaxConcurrency(concurrency < MAX_CONCURRENCY ? concurrency : MAX_CONCURRENCY);
    }

    public void addTask(Task r) {
        tasks.add(r);
    }

    public void doTasks() {
        Iterator<Task> i = tasks.iterator();
        CountDownLatch countDownLatch = new CountDownLatch(tasks.size());
        monitor.setCountDownLatch(countDownLatch);
//        System.out.println(monitor.getCountDownLatch().getCount());
        while (i.hasNext()) {
            Runnable r = i.next();
            Thread t = new Thread(r);
            i.remove();
            t.start();
            synchronized (monitor) {
                monitor.w();
            }
            // System.out.println("Tasks done");
        }

//        System.out.println("-->" + monitor.getCountDownLatch().getCount());
        try {
            monitor.getCountDownLatch().await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//         System.out.println("==>" + monitor.getCountDownLatch().getCount());
    }

    public Monitor getMonitor() {
        return monitor;
    }

    public void setMonitor(Monitor monitor) {
        this.monitor = monitor;
    }
}
