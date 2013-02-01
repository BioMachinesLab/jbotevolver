package evolutionaryrobotics.parallel;

import javax.swing.JOptionPane;

public class ScreenSaverSlave {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			JOptionPane.showMessageDialog(null, "Trying to start the evolutionary robotics screen saver slave, but no master address or too many arguments were provided on the commandline");
		}
		while(true){
			try {
				new Slave(args[0], 0, System.out, true);
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("Connection closed! Trying to reconnect.");
			try {
				Thread.sleep(60000);
			} catch (InterruptedException e) { }
		}

	}

}
