package simulation.physicalobjects;

import simulation.Simulator;

public class GroundBand extends PhysicalObject{
	
	private double innerRadius;
	private double outerRadius;
	private double startOrientation;
	private double endOrientation;

	public GroundBand(Simulator simulator, double x, double y, double innerRadius, double outerRadius, double startOrientation, double endOrientation) {
		super(simulator, "groundcolor"+x+","+y, x, y, 0, 0, PhysicalObjectType.NA);
		this.innerRadius = innerRadius;
		this.outerRadius = outerRadius;
		this.startOrientation = startOrientation;
		this.endOrientation = endOrientation;
	}
	
	public double getInnerRadius() {
		return innerRadius;
	}
	
	public double getOuterRadius() {
		return outerRadius;
	}
	
	public double getStartOrientation() {
		return startOrientation;
	}
	
	public double getEndOrientation() {
		return endOrientation;
	}
}