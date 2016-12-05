package dataObjects;

import java.util.ArrayList;

import commoninterface.dataobjects.GPSData;

public class RobotData {
	private int robotID = -1;
	private ArrayList<GPSData> gpsData = null;

	public int getRobotID() {
		return robotID;
	}

	public ArrayList<GPSData> getGpsData() {
		return gpsData;
	}
}
