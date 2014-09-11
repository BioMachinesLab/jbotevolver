package neat.continuous;

import java.io.Serializable;

class Neuron implements Serializable{
	
	private int type;
	private boolean decayNeuron;
	private double decay;
	private long innovationId;
	private long id;
	
	public Neuron(long id, int type, long innovationId) {
		this.id = id;
		this.type = type;
		this.innovationId = innovationId;
	}
	
	public Neuron(long id, int type, double decay, long innovationId) {
		this(id, type,innovationId);
		this.decay = decay;
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
	
}
