package chess.move;

import static chess.Figure.*;
import static chess.ChessUtil.getCoordName;
import chess.Board;
import chess.Figure;

/**
 * Represents one single ply.
 * 
 * @author Fabian Jordan (0855941)
 * @author Wolfgang Kuellinger (0955711)
 * 
 */
public class Move {
	public static final byte NO_REMIS = 0;
	public static final byte REMIS_OFFER = 1;
	public static final byte REMIS_ACCEPT = 2;
	public static final byte REMIS_REJECT = 4;
	public static final byte REMIS_UNREJECTABLE = 9;

	/** The color of the moved figure */
	private byte color;
	/** The type of the moved figure */
	private byte type;
	/** index of a hit figure or -1 if no figure was hit */
	private int hit;
	/**
	 * The type of figure a pawn is changed into if it reaches the opponent's
	 * base line
	 */
	private byte newFigureType;
	/** The whole battlefield */
	private byte[] figures;
	/** The index where the move starts */
	protected int source;
	/** The index where the move ends */
	protected int destination;

	/** Represents the possibilities for offering, rejecting and accepting remis */
	protected byte remis;

	protected Move() {
		remis = NO_REMIS;
	}

	/**
	 * 
	 * @param board
	 *            the board
	 * @param type
	 * @param source
	 * @param destination
	 */
	public Move(Board board, byte type, int source, int destination) {
		this.type = type;
		this.color = Figure.getColorFromType(type);
		this.source = source;
		this.destination = destination;

		this.figures = board.getFigures();
		this.hit = figures[destination] == EMPTY ? -1 : destination;
		this.newFigureType = EMPTY;
	}

	/**
	 * Gives back the color of this move.
	 * 
	 * @return the color of this move
	 */
	public byte getColor() {
		return color;
	}

	public int getHit() {
		return hit;
	}

	/**
	 * Gives back the index where the move starts
	 * 
	 * @return the index where the move starts
	 */
	public int getSource() {
		return source;
	}

	/**
	 * Gives back the row where the move starts
	 * 
	 * @return the row where the move starts
	 */
	public int getSourceRow() {
		return (source >> 3);
	}

	/**
	 * Gives back the index where the move ends
	 * 
	 * @return the index where the move ends
	 */
	public int getDestination() {
		return destination;
	}

	/**
	 * Gives back the row where the move ends
	 * 
	 * @return the row where the move ends
	 */
	public int getDestRow() {
		return (destination >> 3);
	}

	/**
	 * @return the type of the new figure
	 */
	public byte getNewFigureType() {
		return newFigureType;
	}

	/**
	 * Represents the possibilities for offering, rejecting and accepting remis
	 * 
	 * @return the remis state
	 */
	public byte getRemis() {
		return remis;
	}

	/**
	 * The type of the moved figure.
	 * 
	 * @return the type of the moved figure
	 */
	public byte getType() {
		return type;
	}

	/**
	 * Returns <tt>true</tt> if the execution of the move led to the death of a
	 * figure on the board.
	 * 
	 * @return <tt>true</tt> if the execution of the move led to the death of a
	 *         figure on the board. <tt>false</tt> otherwise.
	 */
	public boolean isHit() {
		return hit >= 0;
	}

	/**
	 * Sets the color of the moves figure.
	 * @param color the moved figure.
	 */
	public void setColor(byte color) {
		this.color = color;
	}

	/**
	 * Sets the index of the hit figure. <tt>-1</tt> if no figure should be hit.
	 * @param index the index of the hit figure. <tt>-1</tt> if no figure should be hit.
	 */
	public void setHit(int index) {
		hit = index;
	}

	/**
	 * Sets the type of figure a pawn is changed into if it reaches the opponent's
	 * base line.
	 * @param newFigureType the type of the new figure.
	 */
	public void setNewFigureType(byte newFigureType) {
		this.newFigureType = newFigureType;
	}

	@Override
	public boolean equals(Object obj) {
		Move move;

		try {
			move = (Move) obj;
		} catch (ClassCastException e) {
			return false;
		}

		return this.color == move.color && this.type == move.type && this.source == move.source
				&& this.destination == move.destination && this.hit == move.hit
				&& this.newFigureType == move.newFigureType;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (newFigureType == EMPTY)
			sb.append(Figure.getFigureName(type).charAt(1));
		sb.append(getCoordName(source));
		if (hit >= 0)
			sb.append('x');
		else
			sb.append('-');
		sb.append(getCoordName(destination));
		if (newFigureType != EMPTY)
			sb.append(Figure.getFigureName(newFigureType).charAt(1));
		return sb.toString();
	}
}
