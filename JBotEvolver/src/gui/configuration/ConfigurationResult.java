package gui.configuration;

import java.util.HashMap;

import simulation.util.Arguments;

public class ConfigurationResult {
	private String[] keys;
	private String selectedKey;
	
	private HashMap<String,Arguments> arguments = new HashMap<String, Arguments>();

	public ConfigurationResult(String[] keys) {
		this.keys = keys;
		
		for (String key : keys) 
			arguments.put(key, new Arguments(""));
	}

	public void setArgument(String name, Arguments arg) {
		selectedKey = name;
		this.arguments.put(name, arg);
	}
	
	public Arguments getArgument(String name) {
		return this.arguments.get(name);
	}
	
	public void setSelectedKey(String selectedKey) {
		this.selectedKey = selectedKey;
	}
	
	public String toString(){
		String res = "";
		
		for(String s : keys) {
			if(arguments.get(s).getNumberOfArguments() > 1)
				res+= "--"+s +"\n "+ Arguments.beautifyString(arguments.get(s) + "") + "\n\n";
			else
				res+= "--"+s + " "+ arguments.get(s) + "\n\n";
		}
		
		return res;
	}
	
	public String toHTMLString(String extraArgs){
		String res = "<pre>";
		
		for(String s : keys) {
			if(s.equals(selectedKey)){
				res+="<span style=\"background-color: #FFFF00\"> ";
			}
			if(arguments.get(s).getNumberOfArguments() > 1)
				res+= "--"+s +"<br> "+ Arguments.beautifyString(arguments.get(s) + "") + "<br><br>";
			else
				res+= "--"+s + " "+ arguments.get(s) + "<br><br>";
			if(s.equals(selectedKey)){
				res+="</span>";
			}

		}
		
		res+=extraArgs;
		
		return res +"</pre>";
	}
	
}