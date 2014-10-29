package utils;


public class ConfigurationResult {
	private String output;
	private RobotsResult robots;
	private ControllersResult controllers;
	private OptionsResult population;
	private OptionsResult environment;
	private OptionsResult executor;
	private OptionsResult evolution;
	private OptionsResult evaluation;
	private String random_seed;

	public ConfigurationResult(RobotsResult robots, ControllersResult controllers) {
		output = "--output ";
		this.robots = robots;
		this.controllers = controllers;
		population = new OptionsResult("--population ");
		environment = new OptionsResult("--environment ");
		executor = new OptionsResult("--executor ");
		evolution = new OptionsResult("--evolution ");
		evaluation = new OptionsResult("--evaluation ");
		random_seed = "--random-seed ";
	}
	
	public String getOutput() {
		return output;
	}
	
	public void setOutput(String output) {
		this.output = output;
	}
	
	public boolean isOutputFilled(){
		return !output.equals("--output ");
	}

	public RobotsResult getRobots() {
		return robots;
	}
	
	public void setRobots(RobotsResult robots) {
		this.robots = robots;
	}

	public ControllersResult getControllers() {
		return controllers;
	}

	public void setControllers(ControllersResult controllers) {
		this.controllers = controllers;
	}
	
	public OptionsResult getPopulation() {
		return population;
	}
	
	public void setPopulation(OptionsResult population) {
		this.population = population;
	}
		
	public OptionsResult getEnvironment() {
		return environment;
	}
	
	public void setEnvironment(OptionsResult environment) {
		this.environment = environment;
	}
	
	public OptionsResult getExecutor() {
		return executor;
	}
	
	public void setExecutor(OptionsResult executor) {
		this.executor = executor;
	}
	
	public OptionsResult getEvolution() {
		return evolution;
	}
	
	public void setEvolution(OptionsResult evolution) {
		this.evolution = evolution;
	}
	
	public OptionsResult getEvaluation() {
		return evaluation;
	}
	
	public void setEvaluation(OptionsResult evaluation) {
		this.evaluation = evaluation;
	}
	
	public String getRandom_seed() {
		return random_seed;
	}

	public void setRandomSeed(String random_seed) {
		this.random_seed = random_seed;
	}
	
	public boolean isRandomSeedFilled(){
		return !random_seed.equals("--random-seed ");
	}
	
	public boolean isConfigurationFileComplete(){
		System.out.println(isOutputFilled());
		System.out.println(robots.isFilled());
		System.out.println(controllers.isFilled());
		System.out.println(population.isFilled());
		System.out.println(environment.isFilled());
		System.out.println(executor.isFilled());
		System.out.println(evolution.isFilled());
		System.out.println(evaluation.isFilled());
		System.out.println(isRandomSeedFilled());
		System.out.println("---");
		return isOutputFilled() && robots.isFilled() && controllers.isFilled() 
				&& population.isFilled() && environment.isFilled() && executor.isFilled() 
				&& evolution.isFilled() && evaluation.isFilled() && isRandomSeedFilled();
	}
	
	public String getResult(){
		return output + "\n\n" + robots + "\n\n" + controllers + "\n\n"
				+ population + "\n\n" + environment + "\n\n" + executor
				+ "\n\n" + evolution + "\n\n" + evaluation + "\n\n"
				+ random_seed;
	}
	
}
