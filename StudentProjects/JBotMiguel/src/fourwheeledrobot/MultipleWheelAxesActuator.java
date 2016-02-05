package fourwheeledrobot;
import mathutils.MathUtils;
import mathutils.Vector2d;
import net.jafama.FastMath;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public abstract class MultipleWheelAxesActuator extends Actuator {

    @ArgumentsAnnotation(name = "wheeldiameter", defaultValue = "0.05")
    protected double wheelDiameter = 0.05;

    @ArgumentsAnnotation(name = "distancewheels", defaultValue = "0.05")
    protected double distanceBetweenWheels = 0.05;

    @ArgumentsAnnotation(name = "maxspeed", defaultValue = "0.1")
    protected double maxSpeed = 0.1;

    protected double angleLimit = Math.toRadians(45);
    protected double anglePerSecond = Math.toRadians(90)/10.0;

    protected double[] speeds = null;
    protected double[] prevSpeeds = null;

    protected double[] rotation = null;
    protected double[] prevRotation = null;

    protected double frictionParam = 1.0;
    protected double maxSlipAngle = 10;//10 deg, will be converted to radians in the constructor
    protected double parallelLineAngle = 1; //1 deg, will be converted to radians in the constructor
    protected double maxSpeedDifference = 0.001;
    protected double w;
    protected double l;
    protected Vector2d[] wheelPos;
    protected double stop = 0;
    
    public MultipleWheelAxesActuator(Simulator simulator, int id, Arguments args, int speeds, int rotations) {
        super(simulator, id, args);
        w = distanceBetweenWheels;
        l = distanceBetweenWheels;
        wheelPos = new Vector2d[]{ // relative to the robot referential
                new Vector2d(w / 2, l / 2), // front left
                new Vector2d(w / 2, -l / 2), // front right
                new Vector2d(-w / 2, l / 2), // rear left
                new Vector2d(-w / 2, -l / 2) // rear right
            };
        this.speeds = new double[speeds];
        this.prevSpeeds = new double[speeds];

        this.rotation = new double[rotations];
        this.prevRotation = new double[rotations];

        frictionParam = args.getArgumentAsDoubleOrSetDefault("friction", frictionParam);
        maxSlipAngle = Math.toRadians(args.getArgumentAsDoubleOrSetDefault("slipangle", maxSlipAngle));
        parallelLineAngle = Math.toRadians(args.getArgumentAsDoubleOrSetDefault("parallelangle", parallelLineAngle));
        
        if(args.getArgumentIsDefined("anglelimit")) {
        	double angle = Math.toRadians(args.getArgumentAsDoubleOrSetDefault("anglelimit", 45));
        	this.angleLimit = angle;
        }
        
    }

    private static class Line {

        private final Vector2d p1; // one point belonging to the line
        private final Vector2d p2; // other point belonging to the line

        Line(Vector2d p, double angle) {
            this.p1 = p;
            this.p2 = new Vector2d(p.x + FastMath.cos(angle), p.y + FastMath.sin(angle));
        }

        Line(Vector2d p1, Vector2d p2) {
            this.p1 = p1;
            this.p2 = p2;
        }

        Vector2d intersect(Line other) {
            double x1 = this.p1.x;
            double x2 = this.p2.x;
            double y1 = this.p1.y;
            double y2 = this.p2.y;
            double x3 = other.p1.x;
            double x4 = other.p2.x;
            double y3 = other.p1.y;
            double y4 = other.p2.y;

            double d = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
            if (d > 0.001 && d < 0.001) {
                return null;
            }
            double xi = ((x3 - x4) * (x1 * y2 - y1 * x2) - (x1 - x2) * (x3 * y4 - y3 * x4)) / d;
            double yi = ((y3 - y4) * (x1 * y2 - y1 * x2) - (y1 - y2) * (x3 * y4 - y3 * x4)) / d;
            return new Vector2d(xi, yi);
        }

        double angle(Line other) {
            Vector2d a = new Vector2d(this.p1);
            a.sub(this.p2);
            Vector2d b = new Vector2d(other.p1);
            b.sub(other.p2);
            double angle = FastMath.atan2(a.x * b.y - a.y * b.x, a.x * b.x + a.y * b.y);
            angle = Math.abs(angle);
            if (angle > Math.PI / 2) {
                angle = Math.PI - angle;
            }
            return angle;
        }
    }
    
    @Override
    public void apply(Robot robot, double timeDelta) {
    	
    	if(stop-- > 0)
    		return;
    	
    	double[] speeds = getCompleteSpeeds();
    	double[] rotations = getCompleteRotations();
    	
    	for(int i = 0 ; i < speeds.length ; i++)
    		speeds[i]*=timeDelta;
    	
    	double robotOrientation = robot.getOrientation();
    	Vector2d robotPosition = new Vector2d(robot.getPosition());
    	
    	 Vector2d newRobotPos = new Vector2d(robotPosition);
         double newOrientation = robotOrientation;

         double meanAngle = 0;
         for (int i = 0; i < rotations.length; i++) {
             meanAngle += rotations[i];
         }
         meanAngle /= rotations.length;
         double maxDifference = Double.NEGATIVE_INFINITY;
         for (int i = 0; i < rotations.length; i++) {
             maxDifference = Math.max(maxDifference, Math.abs(meanAngle - rotations[i]));
         }
         // wheels are parallel and speeds are coherent, move in the meanAngle direction
         if (maxDifference < parallelLineAngle) {
             if (checkSpeedsAll(speeds)) {
                 double meanSpeed = 0;
                 for (double d : speeds) {
                     meanSpeed += d;
                 }
                 meanSpeed /= speeds.length;
                 // movement in the robot referential
                 double newX = meanSpeed * FastMath.cos(meanAngle);
                 double newY = meanSpeed * FastMath.sin(meanAngle);
                 
                 // calculate new position in the world referential
                 newRobotPos = new Vector2d(newX * Math.cos(robotOrientation) - newY * Math.sin(robotOrientation),
                         newX * Math.sin(robotOrientation) + newY * Math.cos(robotOrientation));
                 newRobotPos.add(robotPosition);
                 // orientation does not change
                 newOrientation = robotOrientation;
             }
         } else {
             Line[] axisLines = calculateAxisLines(rotations);
             Vector2d icr = calculateICR(axisLines);
             if (checkWheelSlip(axisLines, icr)) {
                 // ICR is inside the robot and side speeds are consistent
                 if (icr.x < w / 2 && icr.x > -w / 2 && icr.y < l / 2 && icr.y > -l / 2) {
                     if (checkSpeedsSides(speeds)) {
                         // Rotate around the centre
                         double ori = calculateDifferentialDriveOrientation(speeds);
                         newOrientation = MathUtils.modPI2(robotOrientation + ori);
                         // Position does not change
                         newRobotPos = robotPosition;
                     }
                 } else if (checkSpeedsAll(speeds)) { // rotate around the ICR
                     double meanSpeed = 0;
                     for (double d : speeds) {
                         meanSpeed += d;
                     }
                     meanSpeed /= speeds.length;

                     // convert movement along the circle to an angle
                     double turnRadius = icr.length();
                     double moveAngle = (icr.y > 0 ? 1 : -1) * meanSpeed / turnRadius;
                     
                     // compute new position in the robot referential
                     double newX = icr.x + (-icr.x) * Math.cos(moveAngle) - (-icr.y) * Math.sin(moveAngle);
                     double newY = icr.y + (-icr.x) * Math.sin(moveAngle) + (-icr.y) * Math.cos(moveAngle);
                     
                     double ori = moveAngle; // coherent with rotation around ICR, but sometimes produce odd-looking orientations? (back wheels fixed)
                     //double ori = new Vector2d(newX,newY).getAngle(); // same orientation as the movement direction -- not coherent with rotation around ICR
                     
                     // calculate position in the world referential
                     newRobotPos = new Vector2d(newX * Math.cos(robotOrientation) - newY * Math.sin(robotOrientation),
                             newX * Math.sin(robotOrientation) + newY * Math.cos(robotOrientation));
                     newRobotPos.add(robotPosition);
                     
                     // orientation in the world referential
                     newOrientation = MathUtils.modPI2(robotOrientation + ori);       
                 }
             }
         }
         
         robot.setPosition(newRobotPos.x, newRobotPos.y);
         robot.setOrientation(newOrientation);
    }

 // Does not accept speed differences in the same lateral axis
    private boolean checkSpeedsSides(double[] speeds) {
        return Math.abs(speeds[0] - speeds[2]) <= maxSpeedDifference
                && Math.abs(speeds[1] - speeds[3]) <= maxSpeedDifference;
    }

    private boolean checkSpeedsAll(double[] speeds) {
        double meanSpeed = 0;
        for (double d : speeds) {
            meanSpeed += d;
        }
        meanSpeed /= speeds.length;
        for (double d : speeds) {
            if (Math.abs(d - meanSpeed) > maxSpeedDifference) {
                return false;
            }
        }
        return true;
    }

    private boolean checkWheelSlip(Line[] axisLines, Vector2d icr) {
        double[] wheelSlip = calculateSlipAngles(axisLines, icr);
        double maxSlip = Double.NEGATIVE_INFINITY;
        for (double d : wheelSlip) {
            maxSlip = Math.max(maxSlip, d);
        }
        return maxSlip <= maxSlipAngle;
    }

    private double[] calculateSlipAngles(Line[] axisLines, Vector2d icr) {
        double[] wheelSlip = new double[axisLines.length];
        for (int i = 0; i < wheelSlip.length; i++) {
            // slip = angle difference between the axis line and the wheel-ICR line
            Line axisICR = new Line(icr, wheelPos[i]);
            wheelSlip[i] = axisICR.angle(axisLines[i]);
        }
        return wheelSlip;
    }

    private Line[] calculateAxisLines(double[] rotations) {
        Line[] axisLines = new Line[rotations.length];
        for (int i = 0; i < rotations.length; i++) {
            axisLines[i] = new Line(wheelPos[i], MathUtils.modPI2(rotations[i] + Math.PI / 2));
        }
        return axisLines;
    }


    // todo: if one of the sides has parallel wheels it is not very coherent
    private Vector2d calculateICR(Line[] axisLines) {
        // left side intersection
        Vector2d leftICR = null;
        if (axisLines[0].angle(axisLines[2]) > parallelLineAngle) {
            leftICR = axisLines[0].intersect(axisLines[2]);
        }
        // right side intersection
        Vector2d rightICR = null;
        if (axisLines[1].angle(axisLines[3]) > parallelLineAngle) {
            rightICR = axisLines[1].intersect(axisLines[3]);
        }

        if (rightICR == null && leftICR == null) {
            return new Vector2d(0, 0);
        } else if (rightICR != null && leftICR != null) {
            rightICR.add(leftICR);
            rightICR.x /= 2;
            rightICR.y /= 2;
            return rightICR;
        } else if (rightICR != null) {
            return rightICR;
        } else {
            return leftICR;
        }
    }

    private double calculateDifferentialDriveOrientation(double[] speeds) {
        double leftSpeed = (speeds[0] + speeds[2]) / 2;
        double rightSpeed = (speeds[1] + speeds[3]) / 2;
        double ori = (rightSpeed - leftSpeed) / l;
        return ori;
    }
    
    public void setWheelSpeed(int wheelNumber, double val) {
		speeds[wheelNumber] = ( val - 0.5) * maxSpeed * 2.0;
	}
	
	public void setWheelSpeed(double[] vals) {
		for(int i = 0 ; i < vals.length ; i++) {
			setWheelSpeed(i,vals[i]);
		}
	}
	
	public void setRotation(int axisNumber, double val) {
		
		val = val*angleLimit*2 - angleLimit;
		
		if(val - prevRotation[axisNumber] > anglePerSecond) 
			val = prevRotation[axisNumber] + anglePerSecond;
		
		if(val - prevRotation[axisNumber] < -anglePerSecond) 
			val = prevRotation[axisNumber] - anglePerSecond;
		
		if(val > angleLimit)
			val = angleLimit;
		
		if(val < -angleLimit)
			val = -angleLimit;
		
		rotation[axisNumber] = val;
		prevRotation[axisNumber] = rotation[axisNumber];
		
	}
	
	public void setRotation(double[] vals) {
		for(int i = 0 ; i < vals.length ; i++) {
			setRotation(i,vals[i]);
		}
	}
	
	public double getMaxSpeed() {
		return maxSpeed;
	}
	
	public double[] getSpeed(){
		return speeds;
	}
	
	public double[] getRotation() {
		return rotation;
	}
	
	public int getNumberOfSpeeds() {
		return speeds.length;
	}
	
	public int getNumberOfRotations() {
		return rotation.length;
	}
	
	public abstract double[] getCompleteRotations();
	public abstract double[] getCompleteSpeeds();
	
	public void stop(double val) {
		this.stop = val;
	}

}