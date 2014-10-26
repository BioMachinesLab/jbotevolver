package utils;


public class ConfigurationResult {
	private String output;
	private RobotsResult robots;
	private ControllersResult controllers;
	private String population;
	private String environment;
	private String executor;
	private String evolution;
	private String evaluation;
	private String random_seed;

	public ConfigurationResult(RobotsResult robots, ControllersResult controllers) {
		output = "--output ";
		this.robots = robots;
		this.controllers = controllers;
		population = "--population ";
		environment = "--environment ";
		executor = "--executor ";
		evolution = "--evolution ";
		evolution = "--evolution ";
		evaluation = "--evaluation ";
		random_seed = "--random_seed ";
	}
	
	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}

	public RobotsResult getRobotsResult() {
		return robots;
	}
	
	public void setRobots(RobotsResult robots) {
		this.robots = robots;
	}

	public ControllersResult getControllersResult() {
		return controllers;
	}

	public void setControllers(ControllersResult controllers) {
		this.controllers = controllers;
	}
	
	public void appendTextToPopulation(String text){
		if(text.contains("classname=")){
			population += "\n\t" + text;
		}else{
			population += ",\n\t" + text;
		}
	}
	
	public void appendTextToEnvironment(String text){
		if(text.contains("classname=")){
			environment += "\n\t" + text;
		}else{
			environment += ",\n\t" + text;
		}
	}

	public void appendTextToExecutor(String text){
		if(text.contains("classname=")){
			executor += text;
		}else{
			executor += "," + text;
		}
	}
	
	public void appendTextToEvolution(String text){
		if(text.contains("classname=")){
			evolution += text;
		}else{
			evolution += "," + text;
		}
	}
	
	public void appendTextToEvaluation(String text){
		if(text.contains("classname=")){
			evaluation += text;
		}else{
			evaluation += "," + text;
		}
	}
	
	public String getRandom_seed() {
		return random_seed;
	}

	public void setRandomSeed(String random_seed) {
		this.random_seed = random_seed;
	}

	public String getResult(){
		return output + "\n\n" + robots.getResult() + "\n\n" + controllers.getResult() + "\n\n"
				+ population + "\n\n" + environment + "\n\n" + executor
				+ "\n\n" + evolution + "\n\n" + evaluation + "\n\n"
				+ random_seed;
	}
	
}
