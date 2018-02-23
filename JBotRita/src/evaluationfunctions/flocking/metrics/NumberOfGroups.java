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

public class NumberOfGroups extends EvaluationFunction {

	protected Simulator simulator;

	@ArgumentsAnnotation(name = "cohensionDistance", defaultValue = "0.25")
	protected double cohensionDistance;

	HashMap<Integer, Set<Integer>> equivalenceClasses;

	public NumberOfGroups(Arguments args) {
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
		this.simulator = simulator;
		ArrayList<Robot> robots = simulator.getEnvironment().getRobots();

		equivalenceClasses = new HashMap<Integer, Set<Integer>>();

		int robotsSize = robots.size();

		for (int i = 0; i < robotsSize; i++) {
			int robot = robots.get(i).getId();
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
			if (equivalenceClasses.containsKey(robot)) {
				if (equivalenceClasses.get(robot).size() == robotsSize)
					break;
			} else {
				Set<Integer> newEquivalenceClass = new HashSet<Integer>();
				newEquivalenceClass.add(robot);
				equivalenceClasses.put(robot, newEquivalenceClass);
			}
		}

		LinkedList<Set<Integer>> groups = new LinkedList<Set<Integer>>();

		for (Set<Integer> group : equivalenceClasses.values()) {
			if (!groups.contains(group)) {
				groups.add(group);
			}
		}

		double reward = 1 - (groups.size() / (double) robotsSize);
		// Since this never gives 1,given that nº of groups has a min value of 1, 
		// we have to normalize between [0,1]
		fitness += normalize(reward, (1 - 1 / (double) robotsSize), 0, 0, 1);
	}

	protected double normalize(double value, double maxValue, double minValue, double newMinValue, double newMaxValue) {
		return (value - minValue) * (newMaxValue - newMinValue)/ (double) (maxValue - minValue) + newMinValue;
	}

	protected void updateGroups(int robot, int neighbour) {
		if (equivalenceClasses.containsKey(neighbour)) {
			equivalenceClasses.get(robot).addAll(equivalenceClasses.get(neighbour));
			equivalenceClasses.get(neighbour).addAll(equivalenceClasses.get(robot));
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



}