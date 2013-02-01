package simulation.robot.actuators;

import java.util.ArrayList;
import simulation.Simulator;
import simulation.environment.TMazeEnvironment;
import simulation.robot.Robot;
import simulation.robot.behaviors.Behavior;
import simulation.robot.behaviors.DestructiveBehavior;
import simulation.robot.behaviors.DummyBehavior;
import simulation.robot.behaviors.LayeredBehavior;
import simulation.robot.behaviors.ManualWheelsBehavior;
import simulation.robot.behaviors.MoveForwardBehavior;
import simulation.robot.behaviors.TurnLeftBehavior;
import simulation.robot.behaviors.TurnRightBehavior;
import simulation.util.Arguments;

public class BehaviorActuator extends Actuator{

	private ArrayList<Behavior> behaviors = new ArrayList<Behavior>();
	private double[] values;
	private int count = 0;
	private Behavior currentlyChosenBehavior;
	private int numberOfOutputs = 0;
	private Robot robot;
	private boolean lock = true;
	private Simulator simulator;
	
	public BehaviorActuator(Simulator simulator, int id, Robot r, Arguments args) {
		super(simulator, id);
		this.robot = r;
		this.simulator = simulator;
		if(args.getArgumentIsDefined("lock"))
			if(args.getArgumentAsInt("lock") == 0)
				lock = false;
		
		createBehaviors(args);
		values = new double[behaviors.size()];
	}
	
	private void createBehaviors(Arguments args) {
		
		for(int i = 0 ; i < args.getNumberOfArguments() ; i++) {
			String argumentName = args.getArgumentAt(i);
			
			if(argumentName.equals("turnleft"))
				addBehavior(new TurnLeftBehavior(simulator, robot, lock));
			if(argumentName.equals("turnright"))
				addBehavior(new TurnRightBehavior(simulator, robot, lock));
			if(argumentName.equals("moveforward"))
				addBehavior(new MoveForwardBehavior(simulator, robot, lock));
			if(argumentName.equals("manualwheels"))
				addBehavior(new ManualWheelsBehavior(simulator, robot, lock));
			if(argumentName.equals("destructive"))
				addBehavior(new DestructiveBehavior(simulator, robot, lock));
			if(argumentName.equals("layered"))
				addBehavior(new LayeredBehavior(simulator, robot, lock,(TMazeEnvironment)simulator.getEnvironment()));
			if(argumentName.equals("dummy"))
				for(int j = 0 ; j < 4; j++)
					addBehavior(new DummyBehavior(simulator, robot, lock,j));
		}
	}

	public void apply(Robot robot) {
		
		if(count == getNumberOfOutputs()){
//			System.out.println("apply");
			count = 0;
			
			Behavior activeBehavior = null;
			
			for(Behavior b : behaviors)
				if(b.isLocked())
					activeBehavior = b;
			
			Behavior newBehavior;
			
			if(activeBehavior != null)
				newBehavior = activeBehavior;
			else
				newBehavior = findMaxBehavior();
			
//			if(currentlyChosenBehavior != newBehavior) System.out.println("--"+newBehavior.getClass().getSimpleName());
			
			currentlyChosenBehavior = newBehavior;
			//System.out.println(currentlyChosenBehavior.getClass().getSimpleName());
			currentlyChosenBehavior.applyBehavior();
		}
	}
	
	public Behavior getCurrentBehavior() {
		return currentlyChosenBehavior;
	}
	
	private Behavior findMaxBehavior() {
		
		int maxIndex = 0;
		
		for(int i = 1 ; i < values.length ; i++)
			if(values[maxIndex] < values[i])
				maxIndex = i;
		
		return behaviors.get(maxIndex);
	}
	
	public double getValue(int index) {
		return values[index];
	}
	
	public void setValue(int index, double value) {
		if(count == 0){
			values = new double[behaviors.size()];
		}
		
		int i,b;
		Behavior behavior = behaviors.get(0);
		
		for(i = 0, b = 0 ; i < getNumberOfOutputs() ; ) {
			behavior = behaviors.get(b);
			
			if(i + behavior.getNumberOfOutputs() <= index) {
				i+= behavior.getNumberOfOutputs();
				b++;
			}else
				break;
		}
		
		index-= i;
		
		if(index == 0)
			values[b] = value;
		
		behavior.setValue(index, value);
		
		count++;
	}
	
	public void addBehavior(Behavior b) {
		behaviors.add(b);
		numberOfOutputs+=b.getNumberOfOutputs();
	}
	
	public int getActiveBehaviorIndex() {
		for(int i = 0 ; i < behaviors.size() ; i++)
			if(behaviors.get(i).equals(currentlyChosenBehavior))
				return i;
		return 999;
	}
	
	public int getNumberOfBehaviors() {
		return behaviors.size();
	}
	
	public int getNumberOfOutputs() {
		return numberOfOutputs;
	}
	
	public ArrayList<Behavior> getBehaviors() {
		return behaviors;
	}
	
	public double[] getValues() {
		return values;
	}
}