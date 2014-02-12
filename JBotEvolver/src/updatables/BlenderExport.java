package updatables;

import simulation.Simulator;
import simulation.Updatable;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.Wall;

public class BlenderExport implements Updatable {
	
	private boolean firstFrame = true;
	private String output = "";

	@Override
	public void update(Simulator sim) {
		if(firstFrame) {
			createObjects(sim);
			firstFrame = false;
		} else {
			updateObjects(sim);
		}
		saveToFile();
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
				case PREY:
					output+="PREY "+id+" "+x+" "+y+" "+p.getRadius()+"\n";
					break;
				case WALLBUTTON:
					Wall w1 = (Wall)p;
					output+="WALL "+id+" "+x+" "+y+" "+w1.getWidth()+" "+w1.getHeight()+"\n";
					break;
				case WALL:
					Wall w2 = (Wall)p;
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
	
	private void saveToFile() {
		System.out.println(output);
		output = "";
	}

}
