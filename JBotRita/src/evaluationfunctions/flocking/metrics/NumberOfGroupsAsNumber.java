package evaluationfunctions.flocking.metrics;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;

import simulation.robot.Robot;
import simulation.util.Arguments;

public class NumberOfGroupsAsNumber extends NumberOfGroups {
	public NumberOfGroupsAsNumber(Arguments args) {
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
		
		currentFitness=groups.size(); // we have to normalize between [0,1]
		fitness += currentFitness;
		
		return fitness ;
	}
}
