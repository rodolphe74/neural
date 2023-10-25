package algo;

import java.io.IOException;

import com.esotericsoftware.minlog.Log;

public class Xor {
    public static void main(String[] args) {
        Log.set(Log.LEVEL_INFO);

        // Defining a XOR network
        Network network = new Network();

        // Layers
        Layer l0 = Network.createLayer(network, 2, 0.0f);
        Layer l1 = Network.createLayer(network, 3, 0.0f);
        Layer l2 = Network.createLayer(network, 1, 0.0f);

        // Layers connection
        Network.connectLayers(network, l0, l1);
        Network.connectLayers(network, l1, l2);

        // Add layers on network
        network.addLayer(l0);
        network.addLayer(l1);
        network.addLayer(l2);

        // Random weights
        network.xavierInitWeights();

        // Define sets
        double[][] inputs = { { 0, 0 }, { 0, 1 }, { 1, 0 }, { 1, 1 } };
        double[][] outputs = { { 0 }, { 1 }, { 1 }, { 0 } };
        network.setInputs(inputs);
        network.setExpectedOutputs(outputs);

        // train
        network.setLearningRate(0.1);
        long st = System.currentTimeMillis();
        int epochs = network.train(20000, 0.000000005);
        long et = System.currentTimeMillis();
        Log.info("epochs:" + epochs + " in " + (et - st));

        // draw network
        try {
            NetworkDrawer.draw(network, "xor.pdf");
        } catch (IOException e) {
            Log.error("Can't write network pdf", e);
        }

        // tests
        Network trainedNetwork = Network.copy(network, true);
        Log.info("error:" +  String.format("%.10f", network.getTotalError()));

        network = trainedNetwork;
        double[] inputTest = inputs[0];
        double result[] = Network.predict(network, inputTest);
        Network.displayResult(inputTest, result);

        network = trainedNetwork;
        inputTest = inputs[1];
        result = Network.predict(network, inputTest);
        Network.displayResult(inputTest, result);

        network = trainedNetwork;
        inputTest = inputs[2];
        result = Network.predict(network, inputTest);
        Network.displayResult(inputTest, result);

        network = trainedNetwork;
        inputTest = inputs[3];
        result = Network.predict(network, inputTest);
        Network.displayResult(inputTest, result);
    }
}
