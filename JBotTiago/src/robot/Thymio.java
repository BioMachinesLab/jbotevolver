package robot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import network.SimulatedBroadcastHandler;
import sensors.ThymioCameraSensor;
import sensors.ThymioIRSensor;
import simulation.Simulator;
import simulation.physicalobjects.PhysicalObject;
import simulation.robot.DifferentialDriveRobot;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;
import actuator.ThymioTwoWheelActuator;
import commoninterface.CIBehavior;
import commoninterface.CISensor;
import commoninterface.ThymioCI;
import commoninterface.entities.Entity;
import commoninterface.mathutils.Vector2d;
import commoninterface.messageproviders.BehaviorMessageProvider;
import commoninterface.messageproviders.EntitiesMessageProvider;
import commoninterface.messageproviders.EntityMessageProvider;
import commoninterface.messageproviders.LogMessageProvider;
import commoninterface.messageproviders.NeuralActivationsMessageProvider;
import commoninterface.network.ConnectionHandler;
import commoninterface.network.broadcast.BroadcastHandler;
import commoninterface.network.broadcast.BroadcastMessage;
import commoninterface.network.broadcast.HeartbeatBroadcastMessage;
import commoninterface.network.broadcast.SharedThymioBroadcastMessage;
import commoninterface.network.messages.Message;
import commoninterface.network.messages.MessageProvider;
import commoninterface.utils.CIArguments;
import commoninterface.utils.RobotLogger;

public class Thymio extends DifferentialDriveRobot implements ThymioCI {

		private Simulator simulator;
		private ArrayList<Entity> entities = new ArrayList<Entity>();
		private ArrayList<CISensor> cisensors = new ArrayList<CISensor>();
		private SimulatedBroadcastHandler broadcastHandler;
		
        private ThymioIRSensor irSensor;
        private ThymioCameraSensor cameraSensor;
        private ThymioTwoWheelActuator wheels;
        
        private Vector2d virtualPosition;
        private Double virtualOrientation;
        
        private ArrayList<MessageProvider> messageProviders;
        private ArrayList<CIBehavior> alwaysActiveBehaviors;
        private CIBehavior activeBehavior;
        
        private RobotLogger logger;
    	
        private double leftPercentage = 0;
    	private double rightPercentage = 0;
        
    	@ArgumentsAnnotation(name="irsenserobot", values={"0","1"})
    	private boolean irSenseRobots;
    	@ArgumentsAnnotation(name="camerasensor", values={"0","1"})
    	private boolean activeCamera;
    	
    	
        public Thymio(Simulator simulator, Arguments args) {
        	super(simulator, args);
        	this.simulator = simulator;
        	
        	ArrayList<BroadcastMessage> broadcastMessages = new ArrayList<BroadcastMessage>();
        	broadcastMessages.add(new HeartbeatBroadcastMessage(this));
        	broadcastMessages.add(new SharedThymioBroadcastMessage(this));
        	
        	broadcastHandler = new SimulatedBroadcastHandler(this, broadcastMessages);
        	alwaysActiveBehaviors = new ArrayList<CIBehavior>();
        	
        	distanceBetweenWheels = 0.099;
        	
        	if(getRadius() < 0.08)
        		throw new RuntimeException("Radius lower than 0.08m");
        	
        	irSenseRobots = args.getFlagIsTrue("irsenserobot");
        	activeCamera = args.getFlagIsTrue("camerasensor");
        	
        	Arguments irSensorsArgs;
        	
        	if(irSenseRobots)
        		irSensorsArgs = new Arguments("senserobot=1,cutoffangle=45,fixedsensor=0,noiseenabled=1,numberofrays=7,offsetnoise=0");	
        	else
        		irSensorsArgs = new Arguments("senserobot=0,cutoffangle=45,fixedsensor=0,noiseenabled=1,numberofrays=7,offsetnoise=0");
        	
        	sensors.add(new ThymioIRSensor(simulator, sensors.size()+1, this, irSensorsArgs));
        	
        	if(activeCamera){
        		Arguments cameraSensorArgs = new Arguments("numbersensors=2,range=0.2,eyes=1,share=1");
        		sensors.add(new ThymioCameraSensor(simulator, sensors.size()+1, this, cameraSensorArgs));
        	}
        	
        	Arguments twoWheelsArgs = new Arguments("speedincrement=0.155");
        	actuators.add(new ThymioTwoWheelActuator(simulator, actuators.size()+1, twoWheelsArgs));
        }
        
		@Override
		public void shutdown() { } 

		@Override
		public void updateSensors(double simulationStep, ArrayList<PhysicalObject> teleported) {
			super.updateSensors(simulationStep, teleported);
			
			for(CIBehavior b : alwaysActiveBehaviors)
				b.step(simulationStep);
			
			if(activeBehavior != null) {
				activeBehavior.step(simulationStep);
			}
		}
		
		@Override
		public void setMotorSpeeds(double leftMotor, double rightMotor) {
			if(wheels == null)
				wheels = (ThymioTwoWheelActuator) getActuatorByType(ThymioTwoWheelActuator.class);
			
			double leftSpeed = leftMotor/2 + 0.5;
			double rightSpeed = rightMotor/2 + 0.5;
			
			leftPercentage = leftSpeed;
			rightPercentage = rightSpeed;
			
			wheels.setLeftWheelSpeed(leftSpeed);
			wheels.setRightWheelSpeed(rightSpeed);
		}
		
		@Override
		public List<Short> getInfraredSensorsReadings() {
			List<Short> readings = new LinkedList<Short>();
			if(irSensor == null)
				irSensor = (ThymioIRSensor)getSensorByType(ThymioIRSensor.class);
				
			readings.add((short)irSensor.getSensorReading(0));
			readings.add((short)irSensor.getSensorReading(1));
			readings.add((short)irSensor.getSensorReading(2));
			readings.add((short)irSensor.getSensorReading(3));
			readings.add((short)irSensor.getSensorReading(4));
			readings.add((short)irSensor.getSensorReading(5));
			readings.add((short)irSensor.getSensorReading(6));
			
			return readings;
		}       
		
		@Override
		public double[] getCameraReadings() {
			double[] readings = new double[2];
				
			if(cameraSensor == null)
				cameraSensor = (ThymioCameraSensor)getSensorByType(ThymioCameraSensor.class);
			
			readings[0] = cameraSensor.getSensorReading(0);
			readings[1] = cameraSensor.getSensorReading(1);
			
			return readings;
		}
		
		@Override
		public double getTimeSinceStart() {
			return simulator.getTime()*10;
		}

		@Override
		public void begin(HashMap<String, CIArguments> args) { }
		
		@Override
		public ArrayList<Entity> getEntities() {
			return entities;
		}

		@Override
		public ArrayList<CISensor> getCISensors() {
			return cisensors;
		}

		@Override
		public String getNetworkAddress() {
			return getId()+":"+getId()+":"+getId()+":"+getId();
		}

		@Override
		public BroadcastHandler getBroadcastHandler() {
			return broadcastHandler;
		}

		public Simulator getSimulator() {
			return simulator;
		}
		
		@Override
		public Vector2d getVirtualPosition() {
			return virtualPosition;
		}
		
		@Override
		public void setVirtualPosition(double x, double y) {
			if(virtualPosition == null)
				virtualPosition = new Vector2d(x, y);
			else
				virtualPosition.set(x, y);
		}

		@Override
		public Double getVirtualOrientation() {
			return virtualOrientation;
		}

		@Override
		public void setVirtualOrientation(double orientation) {
			virtualOrientation = orientation;
		}

		@Override
		public double getThymioRadius() {
			return getRadius();
		}

		@Override
		public String getInitMessages() {
			return "Simulated drone with ID "+getId();
		}
		
		@Override
		public void processInformationRequest(Message request, ConnectionHandler conn) {
			Message response = null;
			
			for (MessageProvider p : getMessageProviders()) {
				response = p.getMessage(request);
				
				if (response != null)
					break;
			}
			
			if(conn != null && response != null) {
				conn.sendData(response);
			}
		}

		@Override
		public void reset() {
			leftWheelSpeed = 0;
			rightWheelSpeed = 0;
		}
		
		@Override
		public RobotLogger getLogger() {
			return logger;
		}
		
		@Override
		public List<MessageProvider> getMessageProviders() {
			//We only do this here because messageProviders might not be necessary
			//most of the times, and it saves simulation time
			if(messageProviders == null) {
				initMessageProviders();
			}
			
			return messageProviders;
		}

		private void initMessageProviders() {
			messageProviders = new ArrayList<MessageProvider>();

			messageProviders.add(new EntityMessageProvider(this));
			messageProviders.add(new EntitiesMessageProvider(this));
			messageProviders.add(new BehaviorMessageProvider(this));
			messageProviders.add(new NeuralActivationsMessageProvider(this));
			messageProviders.add(new LogMessageProvider(this));
		}
		
		@Override
		public String getStatus() {
			if(getActiveBehavior() != null)
				return "Running behavior "+getActiveBehavior().getClass().getSimpleName();
			return "Idle";
		}

		@Override
		public void startBehavior(CIBehavior b) {
			stopActiveBehavior();
			activeBehavior = b;
			activeBehavior.start();
			log("Starting CIBehavior "+b.getClass().getSimpleName());
		}

		@Override
		public void stopActiveBehavior() {
			if (activeBehavior != null) {
				activeBehavior.cleanUp();
				log("Stopping CIBehavior "+activeBehavior.getClass().getSimpleName());
				activeBehavior = null;
				setMotorSpeeds(0, 0);
			}
		}
		
		@Override
		public CIBehavior getActiveBehavior() {
			return activeBehavior;
		}
		
		private void log(String msg) {
			if(logger != null)
				logger.logMessage(msg);
		}

		@Override
		public double getLeftMotorSpeed() {
			return leftPercentage;
		}

		@Override
		public double getRightMotorSpeed() {
			return rightPercentage;
		}

		@Override
		public void replaceEntity(Entity e) {
			synchronized(entities){
				entities.remove(e);
				entities.add(e);
			}
		}
		
}