package simulation.robot;

import simulation.Simulator;

/**
 * Representation of robot based on a Bee model which has an energy level.
 * 
 * @author Miguel Duarte
 */
public class BeeRobot extends Robot {

	private double energy = 0;
	private double maxEnergy = 0;
	
	public BeeRobot(Simulator simulator, String name, double x, double y, double orientation, double mass, double radius, double wheelDistance, String color) {
		super(simulator, name, x, y, orientation, mass, radius, wheelDistance, color);
	}
	
	public double getEnergy() {
		return energy;
	}
	
	public void setEnergy(double energy) {
		this.energy = energy;
	}
	
	public double getMaxEnergy() {
		return maxEnergy;
	}
	
	public void setMaxEnergy(double maxEnergy){
		this.maxEnergy = maxEnergy;
	}

}

