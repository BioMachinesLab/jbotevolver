package simulation;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;
import java.util.HashMap;

public class SimulatorObject implements KeyListener, Serializable {
	
	protected HashMap<String, Object> parameters = new HashMap<String, Object>();

	protected String name;

	public SimulatorObject(String name) {
		this.name = name;
	}

	public SimulatorObject() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public Object getParameter(String key) {
		return parameters.get(key);
	}
	
	public void setParameter(String key, Object value) {
		parameters.put(key, value);		
	}

	public Double getParameterAsDouble(String key) {
		return (Double) parameters.get(key); 
	}

	public Integer getParameterAsInteger(String key) {
		return (Integer) parameters.get(key); 
	}


	public void keyPressed(KeyEvent e) {
	}

	public void keyReleased(KeyEvent e) {
	}

	public void keyTyped(KeyEvent e) {
	}
	
	public int getNumberExtraParameters() {
		return 0;
	}
	
	public void setExtraParameters(double[] parameters) {
	}
}
