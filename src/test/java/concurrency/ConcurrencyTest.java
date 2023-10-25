package concurrency;

import com.esotericsoftware.minlog.Log;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class ConcurrencyTest
        extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public ConcurrencyTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(ConcurrencyTest.class);
    }

    /**
     * Rigourous Test :-)
     */
    public void testConcurrency() {
        Log.set(Log.LEVEL_INFO);
        long t1 = System.currentTimeMillis();
        for (int i = 0; i < 200; i++) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
            }
        }
        long t2 = System.currentTimeMillis();
        Log.info("Iterative time:" + (t2 - t1));

        Pool pool = new Pool(8);
        t1 = System.currentTimeMillis();
        Task t = new Task() {
            @Override
            public void whatToDo() {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                }
            }
        };
        for (int i = 0; i < 200; i++) {
            pool.addTask(t);
        }
        pool.doTasks();
        t2 = System.currentTimeMillis();
        Log.info("Concurrency time:" + (t2 - t1));
    }

}
