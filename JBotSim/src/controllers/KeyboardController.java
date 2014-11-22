package controllers;

import java.awt.event.KeyEvent;
import simulation.Simulator;
import simulation.robot.DifferentialDriveRobot;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class KeyboardController extends Controller {

	public static final double SPEEDCHANGE = 0.01;

	double leftSpeed = 0;
	double rightSpeed = 0;

	public KeyboardController(Simulator simulator, Robot robot, Arguments args) {
		super(simulator, robot, args);
	}

	public void controlStep(double time) {
		((DifferentialDriveRobot) robot).setWheelSpeed(leftSpeed, rightSpeed);
	}
		
	public void keyTyped(KeyEvent e) {
		if (e.getKeyChar() == 'a' || e.getKeyChar() == 'A') {
			leftSpeed -= SPEEDCHANGE;
			rightSpeed += SPEEDCHANGE;
		}

		if (e.getKeyChar() == 'd' || e.getKeyChar() == 'D') {
			leftSpeed += SPEEDCHANGE;
			rightSpeed -= SPEEDCHANGE;
		}

		if (e.getKeyChar() == 'w' || e.getKeyChar() == 'W') {
			leftSpeed += SPEEDCHANGE;
			rightSpeed += SPEEDCHANGE;
		}

		if (e.getKeyChar() == 's' || e.getKeyChar() == 'S') {
			leftSpeed -= SPEEDCHANGE;
			rightSpeed -= SPEEDCHANGE;
		}

		if (e.getKeyChar() == ' ') {
			leftSpeed = 0;
			rightSpeed = 0;
		}
		boundSpeeds();
	}

	protected void boundSpeeds() {
		if (leftSpeed < -Robot.MAXIMUMSPEED) {
			leftSpeed = -Robot.MAXIMUMSPEED;
		}

		if (leftSpeed > Robot.MAXIMUMSPEED) {
			leftSpeed = Robot.MAXIMUMSPEED;
		}

		if (rightSpeed < -Robot.MAXIMUMSPEED) {
			rightSpeed = -Robot.MAXIMUMSPEED;
		}

		if (rightSpeed > Robot.MAXIMUMSPEED) {
			rightSpeed = Robot.MAXIMUMSPEED;
		}
	}
}