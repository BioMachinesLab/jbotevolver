package updatables;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import simulation.Simulator;
import simulation.Updatable;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.Wall;
import simulation.util.Arguments;

public class BlenderExport implements Updatable {
	
	private boolean firstFrame = true;
	private String output = "";
	private String filename = "blender.txt";
	
	public BlenderExport(Arguments args) {
		filename = args.getArgumentAsStringOrSetDefault("filename", filename);
		setup();
	}
	
	public BlenderExport() {
		setup();
	}
	
	private void setup() {
		if(!(new File(filename).exists())) {
			File f = new File(filename);
			try {
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		saveToFile(false);
	}

	@Override
	public void update(Simulator sim) {
		if(firstFrame) {
			createObjects(sim);
			firstFrame = false;
		} else {
			updateObjects(sim);
		}
		saveToFile(true);
	}
	
	private void createObjects(Simulator sim) {
		for(PhysicalObject p : sim.getEnvironment().getAllObjects()) {
			double x = p.getPosition().getX();
			double y = p.getPosition().getY();
			double orientation = p.getOrientation();
			int id = p.getId();
			switch(p.getType()) {
				case NEST:
					output+="NEST "+id+" "+x+" "+y+" "+p.getRadius()+"\n";
					break;
				case LIGHTPOLE:
					output+="LIGHTPOLE "+id+" "+x+" "+y+" "+p.getRadius()+"\n";
					break;
				case PREY:
					output+="PREY "+id+" "+x+" "+y+" "+p.getRadius()+"\n";
					break;
				case WALLBUTTON:
					Wall w1 = (Wall)p;
					output+="WALLBUTTON "+id+" "+x+" "+y+" "+w1.getWidth()+" "+w1.getHeight()+"\n";
					break;
				case WALL:
					Wall w2 = (Wall)p;
					if(w2.color.equals(Color.BLACK))//door
						output+="DOOR "+id+" "+x+" "+y+" "+w2.getWidth()+" "+w2.getHeight()+"\n";
					else
						output+="WALL "+id+" "+x+" "+y+" "+w2.getWidth()+" "+w2.getHeight()+"\n";
					break;
				case ROBOT:
					output+="ROBOT "+id+" "+x+" "+y+" "+orientation+"\n";
					break;
				default:break;
			}
		}
	}
	
	private void updateObjects(Simulator sim) {
		for(PhysicalObject p : sim.getEnvironment().getAllObjects()) {
			double x = p.getPosition().getX();
			double y = p.getPosition().getY();
			double orientation = p.getOrientation();
			int id = p.getId();
			switch(p.getType()) {
				case NEST:
					output+="KEY NEST "+id+" "+sim.getTime().intValue()+" "+x+" "+y+"\n";
					break;
				case PREY:
					output+="KEY PREY "+id+" "+sim.getTime().intValue()+" "+x+" "+y+"\n";
					break;
				case WALLBUTTON:
					output+="KEY WALL "+id+" "+sim.getTime().intValue()+" "+x+" "+y+"\n";
					break;
				case WALL:
					output+="KEY WALL "+id+" "+sim.getTime().intValue()+" "+x+" "+y+"\n";
					break;
				case ROBOT:
					output+="KEY ROBOT "+id+" "+sim.getTime().intValue()+" "+x+" "+y+" "+orientation+"\n";
					break;
				default:break;
			}
		}
	}
	
	private void saveToFile(boolean append) {
		System.out.println(output);
		
		try {
		    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filename, append)));
		    out.println(output);
		    out.close();
		} catch (IOException e) {
		    e.printStackTrace();
		}
		
		output = "";
	}

}
