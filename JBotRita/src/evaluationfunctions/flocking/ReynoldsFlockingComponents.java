package evaluationfunctions.flocking;


import java.util.LinkedList;

import evaluationfunctions.flocking.metrics.NG_OneDividedByGroups;
import evaluationfunctions.flocking.metrics.NG_PercentageOfFragmentation;
import evaluationfunctions.flocking.metrics.NumberOfGroups;
import mathutils.Vector2d;
import simulation.environment.Environment;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class ReynoldsFlockingComponents extends ReynoldsFlocking {
	LinkedList<Integer> positionsOccupied = new LinkedList<Integer>();
	
	@ArgumentsAnnotation(name = "movement", values = { "distanceToCenter", "distanceToPrey", "areaCovarage", "Nothing" })
	protected String movementComponent;

	@ArgumentsAnnotation(name = "cohesion", values = {"degreeOfBelongingToSwarm", "degreeOfDistanceToSwarm", "numberOfGroups","NG_OneDividedByGroups","NG_PercentageOfFragmentation", "Nothing" })
	protected String cohesionComponent;
	
	@ArgumentsAnnotation(name = "separation", values={"Yes","Nothing"})
	protected String separationComponent;
	
	@ArgumentsAnnotation(name = "alignment", values={"Yes","Nothing"})
	protected String alignmentComponent;
	
	private Arguments args;
	
	private NumberOfGroups numberOfGroups;  //extends os outros... enfim...
	
	public ReynoldsFlockingComponents(Arguments args) {
		super(args);
		this.args=args;
		movementComponent = args.getArgumentIsDefined("movement") ? (args
				.getArgumentAsString("movement")) : "distanceToCenter";
		cohesionComponent = args.getArgumentIsDefined("cohesion") ? (args
				.getArgumentAsString("cohesion")) : "degreeOfBelongingToSwarm";
		separationComponent   = args.getArgumentIsDefined("separation") ? (args.getArgumentAsString("separation")) 	: "Yes";
		alignmentComponent   = args.getArgumentIsDefined("alignment") ?(args.getArgumentAsString("alignment")) 	: "Yes";
		
		if(cohesionComponent.equals("numberOfGroups")){
			numberOfGroups=new NumberOfGroups(args);
		}else if(cohesionComponent.equals("NG_OneDividedByGroups")){
			numberOfGroups=new NG_OneDividedByGroups(args);
		}else if(cohesionComponent.equals("NG_PercentageOfFragmentation")){
			numberOfGroups=new NG_PercentageOfFragmentation(args);
		}
	}
	
	@Override
	protected void init(){
		super.init();
		if(numberOfGroups instanceof NumberOfGroups){
			numberOfGroups.init();
		}
	}
	
	@Override
	protected void separation(Robot robot){ 
		if(separationComponent.equals("Yes")){
			super.separation(robot);
		}else{  //separation="nothing"
			//don't consider this component
		}
	}
	
	@Override
	protected void alignment(Robot robot){
		if(alignmentComponent.equals("Yes")){
			super.alignment(robot);
		}else{  //alignment="nothing"
			//don't consider this component
		}
	}
	
	@Override
	protected void cohesion(int i, Robot r){
		if(cohesionComponent.equals("degreeOfBelongingToSwarm")){
			super.cohesion(i, r);
			
		}else if(cohesionComponent.equals("degreeOfDistanceToSwarm")){
			
			for(int j = i+1 ; j < robots.size() ; j++) {  
				double percentageOfDistance=1-( r.getPosition().distanceTo(robots.get(j).getPosition()))
						/ cohensionDistance;
				if(percentageOfDistance < 0){
					percentageOfDistance=0;
				}
				cohension+=percentageOfDistance ;
				numberOfRobotsForAvarage++;
			}
		}else if(numberOfGroups instanceof NumberOfGroups ){
			if(!numberOfGroups.isAlreadyACohesiveGroup(r.getId(), robots.size())){
				numberOfGroups.computeGroupsPairs(r.getId(),  robots ,  i,  robots.size());
				numberOfGroups.checkRobotIsAlreadyInAGroup(r.getId());
			}	
		}else{ //cohension="nothing"
			//don't consider this component
		}
	}
	
	@Override
	protected void computeFitnessForCohesion(){
		if(numberOfGroups instanceof NumberOfGroups ){

			fitnessForCohesion=numberOfGroups.getCurrentFitness(robots);
		}
	}

	
	@Override
	protected void movement(Robot robot) {
		if(movementComponent.equals( "distanceToCenter")){
			super.movement(robot);

		}else if(movementComponent.equals( "distanceToPrey")){
			
			Vector2d preyPosition = simulator.getEnvironment().getPrey().get(0).getPosition();
			Vector2d nest = new Vector2d(0, 0);
			double initialDistanceToPrey = preyPosition.distanceTo(nest);
			movementContribution += (1 - (robot.getDistanceBetween(preyPosition) / initialDistanceToPrey));
			
		}else if(movementComponent.equals("areaCovarage")){
			int xPos=(int) (robot.getPosition().x);
			int yPos=(int) (robot.getPosition().y);
			if(robot.getPosition().x<0)
				xPos=xPos -1;
			if(robot.getPosition().y<0)
				yPos= yPos-1;
						
			int width=(int) simulator.getEnvironment().getWidth();
			int height=(int) simulator.getEnvironment().getHeight();
			
			xPos=convertPosToGridPosition(xPos,width,1,width);
			yPos=convertPosToGridPosition(yPos,height,0,height-1);

			int posOfGrid=xPos+height*yPos;
		
			if(!positionsOccupied.contains(posOfGrid)  ){
				if(posOfGrid>0 && posOfGrid<=width*height)  //pos needs to be > than minGridPosition and < that maxPos
					positionsOccupied.add(posOfGrid);
			}
			
			Environment env=simulator.getEnvironment();
			
			fitnessForMovement=positionsOccupied.size()/(double)(env.getHeight()*env.getWidth());
			
		}else{ //nothing
			//don't consider this component
		}
	}
	
	protected int normalizePos(int value, int maxValue, int minValue, int newMinValue, int newMaxValue){
		return (value-minValue)*(newMaxValue-newMinValue)/(maxValue-minValue)+newMinValue;
	}
	
	protected int convertPosToGridPosition(int pos, int widthOrHeight, int newMinValue, int newMaxValue){
		if(widthOrHeight%2==0){ //even
			pos=normalizePos(pos, (int) (widthOrHeight/2)-1,-(int)(widthOrHeight/2), newMinValue,newMaxValue );
		}else{//odd
			pos=normalizePos(pos, (int) (widthOrHeight/2+1),-(int)(widthOrHeight/2+1), newMinValue,newMaxValue+1 );
		}
		return pos;
	}
	
	
	
	

}
