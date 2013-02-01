package evolutionaryrobotics.parallel.applet;

import java.io.PrintStream;

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import evolutionaryrobotics.parallel.Slave;

public class RemoteSlaveApplet extends JApplet { 
	JTextArea textArea = new JTextArea();
	private Slave slave;
	@Override
	public void init() {
		super.init();
		textArea.setEditable(false);
		add(new JScrollPane(textArea));
		//add(new JButton("OK"));
		setVisible(true);
	} 
	@Override
	public void start() {
		PrintStream out = new PrintStream(new JTextAreaOutputStream(textArea));
		textArea.setText("Started...");
		super.start();
		while(true){
			try {			
				slave= new Slave("evolve.dcti.iscte.pt", 0, new PrintStream(out), false);
			} catch (Exception e) { //e.printStackTrace(out); 

			}
			out.println("Connection closed! Trying to reconnect.");
			try {
				Thread.sleep(60000);
			} catch (InterruptedException e) { }

		}

	}
	@Override
	public void destroy() {
		super.destroy();
		slave.end();
	}	
	
	
}
