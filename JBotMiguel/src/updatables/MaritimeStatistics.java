package updatables;

import controllers.MaritimeMissionController;
import sensors.InsideBoundarySensor;
import sensors.IntruderSensor;
import simulation.Simulator;
import simulation.Updatable;
import simulation.robot.Robot;

public class MaritimeStatistics implements Updatable {
	
	public long stepsSeeingOne = 0;
	public long stepsSeeingTwoOrMore = 0;
	public long stepsInside = 0;
	public long totalIntruders = 0;
	public long foundIntruders = 0;
	private long stepsUntilSeeing = 0;
	public Robot intruder = null;
	public boolean currentlyInside = false;
	public boolean detected = false;
	public InsideBoundarySensor ibs = null;
	
	private long currentStepsUntilSeeing = 0;

	@Override
	public void update(Simulator simulator) {
		
		if(intruder == null) {
			for(Robot r : simulator.getRobots()) {
				if(r.getDescription().equals("prey")) {
					intruder = r;
					ibs = (InsideBoundarySensor)intruder.getSensorByType(InsideBoundarySensor.class);
					break;
				}
			}
		}
		
		if(ibs.insideBoundary()) {
			if(!currentlyInside) {
				totalIntruders++;
				currentlyInside = true;
				detected = false;
				currentStepsUntilSeeing = 0;
			}
			int count = 0;
			
			for(Robot r : simulator.getRobots()) {
				if(r.getId() != intruder.getId()) {
					IntruderSensor is = (IntruderSensor)r.getSensorByType(IntruderSensor.class);
					if(is.foundIntruder()) 
						count++;
				}
			}
			if(count == 1)
				stepsSeeingOne++;
			else if(count > 1)
				stepsSeeingTwoOrMore++;
			
			if(count > 0 && !detected) {
				foundIntruders++;
				detected = true;
			}
			
			if(currentlyInside && detected && currentStepsUntilSeeing > 0) {
				stepsUntilSeeing+=currentStepsUntilSeeing;
				currentStepsUntilSeeing = 0;
			}
			
			if(currentlyInside && !detected) {
				currentStepsUntilSeeing++;
			}
			
			stepsInside++;
		} else {
			currentlyInside = false;
		}
		
		if(simulator.getTime() % 1000 == 0) {
			
			int[] states = new int[MaritimeMissionController.State.values().length];
			
			for(Robot r : simulator.getRobots()) {
				if(r.getController() instanceof MaritimeMissionController) {
					MaritimeMissionController m = (MaritimeMissionController)r.getController();
					states[m.getCurrentState().ordinal()]++;
				}
			}
			String st = "";
			for(int i = 0 ; i < states.length; i++) {
				st+=MaritimeMissionController.State.values()[i]+":"+states[i]+" ";
			}
			System.out.println(simulator.getTime()+" "+stepsInside+" "+stepsSeeingOne+" "+stepsSeeingTwoOrMore+" "+totalIntruders+" "+foundIntruders+" "+stepsUntilSeeing+" "+st);
		}
	}
}
