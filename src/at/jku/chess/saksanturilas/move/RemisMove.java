package at.jku.chess.saksanturilas.move;

import at.jku.chess.saksanturilas.board.Board;

/**
 *  Dummy class which implements a move in which remis is proclaimed
 * 
 * @author Fabian Jordan (0855941)
 * @author Wolfgang Kuellinger (0955711)
 * 
 */
public class RemisMove extends Move {
	
	/**
	 * Constructor of Move to offer remis
	 */
	public RemisMove() {
		remis = REMIS_OFFER;
		source = -1;
		destination = -1;
	}
	
	/**
	 * Constructor of Move to answer an offered remis
	 * @param <tt>acceptRemis</tt> is true if the offer is accepted otherwise false
	 */
	public RemisMove(boolean acceptRemis) {
		remis = (acceptRemis) ? REMIS_ACCEPT : REMIS_REJECT;
	}
	
	@SuppressWarnings("unused")
	private RemisMove(Board board, byte type, int source, int destination) {
		// Defined to hide the super constructor
	}

	@Override
	public String toString() {
		if (remis == REMIS_ACCEPT) return "Accept remis";
		if (remis == REMIS_REJECT) return "Reject remis";
		return "Offer remis";
	}
}