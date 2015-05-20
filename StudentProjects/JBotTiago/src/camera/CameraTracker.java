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
	
	private int LagBufferSize;
	
	private Vector2d[][] positionLagBuffer;
	private int positionLagBufferIndex = 0;
	
	private double[][] orientationLagBuffer;
	private int orientationLagBufferIndex = 0;

	private double orientationError;

	private Simulator simulator;
	
	public CameraTracker(Simulator simulator, int lag, double orientationError) {
		this.simulator = simulator;
		this.orientationError = orientationError;
		network = simulator.getNetwork();
		numberOfRobots = simulator.getRobots().size();
		LagBufferSize = lag;
	}


	@Override
	public void update(Simulator simulator) {
		
		if(simulator.getTime() == 1){
			if(LagBufferSize > 0){
				positionLagBuffer = new Vector2d[numberOfRobots][LagBufferSize];
				orientationLagBuffer = new double[numberOfRobots][LagBufferSize];
				
				for (int x = 0; x < positionLagBuffer.length; x++) {
					Robot r = simulator.getRobots().get(x);
					for (int y = 0; y < positionLagBuffer[x].length; y++) {
						positionLagBuffer[x][y] = new Vector2d(r.getPosition().x, r.getPosition().y);
						orientationLagBuffer[x][y] = r.getOrientation();
					}
				}
			}
		}
		
		for (Robot r : simulator.getRobots()) {
			Vector2d currentThymioPosition = new Vector2d(r.getPosition().x, r.getPosition().y);
			double currentThymioOrientation = r.getOrientation();
			
			//ADD LAG
			currentThymioPosition = getLaggedPosition(r.getId(), currentThymioPosition);
			currentThymioOrientation = getLaggedOrientation(r.getId(), currentThymioOrientation);
			
			double orientationWithError = getOrientationWithError(currentThymioOrientation);
			
			VirtualPositionBroadcastMessage thymioMessage = new VirtualPositionBroadcastMessage(VirtualPositionType.ROBOT, ((Thymio)r).getNetworkAddress(), currentThymioPosition.x, currentThymioPosition.y, orientationWithError);
			
			String[] encodeMessages = thymioMessage.encode();
			for (String em : encodeMessages)
				network.send(ADDRESS, em);
			
		}
		
		for (Prey p : simulator.getEnvironment().getPrey()) {
			Vector2d currentPreyPosition = new Vector2d(p.getPosition().x, p.getPosition().y);
			VirtualPositionBroadcastMessage preyMessage = new VirtualPositionBroadcastMessage(VirtualPositionType.PREY,p.getName(), currentPreyPosition.x, currentPreyPosition.y, 0);
			
			String[] encodeMessages = preyMessage.encode();
			for (String em : encodeMessages)
				network.send(ADDRESS, em);
			
		}
		
	}
	
	private Vector2d getLaggedPosition(int robotID, Vector2d currentPosition) {
		if(positionLagBuffer != null) {
			Vector2d lagValue = positionLagBuffer[robotID][positionLagBufferIndex % LagBufferSize];
			
			positionLagBuffer[robotID][positionLagBufferIndex] = currentPosition;
			
			positionLagBufferIndex = (positionLagBufferIndex+1) % LagBufferSize;
			
			return lagValue;
		}
		
		return currentPosition;
	}
	
	private double getLaggedOrientation(int robotID, double currentOrientation) {
		if(orientationLagBuffer != null) {
			double lagValue = orientationLagBuffer[robotID][orientationLagBufferIndex % LagBufferSize];
			
			orientationLagBuffer[robotID][orientationLagBufferIndex] = currentOrientation;
			
			orientationLagBufferIndex = (orientationLagBufferIndex+1) % LagBufferSize;
			
			return lagValue;
		}
		
		return currentOrientation;
	}
	
	public double getOrientationWithError(double heading) {
		double error = orientationError*simulator.getRandom().nextDouble()*2-orientationError;
		return heading+Math.toRadians(error);
	}
	
}
