package simulation.physicalobjects;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.collisionhandling.knotsandbolts.CircularShape;

public class ArtificialPheromoneObject extends PhysicalObject {

	private double evaporationRatio;
	private double pheromoneIntensity;
	public ArtificialPheromoneObject(Simulator simulator, String name, double x, double y,
			double orientation, double mass, double radius,
			PhysicalObjectType type, double evaporationRatio, double pheromoneIntensity) {
		super(simulator, name, x, y, 0, 0, PhysicalObjectType.PHEROMONE);
		this.shape = new CircularShape(simulator, name + "CollisionObject", this, 0, 0, 2 * radius, radius);
		this.evaporationRatio=evaporationRatio;
		this.pheromoneIntensity = pheromoneIntensity;
	}
	
	public Vector2d getPosition() {
		return super.getPosition();
	}
	
	public double getEvaporationRatio(){
		return evaporationRatio;
	}
	
	public double getPheromoneIntensity(){
		return pheromoneIntensity;
	}
	
	public void evapore(){
		pheromoneIntensity*=evaporationRatio;
	}
	
}
