package utils;

import java.util.HashMap;
import java.util.Set;

import simulation.util.Arguments;

public class RobotsResult {
	
	private String classname;
	private String attributes;
	private HashMap<String,Arguments> sensors;
	private HashMap<String,Arguments> actuators;
	
	private int sensorCount = 0;
	private int actuatorCount = 0;
	
	public RobotsResult() {
		attributes = "";
		sensors = new HashMap<String,Arguments>();
		actuators = new HashMap<String,Arguments>();
	}
	
	public String getClassname() {
		if(classname == null)
			return "";
		return "\n\t" + classname + ",";
	}
	
	public String getAttributes() {
		return attributes;
	}
	
	public void addClassname(String text){
		classname = text;
		attributes = "";
	}
	
	public void addAttribute(String text){
		attributes += "\n\t" + text + ",";
	}
	
	public void addSensorInformation(String className, String sensorInformation){
		String id = className + " " + sensorCount;
		
		if(!getSensors().equals("") && getSensors().contains(className)){
			String newInfo = className + sensorCount + sensorInformation.substring(sensorInformation.indexOf("="));
			sensors.put(id,new Arguments(newInfo));
		}else{
			sensors.put(id,new Arguments(sensorInformation));
		}
		
		sensorCount ++;
	}
	
	public void removeSensorInformation(String id){
		sensors.remove(id);
	}
	
	public Arguments getArgumentsForSensorId(String id){
		return sensors.get(id);
	}
	
	public Set<String> getSensorIds(){
		return sensors.keySet();
	}
	
	public String getSensors(){
		if(!sensors.isEmpty()){
			String completeSensors = "\tsensors=( \n";
			int count = 0;
			
			for(String s : sensors.keySet()) {
				count++;
				Arguments arg = sensors.get(s);
				String fullArguments = arg.getCompleteArgumentString();
				
				completeSensors += "\t\t" + fullArguments;
				
				if(count < sensors.keySet().size())
					completeSensors += ",\n";
				else
					completeSensors += "\n";
			}
			
			return "\n" + completeSensors + "\t),";
		}else{
			return "";
		}
	}
	
	public void addActuatorInformation(String className, String actuatorInformation){
		String id = className + " " + actuatorCount;
		actuators.put(id, new Arguments(actuatorInformation));
		actuatorCount++;
	}
	
	public void removeActuatorInformation(String id){
		actuators.remove(id);
	}
	
	public Arguments getArgumentsForActuatorId(String id){
		return actuators.get(id);
	}
	
	public Set<String> getActuatorsIds(){
		return actuators.keySet();
	}
	
	public String getActuators(){
		if(!actuators.isEmpty()){
			String completeActuators = "actuators=( \n";
			int count = 0;
			
			for(String s : actuators.keySet()) {
				count++;
				Arguments arg = actuators.get(s);
				String fullArguments = arg.getCompleteArgumentString();
				
				completeActuators += "\t\t" + fullArguments;
				
				if(count < actuators.keySet().size())
					completeActuators += ",\n";
				else
					completeActuators += "\n";
				
			}
			
			return completeActuators + "\t)";
		}else{
			return "";
		}
		
	}
	
	public String getIDForSensor(String sensorKeyName){
		Arguments args = sensors.get(sensorKeyName);
		
		for (String v : args.getValues()) {
			String[] argsAttributes = v.split(",");
			for (String a : argsAttributes) {
				String[] attributeComplete = a.split("=");
				if(attributeComplete[0].equals("id")){
					return attributeComplete[1];
				}
			}
		}
		
		return null;
	}
	
	public boolean isFilled(){
		return classname!=null && !sensors.isEmpty() && !actuators.isEmpty();
	}
	
	@Override
	public String toString() {
		return "--robots " + getClassname() + getAttributes() + getSensors() + getActuators();
	}
	
}
