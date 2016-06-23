package src;

import java.util.HashMap;

import simulation.util.Arguments;

public class Controller {
	
	private String configuration = null;
	private HashMap<String, Arguments> args = null;
	private String name = null;
	private String weights = null;
	private boolean evolving = false;
	private boolean skipEvolution = false;
	
	public Controller(String name, String configuration) {
		this.name = name;
		this.configuration = configuration;
	}
	
	public void createArguments(String defaultConfigs) {
		
		if(readyToEvolve()) {

			try {
				args = Arguments.parseArgs(Arguments.readOptionsFromString(defaultConfigs+"\n"+configuration));
			
			
//				for(String argName : defaultConfigs.keySet())
//				args.put(argName, new Arguments(defaultConfigs.get(argName).getCompleteArgumentString()));
//			
	//			for(int i = 0 ; i < options.length ; i+=2) {
	//				args.put(options[i],Arguments.createOrPrependArguments(args.get(options[i]),options[i+1]));
	//			}
				
				String[] options = Arguments.readOptionsFromString(configuration);
				
				this.skipEvolution = true;
				
				for(String option : options)
					if(option.equals("--population"))
						skipEvolution = false;
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void addSubController(Controller c) {
		String subController = c.getControllerConfiguration();
		String subControllerName = c.getName();
		
		this.configuration = this.configuration.replace("#"+subControllerName, subController);
	}
	
	public boolean readyToEvolve() {
		return !configuration.contains("#");
	}
	
	public boolean hasBeenEvolved() {
		return weights != null || skipEvolution();
	}
	
	public boolean isEvolving() {
		return evolving;
	}
	
	public void setEvolving(boolean evolving) {
		this.evolving = evolving;
	}
	
	public String getConfiguration() {
		return this.configuration;
	}
	
	public void setConfiguration(String configuration) {
		this.configuration = configuration;
	}
	
	public String getName() {
		return name;
	}
	
	public void setWeights(String weights) {
		if(!skipEvolution())
			this.args.put("--controllers",Arguments.createOrPrependArguments(args.get("--controllers"),"+weights=("+weights+")"));
		this.weights = weights;
	}
	
	public boolean needsSubController(String subControllerName) {
		return configuration.contains("(#"+subControllerName+")");
	}

	public String getControllerConfiguration() {
		return args.get("--controllers").getCompleteArgumentString();
	}
	
	public String getCompleteConfiguration() {
		
		String config = "";
		
		for(String key : args.keySet())
			config+=key+" "+args.get(key).getCompleteArgumentString()+"\n";
		
		return config;
	}
	
	public boolean skipEvolution() {
		return skipEvolution;
	}

	public Arguments getArguments(String argName) {
		return args.get(argName);
	}

	public HashMap<String, Arguments> getArguments() {
		return args;
	}
}