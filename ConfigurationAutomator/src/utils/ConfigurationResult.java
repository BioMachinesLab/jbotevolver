package utils;

import java.util.HashMap;

import simulation.util.Arguments;


public class ConfigurationResult {
	private ControllersResult controllers;
	private String[] keys;
	
	private HashMap<String,Arguments> arguments = new HashMap<String, Arguments>();

	public ConfigurationResult(String[] keys, ControllersResult controllers) {
		this.keys = keys;
		this.controllers = controllers;
		
		for (String key : keys) 
			arguments.put(key, new Arguments(""));
	}

	public ControllersResult getControllers() {
		return controllers;
	}

	public void setControllers(ControllersResult controllers) {
		this.controllers = controllers;
		setArgument("--controllers", new Arguments(this.controllers.toString()));
	}
	
	public void setArgument(String name, Arguments arg) {
		this.arguments.put(name, arg);
	}
	
	public Arguments getArgument(String name) {
		return this.arguments.get(name);
	}
	
	public String toString(){
		String res = "";
		
		for(String s : keys) {
			if(arguments.get(s).getCompleteArgumentString().contains(","))
				res+= s +"\n "+ Arguments.beautifyString(arguments.get(s) + "") + "\n\n";
			else
				res+= s + " "+ arguments.get(s) + "\n\n";
		}
		
		return res;
	}
	
}
