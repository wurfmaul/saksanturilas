package at.jku.chess.saksanturilas.ui;

//import java.io.File;
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import at.jku.chess.saksanturilas.board.Board;
import at.jku.chess.saksanturilas.board.ChessException;
import at.jku.chess.saksanturilas.board.ChessUtil;
import at.jku.chess.saksanturilas.board.Figure;
import at.jku.chess.saksanturilas.move.Move;
import at.jku.chess.saksanturilas.player.AbstractPlayer;
import at.jku.chess.saksanturilas.player.DeepIgor;
import at.jku.chess.saksanturilas.player.HumanPlayer;
import at.jku.chess.saksanturilas.player.RandomPlayer;

/**
 * Specifies the user interface for interaction with players.
 * 
 * @author Fabian Jordan (0855941)
 * @author Wolfgang Kuellinger (0955711)
 * 
 */
public abstract class GameUI {
	/** The relative path to the directory in which the players are. */
//	private final String PLAYERPATH = "bin/chess/player";
	/** Number of milliseconds a player is allowed to calculate. */
	protected final int THINKING_TIME = 1000;
	/** The board on which the game is played. */
	protected final Board board;
	/** The players of the game. */
	protected AbstractPlayer p1, p2;
	/** indicates whether the game is running or not */
	public boolean running;

	public GameUI() {
		this.board = new Board();
		running = false;
	}

	/**
	 * Adds a player to the match.
	 * 
	 * @param p
	 *            the player to add.
	 */
	protected void addPlayer(AbstractPlayer p) {
		if (p1 == null) {
			p1 = p;
			p1.setColor(Figure.WHITE);
		} else if (p2 == null) {
			p2 = p;
			p2.setColor(Figure.BLACK);
		} else
			throw new ChessException("Chess can only be played by at most two players.");
	}

	/**
	 * Finds all possible players in the directory that is specified in the
	 * field <tt>PLAYERPATH</tt>
	 * 
	 * @return a lists of possible players
	 */
	protected List<AbstractPlayer> getPossiblePlayers() {
		List<AbstractPlayer> players = new ArrayList<AbstractPlayer>();

//		final File f = new File(PLAYERPATH);
//		final String s = AbstractPlayer.class.getCanonicalName().replaceFirst(
//				AbstractPlayer.class.getSimpleName(), "");
//		String t = null;
//
//		File[] files = f.listFiles();
//		Class<?> p = null;
//
//		for (int i = 0; i < files.length; i++) {
//			try {
//				t = s.concat(files[i].getName()).replaceFirst(".class", "");
//				p = Class.forName(t);
//				if (!Modifier.isAbstract(p.getModifiers()) && Modifier.isPublic(p.getModifiers())) {
//					players.add((AbstractPlayer) p.getConstructor(GameUI.class).newInstance(this));
//				}
//			} catch (ClassNotFoundException e) {
//				throw new ChessException("Could not find player class '%s'.", t);
//			} catch (InstantiationException e) {
//				throw new ChessException("Could not instantiate player class '%s'.", p.getName());
//			} catch (IllegalAccessException e) {
//				throw new ChessException("Not allowed to use Player class '%s'.", p.getName());
//			} catch (SecurityException e) {
//				throw new ChessException("Not allowed to use Player class '%s'.", p.getName());
//			} catch (NoSuchMethodException e) {
//				throw new ChessException("Could not access constructor of Player class '%s'.",
//						p.getName());
//			} catch (IllegalArgumentException e) {
//				throw new ChessException("Did not match constructer of Player class '%s'.",
//						p.getName());
//			} catch (InvocationTargetException e) {
//				throw new ChessException("Could not create Object of Player class '%s'.",
//						p.getName());
//			}
//		}
		players.add(new DeepIgor(this));
		players.add(new HumanPlayer(this));
		players.add(new RandomPlayer(this));
		
		return players;
	}

	/**
	 * Restarts the current game.
	 */
	protected void restart() {
		board.reset();
		startGame();
	}

	/**
	 * Manages the game flow. The main loop that represents the whole game
	 */
	protected final void startGame() {
		final Random seed = new Random(Calendar.getInstance().getTimeInMillis());
		running = true;
		AbstractPlayer curPlayer = p1;

		do {
			// STEP 1: ask player for move
			Move m = curPlayer.chooseMove(board, curPlayer.getColor(), THINKING_TIME, seed);

			// STEP 2: go to STEP 7 if game is over
			if (m == null)
				break;

			// STEP 3: prints the executed move
			printMove(m);

			// STEP 4: execute chosen move
			board.executeMove(m, true);

			// STEP 5: change players
			curPlayer = (curPlayer == p1) ? p2 : p1;

			// STEP 6: go to STEP 1
		} while (board.getResult() == ChessUtil.NO_MATE);

		// STEP 7: print the result
		printResult();
		running = false;
	}

	/**
	 * Asks the user for the next move.
	 * 
	 * @param board
	 *            the board on which the game takes place
	 * @param color
	 *            the current color
	 * @param milliSeconds
	 *            the number of milliseconds the player can calculate
	 * @param seed
	 *            the base of the random choices
	 * @return the move that is going to be taken. <tt>null</tt> if stalemate,
	 *         mate or remis.
	 */
	public abstract Move askForNextMove(Board board, byte color, int milliSeconds, Random seed);

	/**
	 * Asks the user for the choice of players.
	 * 
	 * @return
	 */
	protected abstract boolean askForPlayers();

	/**
	 * Prints the chosen move.
	 * 
	 * @param move
	 *            the chosen move
	 */
	protected abstract void printMove(Move move);

	/**
	 * Prints the result of a game.
	 */
	protected abstract void printResult();

}