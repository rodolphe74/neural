package concurrency;

public abstract class Task implements Runnable {

    Object parameter;
    Pool pool;

    public Task(Pool pool, Object parameter) {
        this.parameter = parameter;
        this.pool = pool;
    }

    public abstract void whatToDo(Object parameter);

    @Override
    public void run() {
        this.whatToDo(parameter);
        synchronized (pool.getMonitor()) {
            pool.getMonitor().n(this);
        }
    }

    public Object getParameter() {
        return parameter;
    }

    public void setParameter(Object parameter) {
        this.parameter = parameter;
    }
}
