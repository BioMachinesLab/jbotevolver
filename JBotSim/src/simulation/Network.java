package simulation;

import simulation.util.Arguments;
import simulation.util.Factory;

public abstract class Network {
	
	protected Simulator sim;
	
	public Network(Arguments args, Simulator sim) {
		this.sim = sim;
	}
	
	public abstract void send(String senderAddress, String msg);
	public abstract void shutdown();
	
	public static Network getNetwork(Simulator sim, Arguments args) {
		return (Network)Factory.getInstance(args.getArgumentAsString("classname"), args, sim);
	}
}
