package algo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.SerializationUtils;
import org.javatuples.Pair;

import com.esotericsoftware.minlog.Log;

public class Network implements Serializable {
	private static final long serialVersionUID = 1L;
	private List<Layer> layers;
	private double learningRate = 0.1;
	private double totalError;
	private double[][] inputs;
	private double[][] exprectedOutputs;
	private static int currentNeuron = 0;

	public Network() {
		layers = new ArrayList<>();
	}

	public void addLayer(Layer l) {
		layers.add(l);
	}

	public void writeOnDisk(String filename) throws IOException {
		FileOutputStream fos = new FileOutputStream(filename);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(this);
		oos.close();
	}

	static public Network readOnDisk(String filename) {
		File f = new File(filename);
		if (!f.exists()) {
			return null;
		}
		try {
			FileInputStream fis = new FileInputStream(filename);
			ObjectInputStream ois = new ObjectInputStream(fis);
			Network network = (Network) ois.readObject();
			ois.close();
			return network;
		} catch (IOException | ClassNotFoundException e) {
			return null;
		}
	}

	public void initWeights() {
		// https://machinelearningmastery.com/weight-initialization-for-deep-learning-neural-networks/
		int sz = getLayers().get(0).getNeurons().size();
		double lower = -(1.0 / Math.sqrt(sz));
		double upper = (1.0 / Math.sqrt(sz));
		for (Layer l : layers) {
			for (Neuron n : l.getNeurons()) {
				for (Synapse s : n.getOutputList()) {
					double r = Math.random();
					double scaled = lower + r * (upper - lower);
					Log.debug("synapse from " + n.getName() + "=" + scaled);
					s.setWeight(scaled);
					// Log.debug("synapse from " + n.getName() + "=" + r);
					// s.setWeight(r);
				}
			}
		}
	}

	public void xavierInitWeights() {
		// https://ai.plainenglish.io/weight-initialization-in-neural-network-9b3935192f6
		for (Layer l : layers) {
			for (Neuron n : l.getNeurons()) {
				for (Synapse s : n.getOutputList()) {
					double r = Math.random();
					int fanIn = n.getInputList().size();
					int fanOut = n.getOutputList().size();
					double lower = -(Math.sqrt(6) / Math.sqrt(fanIn + fanOut));
					double upper = (Math.sqrt(6) / Math.sqrt(fanIn + fanOut));
					double scaled = lower + r * (upper - lower);
					Log.debug("synapse weight from " + n.getName() + "=" + scaled);
					s.setWeight(scaled);
				}
			}
		}
	}

	public static Layer createLayer(Network network, int neuronsCount, float bias) {
		Layer layer = new Layer();
		for (int i = 0; i < neuronsCount; i++) {
			Neuron n = new Neuron("N" + String.valueOf(currentNeuron++), bias);
			layer.addNeuron(n);
		}
		return layer;
	}

	public static void connectLayers(Network network, Layer firstLayer, Layer secondLayer) {
		for (Neuron n : firstLayer.getNeurons()) {
			for (Neuron m : secondLayer.getNeurons()) {
				n.addOutputNeuron(m, 0.0);
			}
		}
	}

	public List<Layer> getLayers() {
		return layers;
	}

	public void setLayers(List<Layer> layers) {
		this.layers = layers;
	}

	public void forward() {
		for (Layer l : layers) {
			List<Pair<Neuron, Double>> neuronsToUpdate = new ArrayList<>();
			for (Neuron n : l.getNeurons()) {
				double w = 0.0;
				boolean updateOutput = n.getInputList().size() > 0;
				for (Synapse s : n.getInputList()) {
					// Neuron has some inputs, need to update output
					w += s.getLeftNeuron().getOutput() * s.getWeight();
				}
				w += n.getBias();

				if (updateOutput) {
					neuronsToUpdate.add(new Pair<Neuron, Double>(n, w));
				}
			}

			// update outputs
			for (Pair<Neuron, Double> p : neuronsToUpdate) {
				Log.debug("Updating " + p.getValue0().getName() + " current output=" +
						p.getValue0().getOutput());
				p.getValue0().setOutput(Calculus.sigmoid(p.getValue1()));
				Log.debug(" to " + p.getValue0().getOutput());
			}
			/////////////
		}
	}

	static double[] predict(Network network, double[] input) {
		int inputLayerLength = network.getLayers().get(0).getNeurons().size();
		for (int j = 0; j < inputLayerLength; j++) {
			network.getLayers().get(0).getNeurons().get(j).setOutput(input[j]);
		}

		network.forward();

		int outputIndex = network.getLayers().size() - 1;
		int outputSize = network.getLayers().get(outputIndex).getNeurons().size();
		double[] o = new double[outputSize];
		for (int k = 0; k < outputSize; k++) {
			o[k] = network.getLayers().get(outputIndex).getNeurons().get(k).getOutput();
		}
		return o;
	}

	static Network copy(Network network, boolean clearSynapsesCache) {
		Network networkCopy = SerializationUtils.clone(network);

		if (clearSynapsesCache == true)
			SynapseFinder.getInstance().clearCache();
		;

		for (Layer l : networkCopy.getLayers()) {
			for (Neuron n : l.getNeurons()) {
				for (Synapse s : n.getOutputList()) {
					SynapseFinder.getInstance().addNeuron(s, n, s.getRightNeuron());
				}
			}
		}

		return networkCopy;
	}

	public double totalError() {
		Layer outputLayer = getLayers().get(getLayers().size() - 1);
		double totalError = 0.0;
		for (Neuron n : outputLayer.getNeurons()) {
			double error = 0;
			if (n.getExpected() != null) {
				error = Calculus.error(n.getExpected(), n.getOutput());
			}
			totalError += error;
		}
		return totalError;
	}

	private void backwardOutputLayer() {
		// update weights by back propagate error
		// specific to output layer
		Layer outputLayer = getLayers().get(getLayers().size() - 1);
		for (Neuron n : outputLayer.getNeurons()) {
			double dErrorOut = -(n.getExpected() - n.getOutput());
			n.setdErrorOut(dErrorOut);
			double dOutNet = n.getOutput() * (1 - n.getOutput());
			n.setdOutNet(dOutNet);

			for (Synapse s : n.getInputList()) {
				double deltaW = dErrorOut * dOutNet * s.getLeftNeuron().getOutput();
				////////
				// s.setWeight(s.getWeight() - learningRate * deltaW);
				s.setPendingWeight(s.getWeight() - learningRate * deltaW);
			}
		}
	}

	private void backwardLayers(int layerIndex) {
		// update weights by back propagate error
		// on intermediate layers
		Layer layer = getLayers().get(layerIndex);
		for (Neuron n : layer.getNeurons()) {
			// find error on output
			double dErrorTotalOut = 0.0;
			for (Synapse s : n.getOutputList()) {
				double dErrorNet = s.getRightNeuron().getdErrorOut() * s.getRightNeuron().getdOutNet();
				double dErrorOut = dErrorNet * s.getWeight();
				dErrorTotalOut += dErrorOut;
			}

			double dOutNet = n.getOutput() * (1 - n.getOutput());

			// update incoming weights
			for (Synapse s : n.getInputList()) {
				// update m<-(w)-n
				double deltaW = dErrorTotalOut * dOutNet * s.getLeftNeuron().getOutput();
				/////////
				// s.setWeight(s.getWeight() - learningRate * deltaW);
				s.setPendingWeight(s.getWeight() - learningRate * deltaW);
			}
		}
	}

	public void backward() {
		// long t1 = System.currentTimeMillis();
		backwardOutputLayer();
		for (int i = 1; i < layers.size() - 1; i++) {
			backwardLayers(i);
		}
		// long t2 = System.currentTimeMillis();
		// System.out.println("#### Backward time:" + (t2 - t1) + " ####");
		// Commit weights
		// TODO only current network weights
		SynapseFinder.getInstance().commitWeights();
	}

	public int train(int epochs, double tresholdError) {
		// int counter = 0;
		// int modCounter = 500;
		double meanEpochError = 0;
		for (int i = 0; i < epochs; i++) {
			meanEpochError = 0;
			// set input and expected output
			for (int j = 0; j < inputs.length; j++) {
				for (int k = 0; k < inputs[j].length; k++)
					getLayers().get(0).getNeurons().get(k).setOutput(inputs[j][k]);
				for (int k = 0; k < exprectedOutputs[j].length; k++)
					getLayers().get(getLayers().size() - 1).getNeurons().get(k).setExpected(exprectedOutputs[j][k]);

				// backpropagation
				forward(); // update neurons output
				totalError = totalError();
				// String formatedError = String.format("%.10f", totalError);
				// if (counter % modCounter == 0)
				// Log.info("Epoch " + i + " Input set " + j + " Total error=" + formatedError);
				// counter++;
				// if (totalError < tresholdError) {
				// Log.debug("Threshold stop at epoch " + i);
				// return i;
				// }
				backward(); // update synapse weights regarding error

				meanEpochError += totalError;

			}
			meanEpochError /= inputs.length;
			String formatedError = String.format("%.10f", meanEpochError);
			Log.info("Mean epoch error " + i + " -> " + formatedError);
		}
		return epochs;
	}

	static void displayResult(double[] inputs, double[] outputs) {
		// for (int i = 0; i < inputs.length; i++) {
		// System.out.print("[" + inputs[i] + "]");
		// }
		System.out.print(" -> ");
		for (int i = 0; i < outputs.length; i++) {
			String formatedError = String.format("%.3f", outputs[i]);
			System.out.print("[" + formatedError + "]");
		}
		System.out.println();
	}

	public double getLearningRate() {
		return learningRate;
	}

	public void setLearningRate(double learningRate) {
		this.learningRate = learningRate;
	}

	public double[][] getInputs() {
		return inputs;
	}

	public void setInputs(double[][] inputs) {
		this.inputs = inputs;
	}

	public double[][] getExprectedOutputs() {
		return exprectedOutputs;
	}

	public void setExpectedOutputs(double[][] exprectedOutput) {
		this.exprectedOutputs = exprectedOutput;
	}

	public double getTotalError() {
		return totalError;
	}

	public void setTotalError(double totalError) {
		this.totalError = totalError;
	}
}
