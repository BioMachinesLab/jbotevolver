package src;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;

public class Main {
	
	protected LinkedList<Controller> controllers = new LinkedList<Controller>();
	protected String defaultArgs = "";
	protected HashMap<String,String> globalVariables = new HashMap<String, String>();
	private String folderName = null;
	protected int currentEvolutions = 0;
	protected int maxEvolutions = 10;
	protected boolean testMode = false;
	
	public Main(String[] args) {
		String conf ="";
		if(args.length==0){
			System.err.println("No configuration file provided!");
			System.exit(1);
		}else{
			conf = args[0];
		}
		
		for(int i = 1 ; i < args.length ; i++) {
			String[] current = args[i].split("=");
			if(current[0].equals("test"))
				testMode = current[1].equals("1");
		}
		
		if(!conf.contains(".conf")) {
			System.err.println("Configuration file must end in .conf");
			System.exit(1);
		}
		
		try {
			
			File f = new File(conf);
			Scanner s = new Scanner(f);
			
			while(s.hasNextLine()) {
				
				String line = s.nextLine();
				
				if(line.startsWith("--"))
					line = parseArgs(s,line);

				if(line.startsWith("%"))
					parseVariable(s,line);

				if(line.startsWith("#"))
					parseController(s, line);
				
				if(line.startsWith("/*")) {
					do{
						line = s.nextLine();
					} while(!line.startsWith("*/"));
				}
			}
			
			if(testMode)
				globalVariables.put("%runs", "1");
			
			replaceGlobalVariables();
			
			if(globalVariables.get("%maxevolutions") != null)
				maxEvolutions = Integer.parseInt(globalVariables.get("%maxevolutions"));
			
			folderName = conf.substring(0, conf.indexOf(".conf"));
			File folder = new File(folderName);
			folder.mkdir();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void execute() {
		boolean allEvolved = false;
		
		while(!allEvolved) {
			
			allEvolved = true;
			
			for(Controller c : controllers) {
				allEvolved = allEvolved && c.hasBeenEvolved();
				if(!c.hasBeenEvolved() && c.readyToEvolve() && !c.isEvolving()) {
					System.out.println("Evolving "+c.getName());
					Evolution evo = new Evolution(this,c, defaultArgs);
					evo.start();
				}
			}
			try {
				if(!allEvolved)
					wait();
			}catch(Exception e) {e.printStackTrace();}
		}
	}
	
	public synchronized void evolutionFinished(String controllerName) {
		
		System.out.println("Finished evolving "+controllerName);
		
		for(int i = 0 ; i < controllers.size() ; i++) {
			Controller c = controllers.get(i);
			
			if(!c.readyToEvolve() && !c.hasBeenEvolved()) {
				for(int j = 0 ; j < controllers.size() ; j++) {
					Controller cj = controllers.get(j);
					if(j != i && cj.hasBeenEvolved() && c.needsSubController(cj.getName()))
						c.addSubController(cj);
				}
			}
		}
		
		notifyAll();
	}
	
	private void replaceGlobalVariables() {
		for(String s : globalVariables.keySet())
			defaultArgs = defaultArgs.replaceAll(s, globalVariables.get(s));
		
		for(String s : globalVariables.keySet())
			for(String j : globalVariables.keySet())
				globalVariables.put(j,globalVariables.get(j).replaceAll(s, globalVariables.get(s)));
		
		for(Controller c : controllers) {
			String conf = c.getConfiguration();
			for(String s : globalVariables.keySet())
				conf = conf.replaceAll(s, globalVariables.get(s));
			c.setConfiguration(conf);
		}
	}
	
	private String parseArgs(Scanner s, String line) {
		
		do {
			defaultArgs+=line+"\n";
			
			if(!s.hasNextLine())
				break;
			
			line = s.nextLine();
		} while(!line.startsWith("%") && ! line.startsWith("#") && !line.startsWith("//"));
				
		return line;
	}
	
	private void parseController(Scanner s, String line) {
		
		String controllerName = line.replace("#", "").replace("{", "").trim();
		
		String controllerConfig = "";
		line = s.nextLine();
		
		while(s.hasNextLine() && !line.equals("}")) {
			controllerConfig+=line+"\n";
			line = s.nextLine();
		}
		
		if(testMode && controllerConfig.contains("--population"))
			controllerConfig+="--population +generations=1,size=10\n";
		
		Controller c = new Controller(controllerName,controllerConfig);
		
		controllers.add(c);
	}
	
	private void parseVariable(Scanner s, String line) {
		
		if(line.contains("{")) {
			
			String var = line.replace("{", "").trim();
			String value = "";
			line = s.nextLine();
			
			while(s.hasNextLine() && !line.equals("}")) {
				value+=line+"\n";
				line = s.nextLine();
			}
			
			globalVariables.put(var, value.trim());
			
		} else {		
			String[] split = line.split(":");
			globalVariables.put(split[0], split[1].trim());
		}
	}
	
	public String getFolderName() {
		return folderName;
	}
	
	public String getGlobalVariable(String variable) {
		return globalVariables.get(variable);
	}
	
	public synchronized void decrementEvolutions() {
		currentEvolutions--;
		notifyAll();
	}
	
	public synchronized void incrementEvolutions() {
		while(currentEvolutions >= maxEvolutions) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		currentEvolutions++;
	}
	
	public static void main(String[] args) {
		Main main = new Main(args);
//		Main main = new Main(new String[]{"rudder_final.conf"});
		main.execute();
	}
}