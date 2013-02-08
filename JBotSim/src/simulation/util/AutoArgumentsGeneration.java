package simulation.util;

import java.util.HashMap;
import java.util.LinkedList;

public class AutoArgumentsGeneration {
	
	public static void getAuto(HashMap<String,Arguments> arguments) {
		try {
			configureNNInputsAuto(arguments);
			configureNNOutputsAuto(arguments);
		} catch(NullPointerException e) {}
	}
	
	private static void configureNNInputsAuto(HashMap<String,Arguments> arguments) {
		try {
			Arguments controllerArgs = arguments.get("--controllers");
			Arguments networkArgs = new Arguments(controllerArgs.getArgumentAsString("network"));
			
			if(networkArgs.getArgumentAsString("inputs").equals("auto")) {
				Arguments robotArgs = arguments.get("--robots");
				Arguments sensorArgs = new Arguments(robotArgs.getArgumentAsString("sensors"));
				String fullAutoInputs = "";
				for(int i = 0 ; i < sensorArgs.getNumberOfArguments() ; i++) {
					String sensorName = sensorArgs.getArgumentAt(i);
					Arguments currentSensorArgs = new Arguments(sensorArgs.getArgumentAsString(sensorName));
					String inputName = sensorName.replace("Sensor","NNInput");
					String fullInputName = ClassSearchUtils.getClassFullName(inputName);
					String id = currentSensorArgs.getArgumentAsString("id");
					fullAutoInputs+=inputName+"=(classname="+fullInputName+",id="+id+"),";					
				}
				networkArgs.setArgument("inputs", fullAutoInputs);
				controllerArgs.setArgument("network", networkArgs.getCompleteArgumentString());
			}
		} catch(Exception e) {}
	}
	
	private static void configureNNOutputsAuto(HashMap<String,Arguments> arguments) {
		try {
			Arguments controllerArgs = arguments.get("--controllers");
			Arguments networkArgs = new Arguments(controllerArgs.getArgumentAsString("network"));
			
			if(networkArgs.getArgumentAsString("outputs").equals("auto")) {
				Arguments robotArgs = arguments.get("--robots");
				Arguments actuatorArgs = new Arguments(robotArgs.getArgumentAsString("actuators"));
				String fullAutoOutputs = "";
				for(int i = 0 ; i < actuatorArgs.getNumberOfArguments() ; i++) {
					String actuatorName = actuatorArgs.getArgumentAt(i);
					Arguments currentSensorArgs = new Arguments(actuatorArgs.getArgumentAsString(actuatorName));
					String outputName = actuatorName.replace("Actuator","NNOutput");
					String fullOutputName = ClassSearchUtils.getClassFullName(outputName);
					String id = currentSensorArgs.getArgumentAsString("id");
					fullAutoOutputs+=outputName+"=(classname="+fullOutputName+",id="+id+"),";					
				}
				networkArgs.setArgument("outputs", fullAutoOutputs);
				controllerArgs.setArgument("network", networkArgs.getCompleteArgumentString());
			}
		} catch(Exception e) {}
	}
}