package environments;

import net.jafama.FastMath;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.GroundBand;
import simulation.physicalobjects.LightPole;
import simulation.util.Arguments;

public class GroundBandEnvironment extends Environment {
	
	private double openingSize = Math.toRadians(90);
	private double chanceOpen = 0.5;
	private double innerRadius = 0.4;
	private double outerRadius = 0.6;
	private double distanceFromCenter = 1.5;
	private int changeSample = 0;
	private int currentSample = 0;
	
	public GroundBandEnvironment(Simulator simulator, Arguments args) {
		super(simulator, args);
		openingSize = args.getArgumentAsDoubleOrSetDefault("openingsize", openingSize);
		chanceOpen = args.getArgumentAsDoubleOrSetDefault("chanceopen", chanceOpen);
		innerRadius = args.getArgumentAsDoubleOrSetDefault("innerradius", innerRadius);
		outerRadius = args.getArgumentAsDoubleOrSetDefault("outerradius", outerRadius);
		distanceFromCenter = args.getArgumentAsDoubleOrSetDefault("distancefromcenter", distanceFromCenter);
		changeSample = args.getArgumentAsIntOrSetDefault("changesample", changeSample);
		currentSample = args.getArgumentAsInt("fitnesssample");
	}

	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		
		if(changeSample > 0 && currentSample % 2 == 0) {
			openingSize = 0;
		} else if(changeSample == 0 && simulator.getRandom().nextDouble() > chanceOpen) {
			openingSize = 0;
		}
		
		double startAngle = 0;
		double endAngle = 2 * Math.PI - openingSize;
		
		GroundBand gb = new GroundBand(simulator, 0, 0, innerRadius, outerRadius, startAngle, endAngle);
		addObject(gb);
		
		double angle = simulator.getRandom().nextDouble() * 2 * Math.PI;
		
		double x = 1.0 * FastMath.cosQuick(angle);
		double y = 1.0 * FastMath.sinQuick(angle);

		robots.get(0).teleportTo(new Vector2d(x,y));
		
		double orientation = simulator.getRandom().nextDouble() * Math.PI/2 - Math.PI/4 + angle + Math.PI;
		robots.get(0).setOrientation(orientation);
		
		LightPole lp = new LightPole(simulator, "lightpole", 0, 0, 0.1);
		addObject(lp);
	}

	@Override
	public void update(double time) {
		
	}
	
	public boolean isOpen() {
		return openingSize > 0;
	}

}
