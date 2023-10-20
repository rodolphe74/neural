package algo;

import java.io.Serializable;
import java.util.ArrayList;

public class Layer implements Serializable {
	private static final long serialVersionUID = 1L;
    private int index;
    private ArrayList<Neuron> neurons;


    public Layer() {
        neurons = new ArrayList<>();
    }

    public void addNeuron(Neuron n) {
        neurons.add(n);
    }

    public ArrayList<Neuron> getNeurons() {
        return neurons;
    }

    public void setNeurons(ArrayList<Neuron> neurons) {
        this.neurons = neurons;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
