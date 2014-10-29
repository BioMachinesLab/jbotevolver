package utils;

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
	
	public void addSensor(String className, String sensorInformation){
		
		int id = sensors.getNumberOfArguments() + 1;
		
		String name = className + "_" + id;
		
		String newInfo = sensorInformation+",id="+id;
		sensors.setArgument(name,newInfo);
	}
	
	public void addActuator(String className, String actuatorInformation){
		actuators.setArgument(className, actuatorInformation);
	}
	
	public void edit(String key, String arguments) {
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
	
	public void removeSensor(String key){
		sensors.removeArgument(key);
		sensors = recalculateIds(sensors);
	}
	
	public void removeActuator(String key){
		actuators.removeArgument(key);
		actuators = recalculateIds(actuators);
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
	
	public Arguments getSensor(String name){
		return new Arguments(sensors.getArgumentAsString(name));
	}
	
	public Arguments getActuator(String name){
		return new Arguments(actuators.getArgumentAsString(name));
	}
	
	public Vector<String> getSensorIds(){
		return sensors.getArguments();
	}
	
	public Vector<String> getActuatorsIds(){
		return actuators.getArguments();
	}
	
	@Override
	public String toString() {
		return "--robots " + getCompleteArguments().getCompleteArgumentString();
	}
	
}