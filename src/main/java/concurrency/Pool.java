package concurrency;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class Pool {

    final public static int MAX_CONCURRENCY = 64;
    private List<Task> tasks = new ArrayList<>();
    private Monitor monitor;

    public Pool() {
        monitor = Monitor.getInstance();
    }

    public Pool(int concurrency) {
        monitor = Monitor.getInstance();
        monitor.setMaxConcurrency(concurrency < MAX_CONCURRENCY ? concurrency : MAX_CONCURRENCY);
    }

    public void setMaxConcurrency(int concurrency) {
        monitor.setMaxConcurrency(concurrency < MAX_CONCURRENCY ? concurrency : MAX_CONCURRENCY);
    }

    public void addTask(Task r) {
        tasks.add(r);
    }

    public void doTasks() {
        Monitor monitor = Monitor.getInstance();

        Iterator<Task> i = tasks.iterator();
        while (i.hasNext()) {
            Runnable r = i.next();
            Thread t = new Thread(r);
            t.start();
            i.remove();
            synchronized (monitor) {
                monitor.w();
            }
            // System.out.println("Tasks done");
        }
    }
}
