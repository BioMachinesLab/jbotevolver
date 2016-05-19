package vrep;

import java.util.HashMap;

public class ControllerFactory {
	
	private static HashMap<Integer,VRepController> controllers = new HashMap<Integer,VRepController>();
	
	static void loadController(int[] handles, float[] parameters) {
		
		if(parameters.length == 0) {
			throw new RuntimeException("Parameters array is empty!");
		}
		
		int type = (int)parameters[0];
		
		for(int i = 0 ; i < handles.length ; i++) {
			switch(type) {
				case 1:	
					controllers.put(handles[i], new VRepRepertoireController(parameters));
					break;
				case 2:	
					controllers.put(handles[i], new VRepNEATController(parameters));
					break;
				case 3:	
					controllers.put(handles[i], new VRepDummyController(parameters));
					break;
			}
		}
	}
	
	static float[] controlStep(int handle, float[] inputs) {
		
		VRepController c = controllers.get(handle);
		
		if(c != null)
			return c.controlStep(inputs);
		
		return null;
	}
	
	public static void main(String[] args) {
		float params[] = new float[]{1.0f,0.0f,1.0f,-0.4116854f,2.0f,0.32566118f,0.0f,2.0f,0.120362185f,2.0f,0.8353517f,0.0f,3.0f,0.5021217f,2.0f,0.4296625f,0.0f,4.0f,0.5295356f,2.0f,0.16707101f,0.0f,5.0f,0.09013563f,2.0f,0.8273033f,0.0f,6.0f,0.20566483f,2.0f,0.34397236f,0.0f,7.0f,0.11503055f,1.0f,0.696754f,0.0f,8.0f,0.63006186f,1.0f,0.5345056f,1.0f,1.0f,1.0f,7.0f,-0.64483446f,1.0f,1.0f,2.0f,7.0f,-0.67705894f,1.0f,1.0f,3.0f,7.0f,0.08936737f,1.0f,1.0f,4.0f,7.0f,0.54270655f,1.0f,1.0f,5.0f,7.0f,-0.6804634f,1.0f,1.0f,6.0f,7.0f,-0.19802904f,1.0f,1.0f,1.0f,8.0f,-0.9327958f,1.0f,1.0f,2.0f,8.0f,-0.46593764f,1.0f,1.0f,3.0f,8.0f,-0.7250518f,1.0f,1.0f,4.0f,8.0f,-0.41131508f,1.0f,1.0f,5.0f,8.0f,0.6247165f,1.0f,1.0f,6.0f,8.0f,-0.16015263f};
		
		loadController(new int[]{0}, params);
		
		for(int i = 0 ; i < 10 ; i++) {
			controlStep(0, new float[6]);
		}
	}

}
