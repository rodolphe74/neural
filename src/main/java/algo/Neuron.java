package algo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Neuron implements Serializable {
	private static final long serialVersionUID = 1L;
	private String name;
    private double output;
    private double error;
    private Double expected;
    private double bias = 0.0;

    private List<Synapse> outputList;

    // Used for backward propagation
    private double dErrorOut;
    private double dOutNet;

    public Neuron() {
        this.name = "?";
        this.bias = 0.0;
        // inputList = new ArrayList<>();
        outputList = new ArrayList<>();
    }

    public double getOutput() {
        return output;
    }

    public void setOutput(double output) {
        this.output = output;
    }

    public double getError() {
        return error;
    }

    public void setError(double error) {
        this.error = error;
    }

    public Double getExpected() {
        return expected;
    }

    public void setExpected(Double expected) {
        this.expected = expected;
    }

    public Neuron(double bias) {
        this.name = "?";
        this.bias = bias;
        // inputList = new ArrayList<>();
        outputList = new ArrayList<>();
        expected = null;
    }

    public Neuron(String name) {
        this.name = name;
        this.bias = 0.0;
        // inputList = new ArrayList<>();
        outputList = new ArrayList<>();
        expected = null;
    }

    public Neuron(String name, double bias) {
        this.name = name;
        this.bias = bias;
        // inputList = new ArrayList<>();
        outputList = new ArrayList<>();
        expected = null;
    }

    public void addOutputNeuron(Neuron n, double weight) {
        Synapse s = new Synapse();
        s.setRightNeuron(n);
        s.setLeftNeuron(this);
        s.setWeight(weight);
        outputList.add(s);
        SynapseFinder.getInstance().addNeuron(s, this, n);
    }

    public double getBias() {
        return bias;
    }

    public void setBias(double bias) {
        this.bias = bias;
    }

    public List<Synapse> getInputList() {
        return SynapseFinder.getInstance().getSynapsesThatEndAt(this);
    }

    public List<Synapse> getOutputList() {
        return outputList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getdErrorOut() {
        return dErrorOut;
    }

    public void setdErrorOut(double diffError) {
        this.dErrorOut = diffError;
    }

    public double getdOutNet() {
        return dOutNet;
    }

    public void setdOutNet(double derivative) {
        this.dOutNet = derivative;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(name + " [b=" + bias + "]");
        sb.append("\n");
        for (Synapse s : getInputList()) {
            sb.append("  <-" + s.getLeftNeuron().name);
            sb.append("\n");
        }
        for (Synapse s : getOutputList()) {
            sb.append("  ->" + s.getRightNeuron().name);
            sb.append("\n");
        }
        return sb.toString();
    }
}
