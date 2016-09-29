package environments;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.Prey;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class TwoRoomsMultiPreyEnvironment extends TwoRoomsMultiEnvironment {
	
	protected Prey prey;
	protected boolean preyRoomRight;
	protected double preyDistance = 0.15;
	protected int preysCaught = 0;
	protected boolean preySameRoom = false;
	protected boolean includePrey = true;
	protected boolean heavyPrey = true;
	
	public TwoRoomsMultiPreyEnvironment(Simulator simulator, Arguments arguments) {
		super(simulator,arguments);
		preyDistance = arguments.getArgumentAsDoubleOrSetDefault("preydistance", preyDistance);
		preySameRoom = arguments.getArgumentAsIntOrSetDefault("preysameroom", 0) == 1;
		includePrey = arguments.getArgumentAsIntOrSetDefault("includeprey", 1) == 1;
		heavyPrey = arguments.getArgumentAsIntOrSetDefault("heavyprey", 0) == 1;
	}
	
	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		
		prey = new Prey(simulator,"prey",new Vector2d(0,0),0,heavyPrey ? robots.size() : 1,0.05);
		addPrey(prey);
		
		preyRoomRight = random.nextDouble() > 0.5;
		placePrey();
	}
	
	public void placePrey() {
		
		if(preySameRoom)
			preyRoomRight = true;
		
		double randX = random.nextDouble()*arenaWidth*0.5+corridorWidth;
		double randY = random.nextDouble()*arenaHeight*0.5-arenaHeight/4;
		
		if(!preyRoomRight)
			randX = -randX;
		
		prey.teleportTo(new Vector2d(randX,randY));
		
		if(!includePrey)
			prey.teleportTo(new Vector2d(-10,-10));
		
		preyRoomRight = !preyRoomRight;
	}
	
	@Override
	public void update(double time) {
		super.update(time);
		
		int robotsNearPrey = 0;
		
		for(Robot r : simulator.getRobots()) {
			if(r.getPosition().distanceTo(prey.getPosition()) < preyDistance) {
				robotsNearPrey++;
			}else
				break;
		}
		
		if(robotsNearPrey == simulator.getRobots().size()) {
			placePrey();
			preysCaught++;
		}
	}
	
	public int getPreysCaught() {
		return preysCaught;
	}
}