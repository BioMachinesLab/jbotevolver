package actuator;


import robots.JumpingSumo;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class JumpSumoActuator extends Actuator{

	private boolean isToJump=false;

	public JumpSumoActuator(Simulator simulator, int id, Arguments args) {
		super(simulator, id, args);
		
	}
			
	@Override
	public void apply(Robot robot) {
		// TODO Auto-generated method stub
		if(isToJump){
			((JumpingSumo) robot).jump();
			isToJump=false;
		}
		
	}

	public void jump( ) {
		isToJump=true;
	}
}