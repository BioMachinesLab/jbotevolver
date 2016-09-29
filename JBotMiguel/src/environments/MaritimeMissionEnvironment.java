package environments;

import java.awt.Color;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;
import controllers.GoStraightController;
import mathutils.Vector2d;
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
	public LinkedList<LightPole> bases = new LinkedList<LightPole>();
	public double baseRadius = 5;
	public boolean intruder = false;
	public boolean patrolPositions = false;
	public boolean disperseRobots = false;
	
	public boolean current = false;
	public double currentSpeed = 0.2;
	public double currentOrientation = 0;
	public boolean mixed = false;
	public double min = 500;
	public double max = 100;
	public int baseNumber = 0;
	
	public double intruderDistance = 0;
	private boolean sameOrientation = false;
	
	private int currentRandomWaypoint = 0;
	
	private Robot intruderRobot = null;
	
	private int deployIntruder = 0;
	
	private LinkedList<LightPole> originalWPs = new LinkedList<LightPole>();
	
	public Simulator sim;
	
	public MaritimeMissionEnvironment(Simulator simulator, Arguments args) {
		super(simulator, args);
		this.sim = simulator;
		missionFile = "missions/"+args.getArgumentAsStringOrSetDefault("mission","mission_evolve.txt");
		intruder = args.getArgumentAsIntOrSetDefault("intruder",0) == 1;
		disperseRobots = args.getArgumentAsIntOrSetDefault("disperserobots",0) == 1;
		patrolPositions = args.getArgumentAsIntOrSetDefault("patrolpositions",0) == 1;
		current = args.getArgumentAsIntOrSetDefault("current",0) == 1;
		baseRadius = args.getArgumentAsDoubleOrSetDefault("baseradius",baseRadius);
		min = args.getArgumentAsDoubleOrSetDefault("min",min);
		max = args.getArgumentAsDoubleOrSetDefault("max",max);
		sameOrientation = args.getArgumentAsIntOrSetDefault("sameorientation",0) == 1;
		deployIntruder = args.getArgumentAsIntOrSetDefault("deployintruder",0);
		if(current) {
			currentSpeed = args.getArgumentAsDoubleOrSetDefault("currentspeed",currentSpeed);
			currentSpeed = simulator.getRandom().nextDouble()*currentSpeed;
			currentOrientation = simulator.getRandom().nextDouble()*Math.PI*2;
		}
		
		intruderDistance = args.getArgumentAsDoubleOrSetDefault("intruderdistance", intruderDistance);
		
		mixed = args.getArgumentAsIntOrSetDefault("mixed", 0) == 1;
	}	
	
	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		
		LinkedList<LightPole> currentWPs = new LinkedList<LightPole>();
		
		double intruderX = 0;
		double intruderY = 0;
		
		try {
			
			String currentName = "";
			
			if(!mixed) {
				
				Scanner s = new Scanner(simulator.getFileProvider().getFile(missionFile));
				
				double offsetX = 0;
				double offsetY = 0;
				int count = 0;
				
				boolean inArea = false;
				
				while(s.hasNextLine()) {
					String line = s.nextLine();
					
					String[] split = line.split(" ");
					
					if(line.startsWith("B")) {
						bases.add(createWP(simulator,"base"+(baseNumber++), Double.parseDouble(split[1]), Double.parseDouble(split[2]),baseRadius));
						bases.getLast().setColor(Color.GREEN);
						addObject(bases.getLast());
					} else if (line.startsWith("A")) {
						
						if(inArea && !currentWPs.isEmpty())
							createArea(simulator,currentWPs,currentName);
						
						currentWPs.clear();
						currentName = split[1];
						offsetX = split.length > 3 ? Double.parseDouble(split[2]) : 0;
						offsetY = split.length > 3 ? Double.parseDouble(split[3]) : 0;
						inArea = true;
					} else if (line.startsWith("L")) {
						LightPole wp = createWP(simulator,currentName+count, offsetX+Double.parseDouble(split[1]), offsetY+Double.parseDouble(split[2]));
						if(wp.getPosition().getY() < intruderY)
							intruderY = wp.getPosition().getY();
						if(Math.abs(wp.getPosition().getX()) > intruderX)
							intruderX = Math.abs(wp.getPosition().getX());
						count++;
						addObject(wp);
						currentWPs.add(wp);
						if(!inArea)
							originalWPs.add(wp);
					}
				}
			} else {
				currentName="area";
				double width = min + simulator.getRandom().nextDouble()*(max-min);
				double height = min + simulator.getRandom().nextDouble()*(max-min);
				intruderX = width;
				intruderY = -height;
				LightPole wp = createWP(simulator,currentName+0, width, height);
				addObject(wp);
				currentWPs.add(wp);
				wp = createWP(simulator,currentName+1, -width, height);
				addObject(wp);
				currentWPs.add(wp);
				wp = createWP(simulator,currentName+3, -width, -height);
				addObject(wp);
				currentWPs.add(wp);
				wp = createWP(simulator,currentName+2, width, -height);
				addObject(wp);
				currentWPs.add(wp);
				wp = createWP(simulator,currentName+0, width, height);
				addObject(wp);
				currentWPs.add(wp);
				
				bases.add(createWP(simulator,"base"+(baseNumber++), 0, 0, baseRadius));
				bases.getLast().setColor(Color.GREEN);
				addObject(bases.getLast());
			}
			
			
			if(!currentWPs.isEmpty())
				createArea(simulator,currentWPs,currentName);
			
			double orientation = simulator.getRandom().nextDouble()*Math.PI*2;
			
			for(Robot r : robots) {
				double distance = baseRadius*simulator.getRandom().nextDouble();
				double angle = simulator.getRandom().nextDouble()*Math.PI*2;
				double rx = bases.getLast().getPosition().getX()+distance*Math.cos(angle);
				double ry = bases.getLast().getPosition().getY()+distance*Math.sin(angle);
				r.setPosition(new Vector2d(rx,ry));
				r.setOrientation(sameOrientation ? orientation : simulator.getRandom().nextDouble()*Math.PI*2);
			}
		
		} catch (IOException e) {
			e.printStackTrace();
		}

		if(patrolPositions)
			createPatrolPositions(simulator,currentWPs,false);
		
		if(disperseRobots)
			createPatrolPositions(simulator,currentWPs,true);
		
		if(intruder) {
			Arguments programmedRobotArguments = simulator.getArguments().get("--programmedrobots");
			Robot robot = Robot.getRobot(simulator,programmedRobotArguments);
			robot.setController(new GoStraightController(simulator,robot,programmedRobotArguments));
			double x = simulator.getRandom().nextDouble()*intruderX - 100;
			if(simulator.getRandom().nextBoolean())
				x*=-1;
			double y = intruderY- 200;
			if(intruderDistance > 0) {
				y = -intruderDistance*sim.getRandom().nextDouble();
				x = intruderDistance*sim.getRandom().nextDouble()*(sim.getRandom().nextBoolean()?-1:1);
			}
			
			if(deployIntruder > 0) {
				x = 100000;
				y = 100000;
			}
			
			robot.setPosition(new Vector2d(x,y));
			robot.setOrientation(Math.PI/2);
			addRobot(robot);
			intruderRobot = robot;
		}
	}
	
	private void createPatrolPositions(Simulator simulator, LinkedList<LightPole> areaWP, boolean placeRobots) {
		
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
		int robotIndex = 0;
		for(double y = minY+areaPerY/2 ; y < maxY ; y+=areaPerY) {
			double posY = y;
			for(double x = minX+areaPerX/2 ; x < maxX ; x+=areaPerX) {
				double posX = x;
				if(placeRobots && robotIndex < robots.size())
					robots.get(robotIndex++).teleportTo(new Vector2d(posX,posY));
				else
					addObject(createWP(simulator, "r"+(id++), posX, posY));
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
		
		if(deployIntruder > 0 && time >= 72000) {
			if(time % deployIntruder == 0) {
				deployIntruder();
			}
		}
		
		if(current) {
			for(Robot r : robots) {
				if(!r.getDescription().equals("prey")) {
					double x = r.getPosition().getX();
					double y = r.getPosition().getY();
					x+= sim.getRandom().nextDouble()*currentSpeed*Math.cos(currentOrientation);
					y+= sim.getRandom().nextDouble()*currentSpeed*Math.sin(currentOrientation);
					double orientation = r.getOrientation();
					r.teleportTo(new Vector2d(x,y));
					r.setOrientation(orientation);
				}
			}
		}
		
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
	
	public void removeWaypoint(String s) {
		Iterator<PhysicalObject> i = this.getAllObjects().iterator();
		while(i.hasNext()) {
			PhysicalObject p = i.next();
			if(p.getType() == PhysicalObjectType.LIGHTPOLE) {
				if(p.getName().equals(s)) {
					i.remove();
					return;
				}
			}
		}
	}
	
	public String getRandomWaypoint() {
		
		String s = "random"+(currentRandomWaypoint++);
		Vector2d pos = getRandomPositionInside();
		addObject(createWP(sim, s, pos.getX(), pos.getY()));
		
		return s;
	}
	
	public String getRandomWaypointLeft(boolean left) { //left or right
		Vector2d pos = null;
		boolean correct = false;
		while(!correct) {
			pos = getRandomPositionInside();
			correct = (left && pos.getX() <= 0) || (!left && pos.getX() > 0);
		}
		String s = "random"+(currentRandomWaypoint++);
		addObject(createWP(sim, s, pos.getX(), pos.getY()));
		return s;
	}
	
	public void deployIntruder() {
		Vector2d pos = getRandomPositionInside();
		intruderRobot.teleportTo(new Vector2d(pos.getX(),pos.getY()-1500));
		intruderRobot.setEnabled(true);
	}
	
	public Vector2d getRandomPositionInside() {
		int pos = sim.getRandom().nextInt(originalWPs.size()-1);
		
		Vector2d prev = originalWPs.get(pos).getPosition();
		Vector2d next = originalWPs.get(pos+1).getPosition();
		
		double rand = sim.getRandom().nextDouble();
		
		double dX = (next.getX()-prev.getX())*rand;
		double dY = (next.getY()-prev.getY())*rand;
		
		dX = prev.getX()+dX;
		dY = prev.getY()+dY;
		return new Vector2d(dX,dY);
	}
	
	public double getMaxApproximationSpeed() {
		return 2.77;
	}
	
	public int getNumberOfBases() {
		return bases.size();
	}
	
	public LinkedList<LightPole> getBases() {
		return bases;
	}
}