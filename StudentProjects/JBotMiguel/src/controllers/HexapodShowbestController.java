package controllers;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import coppelia.CharWA;
import coppelia.FloatWA;
import coppelia.remoteApi;
import evaluationfunctions.DummyEvaluationFunction;

public class HexapodShowbestController extends Controller implements FixedLenghtGenomeEvolvableController{

	
	protected float[] parameters;
	protected boolean init = false;
	protected double[] weights;
	protected int genomeLength;
	protected int inputs;
	protected int outputs;
	protected int controllerType = 0;//0=MAPElites,1=RepertoireNEAT, 2=NEAT, 3=Dummy
	protected Simulator sim;
	protected String ip = "127.0.0.1";
	protected int time = 3;
	
	private static remoteApi vrep;
	private static int clientId;
	
	public HexapodShowbestController(Simulator simulator,Robot robot,Arguments args) {
		super(simulator, robot, args);
		
		if(!args.getArgumentIsDefined("inputs") || !args.getArgumentIsDefined("outputs"))
			throw new RuntimeException("Argument 'inputs' not defined for class HexapodShowbestController!");
		
		ip = args.getArgumentAsStringOrSetDefault("ip", ip);
		
		controllerType= args.getArgumentAsIntOrSetDefault("controllertype", controllerType);
		
//		if(!args.getArgumentIsDefined("time"))
//			throw new RuntimeException("Argument 'time' not defined for class HexapodShowbestController!");
		
		time= args.getArgumentAsIntOrSetDefault("time", time);
		
		if(args.getArgumentIsDefined("weights")) {
			String[] rawArray = args.getArgumentAsString("weights").split(",");
			double[] weights = new double[rawArray.length];
			for(int i = 0 ; i < weights.length ; i++)
				weights[i] = Double.parseDouble(rawArray[i]);
			setNNWeights(weights);
		}
		this.sim = simulator;
	}
	
	public void init() {
		if(vrep == null) {
			vrep = new remoteApi();
			vrep.simxFinish(-1); // just in case, close all opened connections
			clientId = vrep.simxStart(ip,19996,true,false,5000,5);
		}
	}
	
	@Override
	public void end() {
//		vrep.simxStopSimulation(clientId,vrep.simx_opmode_blocking);
	}
	
	@Override
	public void controlStep(double time) {
		
		if(!init) {
			init();
			init = true;
			System.out.println("Sent!");
			sendDataToVREP(parameters);
			float[] data = getDataFromVREP();
			double fit = getFitness(data);
			
			if(!sim.getCallbacks().isEmpty() && sim.getCallbacks().get(0) instanceof DummyEvaluationFunction) {
				DummyEvaluationFunction eval = (DummyEvaluationFunction)sim.getCallbacks().get(0);
				eval.setFitness(fit);
			}else{
				System.out.println("Fitness: "+fit);
			}
			
			System.out.println("Received!");
		}
	}
	
	protected double getFitness(float[] vals) {
		int index = 0;
		
		int nResults = (int)vals[index++];
		
		//id
		int id = (int)vals[index++];
		//number of values
		int nVals = (int)vals[index++];
		
		float fitness = 0;
		
		if(controllerType == 0) {
			float x = vals[index++];
			float y = vals[index++];
			float z = vals[index++];
			float a = vals[index++];
			float b = vals[index++];
			float g = vals[index++];
			float distanceTravelled = vals[index++];
			float minTilt = vals[index++];
			fitness = minTilt;
			
			double orY = (Math.cos(a)*Math.cos(g) - Math.sin(a)*Math.sin(b)*Math.sin(g));
			double orX = - Math.cos(b)*Math.sin(g);
			double orZ = (Math.cos(g)*Math.sin(a) - Math.cos(a)*Math.sin(b)*Math.sin(b));
			
			double orientation = Math.atan2(orY,orX);
			
			System.out.println("Position: "+x+","+y);
			
			robot.setPosition(new Vector2d(x,y));
			robot.setOrientation(orientation);
			
		}else{
			fitness = vals[index++];
		}

		return fitness;
	}
	
	@Override
	public void setNNWeights(double[] weights) {
		this.weights = weights;
		this.genomeLength = weights.length;
		
		int parametersIndex = 0;
		
		this.parameters = new float[6+weights.length];
		
		this.parameters[parametersIndex++] = 1;//1 fixed parameters
		this.parameters[parametersIndex++] = time;//X seconds
		this.parameters[parametersIndex++] = 1;//1 individual
		this.parameters[parametersIndex++] = 1;//id
		this.parameters[parametersIndex++] = genomeLength+1;//size of type+genome
		this.parameters[parametersIndex++] = controllerType;
		System.out.println(controllerType);
		
		for(int i = 0 ; i < weights.length ; i++)
			this.parameters[parametersIndex++] = (float)weights[i];
		
	}

	@Override
	public int getGenomeLength() {
		return genomeLength;
	}

	@Override
	public double[] getNNWeights() {
		return weights;
	}

	@Override
	public int getNumberOfInputs() {
		return inputs;
	}

	@Override
	public int getNumberOfOutputs() {
		return getGenomeLength();
	}
	
	protected float[] getDataFromVREP() {
    	CharWA str=new CharWA(0);
    	while(vrep.simxGetStringSignal(clientId,"toClient",str,remoteApi.simx_opmode_oneshot_wait) != remoteApi.simx_return_ok) {
    		try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
    	
		vrep.simxClearStringSignal(clientId, "toClient", remoteApi.simx_opmode_oneshot);
		 
		FloatWA f = new FloatWA(0);
		f.initArrayFromCharArray(str.getArray());
		return f.getArray();
    }
    
    protected void sendDataToVREP(float[] arr) {
    	FloatWA f = new FloatWA(arr.length);
    	f.setValue(arr);
    	char[] chars = f.getCharArrayFromArray();
    	String tempStr = new String(chars);
		CharWA str = new CharWA(tempStr);
		vrep.simxWriteStringStream(clientId,"fromClient",str,remoteApi.simx_opmode_oneshot);
    }
}
