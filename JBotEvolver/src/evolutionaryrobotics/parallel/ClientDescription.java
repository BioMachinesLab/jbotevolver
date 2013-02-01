package evolutionaryrobotics.parallel;

import java.util.Comparator;

import simulation.util.Arguments;
import evolutionaryrobotics.neuralnetworks.Chromosome;
import evolutionaryrobotics.parallel.task.ChromosomeTask;
import evolutionaryrobotics.populations.Population;

public class ClientDescription implements Comparable<ClientDescription> {
	private Population population;
	private Arguments[] arguments = new Arguments[5];
	private int numberOfChromosomesEvaluated;
	private int numberOfChromosomesDelivered = 0;
	private int totalNumberOfEvaluatiosDone;
	private int priority;
	private int lastStamp = 0;

	public ClientDescription(Arguments[] arguments, int priority) {
		this.arguments = arguments;
		this.priority = priority;

	}

	public Population getPopulation() {
		return population;
	}

	public ChromosomeTask getNextTask() {
		Chromosome chromosome = population.getNextChromosomeToEvaluate();
		if (chromosome != null) {
			numberOfChromosomesDelivered++;
			return new ChromosomeTask(chromosome, this);
		}
		return null;
	}

	public Arguments[] getArguments() {
		return arguments;
	}

	public void setPopulation(Population population) {
		this.population = population;
		numberOfChromosomesDelivered = population.getNumberOfChromosomesEvaluated();
	}

	public synchronized void setEvaluationResult(Chromosome chromosome,
			double fitness, int stamp) {
		population.setEvaluationResult(chromosome, fitness);
		numberOfChromosomesEvaluated++;
		totalNumberOfEvaluatiosDone++;
		if (numberOfChromosomesEvaluated == population.getPopulationSize()) {
			notifyAll();
		}
		lastStamp = stamp;
	}

	@Override
	public String toString() {
		if(population != null)
			return "ClientDescription [lastStamp=" + lastStamp + ", toCalculate="
			+ (population.getPopulationSize()-numberOfChromosomesDelivered) + ", priority=" + priority + "]";
		else 
			return "ClientDescription [lastStamp=" + lastStamp + ", No POPULATION, priority=" + priority + "]";
	}

	public int compareTo(ClientDescription other) {
		if (population != null
				&& numberOfChromosomesDelivered != population
				.getPopulationSize()) {
			if (other.population != null
					&& other.numberOfChromosomesDelivered != other.population
					.getPopulationSize()) {
				if (other.priority == priority)
					return other.lastStamp - lastStamp;
				else
					return other.priority - priority;
			} else
				return 1;
		} else
			return -1;
	}

	public synchronized void waitResults() {
		try {
			while (numberOfChromosomesEvaluated != population
					.getPopulationSize()) {
				wait();
			}
			numberOfChromosomesEvaluated = 0;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//	public static class Sorter implements Comparator<ClientDescription>{
	//
	//		public int compare(ClientDescription client1, ClientDescription client2) {
	//			if (client1.population != null
	//					&& client1.numberOfChromosomesEvaluated != client1.population
	//							.getPopulationSize()) {
	//				if (client2.population != null
	//						&& client2.numberOfChromosomesEvaluated != client2.population
	//								.getPopulationSize()) {
	//					if (client2.priority == client1.priority)
	//						return client2.lastStamp - client1.lastStamp;
	//					else
	//						return client2.priority - client1.priority;
	//				} else
	//					return 1;
	//			} else
	//				return -1;
	//
	//		}
	//
	//		
	//	}

}
