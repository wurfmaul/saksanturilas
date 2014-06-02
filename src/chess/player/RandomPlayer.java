package chess.player;

import java.util.List;
import java.util.Random;

import chess.Board;
import chess.ChessUtil;
import chess.move.Move;
import chess.ui.GameUI;

/**
 * Class represents a player who plays on a random basis.
 * 
 * @author Fabian Jordan (0855941)
 * @author Wolfgang Kuellinger (0955711)
 * 
 */
public class RandomPlayer extends AbstractPlayer {

	public RandomPlayer(GameUI ui) {
		super(ui);
		isHumanPlayer = false;
	}

	@Override
	public int getFitness(Board board, byte color) {
		return 0;
	}

	@Override
	public Move chooseMove(Board board, byte color, int milliSeconds, Random seed) {
		// get valid moves
		List<Move> moves = board.getValidMoves(color);

		if (board.getResult() != ChessUtil.NO_MATE || moves.size() == 0) {
			return null;
		}

		// exclude option to offer remis
		int i = moves.size();
		if (moves.get(i - 1).getRemis() == Move.REMIS_OFFER) {
			i--;
		}

		// choose move by random
		return moves.get(seed.nextInt(i));
	}

	@Override
	public String getDescription() {
		return "A computer player that plays on a random basis.";
	}
}