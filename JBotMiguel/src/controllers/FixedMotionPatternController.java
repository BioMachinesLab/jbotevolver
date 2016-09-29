package controllers;

import fourwheeledrobot.MultipleWheelAxesActuator;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;

public class FixedMotionPatternController extends Controller implements FixedLenghtGenomeEvolvableController{

	protected final double maxAlleleValue = 10;
	
	protected double[] weights;
	protected double[] outputs;
	protected int genomeLength;
	protected MultipleWheelAxesActuator mwaa;

	public FixedMotionPatternController(Simulator simulator,Robot robot,Arguments args) {
		super(simulator, robot, args);
		genomeLength = calculateGenomeLength(robot);
	}
	
	@Override
	public void controlStep(double time) {
		
		int numberOfWheels = mwaa.getNumberOfSpeeds();
		
		for(int i = 0 ; i < numberOfWheels ; i++){
			mwaa.setWheelSpeed(i, outputs[i]);
		}
		
		for(int i = 0 ; i < mwaa.getNumberOfRotations() ; i++){
			mwaa.setRotation(i, outputs[i + numberOfWheels]);
		}
	}
	
	protected int calculateGenomeLength(Robot r) {
		int num = 0;
		for(Actuator a : r.getActuators()) {
			if(a instanceof MultipleWheelAxesActuator) {
				mwaa = (MultipleWheelAxesActuator)a;
				num+= mwaa.getNumberOfRotations() + mwaa.getNumberOfSpeeds();
				break;
			}
		}
		return num;
	}
	
	@Override
	public void setNNWeights(double[] weights) {
		this.weights = weights;
		this.outputs = new double[weights.length];
		
		//output of the controller has to be between 0 and 1
		for(int i = 0 ; i < weights.length ; i++) {
			this.outputs[i] = (this.weights[i]+maxAlleleValue)/(maxAlleleValue*2);
		}
	}

	@Override
	public int getGenomeLength() {
		return genomeLength;
	}

	@Override
	public double[] getNNWeights() {
		return weights;
	}

	@Override
	public int getNumberOfInputs() {
		return 0;
	}

	@Override
	public int getNumberOfOutputs() {
		return getGenomeLength();
	}

}
