package evolutionaryrobotics.parallel.applet;

import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JTextArea;

public class JTextAreaOutputStream extends OutputStream {

	JTextArea textArea;
	private int size = 0;
	
	public JTextAreaOutputStream(JTextArea textArea) {
		this.textArea = textArea;
	}

	@Override
	public void write(int arg) throws IOException {
		if(size ++ > 1000){
			this.textArea.setText("");
			size = 0;
		}
		this.textArea.append((new Character((char) arg)).toString());
//		System.out.print((char) arg);
//		int len = this.textArea.getText().length();
//		this.textArea.setCaretPosition(len);
	}
}
