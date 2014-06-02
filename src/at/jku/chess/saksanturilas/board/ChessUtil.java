package at.jku.chess.saksanturilas.board;

import static at.jku.chess.saksanturilas.board.Figure.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import at.jku.chess.saksanturilas.move.Move;

public class ChessUtil {
	/** Possible values for <tt>result</tt>. */
	public static final byte NO_MATE = 0;
	public static final byte BLACK_MATE = 1;
	public static final byte WHITE_MATE = 2;
	public static final byte BLACK_STALEMATE = 3;
	public static final byte WHITE_STALEMATE = 4;
	public static final byte REMIS = 5;

	/**
	 * Adds a new move to a list of moves after checking whether this is
	 * possible (i.e. the king is not covered afterwards)
	 * 
	 * @param board
	 *            the board
	 * @param moves
	 *            the current list of moves
	 * @param move
	 *            the move that is about to be added
	 * @return <tt>true</tt> if <tt>move</tt> was added to <tt>moves</tt>.
	 *         <tt>false</tt> if the king would be covered by opponent after
	 *         performing this move.
	 */
	public static boolean addToMoves(Board board, List<Move> moves, Move move) {
		// simulate the chosen move
		Board b = board.cloneIncompletely();
		b.executeMove(move, false);
	
		// if it would lead to own check, leave it!
		if (b.whiteInCheck && Figure.isWhite(move.getType()))
			return false;
		if (b.blackInCheck && Figure.isBlack(move.getType()))
			return false;
	
		// Otherwise it is possible
		if (!moves.add(move))
			throw new ChessException("Not able to add move '%s' to list of moves", move);
	
		return true;
	}

	/**
	 * Compares two doubles
	 * 
	 * @param a
	 * @param b
	 * @return true if the two doubles can be considered as equal.
	 */
	public static boolean canBeConsideredEqual(double a, double b) {
		return Math.abs(a - b) < 0.000000000000001;
	}

	/**
	 * Checks for three equal Objects in <tt>stack</tt>.
	 * 
	 * @param stack
	 *            the stack in which is about to be tested for three equal
	 *            objects.
	 * @return true if stack contains three equal objects.
	 */
	public static boolean containsTriple(Stack<Integer> stack) {
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		Integer tmp;
	
		for (Integer field : stack) {
			tmp = map.remove(field);
	
			if (tmp != null) {
				if (++tmp == 3)
					return true;
				map.put(field, tmp);
			} else {
				map.put(field, 1);
			}
		}
		return false;
	}

	/**
	 * Flips the color. Changes BLACK to WHITE and WHITE to BLACK
	 * 
	 * @param color
	 *            the color that is wanted to be changed
	 * @return WHITE if color was BLACK, BLACK otherwise.
	 */
	public static byte flipColor(byte color) {
		return color == BLACK ? WHITE : BLACK;
	}

	/**
	 * Calculates the color of the specified field.
	 * 
	 * @param index
	 *            The index of the field
	 * @return Figure.WHITE or Figure.BLACK
	 */
	public static byte getColorOfField(int index) {
		int row = index / 8;
		int col = index % 8;

		if ((col + row) % 2 == 0)
			return Figure.BLACK;
		return Figure.WHITE;
	}

	/**
	 * Decodes the index into a human readable coordinate on the chess field.
	 * 
	 * @param index
	 *            the index on the board
	 * @return human readable coordinate on the chess field.
	 */
	public static String getCoordName(int index) {
		if (index < 0 || index > 63)
			throw new ChessException("Index is out of border: (%d).", index);
	
		StringBuffer sb = new StringBuffer();
		sb.append((char) ('a' + (index % 8)));
		sb.append((index >> 3) + 1);
		return sb.toString();
	}

	/**
	 * Gets all valid moves a figure at <tt>board[index]</tt> can perform
	 * 
	 * @param board
	 *            the current board object
	 * @param history
	 *            the history of moves is important for calculation possible en
	 *            passant hits
	 * @param index
	 *            the chosen index on the board
	 * @return a list of possible moves for figure at <tt>board[index]</tt>
	 */
	public static List<Move> getValidMoves(Board board, Stack<Move> history, int index) {
		final byte[] changableFiguresWhite = { WHITE_BISHOP, WHITE_KNIGHT, WHITE_QUEEN, WHITE_ROOK };
		final byte[] changableFiguresBlack = { BLACK_BISHOP, BLACK_KNIGHT, BLACK_QUEEN, BLACK_ROOK };

		final List<Move> moves = new ArrayList<Move>();
		final byte[] figures = board.getFigures();
		final byte curFigure = figures[index];
		final byte curColor = Figure.getColorFromType(curFigure);
		final int row = index >> 3;
		final int col = index % 8;
		Move move;

		if (curFigure == WHITE_PAWN || curFigure == BLACK_PAWN) {
			int iNextRow, iNextNextRow;
			byte[] rows;
			byte pawnBefore;
			byte[] changeableFigures = null;

			if (curFigure == WHITE_PAWN) {
				iNextRow = index + 8;
				iNextNextRow = index + 16;
				rows = new byte[] { 0, 1, 2, 3, 4, 5, 6, 7 };
				pawnBefore = BLACK_PAWN;
				changeableFigures = changableFiguresWhite;
			} else {
				iNextRow = index - 8;
				iNextNextRow = index - 16;
				rows = new byte[] { 7, 6, 5, 4, 3, 2, 1, 0 };
				pawnBefore = WHITE_PAWN;
				changeableFigures = changableFiguresBlack;
			}

			if (row == rows[1] && isFree(board, iNextRow) && isFree(board, iNextNextRow)) {
				// initially: two steps at once
				addToMoves(board, moves, new Move(board, curFigure, index, iNextNextRow));
			} else if (row == rows[4] && history != null && history.size() > 0) {
				// en passant
				Move pastMove = history.peek();
				if (pastMove.getType() == pawnBefore && pastMove.getSourceRow() == rows[6]
						&& pastMove.getDestRow() == rows[4]) {

					if (col < 7 && pastMove.getDestination() == index + 1) {
						move = new Move(board, curFigure, index, iNextRow + 1);
						move.setHit(index + 1);
						addToMoves(board, moves, move);
					} else if (col > 0 && pastMove.getDestination() == index - 1) {
						move = new Move(board, curFigure, index, iNextRow - 1);
						move.setHit(index - 1);
						addToMoves(board, moves, move);
					}
				}
			}

			if (row == rows[6]) {
				// change pawn to another figure at last row
				if (isFree(board, iNextRow)) {
					for (byte i = 0; i < 4; i++) {
						move = new Move(board, curFigure, index, iNextRow);
						move.setNewFigureType(changeableFigures[i]);
						addToMoves(board, moves, move);
					}
				}
				if (col > 0 && isHitable(board, curColor, iNextRow - 1)) {
					for (byte i = 0; i < 4; i++) {
						move = new Move(board, curFigure, index, iNextRow - 1);
						move.setNewFigureType(changeableFigures[i]);
						addToMoves(board, moves, move);
					}
				}
				if (col < 7 && isHitable(board, curColor, iNextRow + 1)) {
					for (byte i = 0; i < 4; i++) {
						move = new Move(board, curFigure, index, iNextRow + 1);
						move.setNewFigureType(changeableFigures[i]);
						addToMoves(board, moves, move);
					}
				}

			} else {
				// usual hits
				if (col < 7 && isHitable(board, curColor, iNextRow + 1)) {
					addToMoves(board, moves, new Move(board, curFigure, index, iNextRow + 1));
				}
				if (col > 0 && isHitable(board, curColor, iNextRow - 1)) {
					addToMoves(board, moves, new Move(board, curFigure, index, iNextRow - 1));
				}

				// usual single step
				if (isFree(board, iNextRow)) {
					addToMoves(board, moves, new Move(board, curFigure, index, iNextRow));
				}
			}

		} else if (curFigure == WHITE_KNIGHT || curFigure == BLACK_KNIGHT) {
			final byte[] indices = new byte[] { 6, 15, 17, 10, -6, -15, -17, -10 };
			final int maxLeft = col > 2 ? 2 : col; // maxLeft = max(2, col)
			final int maxRight = 7 - col > 2 ? 2 : 7 - col;
			int tempIndex;

			assert (maxLeft < 3 && maxRight < 3);

			for (byte i = 0; i < 8; i++) {
				tempIndex = index + indices[i];
				if (isInInterval(tempIndex, index, maxLeft, maxRight)
						&& isValidDestination(board, curColor, tempIndex)) {
					addToMoves(board, moves, new Move(board, curFigure, index, tempIndex));
				}
			}
		} else if (curFigure == WHITE_KING || curFigure == BLACK_KING) {
			// usual steps
			final byte[] indices = new byte[] { 1, 7, 8, 9, -1, -7, -8, -9 };
			final int maxLeft = col > 1 ? 1 : col; // maxLeft = max(col, 1)
			final int maxRight = 7 - col > 1 ? 1 : 7 - col;
			int checkIndex;

			for (byte i = 0; i < 8; i++) {
				checkIndex = index + indices[i];

				if (isInInterval(checkIndex, index, maxLeft, maxRight)
						&& isValidDestination(board, curColor, checkIndex)
						&& !isCovered(board, checkIndex, curColor)) {
					addToMoves(board, moves, new Move(board, curFigure, index, checkIndex));
				}
			}

			// Rochade
			boolean inCheck;
			boolean canRochadeA;
			boolean canRochadeH;

			if (curColor == WHITE) {
				inCheck = board.whiteInCheck;
				canRochadeA = board.whiteCanRochadeA;
				canRochadeH = board.whiteCanRochadeH;
			} else {
				inCheck = board.blackInCheck;
				canRochadeA = board.blackCanRochadeA;
				canRochadeH = board.blackCanRochadeH;
			}

			if (!inCheck) {
				if (canRochadeA && isFree(board, index - 3) && isFree(board, index - 2)
						&& isFree(board, index - 1) && !isCovered(board, index - 2, curColor)
						&& !isCovered(board, index - 1, curColor)) {
					addToMoves(board, moves, new Move(board, curFigure, index, index - 2));
				}
				if (canRochadeH && isFree(board, index + 1) && isFree(board, index + 2)
						&& !isCovered(board, index + 1, curColor)
						&& !isCovered(board, index + 2, curColor)) {
					addToMoves(board, moves, new Move(board, curFigure, index, index + 2));
				}
			}
		}

		if (curFigure == WHITE_ROOK || curFigure == BLACK_ROOK || curFigure == WHITE_QUEEN
				|| curFigure == BLACK_QUEEN) {

			int leftEdge = row << 3;
			int rightEdge = leftEdge + 7;

			// go up
			int i = index + 8;

			while (isFree(board, i)) {
				addToMoves(board, moves, new Move(board, curFigure, index, i));
				i = i + 8;
			}
			if (isHitable(board, curColor, i)) {
				addToMoves(board, moves, new Move(board, curFigure, index, i));
			}

			// go down
			i = index - 8;
			while (isFree(board, i)) {
				addToMoves(board, moves, new Move(board, curFigure, index, i));
				i = i - 8;
			}
			if (isHitable(board, curColor, i)) {
				addToMoves(board, moves, new Move(board, curFigure, index, i));
			}

			// go left
			i = index - 1;
			while (i >= leftEdge && isFree(board, i)) {
				addToMoves(board, moves, new Move(board, curFigure, index, i--));
			}
			if (i >= leftEdge && isHitable(board, curColor, i)) {
				addToMoves(board, moves, new Move(board, curFigure, index, i));
			}

			// go right
			i = index + 1;
			while (i <= rightEdge && isFree(board, i)) {
				addToMoves(board, moves, new Move(board, curFigure, index, i++));
			}
			if (i <= rightEdge && isHitable(board, curColor, i)) {
				addToMoves(board, moves, new Move(board, curFigure, index, i));
			}
		}

		if (curFigure == WHITE_BISHOP || curFigure == BLACK_BISHOP || curFigure == WHITE_QUEEN
				|| curFigure == BLACK_QUEEN) {

			final int maxLeft = col; // maximum number of cols the figure can go
										// to the left
			final int maxRight = 7 - col; // maximum number of cols the figure
											// can go to the right

			// go upright
			int i = index + 9;
			int m = maxRight - 1;

			while (m >= 0 && isFree(board, i)) {
				addToMoves(board, moves, new Move(board, curFigure, index, i));
				i = i + 9;
				m--;
			}
			if (m >= 0 && isHitable(board, curColor, i)) {
				addToMoves(board, moves, new Move(board, curFigure, index, i));
			}

			// go downright
			i = index - 7;
			m = maxRight - 1;
			while (m >= 0 && isFree(board, i)) {
				addToMoves(board, moves, new Move(board, curFigure, index, i));
				i = i - 7;
				m--;
			}
			if (m >= 0 && isHitable(board, curColor, i)) {
				addToMoves(board, moves, new Move(board, curFigure, index, i));
			}

			// go upleft
			i = index + 7;
			m = maxLeft - 1;
			while (m >= 0 && isFree(board, i)) {
				addToMoves(board, moves, new Move(board, curFigure, index, i));
				i = i + 7;
				m--;
			}
			if (m >= 0 && isHitable(board, curColor, i)) {
				addToMoves(board, moves, new Move(board, curFigure, index, i));
			}

			// go downleft
			i = index - 9;
			m = maxLeft - 1;
			while (m >= 0 && isFree(board, i)) {
				addToMoves(board, moves, new Move(board, curFigure, index, i));
				i = i - 9;
				m--;
			}
			if (m >= 0 && isHitable(board, curColor, i)) {
				addToMoves(board, moves, new Move(board, curFigure, index, i));
			}
		}

		return moves;
	}

	/**
	 * This method helps to find covered fields. It checks whether
	 * <tt>figures[index]</tt> contains <tt>compare</tt>.
	 * 
	 * @param figures
	 *            the battlefield
	 * @param index
	 *            the index on the battlefield
	 * @param compare
	 *            the figure to compare
	 * @return <tt>true</tt> if <tt>figures[index]</tt> == <tt>compare</tt>
	 */
	public static boolean checkCover(byte[] figures, int index, byte compare) {
		try {
			return figures[index] == compare;
		} catch (IndexOutOfBoundsException e) {
			return false;
		}
	}

	/**
	 * This method is very important for the calculation of the valid
	 * destinations of the king. Due to the fact that the king is only allowed
	 * to move towards fields that are not covered by any other figure, all the
	 * surrounding fields have to be checked against coverage.</br></br>
	 * 
	 * <i><b>Important</b>: Quite expensive (~ 20000 nanoseconds on
	 * QuadCore)!</i>
	 * 
	 * @param board
	 *            the board
	 * @param index
	 *            the index on the board
	 * @return <tt>true</tt> if any of the opponent's figures covers
	 *         <tt>board[index]</tt>
	 */
	static public boolean isCovered(Board board, int index) {
		return isCovered(board, index, Figure.getColorFromType(board.getFigures()[index]));
	}

	/**
	 * This method is very important for the calculation of the valid
	 * destinations of the king. Due to the fact that the king is only allowed
	 * to move towards fields that are not covered by any other figure, all the
	 * surrounding fields have to be checked against coverage.
	 * 
	 * @param board
	 *            the board
	 * @param index
	 *            the index on the board
	 * @param curColor
	 *            defines the color which would be in danger at
	 *            <tt>board[index]</tt>
	 * @return <tt>true</tt> if any of the opponent's figures covers
	 *         <tt>board[index]</tt>
	 */
	public static boolean isCovered(Board board, int index, byte curColor) {
		final byte[] figures = board.getFigures();
		final byte oppPawn, oppRook, oppKnight, oppBishop, oppQueen, oppKing;
		final int iNextRow;
		final int row = index >> 3;
		final int col = index % 8;
		byte[] indices;
		int i;
		int maxLeft;
		int maxRight;

		if (curColor == BLACK) {
			oppPawn = WHITE_PAWN;
			oppRook = WHITE_ROOK;
			oppKnight = WHITE_KNIGHT;
			oppBishop = WHITE_BISHOP;
			oppQueen = WHITE_QUEEN;
			oppKing = WHITE_KING;
			iNextRow = index - 8;
		} else {
			oppPawn = BLACK_PAWN;
			oppRook = BLACK_ROOK;
			oppKnight = BLACK_KNIGHT;
			oppBishop = BLACK_BISHOP;
			oppQueen = BLACK_QUEEN;
			oppKing = BLACK_KING;
			iNextRow = index + 8;
		}

		// CHECK FOR PAWNS
		i = iNextRow - 1;
		if (col > 0 && checkCover(figures, i, oppPawn))
			return true;
		i = iNextRow + 1;
		if (col < 7 && checkCover(figures, i, oppPawn))
			return true;

		// CHECK FOR KNIGHTS
		indices = new byte[] { 6, 15, 17, 10, -6, -15, -17, -10 };
		maxLeft = col > 2 ? 2 : col;
		maxRight = 7 - col > 2 ? 2 : 7 - col;

		for (byte j = 0; j < 8; j++) {
			i = index + indices[j];
			if (isInInterval(i, index, maxLeft, maxRight) && checkCover(figures, i, oppKnight))
				return true;
		}

		// CHECK FOR KINGS
		indices = new byte[] { 1, 7, 8, 9, -1, -7, -8, -9 };
		maxLeft = col > 1 ? 1 : col;
		maxRight = 7 - col > 1 ? 1 : 7 - col;

		for (byte j = 0; j < 8; j++) {
			i = index + indices[j];
			if (isInInterval(i, index, maxLeft, maxRight) && checkCover(figures, i, oppKing))
				return true;
		}

		// CHECK FOR ROOK AND QUEEN
		int leftEdge = row << 3;
		int rightEdge = leftEdge + 7;

		// go up
		i = index + 8;

		while (isFree(board, i)) {
			i = i + 8;
		}
		if (checkCover(figures, i, oppRook) || checkCover(figures, i, oppQueen))
			return true;

		// go down
		i = index - 8;
		while (isFree(board, i)) {
			i = i - 8;
		}
		if (checkCover(figures, i, oppRook) || checkCover(figures, i, oppQueen))
			return true;

		// go left
		i = index - 1;
		while (i >= leftEdge && isFree(board, i)) {
			i--;
		}
		if (i >= leftEdge && (checkCover(figures, i, oppRook) || checkCover(figures, i, oppQueen)))
			return true;

		// go right
		i = index + 1;
		while (i <= rightEdge && isFree(board, i)) {
			i++;
		}
		if (i <= rightEdge && (checkCover(figures, i, oppRook) || checkCover(figures, i, oppQueen)))
			return true;

		// CHECK FOR BISHOP AND QUEEN
		maxLeft = col;
		maxRight = 7 - col;

		// go upright
		i = index + 9;
		int m = maxRight - 1;

		while (m >= 0 && isFree(board, i)) {
			i = i + 9;
			m--;
		}
		if (m >= 0 && (checkCover(figures, i, oppBishop) || checkCover(figures, i, oppQueen)))
			return true;

		// go downright
		i = index - 7;
		m = maxRight - 1;
		while (m >= 0 && isFree(board, i)) {
			i = i - 7;
			m--;
		}
		if (m >= 0 && (checkCover(figures, i, oppBishop) || checkCover(figures, i, oppQueen)))
			return true;

		// go upleft
		i = index + 7;
		m = maxLeft - 1;
		while (m >= 0 && isFree(board, i)) {
			i = i + 7;
			m--;
		}
		if (m >= 0 && (checkCover(figures, i, oppBishop) || checkCover(figures, i, oppQueen)))
			return true;

		// go downleft
		i = index - 9;
		m = maxLeft - 1;
		while (m >= 0 && isFree(board, i)) {
			i = i - 9;
			m--;
		}
		if (m >= 0 && (checkCover(figures, i, oppBishop) || checkCover(figures, i, oppQueen)))
			return true;
		return false;
	}

	/**
	 * This method is needed to check for the edges of a move. It returns true
	 * if <tt>index</tt> is within <tt>mean-left</tt> and <tt>mean+right</tt>
	 * 
	 * @param index
	 *            the index that should be checked
	 * @param mean
	 *            the index of the performing figure
	 * @param left
	 *            the maximum amount of cols the figure is allowed to go to the
	 *            left
	 * @param right
	 *            the maximum amount of cols the figure is allowed to go to the
	 *            right
	 * @return <tt>true</tt> if index is in interval
	 *         <tt>[mean - left .. mean + right]</tt>
	 */
	public static boolean isInInterval(int index, int mean, int left, int right) {
		final int im8 = index % 8;
		final int mm8 = mean % 8;
		return mm8 - im8 <= left && im8 - mm8 <= right;
	}

	/**
	 * This method is needed for calculating possible destinations of pawns.
	 * This is important because pawns are only allowed to move diagonally if it
	 * can hit a figure of the opposite color.
	 * 
	 * @param board
	 *            the board
	 * @param curColor
	 *            the current turn's color
	 * @param index
	 *            the index on the board
	 * @return true if the field <tt>board[index]</tt> is occupied by the
	 *         opposite color.
	 */
	public static boolean isHitable(Board board, byte curColor, int index) {
		if (!isValidField(index))
			return false;
		byte enemyFigure = board.getFigures()[index];
		if (enemyFigure == EMPTY)
			return false;
		if (Figure.getColorFromType(enemyFigure) == flipColor(curColor))
			return true;
		return false;
	}

	/**
	 * This method checks whether the chosen field is valid and empty or can be
	 * hit by the current color.
	 * 
	 * @param board
	 *            the board
	 * @param curColor
	 *            the current turn's color
	 * @param index
	 *            the index on the board
	 * @return <tt>true</tt> if position is valid and empty or hitable by
	 *         curColor
	 */
	public static boolean isValidDestination(Board board, byte color, int index) {
		return isFree(board, index) || isHitable(board, color, index);
	}

	/**
	 * This method checks whether a field is in valid ranges and empty.
	 * 
	 * @param board
	 *            the board
	 * @param index
	 *            the index on the board
	 * @return <tt>true</tt> if board[index]</tt> has valid ranges and is empty.
	 */
	public static boolean isFree(Board board, int index) {
		return isValidField(index) && board.getFigures()[index] == EMPTY;
	}

	/**
	 * This method checks whether a field at the board is valid or not (i.e.
	 * checking if index is out of bounds).
	 * 
	 * @param index
	 *            the index on the board
	 * @return <tt>true<tt> if <tt>index</tt> is in interval 0..63
	 */
	public static boolean isValidField(int index) {
		return index >= 0 && index < 64;
	}
}