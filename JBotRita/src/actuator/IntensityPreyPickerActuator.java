package actuator;

import mathutils.Vector2d;
import physicalobjects.IntensityPrey;
import robots.JumpingRobot;
import simulation.Simulator;
import simulation.physicalobjects.ClosePhysicalObjects;
import simulation.physicalobjects.ClosePhysicalObjects.CloseObjectIterator;
import simulation.robot.Robot;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

/**
 * Remove/take/eat a bit from a prey (according to a specified intensity)
 * @author Rita Ramos
 */

public class IntensityPreyPickerActuator extends Actuator {
	
	protected boolean isToPick=false;
	
	@ArgumentsAnnotation(name = "taking", defaultValue = "1")
	protected double taking;
	
	@ArgumentsAnnotation(name = "pickdistance", defaultValue = "0.1")
	protected double pickDistance;

	protected boolean isPicking = false;
	
	protected Vector2d temp = new Vector2d();
	
	protected IntensityPrey bestPrey = null;

	
	/**
	* Define the maximum distance to eat the prey
	* Define the amount taken when eating the prey
	*/
	
	public IntensityPreyPickerActuator(Simulator simulator, int id, Arguments args) {
		super(simulator, id, args);
		this.pickDistance = args.getArgumentAsDoubleOrSetDefault(
				"pickdistance", 0.1);
		this.taking = args.getArgumentAsDoubleOrSetDefault("taking", 1);
		
	}
	
	/**
	* IntensityPreyPickerNNOutput will inform if is to pick the prey or not
	*/

	public void pick(boolean pick){
		isToPick=pick;
	}
	
	
	@Override
	public void apply(Robot robot,double timeDelta) {
			if(isToPick){
				checkIfPickingIsAllowed(robot);
			}
	}
	
	/**
	 * Only eat prey if the robot is close to a prey and the robot is not jumping 
	 * (when the robot is jumping, it is ignored the wall collisions; 
	 * thus only pick it when it is not to ignore them)
	 */
	protected void checkIfPickingIsAllowed(Robot robot){
		findBestPrey(robot);
		if (bestPrey != null) {

			if (robot instanceof JumpingRobot) {
				if (!((JumpingRobot) robot).ignoreWallCollisions()) {
					pickUpPrey(robot, bestPrey);
				}
			} else {
				pickUpPrey(robot, bestPrey);
			}
		}
	}
	
	
	/**
	 * find closest prey
	 */
	
	protected void findBestPrey(Robot robot){
		double bestLength = pickDistance;
		bestPrey = null;
		ClosePhysicalObjects closePreys = robot.shape.getClosePrey();
		CloseObjectIterator iterator = closePreys.iterator();
		while (iterator.hasNext()) {
			IntensityPrey closePrey = (IntensityPrey) (iterator.next()
					.getObject());
			if (closePrey.isEnabled()) {
				temp.set(closePrey.getPosition());
				temp.sub(robot.getPosition());
				double length = temp.length() - robot.getRadius();
				if (length < bestLength) {
					bestPrey = closePrey;
					bestLength = length;
				}
			}
		}
	}
	
	/**
	 * If the robot wants to eat prey, remove the corresponding amount eaten from the prey
	 */
	
	public void pickUpPrey(Robot robot, IntensityPrey prey) {
		isPicking=true;
		prey.setIntensity(prey.getIntensity() - taking);
	}

	
	/**
	 * to inform the discrete sensor of IntensityPreyCarried: if the prey was eaten or not
	 */
	public boolean isCarryingPrey() {
		boolean isCarryingPrey=isPicking;
		isPicking=false;
		return isCarryingPrey;
	}
}
