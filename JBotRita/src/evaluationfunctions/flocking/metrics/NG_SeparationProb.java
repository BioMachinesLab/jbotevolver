package evaluationfunctions.flocking.metrics;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;

import simulation.robot.Robot;
import simulation.util.Arguments;

public class NG_SeparationProb extends NumberOfGroups {
	
	public NG_SeparationProb(Arguments args) {
		super(args);
		
	}
	
	@Override
	public double getCurrentFitness(ArrayList<Robot> robots) {
		LinkedList<Set<Integer>> groups = new LinkedList<Set<Integer>>();
		
		for (Set<Integer> group : equivalenceClasses.values()) {
			if (!groups.contains(group)) {
				groups.add(group);
			}
		}
		if(groups.size()>1){
			currentFitness=1; // if groups size of 2 or more, prob of split =1
		}else{
			currentFitness=0; // if groups size of 1, prob of split =0
		}
			
		fitness += currentFitness;
		
		return fitness ;
	}
}