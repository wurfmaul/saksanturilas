package at.jku.chess.saksanturilas.ui;

import static at.jku.chess.saksanturilas.ui.ResourceManager.BLACK_BISHOP;
import static at.jku.chess.saksanturilas.ui.ResourceManager.BLACK_KING;
import static at.jku.chess.saksanturilas.ui.ResourceManager.BLACK_KNIGHT;
import static at.jku.chess.saksanturilas.ui.ResourceManager.BLACK_PAWN;
import static at.jku.chess.saksanturilas.ui.ResourceManager.BLACK_QUEEN;
import static at.jku.chess.saksanturilas.ui.ResourceManager.BLACK_ROOK;
import static at.jku.chess.saksanturilas.ui.ResourceManager.WHITE_BISHOP;
import static at.jku.chess.saksanturilas.ui.ResourceManager.WHITE_KING;
import static at.jku.chess.saksanturilas.ui.ResourceManager.WHITE_KNIGHT;
import static at.jku.chess.saksanturilas.ui.ResourceManager.WHITE_PAWN;
import static at.jku.chess.saksanturilas.ui.ResourceManager.WHITE_QUEEN;
import static at.jku.chess.saksanturilas.ui.ResourceManager.WHITE_ROOK;
import static at.jku.chess.saksanturilas.ui.ResourceManager.getImage;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import at.jku.chess.saksanturilas.board.Board;
import at.jku.chess.saksanturilas.board.BoardListener;
import at.jku.chess.saksanturilas.board.Figure;
import at.jku.chess.saksanturilas.move.Move;

/**
 * The panel that really paints the Swing board and handles the interaction
 * between user and board.
 * 
 * @author Fabian Jordan (0855941)
 * @author Wolfgang Kuellinger (0955711)
 * 
 */
public class ChessPanel extends JComponent {
	private static final long serialVersionUID = 1L;

	private Board board;

	private final SwingUI parentFrame;

	/** color for Black */
	private static final Color TILE_COLOR_1 = Color.getHSBColor(295.f / 360,
			99.f / 100, 79.f / 100);
	/** color for White */
	private static final Color TILE_COLOR_2 = Color.getHSBColor(77.f / 360,
			29.f / 100, 1);
	/** color for inactive black */
	private static final Color TILE_GRAY_1 = Color.GRAY;
	/** color for inactive white */
	private static final Color TILE_GRAY_2 = Color.LIGHT_GRAY;
	/** color for highlighted fields */
	private static final Color TILE_COLOR_HIGHLIGHTED = new Color(255, 0, 0,
			180);
	/** the size of the tiles of the board */
	private int tileSize = 46;
	/** to define, which player is up, which down */
	private boolean isWhiteDown = true;
	/** list of all valid moves to get move-highlighting working */
	private static List<Move> allValidMoves;

	/** represents the current selected piece on the board by its index */
	private int selectedPiece;

	/** indicator for the highlighting */
	private final boolean[] high;

	/** the standard size of the rulers */
	public int rulerSize;
	private int fontSize;

	public ChessPanel(Board board, SwingUI parentFrame) {
		this.board = board;
		this.parentFrame = parentFrame;
		this.setLayout(new BorderLayout());
		high = new boolean[64];
		selectedPiece = -1;

		// listen for clicks on the board
		addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				int x = (7 - ((e.getX() - rulerSize) / tileSize));
				int y = ((e.getY() - rulerSize) / tileSize);
				int selection;
				if (x >= 8 || y >= 8) {
					selection = -1;
				} else {
					selection = x + (y * 8);
					selection = (isWhiteDown) ? 63 - selection : selection;
				}
				if (high[selection]) {
					List<Move> chosenMoves = new ArrayList<Move>(4);
					for (Move m : allValidMoves) {
						if (m.getSource() == selectedPiece
								&& m.getDestination() == selection) {
							chosenMoves.add(m);
						}
					}
					ChessPanel.this.parentFrame.doMove(chosenMoves);
				}
				selectedPiece = selection;
				highlighter();
				repaint();
			}
		});

		setMinimumSize(new Dimension(400, 400));

		board.addBoardListener(new BoardListener() {
			@Override
			public void boardChanged(Board board) {
				repaint();
			}
		});
	}

	/**
	 * Paints a horizontal ruler onto the give canvas.
	 * 
	 * @param g2d
	 *            the canvas
	 * @param top
	 *            indicates whether the ruler is on the top edge or on the
	 *            bottom
	 */
	private void paintHRuler(Graphics2D g2d, boolean top) {
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		// just to be sure ...
		g2d.setColor(Color.BLACK);
		FontMetrics f = g2d.getFontMetrics();
		int offsetY = (top) ? rulerSize : rulerSize + tileSize * 8 + fontSize;

		for (int i = 0; i < 8; i++) {
			String s = Character
					.toString((char) (((isWhiteDown) ? i : (7 - i)) + 'A'));
			int w = f.stringWidth(s);
			g2d.drawString(s, rulerSize + (i * tileSize)
					+ (tileSize / 2 - w / 2), offsetY);
		}
	}

	/**
	 * Paints a vertical ruler onto the give canvas.
	 * 
	 * @param g2d
	 *            the canvas
	 * @param left
	 *            indicates whether the ruler is on the left edge or on the
	 *            right
	 */
	private void paintVRuler(Graphics2D g2d, boolean left) {
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		// just to be sure ...
		g2d.setColor(Color.BLACK);
		FontMetrics f = g2d.getFontMetrics();
		int offsetX = (left) ? rulerSize : rulerSize + tileSize * 8;

		for (int i = 0; i < 8; i++) {
			String s = Integer.toString(((isWhiteDown) ? (7 - i) : i) + 1);
			int w = f.stringWidth(s);
			g2d.drawString(s, (left) ? offsetX - w : offsetX, rulerSize + i
					* tileSize + (tileSize - fontSize / 2));
		}
	}

	@Override
	public void paint(Graphics g) {
		highlighter();
		final Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		rulerSize = min(this.getWidth(), this.getHeight()) / 15;

		tileSize = (min(this.getWidth(), this.getHeight()) - rulerSize * 2) / 8;

		fontSize = rulerSize - 4;

		// if you set the components Font, it gets tricky and he paints the
		// panel over and over and over and ... don't ask me why :D
		g2d.setFont(new Font(Font.MONOSPACED, Font.BOLD, fontSize));

		final int circleSpace = tileSize / 4;
		int x1, y1;
		final Color color1 = (parentFrame.running) ? TILE_COLOR_1 : TILE_GRAY_1;
		final Color color2 = (parentFrame.running) ? TILE_COLOR_2 : TILE_GRAY_2;
		Color c = color1;

		for (int x = 63; x >= 0; x--) {
			x1 = (7 - (x % 8)) * tileSize + rulerSize;
			y1 = (x / 8) * tileSize + rulerSize;

			g2d.setColor(c);
			g2d.fillRect(x1, y1, tileSize, tileSize);

			int arrayX = (isWhiteDown) ? 63 - x : x;
			if (high[arrayX]) {
				g2d.setColor(TILE_COLOR_HIGHLIGHTED);
				g2d.fillOval(x1 + circleSpace, y1 + circleSpace, tileSize
						- (2 * circleSpace), tileSize - (2 * circleSpace));
			}

			if (board != null && board.getFigures()[arrayX] != Figure.EMPTY) {
				getFigure(board.getFigures()[arrayX]).draw(g2d, x1, y1,
						tileSize, tileSize);
			}

			if (x % 8 != 0)
				c = (c == color1) ? color2 : color1;

		}

		paintHRuler(g2d, true);
		paintHRuler(g2d, false);
		paintVRuler(g2d, true);
		paintVRuler(g2d, false);
	}

	/**
	 * Determines which tiles on the board have to be highlighted.
	 */
	private void highlighter() {
		for (int x = 0; x < 64; x++) {
			high[x] = false;
		}
		if (allValidMoves != null && selectedPiece != -1) {
			for (Move m : allValidMoves) {
				if (m.getSource() == selectedPiece) {
					high[m.getSource()] = true;
					high[m.getDestination()] = true;
				}
			}
		}
	}

	/**
	 * Simple helping function to determine the minimum out of to integers.
	 * 
	 * @param i1
	 *            integer one
	 * @param i2
	 *            integer two
	 * @return the minimum out of the two given integers
	 */
	private int min(int i1, int i2) {
		return (i1 < i2) ? i1 : i2;
	}

	/**
	 * Gives back the corresponding image for each possible figure.
	 * 
	 * @param figure
	 *            the figure
	 * @return the corresponding image for each possible figure
	 */
	private NormalImage getFigure(byte figure) {
		switch (figure) {
		case Figure.WHITE_PAWN:
			return new NormalImage(getImage(WHITE_PAWN));
		case Figure.WHITE_ROOK:
			return new NormalImage(getImage(WHITE_ROOK));
		case Figure.WHITE_KNIGHT:
			return new NormalImage(getImage(WHITE_KNIGHT));
		case Figure.WHITE_BISHOP:
			return new NormalImage(getImage(WHITE_BISHOP));
		case Figure.WHITE_QUEEN:
			return new NormalImage(getImage(WHITE_QUEEN));
		case Figure.WHITE_KING:
			return new NormalImage(getImage(WHITE_KING));
		case Figure.BLACK_PAWN:
			return new NormalImage(getImage(BLACK_PAWN));
		case Figure.BLACK_ROOK:
			return new NormalImage(getImage(BLACK_ROOK));
		case Figure.BLACK_KNIGHT:
			return new NormalImage(getImage(BLACK_KNIGHT));
		case Figure.BLACK_BISHOP:
			return new NormalImage(getImage(BLACK_BISHOP));
		case Figure.BLACK_QUEEN:
			return new NormalImage(getImage(BLACK_QUEEN));
		case Figure.BLACK_KING:
			return new NormalImage(getImage(BLACK_KING));
		}
		return null;
	}

	/**
	 * Implements a buffered image with automatic file-loading.
	 */
	public static class NormalImage {
		private BufferedImage image;

		public NormalImage(BufferedImage image) {
			this.image = image;
		}

		/**
		 * Draws the image on a Graphics2D context.
		 * 
		 * @param g
		 *            The Graphics2D object to draw this image to.
		 */
		public void draw(Graphics2D g, int x, int y, int w, int h) {
			// Draw (we just want to place it somewhere, so AffineTransform has
			// this method)
			final AffineTransform transformer = AffineTransform
					.getTranslateInstance(x, y);
			transformer.setToScale(w / image.getWidth(), h / image.getHeight());

			g.drawImage(image, x, y, x + w, y + h, 0, 0, image.getWidth(),
					image.getHeight(), null);
		}
	}

	public boolean flipAppearance() {
		isWhiteDown = !isWhiteDown;
		return isWhiteDown;
	}

	public void setValidMoves(List<Move> l) {
		allValidMoves = l;
	}
}