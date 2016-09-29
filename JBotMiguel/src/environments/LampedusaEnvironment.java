package environments;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import updatables.MaritimeStatistics;

public class LampedusaEnvironment extends MaritimeMissionEnvironment {
	
	private int deployIndex = 0;
	private int deployTime = 1;
	private boolean oneByOne = true;

	public LampedusaEnvironment(Simulator simulator, Arguments args) {
		super(simulator, args);
		double random = Math.max(1,simulator.getRandom().nextDouble()*20);
		deployTime = args.getArgumentAsIntOrSetDefault("deploytime",mixed ? (int)(random) : deployTime);
		if(args.getArgumentIsDefined("maritimestatistics")) {
			simulator.addCallback(new MaritimeStatistics());
		}
	}
	
	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		oneByOne = mixed ? simulator.getRandom().nextBoolean() : true;
		if(oneByOne) {
			for(Robot r : robots) {
				r.setEnabled(false);
				r.setPosition(new Vector2d(2000,2000));
			}
		}
	}
	
	private void deploy(int i) {
		Robot r = robots.get(i);
		if(!r.getDescription().equals("prey")) {
			r.setEnabled(true);
			r.teleportTo(new Vector2d(bases.get(r.getId()%this.getNumberOfBases()).getPosition()));
		}
	}
	
	@Override
	public void update(double time) {
		super.update(time);
		if(oneByOne && time % deployTime == 0) {
			if(deployIndex < robots.size()) {
				deploy(deployIndex);
			}
			deployIndex++;
		}
	}
}