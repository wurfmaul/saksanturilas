package chess.move;

public class ThreeEqualBoardsRemisMove extends UnrejectableRemisMove {
	@Override
	public String toString() {
		return "3 times same board remis";
	}
}