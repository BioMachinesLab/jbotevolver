

package evaluationfunctions.flocking.metrics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class NG_OneDividedByGroups extends NumberOfGroups {


	public NG_OneDividedByGroups(Arguments args) {
		super(args);

	}
	
	@Override
	public void update(Simulator simulator) {
		super.update(simulator);
	}

	
	@Override		
	public double getCurrentFitness(ArrayList<Robot> robots) {
		LinkedList<Set<Integer>> groups = new LinkedList<Set<Integer>>();
		
		for (Set<Integer> group : equivalenceClasses.values()) {
			if (!groups.contains(group)) {
				groups.add(group);
			}
		}
				
		currentFitness=1.0/((double) groups.size()); 
		fitness += currentFitness;
		return fitness ;
	}
	
	

}