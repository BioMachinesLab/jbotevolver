package evaluationfunctions;

import java.awt.Color;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import mathutils.Vector2d;
import sensors.DistanceToASensor;
import sensors.DistanceToBSensor;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import environment.OpenEnvironment;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class CoveredAreaEvaluationFunction9 extends EvaluationFunction{

	private OpenEnvironment env;
	private int numberOfSteps;
	private Double timestep;
	private double penalty = 0;
	private int drawImage = 0;

	public CoveredAreaEvaluationFunction9(Arguments args) {
		super(args);
		drawImage = args.getArgumentIsDefined("drawimage") ? args.getArgumentAsInt("drawimage") : 0;
	}


	@Override
	public void update(Simulator simulator) {
		timestep = simulator.getTime();
		//System.out.println(timestep);
		env = (OpenEnvironment) simulator.getEnvironment();
		numberOfSteps = env.getSteps();
		
		if(!env.isConnected())
			penalty+=30;
	}

	@Override
	public double getFitness() {

		if(timestep == numberOfSteps - 1){

			BufferedImage map = new BufferedImage(1000, 1000, BufferedImage.TYPE_BYTE_BINARY);
			map.getGraphics().setColor(Color.WHITE);
			
			

			//mapping robots coordinates to image coordinates: (0,0) = (500,500)
			int factor = 10; // 1 meter = 100 px
			int center = 500;

			for(Robot r: env.getRobots()){
				if(!r.getDescription().equals("prey")){
					double radius = 0;
					if(r.getDescription().equals("type0"))
						radius = ((DistanceToBSensor) r.getSensorByType(DistanceToBSensor.class)).getRange() / 2; // sensory range = 1/2 communication range
					else if(r.getDescription().equals("type1"))
						radius = ((DistanceToASensor) r.getSensorByType(DistanceToASensor.class)).getRange() / 2;

					Vector2d position = toUpperLeftCorner(r.getPosition(), radius);
					Vector2d pixels = toPixels(position, factor, center);

					int x = (int)(Math.round(pixels.x));
					int y = (int)(Math.round(pixels.y));
					int width = (int)(radius*factor*2);
					int height = (int)(radius*factor*2);
					
					if(x < 1000 && x >= 0 && y >= 0 && y < 1000 && (x+width) < 1000 && (x+width) >= 0 && (y+height) < 1000 && (y+height) >= 0)
						map.getGraphics().fillOval(x, y, width, height);


				}
			}
			
//			if(drawImage == 1){
//				JFrame frame = new JFrame();
//				frame.getContentPane().add(new JLabel(new ImageIcon(map)));
//				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//				frame.setVisible(true);		
//			}

			int fitness = 0;

			for(int y = 0; y < map.getHeight(); y++){
				for(int x = 0; x < map.getWidth(); x++){
					Color color = new Color(map.getRGB(x, y));
					if(color.equals(Color.WHITE))
						fitness++;
				}
			}

			
//			System.out.println("Fitness: " + fitness);
//			System.out.println("Penalty: " + penalty);
//			System.out.println((fitness-penalty)/10000);
				
			return (fitness-penalty)/10000.0;

		} else {
			return 0;
		}
	}

	private Vector2d toUpperLeftCorner(Vector2d center, double radius){
		return new Vector2d(center.x - radius, center.y + radius);
	}

	private Vector2d toPixels(Vector2d position, int factor, int center){
		return new Vector2d(center + (position.x*factor), center - (position.y*factor));
	}

}
