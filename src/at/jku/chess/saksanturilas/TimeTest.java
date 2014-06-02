package at.jku.chess.saksanturilas;

import java.util.Random;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import at.jku.chess.saksanturilas.board.Board;
import at.jku.chess.saksanturilas.board.ChessUtil;
import at.jku.chess.saksanturilas.board.Figure;

public class TimeTest {
	private static final int TURNS = 5000;
	private static final boolean HIDE_TIMELIMITS = false;
	
	private Board board;
	private Random r;
	private byte color;
	private long time, estimate;
	
	@Before
	public void setUp() throws Exception {
		board = new Board();
		color = Figure.WHITE;
		r = new Random(System.nanoTime());
	}
	
	@Test
	public final void testIsCovered() {
		// isCovered (QuadCore): ~ 20000 ns
		// TODO: isCovered is quite expensive ... use with caution
		time = System.nanoTime();

		for (int i = 0; i < TURNS; i++) {
			ChessUtil.isCovered(board, r.nextInt(64));
		}

		estimate = ((System.nanoTime() - time) / TURNS);
		System.out.printf("%20s: %10d ns\n", "isCovered", estimate);
		
		assertTrue("Warning: isCovered() takes too much time!", HIDE_TIMELIMITS || estimate < 20000);
	}

	@Test
	public final void testGetValidMoves() {
		// getValidMoves (QuadCore): > 900000 ns
		// TODO: getValidMoves is fucking expensive! Only use when necessary!
		time = System.nanoTime();

		for (int i = 0; i < TURNS; i++) {
			board.getValidMoves(color);
		}

		estimate = ((System.nanoTime() - time) / TURNS);
		System.out.printf("%20s: %10d ns\n", "getValidMoves", estimate);

		assertTrue("Warning: getValidMoves() takes too much time!", HIDE_TIMELIMITS || estimate < 250000);
	}
	
	@Test
	public final void testIsHitable(){
		// isHitable (QuadCore): starts at 500 ns, often calculated it drops too
		// 50 ns ...
		time = System.nanoTime();

		for (int i = 0; i < TURNS; i++) {
			ChessUtil.isHitable(board, (byte) r.nextInt(1), r.nextInt(64));
		}

		estimate = ((System.nanoTime() - time) / TURNS);
		System.out.printf("%20s: %10d ns\n", "isHitable", estimate);
		

		assertTrue("Warning: isHitable() takes too much time!", HIDE_TIMELIMITS || estimate < 700);
	}
	
	@Test
	public final void testCloneIncompletely(){
		// cloneIncompletely (QuadCore): 300 - 400 ns
		time = System.nanoTime();

		for (int i = 0; i < TURNS; i++) {
			board.cloneIncompletely();
		}

		estimate = ((System.nanoTime() - time) / TURNS);
		System.out.printf("%20s: %10d ns\n", "cloneIncompletely", estimate);
		
		assertTrue("Warning: cloneIncompletely() takes too much time!", HIDE_TIMELIMITS || estimate < 350);
	}
}
