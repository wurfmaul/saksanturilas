package app;

import chess.ui.SwingUI;
import chess.ui.TextUI;

/**
 * Class to start the game. Only contains the <tt>static void main</tt> method
 * 
 * @author Fabian Jordan (0855941)
 * @author Wolfgang Kuellinger (0955711)
 * 
 */
public class Game {
	public static boolean DEBUG;
	public static void main(String[] args) {
		final boolean SWINGUI = true;
		
		DEBUG = args.length > 0 && args[0].equalsIgnoreCase("debug");

		// start new game using the swing UI
		if (SWINGUI)
			SwingUI.show();
		// start new game using the textual UI
		else
			new TextUI();

	}
}