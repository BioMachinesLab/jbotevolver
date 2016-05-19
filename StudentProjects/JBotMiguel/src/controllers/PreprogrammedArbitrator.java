package controllers;

import java.util.ArrayList;

import behaviors.Behavior;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class PreprogrammedArbitrator extends Controller {
	
	private boolean[] parallelController;
	protected ArrayList<Controller> subControllers = new ArrayList<Controller>();
	protected int currentSubController = 0;
	boolean resetChosen = true;

	public PreprogrammedArbitrator(Simulator simulator, Robot robot, Arguments args) {
		super(simulator, robot, args);
		setupControllers(simulator, args);
	}
	
	@Override
	public void controlStep(double time) {
		chooseSubController(0,time);
	}
	
	protected void chooseSubController(int output, double time) {
		if(!subControllers.isEmpty()) {
			
			boolean skip = false;
			
			if(subControllers.get(currentSubController) instanceof Behavior) {
				Behavior b = (Behavior)subControllers.get(currentSubController);
				skip = b.isLocked();
			}
			
			if(output != currentSubController && !skip) {
				currentSubController = output;
				if(resetChosen) {
					subControllers.get(currentSubController).reset();
				}
			}
			subControllers.get(currentSubController).controlStep(time);
		}
	}
	
	@Override
	public void reset() {
		for(Controller c : subControllers)
			c.reset();
	}
	
	private void setupControllers(Simulator simulator, Arguments args) {
		
		if(!args.getArgumentAsString("subcontrollers").isEmpty()) {
			Arguments subControllerArgs = new Arguments(args.getArgumentAsString("subcontrollers"));
			
			parallelController = new boolean[subControllerArgs.getNumberOfArguments()];
			
			for(int i = 0 ; i < subControllerArgs.getNumberOfArguments() ; i++) {
				
				boolean parallel = subControllerArgs.getArgumentAt(i).startsWith("_");
				parallelController[i] = parallel;
				
				Arguments currentSubControllerArgs = new Arguments(subControllerArgs.getArgumentAsString(subControllerArgs.getArgumentAt(i)));
				
				Controller c = Controller.getController(simulator, robot, currentSubControllerArgs);
				
				subControllers.add(c);
			}
		}
		
		resetChosen = args.getArgumentAsIntOrSetDefault("resetchosen", 1) == 1;
	}
	
	public ArrayList<Controller> getSubControllers() {
		return subControllers;
	}
	
	public int getCurrentSubController() {
		return currentSubController;
	}
}