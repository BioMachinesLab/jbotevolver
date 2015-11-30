package environments;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Scanner;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.LightPole;
import simulation.physicalobjects.Wall;
import simulation.util.Arguments;

public class SVGMazeEnvironment extends Environment{
	
	private String mazeName = "zigzag";
	private double randomPosition = 0;
	private Vector2d shiftMaze;
	
	public SVGMazeEnvironment(Simulator simulator, Arguments args) {
		super(simulator,args);
		this.mazeName = args.getArgumentAsStringOrSetDefault("mazename", mazeName);
		this.randomPosition = args.getArgumentAsDoubleOrSetDefault("randomposition", randomPosition);
		
		if(args.getArgumentIsDefined("shiftmaze")){
			String[] shift = args.getArgumentAsString("shiftmaze").split(",");
			shiftMaze = new Vector2d(Double.parseDouble(shift[0]),Double.parseDouble(shift[1]));
		}
		
	}
	
	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		
		try {
		
		    InputStream buffer = new BufferedInputStream(simulator.getFileProvider().getFile("mazes/svg/"+mazeName+".txt"));
		    Scanner s = new Scanner(buffer);
		    
		    int numberWalls = s.nextInt();
		    
		    double shiftX = 0;
		    double shiftY = 0;
		    
		    if(shiftMaze != null) {
		    	shiftX = shiftMaze.x;
		    	shiftY = shiftMaze.y;
		    }
		    
		    for(int i = 0 ; i < numberWalls ; i++) {
		    	Wall wall = new Wall(
		    			simulator,
		    			new Vector2d(readDouble(s) + shiftX,readDouble(s) + shiftY), 
		    			new Vector2d(readDouble(s) + shiftX,readDouble(s) + shiftY), 
		    			readDouble(s)
		    		);
		    	addObject(wall);
		    }
		    
			Vector2d start = new Vector2d(readDouble(s) + shiftX,readDouble(s) + shiftY);
			Vector2d end = new Vector2d(readDouble(s) + shiftX,readDouble(s) + shiftY);
			
			s.close();
			
			LightPole lp = new LightPole(simulator, "lp", end.x, end.y, 0.1);
			addObject(lp);
			
			if(randomPosition > 0) {
				start.x+=simulator.getRandom().nextDouble()*randomPosition-randomPosition/2;
				start.y+=simulator.getRandom().nextDouble()*randomPosition-randomPosition/2;
				simulator.getRobots().get(0).setOrientation(Math.PI*2*simulator.getRandom().nextDouble());
				simulator.getRobots().get(0).setPosition(start);
			} else {
				simulator.getRobots().get(0).setPosition(start);
				double angle = Math.atan2(lp.getPosition().y-start.y,lp.getPosition().x-start.x);
				simulator.getRobots().get(0).setOrientation(angle);
			}
		
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private double readDouble(Scanner s) {
		return Double.parseDouble(s.next().trim().replace(',','.'));
	}

	@Override
	public void update(double time) {
		// TODO Auto-generated method stub
	}

}
