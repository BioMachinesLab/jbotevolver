package simulation.robot;

import simulation.Network;
import commoninterface.AquaticDroneCI;
import commoninterface.network.broadcast.BroadcastHandler;

public class SimulatedBroadcastHandler extends BroadcastHandler {
	
	private SimulatedBroadcastMessageSender sender;

	public SimulatedBroadcastHandler(AquaticDroneCI drone) {
		super(drone);
		sender = new SimulatedBroadcastMessageSender(this, broadcastMessages);
	}

	@Override
	public void sendMessage(String message) {
		AquaticDrone r = (AquaticDrone)drone;
		Network net = r.getSimulator().getNetwork();
		if(net != null)
			net.send(drone.getNetworkAddress(), message);
	}

	public void update(double time) {
		sender.update(time);
	}

}
