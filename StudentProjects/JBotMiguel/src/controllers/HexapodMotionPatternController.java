package controllers;

import coppelia.CharWA;
import coppelia.FloatWA;
import coppelia.IntW;
import coppelia.remoteApi;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class HexapodMotionPatternController extends Controller implements FixedLenghtGenomeEvolvableController{

	private static String ROBOT_NAME = "hexapod";
	private static String ROBOT_BODY_NAME = "hexa_body";
	private static String JOINT_NAME = "hexa_joint";
	private static float initialOrientation[] = new float[]{0,-(float)Math.PI,0};
	private static float initialPosition[] = new float[]{0,0,0.087999955f};
	private static int[][] jointHandles = new int[6][3];
	private static int robotHandle = 0;
	private static int robotBodyHandle = 0;
	private static boolean gotHandles = false;
	
	private remoteApi vrep;
	private int clientId;
	
	protected final int legs = 6;
	protected final int jointsPerLeg = 3;
	
	protected double[] weights;
	protected float[] parameters;
	protected int genomeLength;
	
	private float[][] jointPositions = new float[6][3];

	private boolean init = false;
	
    private static float[] ori_original = new float[3];
	
	public HexapodMotionPatternController(Simulator simulator,Robot robot,Arguments args) {
		super(simulator, robot, args);
		genomeLength = 24;
	}
	
	public void init() {
		
//		System.out.println("INIT");
		vrep = new remoteApi();
		vrep.simxFinish(-1); // just in case, close all opened connections
		clientId = vrep.simxStart("127.0.0.1",19997,true,false,5000,5);
		vrep.simxSynchronous(clientId,true);
		vrep.simxStartSimulation(clientId,vrep.simx_opmode_oneshot);
	
		if(!gotHandles) {
	
			//all 6 arms
			for(int arm = 1 ; arm <= 6 ; arm++) {
				for(int joint = 1 ; joint <= 3 ; joint++) {
					String name = JOINT_NAME;
					String jointName = ""+joint;
					String armName = "_"+arm;
					name+= jointName+armName;
					IntW handle = new IntW(0);
					vrep.simxGetObjectHandle(clientId, name, handle, remoteApi.simx_opmode_blocking);
					jointHandles[arm-1][joint-1] = handle.getValue();
				}
			}
			
			//the robot
			IntW handle = new IntW(0);
			vrep.simxGetObjectHandle(clientId, ROBOT_NAME, handle, remoteApi.simx_opmode_blocking);
			robotHandle = handle.getValue();
			vrep.simxGetObjectHandle(clientId, ROBOT_BODY_NAME, handle, remoteApi.simx_opmode_blocking);
			robotBodyHandle = handle.getValue();
			gotHandles = true;
			
			FloatWA angles = new FloatWA(3);
			vrep.simxGetObjectOrientation(clientId,robotBodyHandle,remoteApi.sim_handle_parent,angles,remoteApi.simx_opmode_blocking);
			ori_original = angles.getArray();
		}
		setDefaultJoints(true);
	}
	
	@Override
	public void end() {
		vrep.simxStopSimulation(clientId,vrep.simx_opmode_blocking);
	}
	
	@Override
	public void controlStep(double time) {
		
		if(!init) {
			init();
			init = true;
		}
		
		actuateRobot(parameters, (float)time/20f);
		sendAllActuations();
		
		float[] pos = getPosition();
		robot.setPosition(pos[0], pos[1]);
		
		double theta = getOrientation();
		robot.setOrientation(theta);
		
		vrep.simxSynchronousTrigger(clientId);
	}
	
	public void sendAllActuations() {
		vrep.simxPauseCommunication(clientId,true);
		
		for(int arm = 0 ; arm < 6 ; arm++) {
			for(int joint = 0 ; joint < 3 ; joint++) {
				vrep.simxSetJointTargetPosition(clientId, jointHandles[arm][joint], jointPositions[arm][joint], remoteApi.simx_opmode_streaming);
			}
		}
		
		vrep.simxPauseCommunication(clientId,false);
	}
	
	private float actuationFunction(float phi, float t) {
		return (float)(Math.tanh(4f*Math.sin(2f*Math.PI*(t+phi))));
	}
	
	public void actuateRobot(float[] parameters, float t) {

		int nParamsPerArm = 4;
		for(int arm = 0 ; arm < 6 ; arm++) {
			
			float alpha1 = parameters[arm*nParamsPerArm+0];
			float phi1 = parameters[arm*nParamsPerArm+1];
			
			float alpha2 = parameters[arm*nParamsPerArm+2];
			float phi2 = parameters[arm*nParamsPerArm+3];
			
			float val1 = (float)(alpha1*actuationFunction(phi1,t));
			float val2 = (float)(alpha2*actuationFunction(phi2,t));
			float val3 = (float)(-val2+Math.PI/2);
			
			setJointPosition(arm, 0, val1, false);
			setJointPosition(arm, 1, val2, false);
			setJointPosition(arm, 2, val3, false);
		}
	}
	
	public void setDefaultJoints(boolean sendToVrep) {
		actuateRobot(parameters, 0);
		if(sendToVrep) {
			sendAllActuations();
		}
//		for(int joint = 0 ; joint < 3 ; joint++) {
//			
//			float val = (float)Math.toRadians(0);
//			
//			if(joint == 1) val = (float)Math.toRadians(-30);
//			if(joint == 2) val = (float)Math.toRadians(90);
//			
//			for(int arm = 0 ; arm < 6 ; arm++)
//				setJointPosition(arm,joint,val,false);
//		}
	}
	
	private float[] getPosition() {
		FloatWA pos = new FloatWA(0);
		vrep.simxGetObjectPosition(clientId, robotHandle, -1, pos, remoteApi.simx_opmode_streaming);
		return pos.getArray();
	}
	
	private double getOrientation() {
		FloatWA eulerAngles = new FloatWA(0);
		vrep.simxGetObjectOrientation(clientId, robotBodyHandle, -1, eulerAngles, remoteApi.simx_opmode_streaming);
		
		float[] ori = eulerAngles.getArray();
		
		float a = ori[0] - ori_original[0];
        float b = ori[1] - ori_original[1];
        float g = ori[2] - ori_original[2];
        
        double y = -(Math.sin(b)* Math.cos(a)* Math.cos(g) + Math.sin(a) * Math.sin(g));
        double x = (Math.sin(b)* Math.cos(a)* Math.sin(g) - Math.sin(a) * Math.cos(g));
        double z = Math.cos(b)*Math.cos(a);
		
		return Math.atan2(y,x);
	}

	public void setJointPosition(int armNumber, int jointNumber, float angleInRads, boolean sendToVrep) {
		jointPositions[armNumber][jointNumber] = angleInRads;
		
		if(sendToVrep)
			vrep.simxSetJointTargetPosition(clientId, jointHandles[armNumber][jointNumber], angleInRads, remoteApi.simx_opmode_streaming);
	}
	
	@Override
	public void setNNWeights(double[] weights) {
		this.weights = weights;
		this.parameters = new float[weights.length];
		
		for(int i = 0 ; i < weights.length ; i++)
			this.parameters[i] = (float)weights[i];
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
		return 0;
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
