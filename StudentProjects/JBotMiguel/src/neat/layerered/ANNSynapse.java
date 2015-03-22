package neat.layerered;

import java.io.Serializable;

public class ANNSynapse implements Serializable{

	protected long innovationNumber;
	protected double weight;
	protected long from, to;
	
	public ANNSynapse(long innovationNumber, 
			double weight, long from, long to){
		this.innovationNumber = innovationNumber;
		this.weight = weight;
		this.from = from;
		this.to = to;
	}
	
	public double getWeight() {
		return this.weight;
	}

	public long getInnovationNumber() {
		return this.innovationNumber;
	}
	
	public long getFromNeuron(){
		return this.from;
	}
	
	public long getToNeuron(){
		return this.to;
	}

	public ANNSynapse copy() {
		return new ANNSynapse(this.innovationNumber, this.weight, 
				this.from, this.to);
	}	
}

