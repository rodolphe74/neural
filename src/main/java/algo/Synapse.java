package algo;

import java.io.Serializable;

public class Synapse implements Serializable {
    private static final long serialVersionUID = 1L;
    private Neuron leftNeuron;
    private Neuron rightNeuron;
    private double weight;
    private double pendingWeight;

    public Neuron getLeftNeuron() {
        return leftNeuron;
    }

    public void setLeftNeuron(Neuron leftNeuron) {
        this.leftNeuron = leftNeuron;
    }

    public Neuron getRightNeuron() {
        return rightNeuron;
    }

    public void setRightNeuron(Neuron rightNeuron) {
        this.rightNeuron = rightNeuron;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }


	public double getPendingWeight() {
		return pendingWeight;
	}

	public void setPendingWeight(double pendingWeight) {
		this.pendingWeight = pendingWeight;
	}
	
    public String toString() {
        // return "Synapse->" + neuron.toString() + " [w=" + weight + "]";
        return leftNeuron.toString() + "->" + rightNeuron.toString() + " [w=" + weight + "]";
    }

}
