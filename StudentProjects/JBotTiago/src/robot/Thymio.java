package robot;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import network.SimulatedBroadcastHandler;
import sensors.ThymioIRSensor;
import simulation.Simulator;
import simulation.robot.DifferentialDriveRobot;
import simulation.util.Arguments;
import actuator.ThymioTwoWheelActuator;

import commoninterface.CISensor;
import commoninterface.ThymioCI;
import commoninterface.mathutils.Vector2d;
import commoninterface.network.broadcast.BroadcastHandler;
import commoninterface.network.broadcast.BroadcastMessage;
import commoninterface.network.broadcast.HeartbeatBroadcastMessage;
import commoninterface.objects.Entity;
import commoninterface.utils.CIArguments;

public class Thymio extends DifferentialDriveRobot implements ThymioCI {

		private Simulator simulator;
		private ArrayList<Entity> entities = new ArrayList<Entity>();
		private ArrayList<CISensor> cisensors = new ArrayList<CISensor>();
	
        private ThymioIRSensor irSensor;
        private ThymioTwoWheelActuator wheels;
        
        private Vector2d virtualPosition;
        
        private SimulatedBroadcastHandler broadcastHandler;
        
        public Thymio(Simulator simulator, Arguments args) {
        	super(simulator, args);
        	this.simulator = simulator;
        	
        	ArrayList<BroadcastMessage> broadcastMessages = new ArrayList<BroadcastMessage>();
        	broadcastMessages.add(new HeartbeatBroadcastMessage(this));
        	broadcastHandler = new SimulatedBroadcastHandler(this, broadcastMessages);
        	
        	distanceBetweenWheels = 0.099;
        	
        	if(getRadius() < 0.08)
        		throw new RuntimeException("Radius lower than 0.08m");
        	
        	Arguments irSensorsArgs = new Arguments("senserobot=0, cutoffangle=45, fixedsensor=0, noiseenabled=1, numberofrays=7, offsetnoise=0");	
        	sensors.add(new ThymioIRSensor(simulator, sensors.size()+1, this, irSensorsArgs));
        	
//        	Arguments twoWheelsArgs = new Arguments("randomincrement=1");
        	Arguments twoWheelsArgs = new Arguments("speedincrement=0.155");
        	actuators.add(new ThymioTwoWheelActuator(simulator, actuators.size()+1, twoWheelsArgs));
        }
        
        @Override
		public void begin(CIArguments args) { }

		@Override
		public void shutdown() { } 

		@Override
		public void setMotorSpeeds(double leftMotor, double rightMotor) {
			if(wheels == null)
				wheels = (ThymioTwoWheelActuator) getActuatorByType(ThymioTwoWheelActuator.class);
			
			double leftSpeed = leftMotor/2 + 0.5;
			double rightSpeed = rightMotor/2 + 0.5;
			
			wheels.setLeftWheelSpeed(leftSpeed);
			wheels.setRightWheelSpeed(rightSpeed);
//			wheels.apply(this);
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
		public double getTimeSinceStart() {
			return simulator.getTime()*10;
		}

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
			virtualPosition.set(x, y);
		}

}