package vrep;

public abstract class VRepController {
	
	protected float[] parameters;
	protected double time = 0;
	
	public VRepController(float[] parameters) {
		this.parameters = parameters;
	}
	
	public abstract float[] controlStep(float[] inputs);
	
	public static double[] floatToDouble(float[] f) {
		double[] res = new double[f.length];
		for(int i = 0 ; i < res.length ; i++)
			res[i] = (double)f[i];
		return res;
	}
	
	public static float[] doubleToFloat(double[] d) {
		float[] res = new float[d.length];
		for(int i = 0 ; i < res.length ; i++)
			res[i] = (float)d[i];
		return res;
	}

}
