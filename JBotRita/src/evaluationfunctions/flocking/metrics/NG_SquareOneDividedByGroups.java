package evaluationfunctions.flocking.metrics;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;

import net.jafama.FastMath;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class NG_SquareOneDividedByGroups extends NumberOfGroups {


	public NG_SquareOneDividedByGroups(Arguments args) {
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
				
		currentFitness=FastMath.sqrtQuick(1.0/((double) groups.size()));
		fitness += currentFitness;
		return fitness ;
	}
	
	

}