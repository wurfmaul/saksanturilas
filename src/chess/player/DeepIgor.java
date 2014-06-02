package chess.player;

import static chess.ChessUtil.canBeConsideredEqual;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import app.Game;

import chess.Board;
import chess.ChessUtil;
import chess.Figure;
import chess.move.Move;
import chess.ui.GameUI;

public class DeepIgor extends AbstractPlayer {

	private static final int THRESHOLD = 43;

	/** indicates, whether the thinker should already stop thinking or not */
	private static boolean running;

	public DeepIgor(GameUI ui) {
		super(ui);
		isHumanPlayer = false;
	}

	/**
	 * Implements a heuristic function that tries to evaluate the actual board
	 * situation.
	 * 
	 * @param color
	 *            the color for which the fitness is calculated
	 * @return returns A double number, the smaller the worse for the given
	 *         color. On the other hand it gets higher if it's better for this
	 *         color.
	 */
	@Override
	public int getFitness(Board board, byte color) {
		/** the result */
		int fitness = 0;
		/** all the figures on the board */
		byte[] figures = board.getFigures();

		byte type;

		for (int i = 0; i < figures.length; i++) {
			type = figures[i];
			if (Figure.getColorFromType(type) == color) {

				switch (type & ~Figure.BLACK) {
				case Figure.WHITE_PAWN:
					fitness += 10;
					if (color == Figure.BLACK) {

						if (ChessUtil.isFree(board, i - 8)) {
							// isolated pawn
							fitness -= 1;
							if (figures[i - 8] == Figure.WHITE_PAWN) {
								// isolated by another opponents pawn
								fitness -= 1;
							} else if (figures[i - 8] == Figure.BLACK_PAWN) {
								// not really isolated, friend ahead
								fitness += 1;
							}
						}

						// backups by other pawns behind
						if (figures[i + 7] == Figure.BLACK_PAWN) {
							fitness += 1;
						}
						if (ChessUtil.isValidField(i + 9)
								&& figures[i + 9] == Figure.BLACK_PAWN) {
							fitness += 1;
						}

					} else {

						if (ChessUtil.isFree(board, i + 8)) {
							// isolated pawn
							fitness -= 1;
							if (figures[i + 8] == Figure.BLACK_PAWN) {
								// isolated by another opponents pawn
								fitness -= 1;
							} else if (figures[i + 8] == Figure.WHITE_PAWN) {
								// not really isolated, friend ahead
								fitness += 1;
							}
						}

						// backups by other pawns behind
						if (figures[i - 7] == Figure.WHITE_PAWN) {
							fitness += 1;
						}
						if (ChessUtil.isValidField(i - 9)
								&& figures[i - 9] == Figure.WHITE_PAWN) {
							fitness += 1;
						}

					}
					break;

				case Figure.WHITE_ROOK:
					fitness += 50;
					break;

				case Figure.WHITE_BISHOP:
					if (ChessUtil.getColorOfField(i) == Figure.BLACK) {
						// I'm the bishop on black
						if (color == Figure.BLACK) {
							// I'm the black bishop on black
							fitness -= board.whiteBishopsOnBlack * 2;
							fitness += board.blackBishopsOnWhite * 2;
						} else {
							// I'm the white bishop on black
							fitness -= board.blackBishopsOnWhite * 2;
							fitness += board.whiteBishopsOnBlack * 2;
						}

					} else {
						// I'm the bishop on white
						if (color == Figure.BLACK) {
							// I'm the black bishop on white
							fitness -= board.whiteBishopsOnWhite * 2;
							fitness += board.blackBishopsOnBlack * 2;
						} else {
							// I'm the white bishop on white
							fitness -= board.blackBishopsOnWhite * 2;
							fitness += board.whiteBishopsOnBlack * 2;
						}
					}
					fitness += 30;
					break;

				case Figure.WHITE_KNIGHT:
					fitness += 30;
					break;

				case Figure.WHITE_QUEEN:
					fitness += 100;
					break;

				case Figure.WHITE_KING:
					fitness += 10000;
					break;

				}
			}
		}

		// System.out.println(fitness);

		return fitness;
	}

	@Override
	public Move chooseMove(Board board, byte color, int milliSeconds,
			Random random) {

		final ArrayList<Thinker> thinker = new ArrayList<Thinker>();

		for (Move m : board.getValidMoves(color)) {
			Board b = board.cloneIncompletely();
			if (b.getCheck(ChessUtil.flipColor(color))) {
				if (Game.DEBUG)
					System.err
							.println("this color is in check after one move: "
									+ ChessUtil.flipColor(color));
				continue;
			}
			if (m.getRemis() == Move.REMIS_ACCEPT)
				if (evaluateBoard(b, color, 0) < THRESHOLD)
					return m;
			if (m.getRemis() == Move.REMIS_REJECT)
				if (evaluateBoard(b, color, 0) >= THRESHOLD)
					return m;
			if (m.getRemis() == Move.REMIS_OFFER) {
				continue;
			}

			b.executeMove(m, true);
			thinker.add(new Thinker(b, this, ChessUtil.flipColor(color), m));
		}

		ArrayList<EvalResult> resultList = new ArrayList<DeepIgor.EvalResult>(
				thinker.size());

		for (Thinker t : thinker)
			t.start();

		try {
			Thread.sleep(milliSeconds);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// kicked out the stopper thread as stopping now is quite fast and only
		// makes problems for synchronizing with the board

		// first tell them to please stop thinking
		running = false;

		// then wait for all of them
		for (Thinker t : thinker) {
			try {
				// really waiting, not killing
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		for (Thinker t : thinker) {
			resultList.add(t.getBestMove());
		}

		int qual = Integer.MIN_VALUE;
		Move bestMove = null;

		int i = 1;
		ArrayList<EvalResult> rresults = new ArrayList<EvalResult>(
				resultList.size());
		for (EvalResult er : resultList) {
			if (er.quality > qual) {
				qual = er.quality;
				rresults.clear();
				rresults.add(er);
			} else if (canBeConsideredEqual(er.quality, qual)) {
				rresults.add(er);
			}

			if (Game.DEBUG)
				System.out.printf(
						"Thinker %5d (move: %s) thinks %3d  (qual = %3d)\n",
						i++, er.move, er.quality, qual);
		}

		if (!rresults.isEmpty())
			bestMove = rresults.get(random.nextInt(rresults.size())).move;

		if (Game.DEBUG) {
			System.out.println();
			System.out.println(bestMove);
		}

		return bestMove;
	}

	@Override
	public String getDescription() {
		return "A super intelligent artificial player.";
	}

	private int evaluateBoard(Board board, byte myColor, int level) {
		return evalRecursive(board, myColor, level, Integer.MIN_VALUE,
				Integer.MAX_VALUE);
	}

	private int evalRecursive(Board board, byte myColor, int level, int alpha,
			int beta) {
		byte oppColor = ChessUtil.flipColor(myColor);
		if (level == 0 || !running) {
			int myFitness = getFitness(board, myColor);
			int oppFitness = getFitness(board, oppColor);
			int r = myFitness - oppFitness;
			return r;
		}

		List<Move> validMoves = board.getValidMoves(myColor);
		int localAlpha = Integer.MIN_VALUE;
		for (Move m : validMoves) {
			Board tmp = board.cloneIncompletely();
			tmp.executeMove(m, false);
			int i = -evalRecursive(tmp, oppColor, level - 1, -beta, -alpha);

			if (i > localAlpha) {
				if (i > alpha)
					alpha = i;
				localAlpha = i;
				if (alpha >= beta)
					break;
			}
		}
		return localAlpha;
	}

	private static class EvalResult {
		public final int quality;
		public final Move move;

		public EvalResult(int quality, Move move) {
			this.quality = quality;
			this.move = move;
		}
	}

	private static class Thinker extends Thread {
		private final Board board;
		private final DeepIgor player;
		private final byte color;
		private final Move move;
		private EvalResult bestMove;

		public Thinker(Board board, DeepIgor player, byte color, Move move) {
			this.board = board;
			this.player = player;
			this.color = color;
			this.move = move;
			this.bestMove = null;
		}

		/**
		 * @return the best move
		 */
		public EvalResult getBestMove() {
			if (bestMove == null && Game.DEBUG) {
				System.out.printf("bestmove == null : move = %s\n", move);
			}
			return bestMove;
		}

		@Override
		public void run() {
			// well, of course now you run ...
			running = true;
			int levels = 0;
			final byte oppColor = ChessUtil.flipColor(color);
			final List<Move> moves = board.getValidMoves(color);
			while (running) {
				if (moves.isEmpty()) {
					if (Game.DEBUG) {
						System.err.println("valid moves == empty");
						System.err.println(move);
						System.err.println(board);
					}
					bestMove = new EvalResult(Integer.MAX_VALUE, move);
					break;
				}
				int[] qualities = new int[moves.size()];
				int minQuality = Integer.MAX_VALUE;
				int n = moves.size();

				for (int i = 0; i < n; i++) {

					switch (moves.get(i).getRemis()) {
					case Move.REMIS_ACCEPT:
					case Move.REMIS_OFFER:
						break;

					default:
						Board tmp = board.cloneIncompletely();
						tmp.executeMove(moves.get(i), false);
						int f = player.evaluateBoard(tmp, oppColor, levels);
						qualities[i] = f;
						if (f < minQuality) {
							minQuality = f;
						}
					}

					// quite Russian but effective ;)
					if (!running)
						break;
				}

				bestMove = new EvalResult(minQuality, move);
				levels++;
			}
			if (Game.DEBUG)
				System.out.println(levels);
		}
	}
}