package at.jku.chess.saksanturilas.ui;

import javax.swing.JOptionPane;

public class OptionDialog extends JOptionPane {
	private static final long serialVersionUID = 1L;

	public OptionDialog() {
		messageType = QUESTION_MESSAGE;
	}
}