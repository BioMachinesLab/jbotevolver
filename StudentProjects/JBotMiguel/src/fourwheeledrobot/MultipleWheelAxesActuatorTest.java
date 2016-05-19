package fourwheeledrobot;


import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import mathutils.MathUtils;
import mathutils.Vector2d;
import net.jafama.FastMath;

public class MultipleWheelAxesActuatorTest extends Canvas {

    private double scale = 10;
    private double[] speeds;
    private double[] rotations;
    private Vector2d robotPosition = new Vector2d(0, 0);
    double robotOrientation = 0.5;

    public static void main(String[] args) {
        final MultipleWheelAxesActuatorTest t = new MultipleWheelAxesActuatorTest();
        t.setSize(1000, 1000);

        // around itself
        //t.speeds = new double[]{1, -1, 1, -1};
        //t.rotations = new double[]{-0.78, 0.78, 0.78, -0.78};
        t.speeds = new double[]{1, 1, 1, 1};
        // slide without changing rotation
        //t.rotations = new double[]{-0.5, -0.5, -0.5, -0.5};
        // four-wheel rotation
//      
        t.rotations = new double[]{ -0.03999919450782363, 0.037668074911368254, -0.009085416777568911, -0.06559242767737561};       
//        t.rotations = new double[]{0.6, 0.3, -0.6, -0.3};       
        // back-wheels fixed -- front
        //t.rotations = new double[]{0.1, 0.1, 0, 0};
        // back-wheels fixed -- back
        //t.rotations = new double[]{-0.5, -0.5, 0, 0};
        // invalid
        //t.rotations = new double[]{0.6, 0.5, -0.6, -0.3};
        
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 1100);

        JPanel j = new JPanel();
        j.add(t);
        JButton cena = new JButton("Tau");
        cena.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                t.repaint();
            }

        });
        j.add(cena);
        frame.add(j);        
        frame.setVisible(true);
        frame.invalidate();

    }

    private int tx(double x) {
        return (int) Math.round(x * scale) + this.getWidth() / 2;
    }

    private int ty(double y) {
        return this.getHeight() - (int) Math.round(y * scale) - this.getHeight() / 2;
    }

    private int td(double d) {
        return (int) Math.round(d * scale);
    }

    protected double maxSlipAngle = Math.toRadians(10);
    protected double parallelLineAngle = Math.toRadians(1);
    protected double maxSpeedDifference = 0.1;
    protected double distanceBetweenWheels = 5;
    protected double w = distanceBetweenWheels;
    protected double l = distanceBetweenWheels;
    protected Vector2d[] wheelPos = new Vector2d[]{ // relative to the robot referential
        new Vector2d(w / 2, l / 2), // front left
        new Vector2d(w / 2, -l / 2), // front right
        new Vector2d(-w / 2, l / 2), // rear left
        new Vector2d(-w / 2, -l / 2) // rear right
    };

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

    private int rx(double x, double y) {
        double r = x * Math.cos(robotOrientation) - y * Math.sin(robotOrientation);
        r += robotPosition.x;
        return tx(r);
    }

    private int ry(double x, double y) {
        double r = x * Math.sin(robotOrientation) + y * Math.cos(robotOrientation);
        r += robotPosition.y;
        return ty(r);
    }

    public void paint(Graphics g) {
        // DRAW ROBOT
        g.setColor(Color.BLACK);
        g.drawPolygon(
                new int[]{
                    rx(-w / 2, l / 2),
                    rx(w / 2, l / 2),
                    rx(w / 2, -l / 2),
                    rx(-w / 2, -l / 2),
                    rx(-w / 2, l / 2)
                },
                new int[]{
                    ry(-w / 2, l / 2),
                    ry(w / 2, l / 2),
                    ry(w / 2, -l / 2),
                    ry(-w / 2, -l / 2),
                    ry(-w / 2, l / 2)
                }, 5);
        g.drawLine(rx(l / 4, 0), ry(l / 4, 0), rx(l / 2, 0), ry(l / 2, 0));
        g.setColor(Color.BLUE);
        g.drawLine(rx(0, -w / 4), ry(0, -w / 4), rx(0, w / 4), ry(0, w / 4));        
        g.setColor(Color.RED);
        for (int i = 0; i < wheelPos.length; i++) { // draw wheel speeds
            Vector2d off = new Vector2d(FastMath.cos(rotations[i]), FastMath.sin(rotations[i]));
            off.x *= speeds[i];
            off.y *= speeds[i];
            off.add(wheelPos[i]);
            g.drawLine(rx(wheelPos[i].x, wheelPos[i].y), ry(wheelPos[i].x, wheelPos[i].y), rx(off.x, off.y), ry(off.x, off.y));
        }

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

            // DRAW AXIS LINES            
            g.setColor(Color.ORANGE);
            for (Line l : axisLines) {
                Vector2d extraLow = new Vector2d(l.p2.x - (l.p2.x - l.p1.x) * 1000, l.p2.y - (l.p2.y - l.p1.y) * 1000);
                Vector2d extraHigh = new Vector2d(l.p2.x + (l.p2.x - l.p1.x) * 1000, l.p2.y + (l.p2.y - l.p1.y) * 1000);
                g.drawLine(rx(extraLow.x, extraLow.y), ry(extraLow.x, extraLow.y), rx(extraHigh.x, extraHigh.y), ry(extraHigh.x, extraHigh.y));
            }

            // DRAW ICR
            g.setColor(Color.MAGENTA);
            g.fillOval(rx(icr.x, icr.y) - 3, ry(icr.x, icr.y) - 3, 6, 6);
            int x = rx(icr.x, icr.y);
            int y = ry(icr.x, icr.y);
            int len = td(icr.length());
            g.drawOval(x - len, y - len, len * 2, len * 2);
            for (int i = 0; i < wheelPos.length; i++) { // DRAW
                Line axisICR = new Line(icr, wheelPos[i]);
                g.setColor(Color.GREEN);
                g.drawLine(rx(axisICR.p1.x, axisICR.p1.y), ry(axisICR.p1.x, axisICR.p1.y), rx(axisICR.p2.x, axisICR.p2.y), ry(axisICR.p2.x, axisICR.p2.y));
            }
        }

        // draw orientation
        double rX = FastMath.cos(newOrientation) * 5;
        double rY = FastMath.sin(newOrientation) * 5;
        g.setColor(Color.CYAN);
        g.drawLine(tx(robotPosition.x), ty(robotPosition.y), tx(rX + robotPosition.x), ty(rY + robotPosition.y));
        // draw move vector
        g.setColor(Color.BLUE);
        g.drawLine(tx(robotPosition.x), ty(robotPosition.y), tx(newRobotPos.x), ty(newRobotPos.y));
        
        robotPosition.set(newRobotPos.x, newRobotPos.y);
        robotOrientation = newOrientation;

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

}
