package environments;

import java.util.LinkedList;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.LightPole;
import simulation.util.Arguments;

public class TwoRoomsBasketEnvironment extends TwoRoomsMultiPreyEnvironment {
	
	protected double minDistance = 0.3;
	protected double captureDistance = 0.1;
	protected LinkedList<LightPole> baskets = new LinkedList<LightPole>();

	public TwoRoomsBasketEnvironment(Simulator simulator, Arguments arguments) {
		super(simulator, arguments);
	}
	
	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		
		LightPole p = new LightPole(simulator, "basket_left", -arenaWidth, 0, 0.1);
		baskets.add(p);
		addObject(p);
		p = new LightPole(simulator, "basket_right", arenaWidth, 0, 0.1);
		baskets.add(p);
		addObject(p);
		
		placePrey();
	}
	
	@Override
	public void placePrey() {
		
		if(preySameRoom)
			preyRoomRight = true;
		
		boolean close = true;
		int tries = 0;
		
		while(close) {
			close = false;
		
			double randX = random.nextDouble()*arenaWidth*0.5+corridorWidth;
			double randY = random.nextDouble()*arenaHeight*0.5-arenaHeight/4;
			
			if(!preyRoomRight)
				randX = -randX;
			
			prey.teleportTo(new Vector2d(randX,randY));
			
			for(LightPole p : baskets) {
				if(p.getPosition().distanceTo(prey.getPosition()) < minDistance && tries < 1000) {
					close = true;
					tries++;
				}
			}
		}
		
		if(!includePrey)
			prey.teleportTo(new Vector2d(-10,-10));
		
		preyRoomRight = !preyRoomRight;
	}
	
	@Override
	public void update(double time) {
		
		for(LightPole p : baskets) {
			if(p.getPosition().distanceTo(prey.getPosition()) < captureDistance) {
				preysCaught++;
				placePrey();
			}
		}
		
		if(doorsOpen) {
			doorTime++;
			if(doorTime > doorOpenTime)
				closeDoor();
			else if(autoCloseDoor && !firstDoorClosed) {
				if(allRobotsInsideCorridor())
					closeDoor();
			}
		}
	}

	public LinkedList<LightPole> getBaskets() {
		return baskets;
	}
}