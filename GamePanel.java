import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class GamePanel extends JFrame {
    private final GameState state;
    private final JButton[][] cells = new JButton[GameState.SIZE][GameState.SIZE];
    private final JLabel statusLabel = new JLabel();
    private final JLabel scoreLabel = new JLabel();
    private final String playerXName;
    private final String playerOName;
    private boolean scoreSaved = false;
    private boolean hasMadeMove = false;
    private final StartPage startPage;

    public GamePanel(String playerXName, String playerOName, StartPage startPage) {
        this(playerXName, playerOName, null, startPage);
    }
    public GamePanel(String playerXName,String playerOName,Score.SavedGame savedGame,StartPage startPage) {
        this.playerXName = playerXName;
        this.playerOName = playerOName;
        this.startPage = startPage;
        this.state = new GameState();
        if (savedGame != null) {
            state.loadSavedState(savedGame.board, savedGame.currentPlayer);
            hasMadeMove = true;
        }
        setTitle("Othello");
        setSize(650, 730);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));
        JPanel topPanel = new JPanel(new GridLayout(2, 1));
        statusLabel.setFont(new Font("Tahoma", Font.BOLD, 20));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 4, 0));
        topPanel.add(statusLabel);
        scoreLabel.setFont(new Font("Tahoma", Font.PLAIN, 16));
        scoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
        topPanel.add(scoreLabel);
        add(topPanel, BorderLayout.NORTH);
        JPanel boardPanel = new JPanel(new GridLayout(GameState.SIZE, GameState.SIZE, 2, 2));
        boardPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        boardPanel.setBackground(Color.DARK_GRAY);
        for (int row = 0; row < GameState.SIZE; row++) {
            for (int col = 0; col < GameState.SIZE; col++) {
                final int r = row;
                final int c = col;
                DiscButton button = new DiscButton();
                button.setFocusPainted(false);
                button.setBorder(BorderFactory.createLineBorder(new Color(220, 240, 220), 1));
                button.setBackground(new Color(45, 125, 65));
                button.setOpaque(true);
                button.addActionListener(e -> handleMove(r, c));

                cells[row][col] = button;
                boardPanel.add(button);
            }
        }
        add(boardPanel, BorderLayout.CENTER);
        JPanel bottomPanel = new JPanel();
        JButton restartButton = new JButton("Restart Game");
        restartButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
        restartButton.addActionListener(e -> {
            state.reset();
            scoreSaved = false;
            hasMadeMove = false;
            Score.clearSavedGame();
            refreshBoard();
        });
        JButton backButton = new JButton("Back to Start");
        backButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
        backButton.addActionListener(e -> dispose());
        bottomPanel.add(restartButton);
        bottomPanel.add(backButton);
        add(bottomPanel, BorderLayout.SOUTH);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (!state.isGameOver() && hasMadeMove) {
                    saveCurrentGame();
                } else if (state.isGameOver()) {
                    Score.clearSavedGame();
                }
                startPage.setVisible(true);
            }
        });
        refreshBoard();
    }
    private void saveCurrentGame() {
        Score.saveGame(playerXName, playerOName,
                state.getBoard(), state.getCurrentPlayer());
    }
    private void handleMove(int row, int col) {
        if (!state.makeMove(row, col)) return;
        hasMadeMove = true;
        if (!state.isGameOver()) {
            saveCurrentGame();
        }
        refreshBoard();
        char skipped = state.getPassedPlayer();
        if (!state.isGameOver() && skipped != ' ') {
            char current = state.getCurrentPlayer();
            String skippedName = (skipped == 'X') ? playerXName : playerOName;
            String skippedColor = (skipped == 'X') ? "Black" : "White";
            String currentName = (current == 'X') ? playerXName : playerOName;
            String currentColor = (current == 'X') ? "Black" : "White";

            JOptionPane.showMessageDialog(this,
                    skippedName + " (" + skippedColor + ") has no valid move.\n"
                            + "Turn skipped - " + currentName + " ("
                            + currentColor + ") plays again.",
                    "No Valid Move",
                    JOptionPane.WARNING_MESSAGE);
        }
        if (state.isGameOver() && !scoreSaved) {
            scoreSaved = true;
            char winner = state.checkWinner();
            int xCount = state.countDisc('X');
            int oCount = state.countDisc('O');
            if (winner == 'X') {
                Score.addWin(playerXName);
                Score.addLoss(playerOName);
                JOptionPane.showMessageDialog(this,
                        "Winner: " + playerXName + " (Black)\nScore: "
                                + xCount + " - " + oCount);
            } else if (winner == 'O') {
                Score.addWin(playerOName);
                Score.addLoss(playerXName);
                JOptionPane.showMessageDialog(this,
                        "Winner: " + playerOName + " (White)\nScore: "
                                + oCount + " - " + xCount);
            } else {
                Score.addDraw(playerXName);
                Score.addDraw(playerOName);
                JOptionPane.showMessageDialog(this,
                        "It's a draw!\nScore: " + xCount + " - " + oCount);
            }
            Score.clearSavedGame();
        }
    }
    private void refreshBoard() {
        char[][] board = state.getBoard();
        for (int row = 0; row < GameState.SIZE; row++) {
            for (int col = 0; col < GameState.SIZE; col++) {
                char mark = board[row][col];
                DiscButton button = (DiscButton) cells[row][col];
                button.setMark(mark);

                boolean valid = !state.isGameOver() && state.isValidMove(row, col);
                button.setLegalMove(valid);
                button.setEnabled(!state.isGameOver() && valid);
            }
        }
        int xCount = state.countDisc('X');
        int oCount = state.countDisc('O');
        scoreLabel.setText("Black (" + playerXName + "): " + xCount
                + "        |        White (" + playerOName + "): " + oCount);
        if (!state.isGameOver()) {
            char current = state.getCurrentPlayer();
            String name = (current == 'X') ? playerXName : playerOName;
            String color = (current == 'X') ? "Black" : "White";
            statusLabel.setText("Turn: " + name + " (" + color + ")");
        } else {
            char winner = state.checkWinner();
            if (winner == 'X') {
                statusLabel.setText(playerXName + " (Black) WINS!");
            } else if (winner == 'O') {
                statusLabel.setText(playerOName + " (White) WINS!");
            } else {
                statusLabel.setText("GAME OVER: DRAW!");
            }
        }
    }
    private static class DiscButton extends JButton {
        private char mark = ' ';
        private boolean legalMove = false;
        void setMark(char mark) {
            this.mark = mark;
            repaint();
        }
        void setLegalMove(boolean legalMove) {
            this.legalMove = legalMove;
            repaint();
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(45, 125, 65));
            g2.fillRect(0, 0, getWidth(), getHeight());
            if (legalMove && mark == ' ') {
                g2.setColor(Color.lightGray);
                int dot = Math.min(getWidth(), getHeight()) / 5;
                int x = (getWidth() - dot) / 2;
                int y = (getHeight() - dot) / 2;
                g2.fillOval(x, y, dot, dot);
            } else if (mark == 'X' || mark == 'O') {
                int size = Math.min(getWidth(), getHeight()) - 18;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;
                if (mark == 'X') {
                    g2.setColor(new Color(15, 15, 15));
                    g2.fillOval(x, y, size, size);
                    g2.setColor(new Color(70, 140, 255));
                    g2.setStroke(new BasicStroke(3));
                    g2.drawOval(x, y, size, size);
                } else {
                    g2.setColor(Color.WHITE);
                    g2.fillOval(x, y, size, size);
                    g2.setColor(new Color(255, 140, 0));
                    g2.setStroke(new BasicStroke(3));
                    g2.drawOval(x, y, size, size);
                }
            }
            if (!isEnabled()) {
                g2.setColor(new Color(0, 0, 0, 35));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
            g2.dispose();
        }
    }
}
