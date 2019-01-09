package evaluationfunctions.flocking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import evaluationfunctions.flocking.metrics.NumberOfGroups;
import mathutils.Vector2d;
import sensors.RobotSensorWithSources;
import simulation.Simulator;
import simulation.physicalobjects.PhysicalObject;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class ReynoldsGlobal extends ReynoldsLocally {
	
	protected HashMap<Integer, Set<Integer>> equivalenceClasses;

	public ReynoldsGlobal(Arguments args) {
		super(args);
	}
	
	@Override
	protected void cohesion(int i, Robot robot){
		if(!isAlreadyACohesiveGroup(robot.getId(), robots.size())){
			computeGroupsPairs(robot.getId(),  robots ,  i,  robots.size());
		}
	}
	
	@Override
	protected void computeFitnessForCohesion(){		
		
		LinkedList<Set<Integer>> groups = new LinkedList<Set<Integer>>();
		
		for (Set<Integer> group : equivalenceClasses.values()) {
			if (!groups.contains(group)) {
				groups.add(group);
			}
		}
				
		currentFitness=1.0/((double) groups.size()); 
		fitnessForCohesion += currentFitness;
		
	}

	
	@Override
	protected void init(){
		super.init();
		equivalenceClasses = new HashMap<Integer, Set<Integer>>();

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
		if (!equivalenceClasses.containsKey(robot)) { //if robot had not a pair, put in a group by itself
			Set<Integer> newEquivalenceClass = new HashSet<Integer>();
			newEquivalenceClass.add(robot);
			equivalenceClasses.put(robot, newEquivalenceClass);
		}
	}
		
	protected void updateGroups(int robot, int neighbour) {
		if (equivalenceClasses.containsKey(neighbour)) {

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
			

	protected double normalize(double value, double maxValue, double minValue, double newMinValue, double newMaxValue) {
		return (value - minValue) * (newMaxValue - newMinValue)/ (double) (maxValue - minValue) + newMinValue;
	}
	
	
	public boolean isAlreadyACohesiveGroup(int robot, int robotsSize){
		return equivalenceClasses.containsKey(robot) && equivalenceClasses.get(robot).size() == robotsSize;
	}
		

}