package evolutionaryrobotics.neuralnetworks;

import java.util.ArrayList;

import controllers.Controller;
import controllers.FixedLenghtGenomeEvolvableController;
import simulation.JBotSim;
import simulation.Simulator;
import simulation.robot.Robot;

public class MultipleChromosome extends Chromosome {

	private int[] genomeLengths;
	private int numberOfChromosomes;
	
	
	public MultipleChromosome(double[] alleles, int id, int[] genomeLengths, int numberOfChromosomes) {
		super(alleles, id);
		this.genomeLengths = genomeLengths;
		this.numberOfChromosomes = numberOfChromosomes;
	}

	public Chromosome getChromosome(int index){
		
		int genomeLength = genomeLengths[index];
		double [] alleles = new double[genomeLength];
		
		int startIndex = 0;
		for(int i = 0; i<index; i++){
			startIndex+= genomeLengths[i];
		}
		for(int i = 0; i<genomeLength; i++){
			alleles[i] = this.alleles[i + startIndex];
		}
		
		return new Chromosome(alleles, index);
	}
	
	public int getNumberOfChromosomes() {
		return numberOfChromosomes;
	}

	public static double[] concatArray(double[] subArray1, double[] subArray2){
		double[] result = new double[subArray1.length+subArray2.length];
		for(int i = 0; i < subArray1.length; i++){
			result[i] = subArray1[i];
		}
		for(int i = subArray1.length; i < subArray2.length; i++){
			result[i] = subArray2[i];
		}
		return result;
	}
	
	@Override
	public ArrayList<Robot> setupRobots(JBotSim jbot, Simulator sim) {
		
		ArrayList<Robot> totalRobots = new ArrayList<Robot>();
		
		int previousrobots = 0;
		
		for(int i = 0; i < getNumberOfChromosomes(); i++){
			// get("robots" + i)
			ArrayList<Robot> robots;
			if(i == 0) {
				robots = Robot.getRobots(sim, jbot.getArguments().get("--robots"));
				previousrobots = robots.size();
			} else{
				jbot.getArguments().get("--robots" + i).setArgument("previousrobots", previousrobots);
				robots = Robot.getRobots(sim, jbot.getArguments().get("--robots" + i));
			}
			
			for(Robot r : robots) {
				if(i == 0)
					r.setController(Controller.getController(sim, r, jbot.getArguments().get("--controllers")));
				else
					r.setController(Controller.getController(sim, r, jbot.getArguments().get("--controllers" + i)));
				totalRobots.add(r);
				
				Chromosome subChromosome = getChromosome(i);
				
				if(r.getController() instanceof FixedLenghtGenomeEvolvableController) {
					FixedLenghtGenomeEvolvableController controller = (FixedLenghtGenomeEvolvableController)r.getController();
					controller.setNNWeights(subChromosome.getAlleles());
				}
			}
		}
		
		return totalRobots;
	}
}
