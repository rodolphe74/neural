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

    Integer sum = 0;

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
        int iterativeSum = 0;
        for (int i = 0; i < 200; i++) {
            iterativeSum += i;
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
            }
        }
        long t2 = System.currentTimeMillis();
        Log.info("Iterative time:" + (t2 - t1));
        Log.info("Iterative sum:" + iterativeSum);

        Pool pool = new Pool(16);
        for (int k = 0; k < 5; k++) {
            t1 = System.currentTimeMillis();
            sum = 0;
            for (int i = 0; i < 200; i++) {
                Task t = new Task(pool, i) {
                    @Override
                    public void whatToDo(Object parameter) {
                        try {
                            synchronized (ConcurrencyTest.this) {
                                ConcurrencyTest.this.sum += (int) parameter;
                            }
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                        }
                    }
                };
                pool.addTask(t);
            }
            pool.doTasks();
            t2 = System.currentTimeMillis();
            Log.info("Concurrency time:" + (t2 - t1));
            Log.info("Concurrency sum:" + sum);
            assertTrue(sum == 19900);
        }
    }
}
