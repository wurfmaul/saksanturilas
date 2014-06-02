package at.jku.chess.saksanturilas.ui;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

public class ResourceManager {
	private static final String IMGDIR = "sprites/";
	
	public static final String DOWN = "down.png";
	public static final String UP = "up.png";
	public static final String CHOICE = "choice.png";
	public static final String ICON = "icon.png";
	public static final String START = "start.png";
	public static final String QUIT = "quit.png";
	public static final String UNDO = "undo.png";
	public static final String WHITE_PAWN = "w_Pawn.png";
	public static final String WHITE_ROOK = "w_Rook.png";
	public static final String WHITE_KNIGHT = "w_Knight.png";
	public static final String WHITE_BISHOP = "w_Bishop.png";
	public static final String WHITE_QUEEN = "w_Queen.png";
	public static final String WHITE_KING = "w_King.png";
	public static final String BLACK_PAWN = "b_Pawn.png";
	public static final String BLACK_ROOK = "b_Rook.png";
	public static final String BLACK_KNIGHT = "b_Knight.png";
	public static final String BLACK_BISHOP = "b_Bishop.png";
	public static final String BLACK_QUEEN = "b_Queen.png";
	public static final String BLACK_KING = "b_King.png";
	
	public static Icon getIcon(String fileName) {
		return new ImageIcon(IMGDIR + fileName);
	}
	
	public static BufferedImage getImage(String fileName) {
		try {
			File imageFile = new File(IMGDIR + fileName);
			return ImageIO.read(imageFile);
		} catch (Exception e) {
		}
		return null;
	}
}
