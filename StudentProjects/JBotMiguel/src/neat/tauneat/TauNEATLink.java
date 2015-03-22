package neat.tauneat;

import org.encog.neural.neat.NEATLink;

public class TauNEATLink extends NEATLink {
	
	private double[] buffer;
	private int index = 0;

	public TauNEATLink(int theFromNeuron, int theToNeuron, double theWeight, int bufferSize) {
		super(theFromNeuron, theToNeuron, theWeight);
		buffer = new double[bufferSize];
	}
	
	public double getCurrentValue() {
		return buffer[index];
	}
	
	public void setNewValue(double val) {
		buffer[index] = val;
		index = (index + 1) % buffer.length;
	}
	
	public int getBufferSize() {
		return buffer.length;
	}
	
	@Override
	public int compareTo(final NEATLink other) {
		
		int result = this.getFromNeuron() - other.getFromNeuron();
		if (result != 0) {
			return result;
		}
		
		result = this.getToNeuron() - other.getToNeuron();
		if (result != 0) {
			return result;
		}

		return this.getBufferSize() - ((TauNEATLink)other).getBufferSize();
	}
	
	@Override
	public boolean equals(final Object other) {
		if (other == null) {
			return false;
		}
		if (other == this) {
			return true;
		}
		if (!(other instanceof TauNEATLink)) {
			return false;
		}
		final TauNEATLink otherMyClass = (TauNEATLink) other;
		return compareTo(otherMyClass) == 0;
	}
	
	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append("[TauNEATLink: fromNeuron=");
		result.append(this.getFromNeuron());
		result.append(", toNeuron=");
		result.append(this.getToNeuron());
		result.append(", bufferSize=");
		result.append(this.getBufferSize());
		result.append("]");
		return result.toString();
	}

}
