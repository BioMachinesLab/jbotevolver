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

public class NumberFragmentation extends EvaluationFunction {

	protected Simulator simulator;

	@ArgumentsAnnotation(name = "cohensionDistance", defaultValue = "0.25")
	protected double cohensionDistance;

	protected HashMap<Integer, Set<Integer>> equivalenceClasses;
	
	public NumberFragmentation(Arguments args) {
		super(args);
		cohensionDistance = args.getArgumentIsDefined("cohensionDistance") ? args
				.getArgumentAsDouble("cohensionDistance") : 0.25;
	}

	@Override
	public double getFitness() {
		return fitness / simulator.getTime();
	}
	

	@Override
	public void update(Simulator simulator) {
		init();
		this.simulator=simulator;

		ArrayList<Robot> robots = simulator.getEnvironment().getRobots();
		int robotsSize = robots.size();

		for (int i = 0; i < robotsSize; i++) {
			int robot = robots.get(i).getId();
			
			computeGroupsPairs(robot,  robots ,  i,  robotsSize);
			
			if (equivalenceClasses.containsKey(robot)) {
				if (equivalenceClasses.get(robot).size() == robotsSize)
					break;
			} else { //if robot had not a pair, put in a group by itself
				Set<Integer> newEquivalenceClass = new HashSet<Integer>();
				newEquivalenceClass.add(robot);
				equivalenceClasses.put(robot, newEquivalenceClass);
			}
		}
		getCurrentFitness(robots);
	}
	
	public void computeGroupsPairs( int robot, ArrayList<Robot> robots , int i, int robotsSize){
		for (int j = i + 1; j < robotsSize; j++) {
			if (robots.get(i).getPosition().distanceTo(robots.get(j).getPosition()) <= cohensionDistance) {
				int neighbour = robots.get(j).getId();

				if (equivalenceClasses.containsKey(robot)) {
					updateGroups(robot, neighbour);
				} else if (equivalenceClasses.containsKey(neighbour)) {

					updateGroups(neighbour, robot);
				} else {

					addGroup(robot, neighbour);
				}

				
			}
		}
	}
		
	protected void updateGroups(int robot, int neighbour) {
		if (equivalenceClasses.containsKey(neighbour)) {

			//equivalenceClasses.get(robot).addAll(equivalenceClasses.get(neighbour));
			//equivalenceClasses.get(neighbour).addAll(equivalenceClasses.get(robot));
			if(!equivalenceClasses.get(robot).equals(equivalenceClasses.get(neighbour))){
				
				equivalenceClasses.get(neighbour).addAll(equivalenceClasses.get(robot));
				for( int my_neighbour: equivalenceClasses.get(robot)){
					equivalenceClasses.put(my_neighbour, equivalenceClasses.get(neighbour));
				}
			}
			
		} else {

			equivalenceClasses.get(robot).add(neighbour);
			equivalenceClasses.put(neighbour, equivalenceClasses.get(robot));
		}

	}

	protected void addGroup(int robot, int neighbour) {
		Set<Integer> newEquivalenceClass = new HashSet<Integer>();
		newEquivalenceClass.add(robot);
		newEquivalenceClass.add(neighbour);
		equivalenceClasses.put(robot, newEquivalenceClass);
		equivalenceClasses.put(neighbour, newEquivalenceClass);
	}
			
	
	
		
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
	
	protected double normalize(double value, double maxValue, double minValue, double newMinValue, double newMaxValue) {
		return (value - minValue) * (newMaxValue - newMinValue)/ (double) (maxValue - minValue) + newMinValue;
	}
	
	//---------------------- for being called in Reynolds Component----------//
	
	public boolean isAlreadyACohesiveGroup(int robot, int robotsSize){
		return equivalenceClasses.containsKey(robot) && equivalenceClasses.get(robot).size() == robotsSize;
	}
		
	public void checkRobotIsAlreadyInAGroup(int robot){
		if (!equivalenceClasses.containsKey(robot)) {
			Set<Integer> newEquivalenceClass = new HashSet<Integer>();
			newEquivalenceClass.add(robot);
			equivalenceClasses.put(robot, newEquivalenceClass);
		}
	}
	
	public void init(){
		equivalenceClasses = new HashMap<Integer, Set<Integer>>();
	}

}