package algo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.esotericsoftware.minlog.Log;


public class SynapseFinder implements Serializable {
	private static final long serialVersionUID = 1L;
	private static Map<String, Synapse> synapseCache = new HashMap<String, Synapse>();
    private static String keySeparator = ">";
    private static SynapseFinder singleton;
    private static Map<Neuron, List<Synapse>> neuronInputsCache = new HashMap<>();
    
    public static SynapseFinder getInstance() {
    	if (singleton == null) {
    		singleton = new SynapseFinder();

    	}
		return singleton;
    }
    
    public static void setInstance(SynapseFinder instance) {
    	singleton = instance;
    }

    private String computeKey(Neuron n, Neuron m) {
        return String.valueOf(System.identityHashCode(n)) + keySeparator + String.valueOf(System.identityHashCode(m));
    }

    public void addNeuron(Synapse s, Neuron fromNeuron, Neuron toNeuron) {
        synapseCache.put(computeKey(fromNeuron, toNeuron), s);
    }

    public Synapse getSynapse(Neuron fromNeuron, Neuron toNeuron) {
        return synapseCache.get(computeKey(fromNeuron, toNeuron));
    }

    public List<Synapse> getSynapsesThatEndAt(Neuron n) {
        // check cache
        List<Synapse> slc = neuronInputsCache.get(n);
        if (slc != null)  {
            return slc;
        }

        // not in cache
        List<Synapse> sl = new ArrayList<Synapse>();
        String neuronEndKey = String.valueOf(System.identityHashCode(n));
        for (String key : synapseCache.keySet()) {
            String[] neuronKeys = key.split(keySeparator);
            if (neuronKeys[1].equals(neuronEndKey)) {
                sl.add(synapseCache.get(key));
            }
        }
        neuronInputsCache.put(n, sl);
        return sl;
    }
    
    public int countSynapse() {
    	return synapseCache.size();
    }

    public void clearCache() {
        synapseCache.clear();
        neuronInputsCache.clear();
    }
    
    public void commitWeights() {
    	for (Synapse s : synapseCache.values()) {
    		s.setWeight(s.getPendingWeight());
    		Log.debug(s.getLeftNeuron().getName() + "->" + s.getRightNeuron().getName() + "=" + s.getWeight());
    	}
    }
}
