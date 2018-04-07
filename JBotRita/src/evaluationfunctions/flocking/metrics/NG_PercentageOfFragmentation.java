package evaluationfunctions.flocking.metrics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import simulation.robot.Robot;
import simulation.util.Arguments;

public class NG_PercentageOfFragmentation extends NumberOfGroups {

	public NG_PercentageOfFragmentation(Arguments args) {
		super(args);
		
	}
	
	@Override	
	public double getCurrentFitness(ArrayList<Robot> robots) {
		LinkedList<Set<Integer>> groups = new LinkedList<Set<Integer>>();
		int maxGroup=0;
		for (Set<Integer> group : equivalenceClasses.values()) {
			if (!groups.contains(group)) {
				groups.add(group);
				if(group.size()>maxGroup)
					maxGroup=group.size();
			}
		}
		
		
		double robotsGone=robots.size()- maxGroup;
		currentFitness=1.0-( robotsGone/(double)robots.size()); 
		
		fitness += currentFitness;
		return fitness ;
	}
	
	

}