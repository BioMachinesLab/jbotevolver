package neat.continuous;

import java.io.Serializable;

public class Neuron implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private int type = 0;
	private boolean decayNeuron = false;
	private double decay = 0;
	private double bias;
	private long innovationId = 0;
	private long id = 0;
	
	public Neuron(long id, int type, long innovationId) {
		this.id = id;
		this.type = type;
		this.innovationId = innovationId;
	}
	
	public Neuron(long id, int type, double decay, double bias, long innovationId) {
		this(id, type,innovationId);
		this.decay = decay;
		this.bias = bias;
		this.decayNeuron = true;
	}
	
	public long getId() {
		return id;
	}
	
	public int getType() {
		return type;
	}
	
	public boolean isDecayNeuron() {
		return decayNeuron;
	}
	
	public double getDecay() {
		return decay;
	}
	
	public long getInnovationId() {
		return innovationId;
	}

	public double getBias() {
		return bias;
	}
	
}
