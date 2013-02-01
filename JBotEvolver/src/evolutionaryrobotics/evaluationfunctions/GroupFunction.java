package evolutionaryrobotics.evaluationfunctions;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.robot.Robot;
/**
 * Fitness function to cluster robots
 * @author Andre Bastos
 *
 */
public class GroupFunction  extends EvaluationFunction {

	private double 	    fitness;
	
	public GroupFunction(Simulator simulator){
		super(simulator);
	}
	
	/**
	 * Get the fitness value
	 * @return fitness
	 */
	@Override
	public double getFitness() {
		return fitness;
	}

	/**
	 * Algorithm executed in each step that calculates a fitness value
	 * 
	 */
	@Override
	public void update(double time) {
		int numberOfRobotsClustered = 0;
		double distance = Integer.MAX_VALUE;
		double auxDist = 0;
		Vector2d coord = new Vector2d();
		for(Robot r: simulator.getEnvironment().getRobots()){
			coord.set(r.getPosition());
			for (Robot t: simulator.getEnvironment().getRobots()){
				if(!r.equals(t)){
					distance = coord.distanceTo(t.getPosition());
					// System.out.println("Distance "+distance);
					if (distance <0.5){
						numberOfRobotsClustered++;
						//System.out.println("Agrupados "+numberOfRobotsClustered);					
					}
					auxDist += distance;
				}
			}
			auxDist= (auxDist / simulator.getEnvironment().getRobots().size())/simulator.getEnvironment().getRobots().size();
		}
		fitness += (double) (numberOfRobotsClustered/simulator.getEnvironment().getRobots().size())*0.1-(auxDist)*0.01;
	}
}
