package outputs;

import evolutionaryrobotics.neuralnetworks.outputs.NNOutput;
import actuator.RobotRGColorActuator;
import simulation.robot.actuators.Actuator;
import simulation.robot.actuators.RobotRGBColorActuator;
import simulation.util.Arguments;

public class RobotRGColorNNOutput extends NNOutput {
	boolean controlRed   = true;
	boolean controlGreen = true;
	boolean controlBlue  = true;
	
	private RobotRGColorActuator robotRGColorActuator;
	
	public RobotRGColorNNOutput(Actuator robotRGColorActuator, Arguments args) {
		super(robotRGColorActuator,args);
		this.robotRGColorActuator = (RobotRGColorActuator)robotRGColorActuator;
		controlRed = this.robotRGColorActuator.controlRed();
		controlGreen = this.robotRGColorActuator.controlGreen();
		
	}
	

	@Override
	public int getNumberOfOutputValues() {
		return 1;
	}

	@Override
	public void setValue(int index, double value) {
		if(value >0.5){
			robotRGColorActuator.setGreenColor();
		}else{
			robotRGColorActuator.setBlackColor();
		}
	}


	@Override
	public void apply() {
		// TODO Auto-generated method stub
		
	}
}