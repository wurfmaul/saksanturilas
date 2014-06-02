package at.jku.chess.saksanturilas.ui;

import static at.jku.chess.saksanturilas.board.Figure.WHITE;

import java.util.InputMismatchException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;

import at.jku.chess.saksanturilas.board.Board;
import at.jku.chess.saksanturilas.board.ChessException;
import at.jku.chess.saksanturilas.board.ChessUtil;
import at.jku.chess.saksanturilas.move.Move;
import at.jku.chess.saksanturilas.player.AbstractPlayer;

/**
 * Implements the <tt>GameUI</tt> for textual interaction with the players.
 * 
 * @author Fabian Jordan (0855941)
 * @author Wolfgang Kuellinger (0955711)
 *
 */
public class TextUI extends GameUI {
	
	public TextUI() {
		askForPlayers();
		startGame();
	}

	@Override
	public Move askForNextMove(Board board, byte color, int milliSeconds, Random seed) {
		final List<Move> moves = board.getValidMoves(color);
		String input = null;
		int choice;
		int index = 0;
		int max = moves.size();
		final int optionsColumns = 3;
		final int optionsMaxRows = max / optionsColumns + 1;

		// print initial board
		System.out.println(board);
		System.out.println();
		
		// return null if mate, stalemate or remis 
		if (board.getResult() != ChessUtil.NO_MATE) {
			return null;
		}
		
		// lists all possible moves in columns
		System.out.printf("PLAYER %s - possible moves: \n", color == WHITE ? "WHITE" : "BLACK");
		for (int j = 0; j < optionsMaxRows; j++) {
			for (int k = 0; k < optionsColumns; k++) {
				if (j + k * optionsMaxRows < max) {
					index = j + k * optionsMaxRows;
					System.out.printf("   [%2d]: %s", index, moves.get(index));
				}
			}
			System.out.println();
		}
		max--;

		// ask user for choice of move
		do {
			choice = -1;
			System.out.printf("Make your decision ([%d,%d]): ", 0, max);
			Scanner in = null;
			try {
				in = new Scanner(System.in);
				input = in.nextLine().trim();

				if (input.equalsIgnoreCase("exit")) {
					System.out.println("Quit Game.");
					System.exit(0);
				} else if (input.equalsIgnoreCase("restart")) {
					System.out.println("Restart Game.");
					restart();
				} else if (input.equalsIgnoreCase("undo")) {
					System.out.println("Undo latest two plies.");
					undo();
				} else {
					choice = Integer.parseInt(input);
					if (choice < 0 || choice > max)
						throw new ChessException("Invalid option '%d'. Has to be in [%d, %d]!",
								choice, 0, max);
				}
			} catch (NumberFormatException e) {
				System.out.printf("Invalid input '%s'! Has to ba a valid number " +
						"or one of [exit, restart, undo, remis]. " +
						"Please try again!", input);
				System.out.println();
				choice = -1;
			} catch (ChessException e) {
				System.out.println(e.getMessage() + " Please try again!");
				choice = -1;
			} catch (NoSuchElementException e) {
				// EXIT
				System.out.println("Good Bye!");
			} finally {
				if (in != null)
					in.close();
			}

		} while (choice < 0);

		return moves.get(choice);
	}

	@Override
	protected boolean askForPlayers() {
		final String[] playerNames = new String[] { "WHITE", "BLACK" };
		final List<AbstractPlayer> players = getPossiblePlayers();
		AbstractPlayer p;
		AbstractPlayer chosenPlayer = null;
		Scanner in = null;
		int choice = -1;

		// print list of possible players
		for (int i = 0; i <= 1; i++) {
			System.out.printf("Possibilities for Player %s: \n", playerNames[i]);

			for (int j = 0; j < players.size(); j++) {
				p = players.get(j);
				System.out.printf(" [%d]: %s - %s \n", j, p.getClass().getSimpleName(),
						p.getDescription());
			}

			System.out.printf("Make your choice for Player %s [0, %d]: ", playerNames[i],
					players.size() - 1);
			while (choice == -1 || chosenPlayer == null) {
				try {
					in = new Scanner(System.in);
					choice = in.nextInt();
					chosenPlayer = players.get(choice).getClass().getConstructor(GameUI.class)
							.newInstance(this);
				} catch (IndexOutOfBoundsException e) {
					System.out.printf("Choice has to be inbetween 0 and %d: ", players.size() - 1);
					choice = -1;
				} catch (InputMismatchException e) {
					System.out.printf("Choice has to be a number inbetween 0 and %d: ",
							players.size() - 1);
					choice = -1;
				} catch (Throwable e) {
					e.printStackTrace();
					choice = -1;
				} finally {
					if (in != null)
						in.close();
				}
			}
			choice = -1;
			addPlayer(chosenPlayer);
			System.out.printf("Player %s is %s. \n\n", playerNames[i], chosenPlayer.getClass()
					.getSimpleName());
		}

		System.out.println("Get ready, game is about to be started.");
		System.out.println();
		return true;
	}

	@Override
	protected void printMove(Move move) {
		System.out.printf("%s: Chosen option: %s.", move.getColor() == WHITE ? "WHITE" : "BLACK",
				move);
		System.out.println();
	}

	@Override
	protected void printResult() {
		System.out.println(board);
		switch (board.getResult()) {
		case ChessUtil.BLACK_MATE:
			System.out.println("BLACK is mate!");
			break;
		case ChessUtil.WHITE_MATE:
			System.out.println("WHITE is mate!");
			break;
		case ChessUtil.BLACK_STALEMATE:
			System.out.println("BLACK is stalemate!");
			break;
		case ChessUtil.WHITE_STALEMATE:
			System.out.println("WHITE is stalemate!");
			break;
		default:
			System.out.println("REMIS");
		}
	}

	private void undo() {
		board.undo();
		
	}
}