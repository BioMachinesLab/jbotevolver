package gui.renderer;

import java.awt.Component;
import java.awt.Image;

import mathutils.Point2d;

import simulation.Simulator;

public class NullRenderer implements Renderer {

//	@Override
	public void dispose() {
	}

//	@Override
	public void drawFrame() {
	}

//	@Override
	public void setSimulator(Simulator simulator) {
	}

//	@Override
	public Component getComponent() {
		return null;
	}

//	@Override
	public int getSelectedRobot() {
		return -1;
	}

	public void drawCircle(Point2d center, double radius) {
	}

	@Override
	public void resetZoom() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void zoomIn() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void zoomOut() {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void drawImage(Image image) {
		// TODO Auto-generated method stub
	}
}
