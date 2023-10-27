package algo;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.javatuples.Pair;

import com.esotericsoftware.minlog.Log;

public class Ocr {

	final static int WIDTH = 32;
	final static int HEIGHT = 32;
	final static int SYMBOL_SIZE = WIDTH * HEIGHT;
	final static double THRESHOLD = 128;
	static String[] matchSymbol;

	static double[] readSymbol(String filename) throws IOException {
		double[] array = new double[SYMBOL_SIZE];
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filename));
		BufferedImage rgbImage = ImageIO.read(bis);

		byte[] pixels = ((DataBufferByte) rgbImage.getRaster().getDataBuffer()).getData();

		int comps = 3;
		if (rgbImage.getColorModel().hasAlpha()) {
			comps = 4;
		}

		for (int i = 0; i < SYMBOL_SIZE; i++) {
			int red = (int) pixels[i * comps] & 0xff;
			int blue = (int) pixels[i * comps + 1] & 0xff;
			int green = (int) pixels[i * comps + 2] & 0xff;
			// double gray = 0.299 * red + 0.587 * green + 0.114 * blue;
			// array[i] = 1 - gray;
			if (red < THRESHOLD || green < THRESHOLD || blue < THRESHOLD) {
				array[i] = 1.0;
			}

		}
		return array;
	}

	static void debugSymbol(double[] symbol) {
		for (int i = 0; i < HEIGHT; i++) {
			for (int j = 0; j < WIDTH; j++) {
				if (symbol[i * WIDTH + j] == 1.0) {
					System.out.print("@");
				} else {
					System.out.print(".");
				}
			}
			System.out.println();
		}
	}

	static void recurseFiles(String path, List<Pair<String, File>> filesList) {

		File root = new File(path);
		File[] list = root.listFiles();

		if (list == null)
			return;

		for (File f : list) {
			if (f.isDirectory()) {
				recurseFiles(f.getAbsolutePath(), filesList);
				System.out.println("Dir:" + f.getAbsoluteFile());
			} else {
				// System.out.println("File:" + f.getAbsoluteFile() + " - " +
				// Paths.get(f.getParentFile().toString()).getFileName() );
				filesList.add(new Pair<String, File>(Paths.get(f.getParentFile().toString()).getFileName().toString(),
						f.getAbsoluteFile()));
			}
		}
	}

	// static double[] getOutputForSymbolIndex(int index) {
	// String binaryString = Integer.toBinaryString(index);
	// String padded = String.format("%5s", binaryString).replaceAll(" ", "0");
	// double[] out = new double[padded.length()];
	// for (int i = 0; i < padded.length(); i++) {
	// char bit = padded.charAt(i);
	// out[i] = (bit == '1' ? 1 : 0);
	// }
	// return out;
	// }

	static double[] getOutputForSymbolIndex(int index) {
		double[] out = new double[10];
		out[index] = 1;
		return out;
	}

	// static String convertOutputToSymbol(double[] output) {
	// String binaryString = "";
	// // for (int i = output.length - 1; i >= 0; i--) {
	// for (int i = 0; i < output.length; i++) {
	// int v = (int) Math.round(output[i]);
	// binaryString += String.valueOf(v);
	// }
	// int index = Integer.parseInt(binaryString, 2);
	// return matchSymbol[index];
	// }

	// static String convertOutputToSymbol(double[] output) {
	// double maxNeuron = 0;
	// int maxIndex = 0;
	// for (int i = 0; i < output.length; i++) {
	// if (output[i] > maxNeuron) {
	// maxNeuron = output[i];
	// maxIndex = i;
	// }
	// }
	// Log.info("Max Neuron index:" + maxIndex);
	// return matchSymbol[maxIndex];
	// }

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
		network = Network.readOnDisk("ocr.neu");

		if (network == null) {
			List<Pair<String, File>> filesList = new ArrayList<>();

			// URI uri = ClassLoader.getSystemResource("mathdataset32").toURI();
			// String mainPath = Paths.get(uri).toString();
			// recurseFiles(mainPath, filesList);

			recurseFiles("C:/Users/rodoc/HOME/developpement/eclipse-workspace/neural/target/classes/digits",
					filesList);

			Map<String, List<double[]>> inputsMap = new HashMap<>();
			for (Pair<String, File> p : filesList) {
				System.out.println(p.getValue0() + "->" + p.getValue1());
				double[] input = Ocr.readSymbol(p.getValue1().getAbsolutePath());

				List<double[]> list = inputsMap.get(p.getValue0());
				if (list == null) {
					List<double[]> newList = new ArrayList<>();
					inputsMap.put(p.getValue0(), newList);
				}
				list = inputsMap.get(p.getValue0());
				list.add(input);
			}

			for (String k : inputsMap.keySet()) {
				System.out.println(k + "->" + inputsMap.get(k).size());
			}

			// prepare inputs & outputs
			double batchRatio = 0.75;
			List<double[]> batchInputs = new ArrayList<>();
			List<double[]> batchOutputs = new ArrayList<>();
			int idx = 0;
			matchSymbol = new String[inputsMap.keySet().size()];
			for (String k : inputsMap.keySet()) {
				System.out.println("key:" + k);
				// take batchratio input from dataset
				int batchSize = (int) (inputsMap.get(k).size() * batchRatio);
				List<double[]> inputsList = inputsMap.get(k); // List of same symbol

				for (int j = 0; j < batchSize; j++) {
					batchInputs.add(inputsList.get(j));
					// debugSymbol(inputsList.get(j));
					batchOutputs.add(getOutputForSymbolIndex(idx));
				}
				matchSymbol[idx] = k;
				idx++;
			}
			System.out.println("total symbols:" + inputsMap.keySet().size());
			System.out.println("batch input size:" + batchInputs.size());
			System.out.println("batch output size:" + batchOutputs.size());

			double[][] inputs = new double[batchInputs.size()][WIDTH * HEIGHT];
			double[][] outputs = new double[batchInputs.size()][10];
			for (int i = 0; i < batchInputs.size(); i++) {
				System.arraycopy(batchInputs.get(i), 0, inputs[i], 0, WIDTH * HEIGHT);
				System.arraycopy(batchOutputs.get(i), 0, outputs[i], 0, 10);
				// debugSymbol(batchInputs.get(i));
			}

			System.out.println("Symbols loaded");

			// Defining an OCR network
			network = new Network();

			// Layers
			Layer l0 = Network.createLayer(network, 1024, 0.0f);
			Layer l1 = Network.createLayer(network, 20, 0.0f);
			Layer l2 = Network.createLayer(network, 10, 0.0f);

			// Layers connection
			Network.connectLayers(network, l0, l1);
			Network.connectLayers(network, l1, l2);

			// Add layers on network
			network.addLayer(l0);
			network.addLayer(l1);
			network.addLayer(l2);

			// Random weights
			network.xavierInitWeights();

			System.out.println("Network defined");

			// draw network
			// try {
			// NetworkDrawer.draw(network, "ocr.pdf");
			// } catch (IOException e) {
			// Log.error("Can't write network pdf", e);
			// }

			// System.out.println("Network drawed");

			// Define sets
			network.setInputs(inputs);
			network.setExpectedOutputs(outputs);

			// train
			network.setLearningRate(0.3);
			int epochs = network.train(1000, 0.000000005);
			Log.info("trained until epochs " + epochs);

			network.writeOnDisk("ocr.neu");
		}

		// tests
		Network trainedNetwork = Network.copy(network, true);
		Log.info("error:" + String.format("%.10f", network.getTotalError()));

		network = trainedNetwork;
		double[] symbol = readSymbol(
				"C:/Users/rodoc/HOME/developpement/eclipse-workspace/neural/target/classes/digits/7/41840.png");
		debugSymbol(symbol);
		double result[] = Network.predict(network, symbol);
		Network.displayResult(symbol, result);
		Log.info("Max output neuron index:" + getMaxStimulatedNeuron(result) + "\n");

		network = trainedNetwork;
		symbol = readSymbol(
				"C:/Users/rodoc/HOME/developpement/eclipse-workspace/neural/target/classes/digits/9/42367.png");
		debugSymbol(symbol);
		result = Network.predict(network, symbol);
		Network.displayResult(symbol, result);
		Log.info("Max output neuron index:" + getMaxStimulatedNeuron(result) + "\n");

		network = trainedNetwork;
		symbol = readSymbol(
				"C:/Users/rodoc/HOME/developpement/eclipse-workspace/neural/target/classes/digits/2/52888.png");
		debugSymbol(symbol);
		result = Network.predict(network, symbol);
		Network.displayResult(symbol, result);
		Log.info("Max output neuron index:" + getMaxStimulatedNeuron(result) + "\n");
	}
}
