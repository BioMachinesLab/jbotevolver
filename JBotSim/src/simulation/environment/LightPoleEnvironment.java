package simulation.environment;

import gui.renderer.Renderer;
import java.util.ArrayList;

import mathutils.Vector2d;

import simulation.Simulator;
import simulation.physicalobjects.LightPole;
import simulation.util.Arguments;

/**
 * Class that defines a lightpole environment
 * @author Andre Bastos
 */
public class LightPoleEnvironment extends Environment {


	private double lightPoleRadius;
	private double lightPoleDistance;
	private double forageLimit, forbiddenArea;
	private int currentSample;
	private int maxNumberRobots;
    private int lightPoleNumber;
    String randomPoles;
    private ArrayList<LightPole> poles;

    /**
     * Constructs a lightpole environment
     *
     * @param arguments
     */
	public LightPoleEnvironment(Simulator simulator, Arguments arguments){
		super(simulator, arguments.getArgumentIsDefined("forbiddenarea") ? arguments.getArgumentAsDouble("forbiddenarea")       : 5.0,
			  arguments.getArgumentIsDefined("forbiddenarea") ? arguments.getArgumentAsDouble("forbiddenarea") : 5.0);
        lightPoleDistance = arguments.getArgumentIsDefined("lightpoledistance") ? arguments.getArgumentAsDouble("lightpoledistance"): 2.0;
        lightPoleRadius = arguments.getArgumentIsDefined("lightpoleradius") ? arguments.getArgumentAsDouble("lightpoleradius") : 0.10;
        lightPoleNumber = arguments.getArgumentIsDefined("lightPoleNumber") ? arguments.getArgumentAsInt("lightPoleNumber") : 0;
        String vary = arguments.getArgumentIsDefined("varyexperiments") ? arguments.getArgumentAsString("varyexperiments") : "false";
        currentSample = arguments.getArgumentIsDefined("fitnesssample") ? arguments.getArgumentAsInt("fitnesssample")	: 0;
        randomPoles = arguments.getArgumentIsDefined("israndom") ? arguments.getArgumentAsString("israndom") : "false";
        if(vary.equalsIgnoreCase("true")){
        	
        	poles = createMultipleEnvironments(lightPoleRadius);
        }
        else{
        	if(randomPoles.equalsIgnoreCase("true")){
        		poles = createPoles(getRandomPositions(lightPoleNumber), lightPoleRadius);
        	}
        	else
        		poles = createPoles(getPositions(lightPoleNumber), lightPoleRadius);
        }
        currentSample = arguments.getArgumentIsDefined("fitnesssample") ? arguments.getArgumentAsInt("fitnesssample")	: 0;
        
     
                    
        for(LightPole p: poles)
            addObject(p);

	}
	
	
        /**
         * Generates the poles
         * @param list, the positions list of the poles
         * @param radius
         * @return list of poles
         */
        private ArrayList<LightPole> createPoles(ArrayList<Vector2d> list, double radius){
            LightPole light;
            ArrayList<LightPole> polesToAdd = new ArrayList<LightPole>();
            for(int i = 0; i<list.size(); i++){
                light = new LightPole(simulator, "pole "+Integer.toString(i), list.get(i).x, list.get(i).y, radius);
                polesToAdd.add(light);
               
            }
            return polesToAdd;
        }
        
        private ArrayList<LightPole> createMultipleEnvironments(double radius){
        	LightPole light;
        	ArrayList<LightPole> polesToAdd = new ArrayList<LightPole>();
        	ArrayList<Vector2d> positions;
    		switch(currentSample%4){
    		case 0: lightPoleNumber = 0;break;
    		case 1: lightPoleNumber = 1;break;
    		case 2: lightPoleNumber = 2;break;
    		case 3: lightPoleNumber = 5;break;
    		}
    		if(randomPoles.equalsIgnoreCase("true")){
    			 positions = getRandomPositions(lightPoleNumber);
    		}
    		else{
    			 positions = getPositions(lightPoleNumber);
    		}
        	for(int i=0; i<positions.size();i++){
        		light = new LightPole(simulator, "pole "+Integer.toString(i), positions.get(i).x, positions.get(i).y, radius);
                polesToAdd.add(light);
               
               
        	}
        	return polesToAdd;
        }

        /**
         * Calculates the position of the poles
         * @param numberOfPoles
         * @return positions
         */
        private ArrayList<Vector2d> getPositions(int numberOfPoles){
            ArrayList<Vector2d> list = new ArrayList<Vector2d>();
            if(numberOfPoles == 1){
                list.add(new Vector2d(0.0, 0.0));
            }
            else{
	            for (int i = 1; i<= numberOfPoles; i++){
	            	list.add(new Vector2d(lightPoleDistance*Math.cos(2*(Math.PI)*i/numberOfPoles), 
	            			lightPoleDistance*Math.sin(2*(Math.PI)*i/numberOfPoles)));
	         
	            }
            
            }
            return list;
        }
        
       
        private ArrayList<Vector2d> getRandomPositions(int numberOfPoles){
        	
        	
        	ArrayList<Vector2d> list = new ArrayList<Vector2d>();
        	do{
        		list.clear();
	        	for(int i=0; i<numberOfPoles; i++){
	        		
	        		list.add(new Vector2d(this.getWidth()  * (simulator.getRandom().nextDouble() - 0.5),
	        				this.getHeight() * (simulator.getRandom().nextDouble() - 0.5)));
	        		
	        		
	        	}
        	}while(!isValid(list));
        	
        	return list;
        
        }
        
        private boolean isValid(ArrayList<Vector2d> list){
        	
        	boolean valid = true;
        	for(int j=0; j<list.size()-1; j++){
        		for(int k=j+1; k<list.size(); k++){
        			if(list.get(j).distanceTo(list.get(k))<=lightPoleRadius*2){
        				valid=false;
        				
        			}
        		}
        	}
        
        	return valid;
        }
        
	public double getLightPolesRadius(){
		return lightPoleRadius;
	}

	public double getLightPoleDistance(){
		return lightPoleDistance;
	}

	public double getForageRadius() {
		return forageLimit;
	}

	public double getForbiddenArea() {
		return forbiddenArea;
	}
	
	public ArrayList<LightPole> getPoles(){
		return poles;
	}

	@Override
	public void draw(Renderer renderer){
            for(LightPole p: poles){
                renderer.drawCircle(p.getPosition(), p.getRadius());
            }
	}

	

}
