package environments;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.Wall;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class RoomWithObstaclesEnvironment extends SingleRoomEnvironment {
	
	@ArgumentsAnnotation(name="obstacledensity", defaultValue="0.5")
	private double obstacleDensity = 0;
	
	@ArgumentsAnnotation(name="maxobstaclewidth", defaultValue="0.5")
	private double maxObstacleWidth = 0.5;
	
	@ArgumentsAnnotation(name="minobstaclewidth", defaultValue="0.1")
	private double minObstacleWidth = 0.1;

	public RoomWithObstaclesEnvironment(Simulator simulator, Arguments args) {
		super(simulator, args);
		this.obstacleDensity = args.getArgumentAsDoubleOrSetDefault("obstacledensity", 0);
		this.maxObstacleWidth = args.getArgumentAsDoubleOrSetDefault("maxobstaclewidth", maxObstacleWidth);
		this.minObstacleWidth = args.getArgumentAsDoubleOrSetDefault("minobstaclewidth", minObstacleWidth);
		
	}
	
	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		
		int obstacles = (int)((getWidth()*getHeight())/(1.0/obstacleDensity));
		
		for(int i = 0 ; i < obstacles ; i++) {
			double x = (random.nextDouble()*width-width/2)*0.75;
			double y = (random.nextDouble()*height-height/2)*0.75;
			double width = (random.nextDouble()*(maxObstacleWidth-minObstacleWidth)+minObstacleWidth);
			Wall w = new Wall(simulator, x, y, width, width);
			addObject(w);
		}
		
		updateCollisions(0);
		
		for(Robot r : simulator.getRobots()) {
			int i = 0;
			while(r.isInvolvedInCollison() && i++ < 100) {
				r.teleportTo(new Vector2d((random.nextDouble()*width-width/2)*0.75,(random.nextDouble()*height-height/2)*0.75));
			}
		}
	}
}
