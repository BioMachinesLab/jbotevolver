package neat.continuous;

public class EvolutionaryParameter {
	
	private double parameter;
	
	public EvolutionaryParameter(double parameter) {
		this.parameter = parameter;
	}
	
	public EvolutionaryParameter() {
		this(0);
	}
	
	public void setParameter(double parameter) {
		this.parameter = parameter;
	}
	
	public double getParameter() {
		return parameter;
	}

}
