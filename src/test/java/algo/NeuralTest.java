package algo;

import com.esotericsoftware.minlog.Log;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class NeuralTest
        extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public NeuralTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(NeuralTest.class);
    }

    private Network createSimpleNetwork() {
        Log.set(Log.LEVEL_INFO);
        SynapseFinder.getInstance().clearCache();
        
        // Neurons
        Neuron i1 = new Neuron("i1");
        i1.setOutput(.05);
        Neuron i2 = new Neuron("i2");
        i2.setOutput(.10);
        Neuron h1 = new Neuron("h1", .35);
        Neuron h2 = new Neuron("h2", .35);
        Neuron o1 = new Neuron("o1", .60);
        o1.setExpected(0.01);
        Neuron o2 = new Neuron("o2", .60);
        o2.setExpected(0.99);

        // Neurons connection
        i1.addOutputNeuron(h1, .15);
        i1.addOutputNeuron(h2, .25);
        i2.addOutputNeuron(h1, .20);
        i2.addOutputNeuron(h2, .30);
        h1.addOutputNeuron(o1, .40);
        h1.addOutputNeuron(o2, .50);
        h2.addOutputNeuron(o1, .45);
        h2.addOutputNeuron(o2, .55);
        
        // Define layers
        Layer l0 = new Layer();
        l0.addNeuron(i1);
        l0.addNeuron(i2);

        Layer l1 = new Layer();
        l1.addNeuron(h1);
        l1.addNeuron(h2);

        Layer l2 = new Layer();
        l2.addNeuron(o1);
        l2.addNeuron(o2);

        // Define network
        Network network = new Network();
        network.addLayer(l0);
        network.addLayer(l1);
        network.addLayer(l2);
        // network.consolidate(false);

        // In - Out
        double[][] inputs = { { 0.05, 0.10 } };
        double[][] outputs = { { 0.01 }, { 0.99 } };
        network.setInputs(inputs);
        network.setExpectedOutputs(outputs);

        return network;
    }

    /**
     * Rigourous Test :-)
     */
    public void testThreshold() {
    	SynapseFinder.getInstance().clearCache();
        Log.set(Log.LEVEL_INFO);
        Network network = createSimpleNetwork();

        // train
        network.setLearningRate(0.5);
        int stopAt = network.train(10000, 0.00005);
        assertTrue(stopAt == 8256);
    }
    
    public void testAllEpochs() {
    	SynapseFinder.getInstance().clearCache();
    	Log.set(Log.LEVEL_INFO);
        Network network = createSimpleNetwork();
        network.setLearningRate(0.5);
        int stopAt = network.train(10000, 0.0000000005);
        assertTrue(stopAt == 10000);
    }
    
    public void testOneEpoch() {
    	SynapseFinder.getInstance().clearCache();
    	Log.set(Log.LEVEL_DEBUG);
        Network network = createSimpleNetwork();
        network.setLearningRate(0.5);
        int stopAt = network.train(1, 0.0000000005);
        assertTrue(stopAt == 1);
        // 0.298371109 in https://mattmazur.com/2015/03/17/a-step-by-step-backpropagation-example/
        assertTrue(network.getTotalError() == 0.2983711087600027);
    }

    public void testSigmoid() {
        double x = Calculus.sigmoid(-.9);
        assertTrue(x == 0.289050497374996);
    }

    public void testDeepCopy() {
    	SynapseFinder.getInstance().clearCache();
        Log.set(Log.LEVEL_INFO);
        Network network = createSimpleNetwork();
        long startTime = System.currentTimeMillis();
        
        Network networkCopy = Network.copy(network, false);
        long endTime = System.currentTimeMillis();
        
        assertNotNull(networkCopy);
        Log.info("copy network in " + (endTime - startTime));

        // Verify copy
        networkCopy.setLearningRate(0.5);
        int stopAt = networkCopy.train(10000, 0.00005);
        assertTrue(stopAt == 8256);
    }

    public void testSynapseFinder() {
    	SynapseFinder.getInstance().clearCache();
        Neuron n1 = new Neuron("n1");
        Neuron n2 = new Neuron("n2");
        n1.addOutputNeuron(n2, 6.6);
        assertTrue(SynapseFinder.getInstance().countSynapse() == 1);
    }
}
