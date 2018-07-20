package evaluationfunctions;

import java.util.LinkedList;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.robot.Robot;
import simulation.util.Arguments;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class AreaCoverage extends EvaluationFunction {

		private Simulator simulator;
		LinkedList<Integer> positionsOccupied = new LinkedList<Integer>();
		private int numberCollisions;

		
		public AreaCoverage(Arguments args) {
			super(args);
		}
		
		@Override
		public double getFitness() {
				Environment env=simulator.getEnvironment();
				double max_PossibleNumberOfCollisions=simulator.getTime()*env.getRobots().size();
				double penalty_for_collision= -numberCollisions/max_PossibleNumberOfCollisions;
				return positionsOccupied.size()/(double)(env.getHeight()*env.getWidth())/+ penalty_for_collision; 
		}
		
		@Override
		public void update(Simulator simulator) { 
			this.simulator=simulator;
			for(Robot r : simulator.getEnvironment().getRobots()){
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
				
				if(r.isInvolvedInCollison()){
					numberCollisions++;
				}
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