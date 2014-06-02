package chess;

import static chess.ChessUtil.*;
import static chess.Figure.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import chess.move.HundredPlyRemisMove;
import chess.move.Move;
import chess.move.RemisMove;
import chess.move.ThreeEqualBoardsRemisMove;

/**
 * Represents a board and it's current state of figures and possibilities
 * 
 * @author Fabian Jordan (0855941)
 * @author Wolfgang Kuellinger (0955711)
 * 
 */
public class Board {
	/** The complete board and its figures */
	private byte[] figures;

	/** Indicates the possibility of WHITE to perform the rochade to a */
	boolean whiteCanRochadeA;
	/** Indicates the possibility of WHITE to perform the rochade to h */
	boolean whiteCanRochadeH;
	/** Indicates the possibility of BLACK to perform the rochade to a */
	boolean blackCanRochadeA;
	/** Indicates the possibility of BLACK to perform the rochade to h */
	boolean blackCanRochadeH;

	/** Indicates whether WHITE is in check */
	boolean whiteInCheck;
	/** Indicates whether BLACK is in check */
	boolean blackInCheck;
	/** Gives the current position of the white king */
	int indexWhiteKing;
	/** Gives the current position of the black king */
	int indexBlackKing;

	/** The number of white bishops on white fields */
	public int whiteBishopsOnWhite;
	/** The number of white bishops on black fields */
	public int whiteBishopsOnBlack;
	/** The number of black bishops on white fields */
	public int blackBishopsOnWhite;
	/** The number of black bishops on black fields */
	public int blackBishopsOnBlack;

	/** Gives the current state of this board */
	private byte result;
	/** True if the opponent is offering remis */
	private boolean offeringRemis;
	/** Indicates the plies made since the last hit or pawn move */
	private int countPlies;
	/** The local history of moves */
	private Stack<Move> history;
	/** Stores the boards of a game. */
	private Stack<Integer> historyOfBoards;
	/** Action Listeners for the Swing UI. */
	private final List<BoardListener> listeners;

	public Board() {
		figures = new byte[64];
		listeners = new ArrayList<BoardListener>();
		reset();
	}

	/** Resets the situation at the board. */
	public void reset() {
		figures[0] = WHITE_ROOK;
		figures[1] = WHITE_KNIGHT;
		figures[2] = WHITE_BISHOP;
		figures[3] = WHITE_QUEEN;
		figures[4] = WHITE_KING;
		figures[5] = WHITE_BISHOP;
		figures[6] = WHITE_KNIGHT;
		figures[7] = WHITE_ROOK;
		for (int i = 8; i < 16; i++)
			figures[i] = WHITE_PAWN;

		figures[56] = BLACK_ROOK;
		figures[57] = BLACK_KNIGHT;
		figures[58] = BLACK_BISHOP;
		figures[59] = BLACK_QUEEN;
		figures[60] = BLACK_KING;
		figures[61] = BLACK_BISHOP;
		figures[62] = BLACK_KNIGHT;
		figures[63] = BLACK_ROOK;
		for (int i = 48; i < 56; i++)
			figures[i] = BLACK_PAWN;

		for (int i = 16; i < 48; i++)
			figures[i] = EMPTY;

		whiteCanRochadeA = true;
		whiteCanRochadeH = true;
		blackCanRochadeA = true;
		blackCanRochadeH = true;

		whiteInCheck = false;
		blackInCheck = false;
		indexWhiteKing = 4;
		indexBlackKing = 60;

		whiteBishopsOnWhite = 1;
		whiteBishopsOnBlack = 1;
		blackBishopsOnBlack = 1;
		blackBishopsOnWhite = 1;

		history = new Stack<Move>();
		historyOfBoards = new Stack<Integer>();
		historyOfBoards.push(Arrays.hashCode(figures));
		countPlies = 0;
		offeringRemis = false;
		result = NO_MATE;
	}

	/**
	 * Performs a given move.
	 * 
	 * @param move
	 *            the <tt>Move</tt> that is about to be executed
	 * @param count
	 *            <tt>false</tt> if move is only executed for
	 *            looking-forward-issues. <tt>true</tt> if the move is finally
	 *            executed.
	 */
	public void executeMove(Move move, boolean count) {
		byte r = move.getRemis();
		if (r != Move.NO_REMIS) {
			// check for unrejectable remis offer
			if (r == Move.REMIS_UNREJECTABLE || r == Move.REMIS_ACCEPT) {
				result = REMIS;
				return;
			}
			// check for usual remis offers
			if (r == Move.REMIS_OFFER)
				offeringRemis = true;
			else
				offeringRemis = false;
			return;
		}

		final byte type = move.getType();
		final int source = move.getSource();
		final int destination = move.getDestination();
		final int row = destination >> 3;
		Move consequence = null;

		// it is not allowed to hit the king
		assert (!count || destination != indexBlackKing);
		assert (!count || destination != indexWhiteKing);

		// execute move
		setFigure(source, EMPTY);
		if (move.getHit() >= 0)
			setFigure(move.getHit(), EMPTY);
		setFigure(destination, type);

		// if the move is finally executed permanently
		if (count) {
			// add current move to history
			history.add(move);

			// count ply if no hit and no pawn move was performed
			if (move.isHit() || (move.getType() & 1) == 1) {
				countPlies = 0;
				historyOfBoards = new Stack<Integer>();
			} else {
				countPlies++;
				historyOfBoards.push(Arrays.hashCode(figures));
			}
		}

		// specify modifications by different figures
		switch (type) {
		case BLACK_KING:
			indexBlackKing = destination;
			if (blackCanRochadeA && destination == 58)
				consequence = new Move(this, BLACK_ROOK, 56, 59);
			else if (blackCanRochadeH && destination == 62)
				consequence = new Move(this, BLACK_ROOK, 63, 61);

			blackCanRochadeA = false;
			blackCanRochadeH = false;
			break;

		case WHITE_KING:
			indexWhiteKing = destination;

			if (whiteCanRochadeA && destination == 2)
				consequence = new Move(this, WHITE_ROOK, 0, 3);
			else if (whiteCanRochadeH && destination == 6)
				consequence = new Move(this, WHITE_ROOK, 7, 5);

			whiteCanRochadeA = false;
			whiteCanRochadeH = false;
			break;

		case WHITE_ROOK:
			if (source == 0)
				whiteCanRochadeA = false;
			else if (source == 7)
				whiteCanRochadeH = false;
			break;

		case BLACK_ROOK:
			if (source == 56)
				blackCanRochadeA = false;
			else if (source == 63)
				blackCanRochadeH = false;
			break;

		case WHITE_PAWN:
			if (row == 7) {
				setFigure(destination, move.getNewFigureType());

				// update amount of white bishops
				if (move.getNewFigureType() == WHITE_BISHOP) {
					if (getColorOfField(destination) == WHITE)
						whiteBishopsOnWhite++;
					if (getColorOfField(destination) == BLACK)
						whiteBishopsOnBlack++;
				}
			}
			break;

		case BLACK_PAWN:
			if (row == 0) {
				setFigure(destination, move.getNewFigureType());

				// update amount of white bishops
				if (move.getNewFigureType() == BLACK_BISHOP) {
					if (getColorOfField(destination) == WHITE)
						blackBishopsOnWhite++;
					if (getColorOfField(destination) == BLACK)
						blackBishopsOnBlack++;
				}
			}
			break;

		default:
		}

		// check for figures that are hit
		switch (figures[destination]) {
		case WHITE_ROOK:
			if (destination == 0)
				whiteCanRochadeA = false;
			if (destination == 7)
				whiteCanRochadeH = false;
			break;
		case BLACK_ROOK:
			if (destination == 56)
				blackCanRochadeA = false;
			if (destination == 63)
				blackCanRochadeH = false;
			break;
		case WHITE_BISHOP:
			if (getColorOfField(destination) == Figure.WHITE)
				whiteBishopsOnWhite--;
			if (getColorOfField(destination) == Figure.BLACK)
				whiteBishopsOnBlack--;
			break;
		case BLACK_BISHOP:
			if (getColorOfField(destination) == Figure.WHITE)
				blackBishopsOnWhite--;
			if (getColorOfField(destination) == Figure.BLACK)
				blackBishopsOnBlack--;
			break;

		default:
		}

		// update status of check
		whiteInCheck = isCovered(this, indexWhiteKing);
		blackInCheck = isCovered(this, indexBlackKing);

		// perform consequence move (i.e. rook after rochade of the king)
		if (consequence != null)
			executeMove(consequence, false);
		if (count)
			fireBoardChanged();
	}

	/**
	 * Returns the check status on the board. Finds out whether the specified
	 * color's king is in check or not.
	 * 
	 * @param color
	 *            The color of the player.
	 * @return <tt>true</tt> if king of player <tt>color</tt> is in danger
	 *         (check).
	 */
	public boolean getCheck(byte color) {
		if (color == Figure.WHITE)
			return whiteInCheck;
		if (color == Figure.BLACK)
			return blackInCheck;
		return false;
	}

	/**
	 * Gets all possible moves for one player. Runs through all positions at the
	 * board and gets valid moves.</br></br>
	 * 
	 * <i><b>Important</b>: Pretty expensive (more than 900000 nanoseconds on
	 * QuadCore)!</i>
	 * 
	 * @param color
	 *            the color of the current player.
	 * @return a list of possible moves for the player of color
	 */
	public List<Move> getValidMoves(byte color) {
		List<Move> moves = new ArrayList<Move>();
		if (offeringRemis) {
			moves.add(new RemisMove(true));
			moves.add(new RemisMove(false));
			return moves;
		}

		byte figure;

		for (byte i = 0; i < 64; i++) {
			figure = figures[i];
			if (figure != EMPTY && Figure.getColorFromType(figure) == color) {
				moves.addAll(ChessUtil.getValidMoves(this, history, i));
			}
		}

		if (moves.isEmpty()) {
			
			if (blackInCheck)
				result = BLACK_MATE;
			else if (whiteInCheck)
				result = WHITE_MATE;
			else if (color == WHITE)
				result = WHITE_STALEMATE;
			else
				result = BLACK_STALEMATE;

		} else {
			// 100 ply rule
			if (countPlies >= 99)
				moves.add(new HundredPlyRemisMove());
			else
				moves.add(new RemisMove());

			if (containsTriple(historyOfBoards))
				moves.add(new ThreeEqualBoardsRemisMove());
		}
		return moves;
	}

	/**
	 * Gives the current state of this board
	 * 
	 * @return current state of this board
	 */
	public byte getResult() {
		return result;
	}

	/**
	 * The complete battlefield and its figures
	 * 
	 * @return complete battlefield and its figures
	 */
	public byte[] getFigures() {
		return figures;
	}

	/**
	 * @return the history
	 */
	public Stack<Move> getHistory() {
		return history;
	}

	/**
	 * Builds new board with the same figure constellation and the indices of
	 * the kings. All other values are not copied.
	 * 
	 * @return incomplete copy of this board
	 */
	public Board cloneIncompletely() {
		Board b = new Board();
		b.figures = this.figures.clone();
		b.indexBlackKing = this.indexBlackKing;
		b.indexWhiteKing = this.indexWhiteKing;
		return b;
	}

	/**
	 * Adds a <tt>BoardListener</tt> to the board.
	 * 
	 * @param boardListener
	 *            the listener to be added
	 */
	public void addBoardListener(BoardListener boardListener) {
		listeners.add(boardListener);
	}

	/**
	 * Undo the last two plies.
	 * 
	 * @return the color of the player that performs the undo.
	 */
	public byte undo() {
		assert (history.size() >= 2);

		// get color of the player that performs the undo
		byte color = flipColor(history.peek().getColor());

		// perform undo
		List<Move> tempHistory = history;
		reset();
		for (int i = 0; i < tempHistory.size() - 2; i++) {
			executeMove(tempHistory.get(i), true);
		}
		fireBoardChanged();

		return color;
	}

	@Override
	public String toString() {
		byte figure;
		StringBuffer sb = new StringBuffer();
		sb.append("   ===========================================");
		sb.append("\n");

		for (int i = 7; i >= 0; i--) {
			sb.append(" ");
			sb.append(i + 1);
			sb.append(" ||");
			for (int j = 0; j <= 7; j++) {
				figure = figures[(i << 3) + j];
				sb.append(" ");
				if (figure != EMPTY) {
					sb.append(Figure.getFigureName(figure));
				} else {
					sb.append("  ");
				}
				sb.append(" |");
			}
			if (i > 0) {
				sb.append("|\n --||----|----|----|----|----|----|----|----||\n");
			}
		}
		sb.append("|\n   ===========================================\n");
		sb.append("      a  | b  | c  | d  | e  | f  | g  | h");
		return sb.toString();
	}

	/**
	 * Fires a notification to all listeners.
	 */
	private void fireBoardChanged() {
		for (BoardListener l : listeners) {
			l.boardChanged(this);
		}
	}

	/**
	 * Sets a position at the board to the given figure.
	 * 
	 * @param index
	 *            the chosen index at the board
	 * @param figure
	 *            the figure that is inserted at this position
	 */
	private void setFigure(int index, byte figure) {
		figures[index] = figure;
	}
}