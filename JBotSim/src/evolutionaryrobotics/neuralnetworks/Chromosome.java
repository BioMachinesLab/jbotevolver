package evolutionaryrobotics.neuralnetworks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;

import controllers.FixedLenghtGenomeEvolvableController;
import simulation.JBotSim;
import simulation.Simulator;
import simulation.robot.Robot;

public class Chromosome implements Serializable, Cloneable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected double[] alleles;
	protected double   fitness;
	protected int      id;
	protected boolean  fitnessSet = false;
		
	public Chromosome(double[] alleles, int id) {
		this.alleles = alleles;
		this.id      = id;
	}
	
	public void setFitness(double fitness) {
		this.fitness = fitness; 
		this.fitnessSet = true;
	}
	
	public void setAlleles(double[] newAlleles){
		this.alleles = newAlleles;
	}
	
	public double  getFitness() {
		return fitness;
	}
	
	public double[] getAlleles() {
		return alleles;
	}

	public int      getID() {
		return id;
	}

	public boolean getFitnessSet() {
		return fitnessSet;
	}

	// Compare to get the chromosome in descending order
	public static class CompareChromosomeFitness implements Comparator<Chromosome> {
		public int compare(Chromosome arg0, Chromosome arg1) {			
			if (arg0.getFitness() < arg1.getFitness())
				return 1;
			if (arg0.getFitness() > arg1.getFitness())
				return -1;
			
			return 0;
		}
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public Chromosome clone() throws CloneNotSupportedException{
		Chromosome newChromo = (Chromosome) super.clone();
		newChromo.alleles = alleles.clone();
		return newChromo;
	}
	
	public String getAllelesString() {
		String str = "";
		
		for(double d : alleles)
			str+=d+",";
		
		return str;
	}
	
	public void setupRobot(Robot r) {
		if(r.getController() instanceof FixedLenghtGenomeEvolvableController) {
			FixedLenghtGenomeEvolvableController controller = (FixedLenghtGenomeEvolvableController)r.getController();
			controller.setNNWeights(getAlleles());
		}
	}
	
	public ArrayList<Robot> setupRobots(JBotSim jbot, Simulator sim) {
		ArrayList<Robot> totalRobots = jbot.createRobots(sim);
		for (Robot r : totalRobots) {
			setupRobot(r);
		}
		return totalRobots;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Chromosome) {
			Chromosome c = (Chromosome)obj;
			return c.getAlleles().equals(this.getAlleles());
		}
		return false;
	}
}