package at.jku.chess.saksanturilas.board;

import java.util.Hashtable;
import java.util.Map.Entry;

/**
 * Enum wurde als Bit Array implementiert und ist somit so effizient wie native
 * datentypen aber typsicher!
 */
public class Figure {
	// we are using chars instead of Strings because it enables the usage of
	// switches.
	private static final Hashtable<Byte, String> names = initNames();
	private static final Hashtable<String, Byte> namesInvert = initNamesInvert();

	public static final byte EMPTY = 0;
	public static final byte WHITE_PAWN = 1;
	public static final byte WHITE_ROOK = 2;
	public static final byte WHITE_KNIGHT = 4;
	public static final byte WHITE_BISHOP = 8;
	public static final byte WHITE_QUEEN = 16;
	public static final byte WHITE_KING = 32;
	public static final byte BLACK_PAWN = 65;
	public static final byte BLACK_ROOK = 66;
	public static final byte BLACK_KNIGHT = 68;
	public static final byte BLACK_BISHOP = 72;
	public static final byte BLACK_QUEEN = 80;
	public static final byte BLACK_KING = 96;

	public static final byte WHITE = 0;
	public static final byte BLACK = 64;

	public static boolean isWhite(byte figure) {
		return (figure & BLACK) == WHITE;
	}

	public static boolean isBlack(byte figure) {
		return (figure & BLACK) == BLACK;
	}

	public static String getColorName(byte color) {
		if (color == WHITE)
			return "w";
		if (color == BLACK)
			return "s";
		throw new ChessException("Not a valid color '%d'.", color);
	}

	public static String getFigureName(byte figure) {
		return names.get(figure);
	}

	/**
	 * Gets the color of a <tt>Figure</tt>
	 * 
	 * @param figure
	 *            The figure to check
	 * @return The color of the given figure
	 */
	public static byte getColorFromType(byte figure) {
		return (byte) (figure & BLACK);
	}

	public static byte getFigureType(String type) {
		return namesInvert.get(type);
	}

	private static Hashtable<Byte, String> initNames() {
		Hashtable<Byte, String> map = new Hashtable<Byte, String>();
		map.put(WHITE_PAWN, "wB");
		map.put(WHITE_ROOK, "wT");
		map.put(WHITE_KNIGHT, "wS");
		map.put(WHITE_BISHOP, "wL");
		map.put(WHITE_QUEEN, "wD");
		map.put(WHITE_KING, "wK");

		map.put(BLACK_PAWN, "sB");
		map.put(BLACK_ROOK, "sT");
		map.put(BLACK_KNIGHT, "sS");
		map.put(BLACK_BISHOP, "sL");
		map.put(BLACK_QUEEN, "sD");
		map.put(BLACK_KING, "sK");
		return map;
	}

	private static Hashtable<String, Byte> initNamesInvert() {
		Hashtable<String, Byte> map = new Hashtable<String, Byte>(names.size());
		for (Entry<Byte, String> e : names.entrySet()) {
			map.put(e.getValue(), e.getKey());
		}
		return map;
	}

	public static String getFullColorName(byte color) {
		if (color == WHITE)
			return "WHITE";
		if (color == BLACK)
			return "BLACK";
		throw new ChessException("Not a valid color '%d'.", color);
	}

	public static String getFullFigureName(byte figure) {
		// filter figures to find out figure without color
		switch (figure & 63) {
		case WHITE_PAWN:
			return "Pawn";
		case WHITE_ROOK:
			return "Rook";
		case WHITE_KNIGHT:
			return "Knight";
		case WHITE_BISHOP:
			return "Bishop";
		case WHITE_QUEEN:
			return "Queen";
		case WHITE_KING:
			return "King";
		default:
			throw new ChessException("Figure %d not valid", figure);
		}
	}
}