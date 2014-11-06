package gui.configuration;

import java.util.Vector;
import simulation.util.Arguments;

public class RobotsResult {
	
	private Arguments robotArgs;
	private Arguments sensors;
	private Arguments actuators;
	
	public RobotsResult() {
		robotArgs = new Arguments("");
		sensors = new Arguments("");
		actuators = new Arguments("");
	}
	
	public void setAttributes(String text){
		robotArgs = new Arguments(text,false);
	}
	
	public Arguments getCompleteArguments() {
		Arguments result = new Arguments(robotArgs.getCompleteArgumentString());
		
		if(sensors.getNumberOfArguments() > 0)
			result.setArgument("sensors", sensors.getCompleteArgumentString());
		if(actuators.getNumberOfArguments() > 0)
			result.setArgument("actuators", actuators.getCompleteArgumentString());
		
		return result;
	}
	
	public void addSensorActuator(String keyName, String className, String sensorInformation){
		if(keyName.isEmpty()) {
			if(className.contains("Sensor"))
				addSensor(className, sensorInformation);
			else 
				addActuator(className, sensorInformation);
		}else
			editSensorActuator(keyName, sensorInformation);
	}
	
	private void addSensor(String className, String sensorInformation){
		
		int id = sensors.getNumberOfArguments() + 1;
		
		String name = className + "_" + id;
		
		String newInfo = sensorInformation+",id="+id;
		sensors.setArgument(name,newInfo);
	}
	
	private void addActuator(String className, String sensorInformation){
		
		int id = actuators.getNumberOfArguments() + 1;
		
		String name = className + "_" + id;
		
		String newInfo = sensorInformation+",id="+id;
		actuators.setArgument(name,newInfo);
	}
	
	private void editSensorActuator(String key, String arguments) {
		if(sensors.getArgumentIsDefined(key)) {
			sensors.removeArgument(key);
			sensors.setArgument(key, arguments);
			sensors = recalculateIds(sensors);
		}
		if(actuators.getArgumentIsDefined(key)) {
			actuators.setArgument(key, arguments);
			actuators = recalculateIds(actuators);
		}
	}
	
	public void remove(String key){
		if(sensors.getArgumentIsDefined(key)) {
			sensors.removeArgument(key);
			sensors = recalculateIds(sensors);
		} else {
			actuators.removeArgument(key);
			actuators = recalculateIds(actuators);
		}
	}
	
	private Arguments recalculateIds(Arguments args) {
		int id = 1;
		
		Arguments newArgs = new Arguments("");
		
		for(String s : args.getArguments()) {
			String currentArgsString = args.getArgumentAsString(s);
			Arguments currentArgs = new Arguments(currentArgsString);
			currentArgs.setArgument("id", id);
			
			String newName = s.split("_")[0] + "_" + (id++);
			
			newArgs.setArgument(newName, currentArgs.getCompleteArgumentString());
		}
		
		return newArgs;
	}
	
	public Arguments getSensorActuator(String name){
		if(sensors.getArgumentIsDefined(name))
			return new Arguments(sensors.getArgumentAsString(name));
		
		return new Arguments(actuators.getArgumentAsString(name));
	}
	
	public Vector<String> getSensorActuatorsIds(){
		Vector<String> vector = new Vector<String>();
		vector.addAll(sensors.getArguments());
		vector.addAll(actuators.getArguments());
		return vector;
	}
	
	@Override
	public String toString() {
		return "--robots " + getCompleteArguments().getCompleteArgumentString();
	}
	
}