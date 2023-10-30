package algo;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

import com.esotericsoftware.minlog.Log;

import mnist.MnistDataReader;
import mnist.MnistMatrix;

public class Mnist {

	final static int WIDTH = 28;
	final static int HEIGHT = 28;
	final static int SYMBOL_SIZE = WIDTH * HEIGHT;
	final static double THRESHOLD = 0.6;
	final static char[] gradient = { ' ', '.', 'o', 'O', '0' };
	static String[] matchSymbol;

	static double[] getDoubleValues(final MnistMatrix matrix) {
		int[][] data = matrix.getData();
		double[] symbol = new double[matrix.getNumberOfRows() * matrix.getNumberOfColumns()];
		for (int i = 0; i < matrix.getNumberOfRows(); i++) {
			for (int j = 0; j < matrix.getNumberOfColumns(); j++) {
				symbol[i * matrix.getNumberOfColumns() + j] = data[i][j] / 255.0;
			}
		}
		return symbol;
	}

	static void debugSymbol(final MnistMatrix matrix) {
		System.out.println("label: " + matrix.getLabel());
		double[] symbol = getDoubleValues(matrix);
		for (int i = 0; i < HEIGHT; i++) {
			for (int j = 0; j < WIDTH; j++) {
				char c = gradient[(int) (symbol[i * WIDTH + j] * (gradient.length - 1))];
				System.out.print(c);

			}
			System.out.println();
		}
	}

	static double[] getOutputForSymbolIndex(int index) {
		double[] out = new double[10];
		out[index] = 1;
		return out;
	}

	static void debugOutput(double[] output) {
		for (int i = 0; i < output.length; i++) {
			System.out.print(String.format(" %.2f", output[i]));
		}
		System.out.println();
	}

	static int getMaxStimulatedNeuron(double[] output) {
		double maxNeuron = 0;
		int maxIndex = 0;
		for (int i = 0; i < output.length; i++) {
			if (output[i] > maxNeuron) {
				maxNeuron = output[i];
				maxIndex = i;
			}
		}

		return maxIndex;
	}

	public static void main(String[] args) throws IOException, URISyntaxException {
		Log.set(Log.LEVEL_INFO);

		Network network = null;

		// check if network was done before
		network = Network.readFromDisk("mnist.neu");

		// where the data set is
		URL url = Mnist.class.getClassLoader().getResource("mnist");
		String mainPath = Paths.get(url.toURI()).toString();

		if (network == null) {

			MnistMatrix[] mnistMatrix = new MnistDataReader().readData(mainPath + "/train-images.idx3-ubyte",
					mainPath + "/train-labels.idx1-ubyte");

			debugSymbol(mnistMatrix[0]);
			debugSymbol(mnistMatrix[mnistMatrix.length - 1]);

			// define in/out sets
			double[][] inputs = new double[mnistMatrix.length][SYMBOL_SIZE];
			double[][] outputs = new double[mnistMatrix.length][10];
			int count = 0;
			for (MnistMatrix matrix : mnistMatrix) {
				outputs[count] = getOutputForSymbolIndex(matrix.getLabel());
				inputs[count++] = getDoubleValues(matrix);
			}

			// Defining an OCR network
			network = new Network();

			// Layers
			Layer l0 = Network.createLayer(network, 784, 0.0f);
			Layer l1 = Network.createLayer(network, 128, 0.0f);
			Layer l2 = Network.createLayer(network, 64, 0.0f);
			Layer l3 = Network.createLayer(network, 10, 0.0f);

			// Layers connection
			Network.connectLayers(network, l0, l1);
			Network.connectLayers(network, l1, l2);
			Network.connectLayers(network, l2, l3);

			// Add layers on network
			network.addLayer(l0);
			network.addLayer(l1);
			network.addLayer(l2);
			network.addLayer(l3);

			// Random weights
			network.xavierInitWeights();

			System.out.println("Network defined");

			// draw network
//			try {
//				NetworkDrawer.draw(network, "mnist.svg");
//			} catch (IOException e) {
//				Log.error("Can't write network pdf", e);
//			}
			
			// Define sets
			network.setInputs(inputs);
			network.setExpectedOutputs(outputs);

			// train on 2 epochs
			network.setLearningRate(0.3);
			Log.info("Start training");
			int epochs = network.train(2, -1);
			Log.info("Trained until epochs " + epochs);

			network.writeOnDisk("mnist.neu");
		} else {
			System.out.println("Continue training");
			// train
			network.setLearningRate(0.3);
			int epochs = network.train(2, -1); // ~ 1,5 hour
			Log.info("trained until epochs " + epochs);
			
			network.writeOnDisk("mnist.neu");
		}

	}
}
