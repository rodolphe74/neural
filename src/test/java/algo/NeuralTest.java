package algo;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Random;

import com.esotericsoftware.minlog.Log;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import mnist.MnistDataReader;
import mnist.MnistMatrix;

/**
 * Unit test for simple App.
 */
public class NeuralTest extends TestCase {
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

	private static int getRandomNumberInRange(int min, int max) {
		Random r = new Random();
		return r.nextInt((max - min) + 1) + min;
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
		// 0.298371109 in
		// https://mattmazur.com/2015/03/17/a-step-by-step-backpropagation-example/
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

//	public void testOcrNetwork() throws URISyntaxException, IOException {
//		Log.set(Log.LEVEL_INFO);
//		URL url = Ocr.class.getClassLoader().getResource("ocr.neu");
//		String mainPath = Paths.get(url.toURI()).toString();
//		Network network = Network.readFromDisk(mainPath);
//
//		// load unlearned symbols
//		url = Ocr.class.getClassLoader().getResource("digits");
//		mainPath = Paths.get(url.toURI()).toString();
//		List<Pair<String, File>> filesList = new ArrayList<>();
//		Ocr.recurseFiles(mainPath, filesList);
//
//		// keep a copy of trained network
//		// since predictions update weights
//		Network trainedNetwork = Network.copy(network, true);
//		Log.info("error:" + String.format("%.10f", network.getTotalError()));
//
//		Log.info("Samples size:" + filesList.size());
//		int count = 0;
//		int randomCount = 0;
//		for (Pair<String, File> p : filesList) {
//			double[] input = Ocr.readSymbol(p.getValue1().getAbsolutePath());
//			network = trainedNetwork;
//			double result[] = Network.predict(network, input);
////			Ocr.debugSymbol(input);
////			Network.displayResult(null, result);
//			Log.info(p.getValue0() + " - Max output neuron index:" + Ocr.getMaxStimulatedNeuron(result) + "["
//					+ p.getValue1().getName() + "]");
//			if (p.getValue0().equals(String.valueOf(Ocr.getMaxStimulatedNeuron(result)))) {
//				count++;
//			}
//			if (getRandomNumberInRange(0, 9) == Ocr.getMaxStimulatedNeuron(result)) {
//				randomCount++;
//			}
//		}
//		Log.info("Accuracy:" + count + "/" + filesList.size());
//		Log.info("Random:" + randomCount + "/" + filesList.size());
//	}

	public void testOcrMnistNetwork() throws URISyntaxException, IOException {
		// load trained network
		Log.set(Log.LEVEL_INFO);
		URL url = Ocr.class.getClassLoader().getResource("mnist.neu");
		String mainPath = Paths.get(url.toURI()).toString();
		Network network = Network.readFromDisk(mainPath);

		// load unlearned symbols
		url = Ocr.class.getClassLoader().getResource("mnist");
		mainPath = Paths.get(url.toURI()).toString();
		MnistMatrix[] mnistMatrix = new MnistDataReader().readData(mainPath + "/t10k-images.idx3-ubyte",
				mainPath + "/t10k-labels.idx1-ubyte");

		OcrMnist.debugSymbol(mnistMatrix[0]);
		OcrMnist.debugSymbol(mnistMatrix[mnistMatrix.length - 1]);

		// keep a copy of trained network
		// since predictions update weights
		Network trainedNetwork = Network.copy(network, true);
		Log.info("error:" + String.format("%.10f", network.getTotalError()));

		int count = 0;
		int accuracy = 0;
		int randomCount = 0;
		final int iterTests = mnistMatrix.length;

		for (MnistMatrix matrix : mnistMatrix) {
			double[] input = OcrMnist.getDoubleValues(matrix);

			network = trainedNetwork;
			double result[] = Network.predict(network, input);

//			OcrMnist.debugSymbol(matrix);
//			OcrMnist.debugOutput(result);
//			Log.info(matrix.getLabel() + " - Max output neuron index:" + OcrMnist.getMaxStimulatedNeuron(result));

			if (matrix.getLabel() == OcrMnist.getMaxStimulatedNeuron(result)) {
				accuracy++;
			}

			if (getRandomNumberInRange(0, 9) == Ocr.getMaxStimulatedNeuron(result)) {
				randomCount++;
			}

			if (count == iterTests) {
				break;
			}

			count++;
		}

		Log.info("Accuracy:" + accuracy + "/" + iterTests);
		Log.info("Random:" + randomCount + "/" + iterTests);
	}
}
