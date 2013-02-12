package simulation.util;

import java.util.HashMap;

public class AutoArgumentsGeneration {
	
	public static void getAuto(HashMap<String,Arguments> arguments) {
		try {
			configureNNInputsAuto(arguments);
			configureNNOutputsAuto(arguments);
		} catch(NullPointerException e) {}
	}
	
	private static Arguments getNNInputsAutoForController(Arguments controllerArgs, Arguments robotArgs) {
		try {
			Arguments networkArgs = new Arguments(controllerArgs.getArgumentAsString("network"));
			
			String fullAutoInputs = "";
			
			if(networkArgs.getArgumentAsString("inputs").equals("auto")) {
				Arguments sensorArgs = new Arguments(robotArgs.getArgumentAsString("sensors"));
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
			
			if(controllerArgs.getArgumentIsDefined("subcontrollers")) {
				Arguments subs = new Arguments(controllerArgs.getArgumentAsString("subcontrollers"));
				
				for(String s : subs.getArguments()) {
					Arguments auto = getNNInputsAutoForController(new Arguments(subs.getArgumentAsString(s)),robotArgs);
					subs.setArgument(s, auto.getCompleteArgumentString());
				}
				
				controllerArgs.setArgument("subcontrollers", subs.getCompleteArgumentString());
			}
		} catch(Exception e) {}
		
		return controllerArgs;
	}
	
	private static Arguments getNNOutputsAutoForController(Arguments controllerArgs, Arguments robotArgs) {
		try {
			
			Arguments networkArgs = new Arguments(controllerArgs.getArgumentAsString("network"));
			
			String fullAutoOutputs = "";
			
			if(networkArgs.getArgumentAsString("outputs").equals("auto")) {
				Arguments actuatorArgs = new Arguments(robotArgs.getArgumentAsString("actuators"));
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
			if(controllerArgs.getArgumentIsDefined("subcontrollers")) {
				Arguments subs = new Arguments(controllerArgs.getArgumentAsString("subcontrollers"));
				
				for(String s : subs.getArguments()) {
					Arguments auto = getNNOutputsAutoForController(new Arguments(subs.getArgumentAsString(s)),robotArgs);
					subs.setArgument(s, auto.getCompleteArgumentString());
				}
				
				controllerArgs.setArgument("subcontrollers", subs.getCompleteArgumentString());
			}
		} catch(Exception e) {}
		return controllerArgs;
	}
	
	private static void configureNNOutputsAuto(HashMap<String,Arguments> arguments) {
		if(arguments.get("--controllers") != null && arguments.get("--robots") != null) {
			Arguments auto = getNNOutputsAutoForController(arguments.get("--controllers"),arguments.get("--robots"));
			arguments.put("--controllers", auto);
		}
	}
	
	private static void configureNNInputsAuto(HashMap<String,Arguments> arguments) {
		if(arguments.get("--controllers") != null && arguments.get("--robots") != null) {
			Arguments auto = getNNInputsAutoForController(arguments.get("--controllers"),arguments.get("--robots"));
			arguments.put("--controllers", auto);
		}
	}
}