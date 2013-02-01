package evolutionaryrobotics.parallel.applet;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.io.PrintStream;

import javax.swing.JApplet;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.MouseInputListener;

import evolutionaryrobotics.parallel.Slave;

public class ScreenSaverApplet extends JApplet implements MouseInputListener, KeyListener { 
	JTextArea textArea = new JTextArea();
	Slave     slave = null;

	@Override
	public void init() {
		super.init();
	} 
	@Override
	public void start() {
		// TODO Auto-generated method stub
		textArea.setEditable(false);
		add(new JScrollPane(textArea));
		System.out.println("width: " + getWidth() + ", height: " + getHeight());
		textArea.addMouseListener(this);
		textArea.addKeyListener(this);	
		final PrintStream out = new PrintStream(new JTextAreaOutputStream(textArea));

		super.start();
		try {			
			slave = new Slave("localhost", 0, new PrintStream(out), false);
		} catch (Exception e) {
			e.printStackTrace(out);
		}

		end();
	}
//	@Override
	public void mouseClicked(MouseEvent arg0) {
		end();
	}
//	@Override
	public void mouseEntered(MouseEvent arg0) {
	}
//	@Override
	public void mouseExited(MouseEvent arg0) {
	}
//	@Override
	public void mousePressed(MouseEvent arg0) {
		end();
	}
//	@Override
	public void mouseReleased(MouseEvent arg0) {
		end();
	}
//	@Override
	public void mouseDragged(MouseEvent arg0) {
		end();
	}
//	@Override
	public void mouseMoved(MouseEvent arg0) {
		end();
	}
//	@Override
	public void keyPressed(KeyEvent e) {
		end();
	}
//	@Override
	public void keyReleased(KeyEvent e) {
		end();
	}
//	@Override
	public void keyTyped(KeyEvent e) {
		end();
	}	

	public void end() {
		System.out.println("Trying to end slave");
		textArea.setText(textArea.getText()+ "\n" + "TRYING TO END SLAVE");

		if (slave != null) 
			slave.end();

		System.exit(0);
	}
}
