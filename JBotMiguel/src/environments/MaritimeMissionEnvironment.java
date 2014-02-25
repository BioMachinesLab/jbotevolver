package environments;

import java.awt.Color;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Scanner;

import mathutils.Vector2d;
import sensors.ParameterSensor;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.LightPole;
import simulation.physicalobjects.Line;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class MaritimeMissionEnvironment extends Environment {
	
	private String missionFile;
	public LightPole base;
	public double baseRadius = 5;

	public MaritimeMissionEnvironment(Simulator simulator, Arguments args) {
		super(simulator, args);
		missionFile = "missions/"+args.getArgumentAsStringOrSetDefault("mission","mission_evolve.txt");
	}	
	
	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		
		LinkedList<LightPole> currentWPs = new LinkedList<LightPole>();
		
		try {
			Scanner s = new Scanner(simulator.getFileProvider().getFile(missionFile));
			
			double offsetX = 0;
			double offsetY = 0;
			int count = 0;
			String currentName = "";
			
			base = createWP(simulator, "base", 0, 0, baseRadius);
			
			while(s.hasNextLine()) {
				String line = s.nextLine();
				
				String[] split = line.split(" ");
				
				if(line.startsWith("B")) {
					base = createWP(simulator,"base", Double.parseDouble(split[1]), Double.parseDouble(split[2]),baseRadius);
				} else if (line.startsWith("A")) {
					
					if(!currentWPs.isEmpty())
						createArea(simulator,currentWPs,currentName);
					
					currentWPs.clear();
					currentName = split[1];
					offsetX = split.length > 3 ? Double.parseDouble(split[2]) : 0;
					offsetY = split.length > 3 ? Double.parseDouble(split[3]) : 0;
				} else if (line.startsWith("L")) {
					LightPole wp = createWP(simulator,currentName+count, offsetX+Double.parseDouble(split[1]), offsetY+Double.parseDouble(split[2]));
					count++;
					addObject(wp);
					currentWPs.add(wp);
				}
			}
			
			base.setColor(Color.GREEN);
			addObject(base);
			
			if(!currentWPs.isEmpty())
				createArea(simulator,currentWPs,currentName);
			
			for(Robot r : robots) {
				r.setPosition(new Vector2d(base.getPosition().getX()+simulator.getRandom().nextDouble()*baseRadius,base.getPosition().getY()+simulator.getRandom().nextDouble()*baseRadius));
				r.setOrientation(simulator.getRandom().nextDouble()*Math.PI*2);
				
				ParameterSensor sensor = (ParameterSensor)r.getSensorByType(ParameterSensor.class);
				if(sensor != null)
					sensor.setCurrentValue(1);
			}
		
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		createPatrolPositions(simulator,currentWPs);
	}
	
	private void createPatrolPositions(Simulator simulator, LinkedList<LightPole> areaWP) {
		
		double minX = Double.MAX_VALUE;
		double maxX = -Double.MAX_VALUE;
		double minY = Double.MAX_VALUE;
		double maxY = -Double.MAX_VALUE;
		
		for(LightPole l : areaWP) {
			minX = minX < l.getPosition().getX() ? minX : l.getPosition().getX();
			maxX = maxX > l.getPosition().getX() ? maxX : l.getPosition().getX();
			minY = minY < l.getPosition().getY() ? minY : l.getPosition().getY();
			maxY = maxY > l.getPosition().getY() ? maxY : l.getPosition().getY();
		}
		
		double sizeX = maxX - minX;
		double sizeY = maxY - minY;
		
		double sqrt = Math.sqrt(robots.size());
		
		double areaPerX = sizeX/sqrt;
		double areaPerY = sizeY/sqrt;
		
		if(sqrt != Math.floor(sqrt)) {
			areaPerX = sizeX/Math.ceil(sqrt);
			areaPerY = sizeY/Math.floor(sqrt);
			
			if(sizeX < sizeY) {
				double temp = areaPerX;
				areaPerX = areaPerY;
				areaPerY = temp;
			}
		}
		
		int id = robots.get(0).getId();
		
		for(double y = minY+areaPerY/2 ; y < maxY ; y+=areaPerY) {
			double posY = y;
			for(double x = minX+areaPerX/2 ; x < maxX ; x+=areaPerX) {
				double posX = x;
				addObject(createWP(simulator, "r"+id, posX, posY));
				id++;
			}
		}
	}
	
	private LightPole createWP(Simulator simulator, String name, double x, double y) {
		return createWP(simulator,name,x,y,0.1);
	}
	
	private LightPole createWP(Simulator simulator, String name, double x, double y, double radius) {
		return new LightPole(simulator,name,x,y,radius);
	}
	
	public void createArea(Simulator simulator, LinkedList<LightPole> wps, String name) {
		
		LightPole prev = wps.getFirst();
		
		double x = 0;
		double y = 0;

		for(int j = 1 ; j < wps.size() ; j++) {
			LightPole current = wps.get(j);
			if(j == 0)
				prev = current;
			else {
				
				x+=current.getPosition().getX();
				y+=current.getPosition().getY();
				
				Line l = new Line(simulator,prev.getName()+"_"+current.getName(),prev.getPosition().getX(),prev.getPosition().getY(),current.getPosition().getX(),current.getPosition().getY());
				addObject(l);
				prev = current;
			}
		}
		
		x/=wps.size()-1;
		y/=wps.size()-1;
		
		LightPole center = createWP(simulator, name, x, y);
		center.setColor(Color.GRAY);
		addObject(center);
	}

	@Override
	public void update(double time) {
		
	}

	public String getDestination(int id) {
		for(PhysicalObject p : this.getAllObjects()) {
			if(p.getType() == PhysicalObjectType.LIGHTPOLE) {
				if(p.getName().equals("r"+id))
					return p.getName();
			}
		}
		return "area1";
	}
}