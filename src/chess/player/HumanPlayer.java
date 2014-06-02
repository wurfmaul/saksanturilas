package chess.player;

import java.util.Random;

import chess.Board;
import chess.move.Move;
import chess.ui.GameUI;

/**
 * Implements a human player.
 * 
 * @author Fabian Jordan (0855941)
 * @author Wolfgang Kuellinger (0955711)
 * 
 */
public class HumanPlayer extends AbstractPlayer {

	public HumanPlayer(GameUI ui) {
		super(ui);
		isHumanPlayer = true;
	}

	@Override
	public int getFitness(Board board, byte color) {
		return 0;
	}

	@Override
	public Move chooseMove(Board board, byte color, int milliSeconds, Random random) {
		return ui.askForNextMove(board, color, milliSeconds, random);
	}

	@Override
	public String getDescription() {
		return "A human player.";
	}
}