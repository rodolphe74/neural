package concurrency;

public abstract class Task implements Runnable {

    public abstract void whatToDo();

    @Override
    public void run() {
        Monitor monitor = Monitor.getInstance();
        this.whatToDo();
        // System.out.println("Avant notify");
        synchronized(monitor) {
            monitor.n();
        }
        // System.out.println("Apres notify");
    }
}
