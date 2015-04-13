package camera;

import java.io.Serializable;

import robot.Thymio;
import simulation.Network;
import simulation.Simulator;
import simulation.Updatable;
import simulation.physicalobjects.Prey;
import simulation.robot.Robot;
import commoninterface.mathutils.Vector2d;
import commoninterface.network.broadcast.VirtualPositionBroadcastMessage;
import commoninterface.network.broadcast.VirtualPositionBroadcastMessage.VirtualPositionType;

public class CameraTracker implements Updatable, Serializable {

	private final static String ADDRESS = "1.2.3.4";
	
	private Network network;
	private int numberOfRobots;
	private int numberOfPreys;
	
	private Vector2d[][] lagBuffer;
	private int lagBufferSize = 3;
	private int lagBufferIndex = 0;
	
	public CameraTracker(Simulator simulator) {
		network = simulator.getNetwork();
		numberOfRobots = simulator.getRobots().size();
		numberOfPreys = simulator.getEnvironment().getPrey().size();
		
		if(lagBufferSize > 0)
			lagBuffer = new Vector2d[numberOfRobots][lagBufferSize];
	}


	@Override
	public void update(Simulator simulator) {
		for (Robot r : simulator.getRobots()) {
			Vector2d currentThymioPosition = new Vector2d(r.getPosition().x, r.getPosition().y);
			double currentThymioOrientation = r.getOrientation();
			
//			ADD LAG
//			currentThymioPosition = getLaggedPosition(currentPosition);
//			currentThymioOrientation = getLaggedPosition(currentPosition);
			
			VirtualPositionBroadcastMessage thymioMessage = new VirtualPositionBroadcastMessage(VirtualPositionType.ROBOT, ((Thymio)r).getNetworkAddress(), currentThymioPosition.x, currentThymioPosition.y, currentThymioOrientation);
			network.send(ADDRESS, thymioMessage.encode());
		}
		
		for (Prey p : simulator.getEnvironment().getPrey()) {
			Vector2d currentPreyPosition = new Vector2d(p.getPosition().x, p.getPosition().y);
			
//			ADD LAG
//			currentPosition = getLaggedPosition(currentPosition);
			
			VirtualPositionBroadcastMessage preyMessage = new VirtualPositionBroadcastMessage(VirtualPositionType.PREY,p.getName(), currentPreyPosition.x, currentPreyPosition.y, 0);
			network.send(ADDRESS, preyMessage.encode());
		}
		
	}
	
	private Vector2d getLaggedPosition(Vector2d currentPosition) {
		if(lagBuffer != null) {
			
			Vector2d lagValue = lagBuffer[numberOfRobots][lagBufferIndex % lagBufferSize];
			
			lagBuffer[numberOfRobots][lagBufferIndex] = currentPosition;
			
			lagBufferIndex = (lagBufferIndex+1) % lagBufferSize;
			
			return lagValue;
		}
		
		return currentPosition;
	}
	
}
