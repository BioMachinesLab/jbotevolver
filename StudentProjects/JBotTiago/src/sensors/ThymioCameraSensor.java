package sensors;

import java.util.ArrayList;

import simulation.Simulator;
import simulation.physicalobjects.GeometricInfo;
import simulation.physicalobjects.PhysicalObjectDistance;
import simulation.physicalobjects.Prey;
import simulation.physicalobjects.checkers.AllowPreyChecker;
import simulation.robot.Robot;
import simulation.robot.sensors.ConeTypeSensor;
import simulation.util.Arguments;

import commoninterface.ThymioCI;
import commoninterface.entities.ThymioSharedEntity;
import commoninterface.mathutils.Vector2d;

public class ThymioCameraSensor extends ConeTypeSensor{

	private ThymioCI thymio;
	private boolean share = false;
	
	public ThymioCameraSensor(Simulator simulator, int id, Robot robot, Arguments args) {
		super(simulator, id, robot, args);
		setAllowedObjectsChecker(new AllowPreyChecker(id));
		thymio = (ThymioCI) robot;
		share = args.getFlagIsTrue("share");
	}

	@Override
	protected double calculateContributionToSensor(int i, PhysicalObjectDistance source) {
		GeometricInfo sensorInfo = getSensorGeometricInfo(i, source);
		
		if((sensorInfo.getDistance() < getCutOff()) && (sensorInfo.getAngle() < (openingAngle / 2.0)) && (sensorInfo.getAngle() > (-openingAngle / 2.0)))
			return ((openingAngle/2) - Math.abs(sensorInfo.getAngle())) / (openingAngle/2);
		else
			return 0;
	}

	@Override
	protected void calculateSourceContributions(PhysicalObjectDistance source) {
		ArrayList<Prey> preys = new ArrayList<Prey>();
		
		for(int j = 0; j < readings.length; j++){
			Prey prey = null;
			
			if(openingAngle > 0.018){ //1degree
				readings[j] = calculateContributionToSensor(j, source);
				prey = (Prey) source.getObject();
			}
			
			if(prey != null)
				preys.add(prey);
		}
		
		if(share)
			shareInformation(preys);
	}
	
	private void shareInformation(ArrayList<Prey> preys){
		for (Prey p : preys) {
			ThymioSharedEntity tse = new ThymioSharedEntity(p.getName(), thymio.getNetworkAddress(), new Vector2d(p.getPosition().x, p.getPosition().y));
			tse.setTimestepReceived((long)(thymio.getTimeSinceStart()*10));
			thymio.replaceEntity(tse);
		}
	}
	
}
