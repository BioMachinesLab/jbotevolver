package evolution;

import java.io.Serializable;
import java.util.LinkedList;

import multiobjective.MOChromosome;
import multiobjective.MOFitnessResult;
import multiobjective.MOTask;
import novelty.EvaluationResult;
import novelty.ExpandedFitness;
import simulation.util.Arguments;
import taskexecutor.TaskExecutor;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.neuralnetworks.Chromosome;

/**
 *	Behavior Repertoire evolutionary algorithm
 *	A. Cully and J.-B. Mouret, Behavioral Repertoire Learning in Robotics, GECCO, 2013 
 * @author miguelduarte
 */
public class BREvolution extends NSGA2Evolution{
	
    public BREvolution(JBotEvolver jBotEvolver, TaskExecutor taskExecutor, Arguments arg) {
    	super(jBotEvolver, taskExecutor, arg);
    }
    
    /*
		popcontroller ←{c1,c2,...,cScontroller}(randomlygenerated)
		archive ← ∅
		for each generations do
			Execution of each controller in simulation
			for each individual i ∈ popcontroller do
				neigh(i) ← The 15 individuals ∈ (popcontroller ∪ archive) similar to i  j∈neigh(i) ∥Ei−Ej∥
				Computation of the novelty objective: Novelty(i) = |neigh(i)|
				Computation of the local competition objectives: Local(i) = card(j ∈ neigh(i),perf(j) > perf(i))
				if Novelty(i) > ρ then
					Add the individual in the archive
				end if
				if perf(i) > than the nearest individual in the archive then
					Swap individual i with the nearest individual in the archive
				end if
			end for
		Iteration of MOEA on popcontroller
		end for
     */
    @Override
	public void executeEvolution() {
    	
    	startOrResumeEvolution();
		
		double highestFitness = 0;
		
		while(!population.evolutionDone() && executeEvolution) {
			
			//Execution of each controller in simulation
			double d = Double.valueOf(df.format(highestFitness));
			taskExecutor.setDescription(output+" "+population.getNumberOfCurrentGeneration()+"/"+population.getNumberOfGenerations() + " " + d);
			
			int totalEvaluations = 0;
			
			for(Chromosome c : ((NSGA2Population)population).getChromosomes()) {

				if(!executeEvolution)
					break;
				
				JBotEvolver jbot = new JBotEvolver(jBotEvolver.getArgumentsCopy(), jBotEvolver.getRandomSeed());
		
				int samples = population.getNumberOfSamplesPerChromosome();
			
				taskExecutor.addTask(new MOTask(jbot,
						samples,c,population.getGenerationRandomSeed())
				);
				totalEvaluations++;
				print(".");
				
			}
			
			print("\n");
			
			while(totalEvaluations-- > 0 && executeEvolution) {
				MOFitnessResult result = (MOFitnessResult)taskExecutor.getResult();
				((NSGA2Population)population).setEvaluationResultForId(result.getChromosomeId(), result.getEvaluationResult());
				print("!");
			}
			
			for(PostEvaluator p : postEvaluators)
				p.processPopulation(population);
			
			processPopulation();
			
			if(executeEvolution) {
				
				print("\nGeneration "+population.getNumberOfCurrentGeneration()+
						"\tHighest: "+population.getHighestFitness()+
						"\tAverage: "+population.getAverageFitness()+
						"\tLowest: "+population.getLowestFitness()+"\n");
				
				try {
					diskStorage.savePopulation(population);
				} catch(Exception e) {e.printStackTrace();}
				
				highestFitness = population.getHighestFitness();
				population.createNextGeneration();
			}
		}
		
		evolutionFinished = true;
		diskStorage.close();
	}
    
}