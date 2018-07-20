package environment;

import physicalobjects.WallWithZ;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class EmptyEnvWithWalls extends EmptyEnviromentsWithFixPositions{
	
	public EmptyEnvWithWalls(Simulator simulator, Arguments arguments) {
		super(simulator, arguments);
		putWalls=true;
		
	}
	
	protected void addWalls() {
		if(width%2==0){
		super.addWalls();
		
		for(int i=0;i<width; i++){
			addStaticObject(new WallWithZ(simulator, 0, width / 2 + i*-1,
					width, wallsDensity, 10)); // HorizontalWallNorth
			addStaticObject(new WallWithZ(simulator, height / 2 +i*-1, 0,
					wallsDensity, height, 10)); // VerticalEast
		}
		}else{
			super.addWalls();

			width=width+1;
			height=height+1;
			super.addWalls();
			for(int i=0;i<width; i++){
				addStaticObject(new WallWithZ(simulator, 0, width / 2 +0.2+ i*-1,
						width, wallsDensity, 10)); // HorizontalWallNorth
				addStaticObject(new WallWithZ(simulator, height / 2 +i*-1, 0,
						wallsDensity, height, 10)); // VerticalEast
			}
			
			
		}
			int more=0;
			for(int a=0; a<width; a++){
				for (int j=0;j<height; j++){
					double x=width/2-0.5 +a*-1;
					double y=height/2-0.5+j*-1;
					//System.out.println(x);
					if((int)(j+a*(width))<=35){
						robots.get((int)(j+a*(width))).setPosition(x,y);
					}
				}
				more++;
			}
			robots.get(0).setPosition(1+0.5,1+0.5);
			robots.get(3).setPosition(2+0.7,0);
			robots.get(37).setPosition(1+0.7,0);
			robots.get(38).setPosition(3+0.7,0);



	}



	
	

}
