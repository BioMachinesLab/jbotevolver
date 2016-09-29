package arbitrators;

import java.util.HashMap;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import controllers.Controller;

public abstract class PreprogrammedArbitrator extends Controller {
	
	protected HashMap<String,Controller> subControllers = new HashMap<String,Controller>();

	public PreprogrammedArbitrator(Simulator simulator, Robot robot, Arguments args) {
		super(simulator, robot, args);
		if(!args.getArgumentAsString("subcontrollers").isEmpty()) {
			Arguments subControllerArgs = new Arguments(args.getArgumentAsString("subcontrollers"));
			
			for(int i = 0 ; i < subControllerArgs.getNumberOfArguments() ; i++) {
				String subControllerName = subControllerArgs.getArgumentAt(i);
				Arguments currentSubControllerArgs = new Arguments(subControllerArgs.getArgumentAsString(subControllerArgs.getArgumentAt(i)));
				Controller c = Controller.getController(simulator, robot, currentSubControllerArgs);
				subControllers.put(subControllerName,c);
			}
		}
	}
}