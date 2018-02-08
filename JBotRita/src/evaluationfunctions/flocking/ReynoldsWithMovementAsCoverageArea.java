package evaluationfunctions.flocking;

import java.util.LinkedList;
import simulation.environment.Environment;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class ReynoldsWithMovementAsCoverageArea extends ReynoldsFlocking {
	
	LinkedList<Integer> positionsOccupied = new LinkedList<Integer>();
	
	@Override
	public double getFitness() {
		Environment env=simulator.getEnvironment();
		fitnessForMovement=positionsOccupied.size()/(double)(env.getHeight()*env.getWidth());
		return super.getFitness();
	}
	
	public ReynoldsWithMovementAsCoverageArea(Arguments args) {
		super(args);
	}
	
	@Override
	protected void movement(Robot r){
					
			int xPos=(int) (r.getPosition().x);
			int yPos=(int) (r.getPosition().y);
			if(r.getPosition().x<0)
				xPos=xPos -1;
			if(r.getPosition().y<0)
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
