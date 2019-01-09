package evaluationfunctions.flocking;


import simulation.robot.Robot;
import simulation.util.Arguments;

public class ReynoldsGlobalWithoutAlignment extends ReynoldsGlobal {
	

	public ReynoldsGlobalWithoutAlignment(Arguments args) {
		super(args);
	}
	
	@Override
	protected void alignment(Robot robot){
		currentAlignment=0;
	}
		

}