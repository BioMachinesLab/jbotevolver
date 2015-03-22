package robot;

import sensors.ThymioIRSensor;
import simulation.Simulator;
import simulation.robot.DifferentialDriveRobot;
import simulation.util.Arguments;

public class Thymio extends DifferentialDriveRobot {

        private ThymioIRSensor irSensor;
        
        public Thymio(Simulator simulator, Arguments args) {
                super(simulator, args);
        }
        
        public void setIRSensor(ThymioIRSensor epuckIRSensor) {
                this.irSensor = epuckIRSensor;
        }
        public ThymioIRSensor getIrSensor() {
                return irSensor;
        }       
}