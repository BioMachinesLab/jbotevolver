package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

import mathutils.Vector2d;
import multiobjective.MOChromosome;
import evolution.NDBehaviorMap;

public class NDMapTest {
	
	NDBehaviorMap map;
	int type = 0;
	double[] actuations = new double[3];
	
	public static void main(String[] args) {
		new NDMapTest();
	}
	
	public NDMapTest() {
		
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File("rep_test/repertoire_1868353768.obj")));
			map = (NDBehaviorMap)ois.readObject();
			ois.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		for(double x = 0 ; x <= 1 ; x+=0.01) {
			for(double y = 0 ; y <= 1 ; y+=0.01) {
				for(double z = 0 ; z <= 1 ; z+=0.01) {
					actuations = new double[]{x,y,z};
					int[] bucket = selectBehaviorFromRepertoire();
					double[] vec = map.getValuesFromBucketVector(bucket);
					MOChromosome c = map.getChromosome(vec);
					if(c == null) {
						System.out.println("#####"+x+" "+y+" "+z+" ___ "+bucket[0]+" "+bucket[1]+" "+bucket[2]+" "+new Vector2d(vec[0],vec[1]).length());
					}else
						System.out.println(x+" "+y+" "+z+" ___ "+bucket[0]+" "+bucket[1]+" "+bucket[2]+" "+new Vector2d(vec[0],vec[1]).length());
				}
			}
		}
		
	}
	
	private int[] selectBehaviorFromRepertoire() {
		
		int[] buckets = new int[map.getNDimensions()];

		if(type == 0) {
			//cartesian
			for(int dim = 0 ; dim < map.getNDimensions() ; dim++) {
				
				double limit = map.getLimits()[dim];
				
				//this is selection from the square, not the circle!
				
				if(!map.getCircularDimension(dim)) {//if not circular, we have to check the filling radius
					limit = ((int)map.getCircleRadius())*map.getResolutions()[dim];
				}
				
				double val =  limit*2.0*actuations[dim] - limit;//actuations [0,1]
				buckets[dim] = map.valueToBucket(dim, val);
			}
		} if(type == 1) {
			//n-sphere: https://en.wikipedia.org/wiki/N-sphere#Spherical_coordinates
			
			int nSphere = 0;
			
			for(int i = 0 ; i < map.getNDimensions() ; i++) {
				if(map.getCircularDimension(i))
					break;
				nSphere++;
			}
			
			int actuationIndex = 0;
			
			double r = actuations[actuationIndex++];
			double[] phi = new double[nSphere-1];
			
			for(int i = 0 ; i < phi.length ; i++) {
				if(i == phi.length-1)
					phi[i] = actuations[actuationIndex++]*2*Math.PI;
				else
					phi[i] = actuations[actuationIndex++]*Math.PI;
			}
			
			double[] vals = new double[map.getNDimensions()];
			
			for(int i = 0 ; i < nSphere ; i++) {
				double val = r;
				
				if(i != vals.length-1) {
					for(int j = 0 ; j <= i ; j++) {
						if(j == i) {
							val*=Math.cos(phi[j]);
						} else {
							val*=Math.sin(phi[j]);
						}
					}	
				} else {
					for(int j = 0 ; j < i ; j++) {
						val*=Math.sin(phi[j]);
					}
				}
				vals[i] = val;
				double circleRadius = (int)map.getCircleRadius();//circle radius in buckets
				circleRadius*= map.getResolutions()[i];//circle radius in cartesian coordinates
				vals[i]*= circleRadius*vals[i];
				buckets[i] = map.valueToBucket(i, val);
			}
			
			//regular cartesian conversion for non-circular dimensions
			for(int dim = nSphere ; dim < vals.length ; dim++) {
				double limit = map.getLimits()[dim];
				
				if(!map.getCircularDimension(dim))//if not circular, we have to check the filling radius
					limit = map.getCircleRadius()*map.getResolutions()[dim];
				
				double val =  limit*2*actuations[dim] - limit;//limit*2 * [0,1] - limit
				buckets[dim] = map.valueToBucket(dim, val);
			}
			
		}
		return buckets;
	}

}
