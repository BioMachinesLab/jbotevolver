package utils;

import java.util.HashMap;
import java.util.Set;

import simulation.util.Arguments;

public class RobotsResult {
	
	private String result;
	private HashMap<String,Arguments> sensors;
	private HashMap<String,Arguments> actuators;
	
	private int sensorCount = 0;
	private int actuatorCount = 0;
	
	public RobotsResult() {
		result = "--robots ";
		sensors = new HashMap<String,Arguments>();
		actuators = new HashMap<String,Arguments>();
	}
	
	public void appendTextToResult(String text){
		result += "\n\t" + text + ",";
	}
	
	public void addSensorInformation(String className, String sensorInformation){
		String id = className + " " + sensorCount;
		sensors.put(id,new Arguments(sensorInformation));
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
	
	public String getResult() {
		return result + getSensors() + getActuators();
	}
	
}
