# neural

<img src="homer.jpg" height="300">

My multi-layer perceptron back propagation implementation

# xor sample
## define network and train
```java
// Defining a XOR network
			network = new Network();

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
			network.setInputs(inputs);
			network.setExpectedOutputs(outputs);

			// train
			network.setLearningRate(0.1);
			long st = System.currentTimeMillis();
			int epochs = network.train(20000, 0.000000005);
			long et = System.currentTimeMillis();
			Log.info("epochs:" + epochs + " in " + (et - st));
```
## prediction
```java
		// tests
		Network trainedNetwork = Network.copy(network, true);
		Log.info("error:" + String.format("%.10f", network.getTotalError()));

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
```

## results
```
[0,000][0,000] -> [0,153]
[0,000][1,000] -> [0,887]
[1,000][0,000] -> [0,895]
[1,000][1,000] -> [0,099]
```
