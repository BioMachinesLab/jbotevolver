package logprocessing.dataObjects;

import java.io.Serializable;
import java.util.ArrayList;

import org.joda.time.DateTime;

import commoninterface.utils.logger.ToLogData;

public class Experiment implements Serializable {
	private static final long serialVersionUID = -7235148601925646447L;

	private DateTime experimentStart;
	private DateTime experimentEnd;
	private int timeSteps;
	private String controllerName;
	private int controllerNumber;
	private ArrayList<Integer> robots = new ArrayList<Integer>();
	private int sample;
	private ArrayList<ToLogData> logs = new ArrayList<ToLogData>();
	private int activeRobot = -1;

	@Override
	public String toString() {
		return controllerName + "\t" + controllerNumber + "_" + sample + "_" + robots.size();
	}

	public DateTime getExperimentStart() {
		return experimentStart;
	}

	public void setExperimentStart(DateTime experimentStart) {
		this.experimentStart = experimentStart;
	}

	public DateTime getExperimentEnd() {
		return experimentEnd;
	}

	public void setExperimentEnd(DateTime experimentEnd) {
		this.experimentEnd = experimentEnd;
	}

	public int getTimeSteps() {
		return timeSteps;
	}

	public void setTimeSteps(int timeSteps) {
		this.timeSteps = timeSteps;
	}

	public String getControllerName() {
		return controllerName;
	}

	public void setControllerName(String controllerName) {
		this.controllerName = controllerName;
	}

	public int getControllerNumber() {
		return controllerNumber;
	}

	public void setControllerNumber(int controllerNumber) {
		this.controllerNumber = controllerNumber;
	}

	public ArrayList<Integer> getRobots() {
		return robots;
	}

	public void setRobots(ArrayList<Integer> robots) {
		this.robots = robots;
	}

	public int getSample() {
		return sample;
	}

	public void setSample(int sample) {
		this.sample = sample;
	}

	public ArrayList<ToLogData> getLogs() {
		return logs;
	}

	public void setLogs(ArrayList<ToLogData> logs) {
		this.logs = logs;
	}

	public int getActiveRobot() {
		return activeRobot;
	}

	public void setActiveRobot(int activeRobot) {
		this.activeRobot = activeRobot;
	}
}
