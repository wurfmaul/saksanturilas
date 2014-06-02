package at.jku.chess.saksanturilas.ui;

import static at.jku.chess.saksanturilas.ui.ResourceManager.CHOICE;
import static at.jku.chess.saksanturilas.ui.ResourceManager.DOWN;
import static at.jku.chess.saksanturilas.ui.ResourceManager.ICON;
import static at.jku.chess.saksanturilas.ui.ResourceManager.QUIT;
import static at.jku.chess.saksanturilas.ui.ResourceManager.START;
import static at.jku.chess.saksanturilas.ui.ResourceManager.UNDO;
import static at.jku.chess.saksanturilas.ui.ResourceManager.UP;
import static at.jku.chess.saksanturilas.ui.ResourceManager.getIcon;
import static at.jku.chess.saksanturilas.ui.ResourceManager.getImage;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import at.jku.chess.saksanturilas.board.Board;
import at.jku.chess.saksanturilas.board.ChessException;
import at.jku.chess.saksanturilas.board.ChessUtil;
import at.jku.chess.saksanturilas.board.Figure;
import at.jku.chess.saksanturilas.move.Move;
import at.jku.chess.saksanturilas.player.AbstractPlayer;

/**
 * Implements the <tt>GameUI</tt> for graphical interaction (using Swing) with
 * the players.
 * 
 * @author Fabian Jordan (0855941)
 * @author Wolfgang Kuellinger (0955711)
 * 
 */
public class SwingUI extends GameUI {
	private static final int SIDEBAR_MIN_WIDTH = 200;
	private static final int SIDEBAR_MIN_HEIGHT = 100;
	private static final Font MONO_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);

	/** the move chosen in the ListModel */
	private Move chosenMove;
	/** the frame itself */
	private JFrame frame;
	/** the panel to represent the board */
	private ChessPanel chessPanel;
	/** a list for the valid moves */
	@SuppressWarnings("rawtypes")
	private JList movesList;
	/** A list that represents the history of the game. */
	@SuppressWarnings("rawtypes")
	private JList historyList;

	/** button in the tool bar */
	private JButton undoToolButton;
	/** item in the menu */
	private JMenuItem undoMenu;
	/** MenuItem for the flipper */
	private JMenuItem flipMenu;
	/** ToolbarButton for the flipper */
	private JButton flipButton;
	/** button to declare user chose his move */
	private JButton buttonSubmit;
	/** label to give feedback about current state of board */
	private JLabel statusLabel;
	/** a label to give feedback about the size of the list */
	private JLabel labelPossibleMoves;
	/** for awaiting the GUI-interactions */
	private CountDownLatch waitingLatch;
	
	private Action exitAction = new AbstractAction("Quit") {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent arg0) {
			frame.dispose();
			System.exit(0);
		}
	};

	private Action restartAction = new AbstractAction("New Game") {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			if (JOptionPane.showConfirmDialog(frame,
					"Do you really want to restart the game?", "Restart Game",
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				if (askForPlayers())
					restart();
			}
		}
	};

	private Action flipAppearanceAction = new AbstractAction("Flip appearance") {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent ae) {
			if (ae.getSource() == flipButton)
				flipMenu.setSelected(flipButton.isSelected());
			else
				flipButton.setSelected(flipMenu.isSelected());

			if (chessPanel.flipAppearance()) {
				flipMenu.setIcon(getIcon(DOWN));
				flipButton.setIcon(getIcon(DOWN));
			} else {
				flipMenu.setIcon(getIcon(UP));
				flipButton.setIcon(getIcon(UP));
			}
			chessPanel.repaint();
		}
	};

	private Action undoAction = new AbstractAction("Undo move") {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			undoMove();
		}
	};

	@Override
	public Move askForNextMove(Board board, byte color, int milliSeconds,
			Random seed) {

		updatePossibleMoves(board.getValidMoves(color));
		undoMenu.setEnabled(board.getHistory().size() >= 2);
		undoToolButton.setEnabled(undoMenu.isEnabled());

		// initialize the count down latch with 1
		waitingLatch = new CountDownLatch(1);
		try {
			// wait until the button was clicked and therefore the count down
			// latch counted down
			waitingLatch.await();
		} catch (InterruptedException e) {
			// don't really know, when this here could happen ... but necessary
			e.printStackTrace();
		}
		buttonSubmit.setEnabled(false);
		undoMenu.setEnabled(false);
		undoToolButton.setEnabled(false);

		// return the move, the list model chose
		return chosenMove;
	}

	@Override
	protected boolean askForPlayers() {
		List<AbstractPlayer> players = getPossiblePlayers();
		Object[] options = players.toArray();
		Object selectedPlayer;

		p1 = null;
		p2 = null;

		int selection = JOptionPane.showOptionDialog(frame,
				"Choose the player for White", "Choose player", 0,
				JOptionPane.QUESTION_MESSAGE, getIcon(CHOICE), options, options[0]);

		if (selection < 0)
			return false;

		assert (selection >= 0);

		selectedPlayer = players.get(selection);
		try {
			selectedPlayer = selectedPlayer.getClass()
					.getConstructor(GameUI.class).newInstance(this);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		addPlayer((AbstractPlayer) selectedPlayer);

		selection = JOptionPane.showOptionDialog(frame,
				"Choose the player for Black", "Choose player", 0,
				JOptionPane.QUESTION_MESSAGE, getIcon(CHOICE), options, options[0]);

		if (selection < 0)
			return false;

		selectedPlayer = players.get(selection);
		try {
			selectedPlayer = selectedPlayer.getClass()
					.getConstructor(GameUI.class).newInstance(this);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		addPlayer((AbstractPlayer) selectedPlayer);
		return true;
	}

	@Override
	protected void printMove(Move move) {
		String string;
		switch (move.getRemis()) {
		case Move.NO_REMIS:
			if (move.isHit()) {
				string = String.format(
						"Player '%s' moved and hit with '%s' from %s to %s.",
						Figure.getFullColorName(move.getColor()),
						Figure.getFullFigureName(move.getType()),
						ChessUtil.getCoordName(move.getSource()),
						ChessUtil.getCoordName(move.getDestination()));
			} else {
				string = String.format(
						"Player '%s' moved with '%s' from %s to %s.",
						Figure.getFullColorName(move.getColor()),
						Figure.getFullFigureName(move.getType()),
						ChessUtil.getCoordName(move.getSource()),
						ChessUtil.getCoordName(move.getDestination()));
			}
			break;
		case Move.REMIS_ACCEPT:
			string = String.format("Player '%s' accepted remis offer.",
					Figure.getFullColorName(move.getColor()));
			break;
		case Move.REMIS_OFFER:
			string = String.format("Player '%s' offers remis.",
					Figure.getFullColorName(move.getColor()));
			break;
		case Move.REMIS_REJECT:
			string = String.format("Player '%s' rejects remis offer.",
					Figure.getFullColorName(move.getColor()));
			break;
		case Move.REMIS_UNREJECTABLE:
			string = String.format("Player '%s' offers unrejectable remis.",
					Figure.getFullColorName(move.getColor()));
			break;
		default:
			throw new ChessException("Remis value %d of Move invalid",
					move.getRemis());
		}
		statusLabel.setText(string);

		updateHistory();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void printResult() {
		statusLabel.setForeground(Color.RED);
		statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
		switch (board.getResult()) {
		case ChessUtil.BLACK_MATE:
			statusLabel.setText("BLACK is mate!");
			break;
		case ChessUtil.WHITE_MATE:
			statusLabel.setText("WHITE is mate!");
			break;
		case ChessUtil.BLACK_STALEMATE:
			statusLabel.setText("BLACK is stalemate!");
			break;
		case ChessUtil.WHITE_STALEMATE:
			statusLabel.setText("WHITE is stalemate!");
			break;
		default:
			statusLabel.setText("REMIS");
		}
		movesList.setListData(new Object[] {});
		labelPossibleMoves.setText("no possible moves");
		buttonSubmit.setEnabled(false);
		chessPanel.repaint();
	}

	@Override
	protected void restart() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				SwingUI.super.restart();
			}
		}).start();
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
		}
		statusLabel.setForeground(Color.BLACK);
		chessPanel.repaint();
		updateHistory();
	}

	public void doMove(List<Move> m) {
		if (m.isEmpty())
			return;
		if (m.size() == 1) {
			doMove(m.get(0));
		} else {
			final String[] options = { "Bishop", "Knight", "Queen", "Rook" };
			int response = JOptionPane.showOptionDialog(frame,
					"Which figure would you like to change your pawn into?",
					"Choose figure", 0, JOptionPane.QUESTION_MESSAGE, getIcon(CHOICE), options, options[0]);
			assert (response >= 0);
			doMove(m.get(response));
		}
	}

	private void doMove(Move m) {
		chosenMove = m;
		waitingLatch.countDown();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void initAndShow() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}

		frame = new JFrame("Saksanturilas 1.0.1.0.1.0 ALPHA");
		{
			frame.setIconImage(getImage(ICON));
			frame.setLayout(new BorderLayout());
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(650, 500);
			frame.setMinimumSize(new Dimension(936, 595));
		}

		chessPanel = new ChessPanel(board, this);
		{
			chessPanel.setBackground(Color.WHITE);
			chessPanel.setDoubleBuffered(true);
			frame.add(chessPanel, BorderLayout.CENTER);
		}

		// menu bar
		JMenuBar menubar = new JMenuBar();
		{
			JMenuItem x;

			JMenu fileM = new JMenu("File");
			{
				x = new JMenuItem(restartAction);
				x.setIcon(getIcon(START));
				x.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
						ActionEvent.CTRL_MASK));
				fileM.add(x);

				fileM.addSeparator();

				x = new JMenuItem(exitAction);
				x.setIcon(getIcon(QUIT));
				x.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
						ActionEvent.CTRL_MASK));
				fileM.add(x);

				menubar.add(fileM);
			}

			JMenu editM = new JMenu("Edit");
			{
				undoMenu = new JMenuItem(undoAction);
				undoMenu.setIcon(getIcon(UNDO));
				undoMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
						ActionEvent.CTRL_MASK));
				undoMenu.setEnabled(false);
				editM.add(undoMenu);

				editM.addSeparator();
				
				flipMenu = new JMenuItem(flipAppearanceAction);
				flipMenu.setIcon(getIcon(DOWN));
				flipMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,
						ActionEvent.CTRL_MASK));
				editM.add(flipMenu);

				menubar.add(editM);
			}

			frame.setJMenuBar(menubar);
		}
		// ...

		// tool bar
		JPanel north = new JPanel(new FlowLayout(FlowLayout.LEFT));

		frame.add(north, BorderLayout.NORTH);
		JToolBar jtb = new JToolBar("File");
		{
			JButton b = new JButton(getIcon(START));
			b.setToolTipText("Start new game");
			b.addActionListener(restartAction);
			jtb.add(b);

			b = new JButton(getIcon(QUIT));
			b.setToolTipText("Quit program");
			b.addActionListener(exitAction);
			jtb.add(b);

			north.add(jtb);
		}

		jtb = new JToolBar("Edit");
		{
			undoToolButton = new JButton(getIcon(UNDO));
			undoToolButton.setToolTipText("Undo last move");
			undoToolButton.addActionListener(undoAction);
			undoToolButton.setEnabled(false);
			jtb.add(undoToolButton);

			flipButton = new JButton(getIcon(DOWN));
			flipButton.setToolTipText("Flip appearance");
			flipButton.addActionListener(flipAppearanceAction);
			jtb.add(flipButton);

			north.add(jtb);
		}
		// ...

		// list for valid moves
		JPanel sidebarMoves = new JPanel(new BorderLayout());
		{
			sidebarMoves.setPreferredSize(new Dimension(SIDEBAR_MIN_WIDTH,
					SIDEBAR_MIN_HEIGHT));

			JPanel panelMovesTitle = new JPanel(new BorderLayout());
			{
				JLabel labelMovesTitle = new JLabel(
						"<html><h3>POSSIBLE MOVES</h3></html>", JLabel.CENTER);
				panelMovesTitle.add(labelMovesTitle, BorderLayout.NORTH);

				labelPossibleMoves = new JLabel("0 possible moves",
						JLabel.CENTER);
				panelMovesTitle.add(labelPossibleMoves, BorderLayout.SOUTH);

				sidebarMoves.add(panelMovesTitle, BorderLayout.NORTH);
			}

			JScrollPane scrollPaneList = new JScrollPane(
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			{
				movesList = new JList();
				{
					movesList.setFont(MONO_FONT);
					movesList
							.addListSelectionListener(new ListSelectionListener() {

								@Override
								public void valueChanged(ListSelectionEvent e) {
									if (movesList.getSelectedIndex() >= 0) {
										Object o = movesList.getSelectedValue();
										if (o != null) {
											chosenMove = (Move) o;
										}
										buttonSubmit.setEnabled(true);
										movesList.transferFocus();
									} else {
										chosenMove = null;
										buttonSubmit.setEnabled(false);
									}
								}
							});

					movesList.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseClicked(MouseEvent e) {
							if (e.getClickCount() == 2) {
								if (chosenMove != null) {
									buttonSubmit.doClick();
								}
							}
						}
					});
					movesList
							.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					movesList.setCellRenderer(new DefaultListCellRenderer());

					scrollPaneList.setViewportView(movesList);
				}

				sidebarMoves.add(scrollPaneList, BorderLayout.CENTER);
			}

			buttonSubmit = new JButton("Choose");
			{
				buttonSubmit.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						doMove(chosenMove);
					}
				});
				buttonSubmit.setEnabled(false);

				sidebarMoves.add(buttonSubmit, BorderLayout.SOUTH);
			}
		}

		JPanel sidebarHistory = new JPanel(new BorderLayout());
		{
			sidebarHistory.setPreferredSize(new Dimension(SIDEBAR_MIN_WIDTH,
					SIDEBAR_MIN_HEIGHT));

			JLabel labelHistoryTitle = new JLabel(
					"<html><h3>HISTORY</h3></html>", SwingConstants.CENTER);
			{
				sidebarHistory.add(labelHistoryTitle, BorderLayout.NORTH);
			}

			JScrollPane scrollPaneHistory = new JScrollPane(
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			{
				historyList = new JList();
				{
					historyList.setFont(MONO_FONT);
					historyList.setCellRenderer(new DefaultListCellRenderer());
					scrollPaneHistory.setViewportView(historyList);
				}
				sidebarHistory.add(scrollPaneHistory, BorderLayout.CENTER);
			}
		}

		JPanel panelMain = new JPanel(new BorderLayout());
		{
			panelMain.add(sidebarMoves, BorderLayout.WEST);
			panelMain.add(chessPanel, BorderLayout.CENTER);
			panelMain.add(sidebarHistory, BorderLayout.EAST);

			frame.add(panelMain);
		}

		// status bar
		statusLabel = new JLabel("Status", JLabel.LEFT);
		{
			statusLabel.setBorder(new LineBorder(Color.GRAY));
			frame.add(statusLabel, BorderLayout.SOUTH);
		}

		frame.pack();
		frame.setVisible(true);
		if (askForPlayers())
			restart();
	}

	private void undoMove() {
		byte color = board.undo();
		updateHistory();
		updatePossibleMoves(board.getValidMoves(color));
	}

	@SuppressWarnings("unchecked")
	private void updateHistory() {
		historyList.setListData(board.getHistory());
		historyList.repaint();
		undoMenu.setEnabled(board.getHistory().size() >= 2);
		undoToolButton.setEnabled(undoMenu.isEnabled());
	}

	@SuppressWarnings("unchecked")
	private void updatePossibleMoves(List<Move> moves) {
		chessPanel.setValidMoves(moves);
		labelPossibleMoves.setText(moves.size() + " possible moves");
		movesList.setListData(moves.toArray());
		movesList.repaint();
	}

	public static void show() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new SwingUI().initAndShow();
			}
		});
	}
}