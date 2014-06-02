package at.jku.chess.saksanturilas.board;

public class ChessException extends RuntimeException {
	private static final long serialVersionUID = 7385293169946165485L;

	public ChessException(String msg, Object... args) {
		super(String.format(msg, args));
	}
}