package simulation.physicalobjects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import simulation.environment.Environment;
import simulation.physicalobjects.checkers.AllowedObjectsChecker;

public class ClosePhysicalObjects implements Serializable {

	private static final double EXTENDED_VISIBILITY = 1;

	private LinkedList<PhysicalObjectDistance> closeObjects = new LinkedList<PhysicalObjectDistance>();
	private LinkedList<PhysicalObjectDistance> farObjects   = new LinkedList<PhysicalObjectDistance>();
	private Double time;
	private double farTime;
	private boolean notInitialized = true;

	private double range;
	private double twiceSpeed;
	private AllowedObjectsChecker allowedObjectsChecker;

	protected Environment env;

	public ClosePhysicalObjects(Environment env, double range, AllowedObjectsChecker allowedObjectsChecker) {
		this.env = env;
		this.range = range*1.5;
		this.allowedObjectsChecker = allowedObjectsChecker;
		farTime = 0;
		time = 0.0;
		twiceSpeed = env.getMaxApproximationSpeed();
	}

	public void update(double time, ArrayList<PhysicalObject> teleported){
		if(notInitialized){
			teleported = env.getAllObjects();
			notInitialized=false;
		}
		this.time = time;

		if (teleported.size() > 0) {
			if(closeObjects.isEmpty() && farObjects.isEmpty()){
				updateTeleportedNoCheck(teleported);
			} else {
				updateTeleported(teleported);
			}
		}
		updateCloseObjects();
	}

	public CloseObjectIterator iterator(){
		return new CloseObjectIterator(closeObjects.listIterator());
	}

	private void updateCloseObjects() {
		if(farObjects.size() > 0){
			Iterator<PhysicalObjectDistance> i = farObjects.iterator();
			PhysicalObjectDistance next = i.next();
			int o = 0;
			while(next.getTime() <= time){
				if(next.getObject().isEnabled()) {
					i.remove();
					o++;
					closeObjects.add(next);
				}
				if (!i.hasNext()){
					break;
				}
				next = i.next();
			}
		}
	}


	private void updateTeleported(ArrayList<PhysicalObject> teleportedObjects){
		//Expensive operation... should be called few times
		for(PhysicalObject physicalObject : teleportedObjects){
			if(allowedObjectsChecker.isAllowed(physicalObject)){
				PhysicalObjectDistance teleported = new PhysicalObjectDistance(physicalObject,0.0);
				if(!closeObjects.contains(teleported)){
					farObjects.remove(teleported);
					closeObjects.add(teleported);
				}
			}
		}
	}

	private void updateTeleportedNoCheck(ArrayList<PhysicalObject> teleportedObjects){
		//Expensive operation... should be called few times
		for(PhysicalObject physicalObject : teleportedObjects){
			if(allowedObjectsChecker.isAllowed(physicalObject)){
				PhysicalObjectDistance teleported = new PhysicalObjectDistance(physicalObject,0.0);
				closeObjects.add(teleported);
			}
		}
	}

	private void insertInFarObjects(PhysicalObjectDistance physicalObject) {
		ListIterator<PhysicalObjectDistance> i = farObjects.listIterator();

		while(i.hasNext()){
			PhysicalObjectDistance next = i.next();
			if(next.getTime()>=physicalObject.getTime()){
				i.previous();
				i.add(physicalObject);
				return;
			}
		}
		farObjects.add(physicalObject);

	}

	public void debugInfo(){
		System.out.println(time + " CO " +closeObjects.size() + " FO: " + farObjects.size());
	}
	public class CloseObjectIterator{
		private ListIterator<PhysicalObjectDistance> iterador;

		//To save some null checks... and a new instantiation every cycle
		private PhysicalObjectDistance emptyObject				= new PhysicalObjectDistance(null, 0.0); 
		private PhysicalObjectDistance currentObject 			= emptyObject;


		public CloseObjectIterator(ListIterator<PhysicalObjectDistance> iterador) {
			this.iterador = iterador;
			currentObject=emptyObject;
		}

		public boolean hasNext(){
			if (iterador.hasNext())
				return true;
			updateCurrentElement();
			return false;
		}

		public PhysicalObjectDistance next(){
			updateCurrentElement();
			currentObject  = iterador.next();
			return currentObject;
		}

		private void updateCurrentElement() {
			if(currentObject.getTime() > time) {
				iterador.remove();
				insertInFarObjects(currentObject);
			}
		}

		public void updateCurrentDistance(Double distanceBetween) {
			double d = range - distanceBetween;
			if(d < 0) //far away
				currentObject.setTime(time-(d/twiceSpeed));	
			else
				currentObject.setTime(time);
		}
	}
}