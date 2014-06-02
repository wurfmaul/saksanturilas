package at.jku.chess.saksanturilas.player;

import at.jku.chess.saksanturilas.board.Board;
import at.jku.chess.saksanturilas.board.ChessUtil;
import at.jku.chess.saksanturilas.move.Move;
import at.jku.chess.saksanturilas.ui.GameUI;

/**
 * Specifies a player.
 * 
 * @author Fabian Jordan (0855941)
 * @author Wolfgang Kuellinger (0955711)
 * 
 */
public abstract class AbstractPlayer {
	/** The user interface the player is using */
	final protected GameUI ui;
	/** The color of this player */
	protected byte color;
	/** True if player is human */
	protected boolean isHumanPlayer;

	public AbstractPlayer(GameUI ui) {
		this.ui = ui;
	}

	/**
	 * Calculates the fitness of a given board for given color. For example, the
	 * method would calculate the strength of a given configuration of a board
	 * in the point of view for the given color.
	 * 
	 * @param board
	 *            the board
	 * @param color
	 *            the color
	 * @return a value that represents the fitness of the given board in the
	 *         point of view of the given color.
	 */
	public abstract int getFitness(Board board, byte color);

	/**
	 * Chooses a move out of all possibilities
	 * 
	 * @param board
	 *            the board
	 * @param color
	 *            the current color
	 * @param milliSeconds
	 *            milliseconds the player has time to think about life universe
	 *            and everything
	 * @param random
	 *            the seed of all following random-based decisions
	 * @return the move the <tt>Player</tt> has chosen and <tt>null</tt> if the
	 *         game is over (i.e. mate, stale mate and remis)
	 */
	public abstract Move chooseMove(Board board, byte color, int milliSeconds,
			java.util.Random random);

	/**
	 * Gives back a simple description of this player
	 * 
	 * @return a simple description of this player
	 */
	public abstract String getDescription();

	/**
	 * Getter for players color
	 * 
	 * @return color of this player
	 */
	public byte getColor() {
		return color;
	}

	/**
	 * Setter for player's color
	 * 
	 * @param color
	 *            the color
	 */
	public void setColor(byte color) {
		this.color = color;
	}

	/**
	 * Turns <tt>WHITE</tt> to <tt>BLACK</tt> and vice versa
	 */
	public void flipColor() {
		color = ChessUtil.flipColor(color);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
}