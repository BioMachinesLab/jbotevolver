package enviromentsSwarms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import simulation.Simulator;
import simulation.environment.Environment;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class EmptyEnviromentsWithFixPositions extends Environment{

	private double forageLimit;
	private double forbiddenArea;
	//private double distance;
	
	public EmptyEnviromentsWithFixPositions(Simulator simulator, Arguments arguments) {
		super(simulator, arguments);
		//distance =2;
	}
	
	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		
		
			for(Robot r : robots) {
//				double x = simulator.getRandom().nextDouble()*distance*2-distance;
//				double y = simulator.getRandom().nextDouble()*distance*2-distance;
//				r.setPosition(x, y);
				r.setOrientation(simulator.getRandom().nextDouble()*Math.PI*2);
				//System.out.println("radius"+r.getOrientation());
			}
		
//			robots.get(0).setPosition(0,0);
//			robots.get(1).setPosition(-2,0);
//			robots.get(2).setPosition(2,0);
//			robots.get(3).setPosition(4,5);
//			robots.get(4).setPosition(-5,5);
//			robots.get(1).setOrientation(simulator.getRandom().nextDouble()*Math.PI*2);
//			robots.get(2).setOrientation(simulator.getRandom().nextDouble()*Math.PI*2);

			//robots.get(4).setPosition(0,1);


	}
	
	public double getForageRadius() {
		return forageLimit;
	}

	public double getForbiddenArea() {
		return forbiddenArea;
	}
	
	@Override
	public void update(double time) {
		
	}
	
	public List<List<Robot>> getClusters(Collection<Robot> robots) {
        List<List<Robot>> clusters = new ArrayList<>();
        // One cluster for each robot
        for (Robot r : robots) {
            List<Robot> cluster = new ArrayList<>();
            cluster.add(r);
            clusters.add(cluster);
        }

        boolean merged = true;
        // stop when the clusters cannot be merged anymore
        
        while(merged) {
            merged = false;
            // find two existing clusters to merge
            for (int i = 0; i < clusters.size() && !merged; i++) {
                for (int j = i + 1; j < clusters.size() && !merged; j++) {
                    List<Robot> c1 = clusters.get(i);
                    List<Robot> c2 = clusters.get(j);
                    // check if the two clusters have (at least) one individual close to the other
                    for (int ri = 0; ri < c1.size() && !merged; ri++) {
                        for (int rj = 0; rj < c2.size() && !merged; rj++) {
                            Robot r1 = c1.get(ri);
                            Robot r2 = c2.get(rj);
                            
                            if (r1.getPosition().distanceTo(r2.getPosition()) - r1.getRadius() - r2.getRadius() <= 0.5) {
	                            // do the merge
	                            merged = true;
	                            clusters.get(i).addAll(clusters.get(j));
	                            clusters.remove(j);
                            }
                        }
                    }
                }
            }
        }
       //System.out.println(clusters.size()+"migue");
        return clusters;
    }
}
	
