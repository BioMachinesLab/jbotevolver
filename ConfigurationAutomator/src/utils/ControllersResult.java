package utils;

import java.util.HashMap;
import java.util.Set;

import simulation.util.Arguments;

public class ControllersResult {
	
	private String result;
	private String classname;
	private String network;
	private HashMap<String,Arguments> inputs;
	private HashMap<String,Arguments> outputs;
	
	public ControllersResult() {
		result = "--controllers ";
		inputs = new HashMap<String,Arguments>();
		outputs = new HashMap<String,Arguments>();
	}
	
	public void addClassname(String text){
		classname = text;
	}
	
	public void removeInputInformation(String id){
		inputs.remove(id);
	}
	
	public Arguments getArgumentsForInputId(String id){
		return inputs.get(id);
	}
	
	public Set<String> getInputIds(){
		return inputs.keySet();
	}
	
	public void removeOutputInformation(String id){
		outputs.remove(id);
	}
	
	public Arguments getArgumentsForOutputId(String id){
		return outputs.get(id);
	}
	
	public Set<String> getOutputIds(){
		return outputs.keySet();
	}

	public String getClassname() {
		if(classname == null)
			return "";
		return "\n\t" + classname + ",\n";
	}
	
	public String getNetwork(){
		if(network == null)
			return "";

		return network + ",\n\t\t" + "inputs=auto" + ",\n\t\t" + "outputs=auto" + "\n\t)";
		
	}
	
	public void clearNetwork(){
		network=null;
	}
	
	public void addNetworkClassname(String text){
		network = "\tnetwork=( \n\t\t" + text;
	}
	
	public void addToNetworkString(String text){
		network += ",\n\t\t" + text;
	}
	
	public String getResult() {
		return result + getClassname() + getNetwork();
	}
	

}
