package controllers;

import java.awt.Color;
import simulation.Controller;
import simulation.Simulator;
import simulation.robot.Robot;
// import simulation.robot.sensors.BehaviorSensor;
import simulation.robot.sensors.GroundRGBColorSensor;
import simulation.util.Arguments;

public class ProgrammedMazeSolver extends Controller {

//	private BehaviorSensor behaviorSensor;
	private GroundRGBColorSensor rgbSensor;
	private Color lastColor = Color.BLACK;

	private enum Action {
		TURN_LEFT, TURN_RIGHT, FORWARD
	};

	public ProgrammedMazeSolver(Simulator simulator, Robot robot, Arguments arguments) {
		super(simulator, robot);
		rgbSensor = (GroundRGBColorSensor) (robot.getSensorWithId(1));
//		behaviorSensor = (BehaviorSensor) (robot.getSensorWithId(2));
	}

	@Override
	public void begin() {
	}

	@Override
	public void controlStep(double time) {
		chooseBehavior();
	}
	
	private void chooseBehavior() {
		double r = rgbSensor.getSensorReading(0);
		double g = rgbSensor.getSensorReading(1);
		double b = rgbSensor.getSensorReading(2);
		
		if(r > 250 && g < 250 && b < 250 && lastColor != Color.RED)
			move(Action.TURN_RIGHT);
		else if(r < 250 && g < 250 && b > 250 && lastColor != Color.BLUE)
			move(Action.TURN_LEFT);
		else
			move(Action.FORWARD);
		
		updateLastColor(r,g,b);
	}
	
	private void updateLastColor(double r, double g, double b) {

		if(r > 250 && g < 250 && b < 250) 
			lastColor = Color.RED;
		else if(r < 250 && g > 250 && b < 250) 
			lastColor = Color.GREEN;
		else if(r < 250 && g < 250 && b > 250) 
			lastColor = Color.BLUE;
		else
			lastColor = Color.BLACK;
	}

	private void move(Action a) {
		
		switch(a) {
			case FORWARD:
				this.setValues(0,0,1);
				break;
			case TURN_LEFT:
				this.setValues(0,1,0);
				break;
			case TURN_RIGHT:
				this.setValues(1,0,0);
		}
	}
	
	private void setValues(int right, int left, int forward) {
		/*BehaviorActuator bc = robot.getBehaviorChooser();
		bc.setValue(bc.getBehaviors().get(0).getClass(), 0, right);
		bc.setValue(bc.getBehaviors().get(1).getClass(), 0, left);
		bc.setValue(bc.getBehaviors().get(2).getClass(), 0, forward);
		bc.apply();*/
	}

}
