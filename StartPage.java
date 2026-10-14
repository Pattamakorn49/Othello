import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

public class StartPage extends JFrame {
    private JTextField player1Field;
    private JTextField player2Field;
    private JButton continueButton;

    private static final int BG_W = 350;
    private static final int BG_H = 420;
    private BufferedImage background;

    public StartPage() {
        Score.ensureFiles();
        loadBackground();
        setTitle("Othello");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Panel that paints start_bg.png as the whole page background
        JPanel panel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (background != null) {
                    g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };
        panel.setPreferredSize(new Dimension(BG_W, BG_H));
        panel.setBackground(new Color(205, 197, 250));

        // Inputs and buttons sit inside the light rounded box of the image
        player1Field = new JTextField();
        player1Field.setBorder(BorderFactory.createTitledBorder("Player 1 (Black)"));
        player1Field.setBounds(68, 148, 214, 48);

        player2Field = new JTextField();
        player2Field.setBorder(BorderFactory.createTitledBorder("Player 2 (White)"));
        player2Field.setBounds(68, 198, 214, 48);

        JButton startButton = new JButton("START");
        startButton.setBounds(100, 258, 150, 34);
        startButton.addActionListener(e -> startGame());

        continueButton = new JButton("CONTINUE");
        continueButton.setBounds(100, 296, 150, 34);
        continueButton.addActionListener(e -> continueGame());

        JButton historyButton = new JButton("HISTORY");
        historyButton.setBounds(100, 334, 150, 34);
        historyButton.addActionListener(e -> showHistory());

        panel.add(player1Field);
        panel.add(player2Field);
        panel.add(startButton);
        panel.add(continueButton);
        panel.add(historyButton);

        // Re-check for a saved game every time this page is shown again
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                updateContinueButton();
            }
        });
        updateContinueButton();

        setContentPane(panel);
        pack();
        setLocationRelativeTo(null);
    }

    private void loadBackground() {
        try {
            File file = new File(System.getProperty("user.dir"), "start_bg.png");
            if (file.exists()) {
                background = ImageIO.read(file);
            }
        } catch (IOException e) {
            System.out.println("Could not load start_bg.png: " + e.getMessage());
        }
    }

    private void updateContinueButton() {
        continueButton.setEnabled(Score.hasSavedGame());
    }

    private void continueGame() {
        Score.SavedGame savedGame = Score.loadGame();
        if (savedGame == null || !Score.hasSavedGame()) {
            JOptionPane.showMessageDialog(this, "No unfinished game found");
            updateContinueButton();
            return;
        }

        GamePanel game = new GamePanel(
                savedGame.playerX,
                savedGame.playerO,
                savedGame,
                this
        );
        game.setVisible(true);
        setVisible(false);
    }

    private void startGame() {
        String player1 = player1Field.getText().trim();
        String player2 = player2Field.getText().trim();

        if (player1.isEmpty() || player2.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter names for both players"
            );
            return;
        }

        if (Score.hasSavedGame()) {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Starting a new game will discard the unfinished game.\n"
                            + "Continue?",
                    "New Game",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (choice != JOptionPane.YES_OPTION) return;
            Score.clearSavedGame();
        }

        GamePanel game = new GamePanel(player1, player2, this);
        game.setVisible(true);
        setVisible(false);
    }

    private void showHistory() {
        java.util.Map<String, int[]> scores = Score.readScore();

        if (scores.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "No game history yet",
                    "Game History",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        java.util.List<java.util.Map.Entry<String, int[]>> sorted =
                new java.util.ArrayList<>(scores.entrySet());
        sorted.sort((a, b) -> Integer.compare(b.getValue()[0], a.getValue()[0]));

        String[] columns = {"Player", "Win", "Loss", "Draw"};
        Object[][] rows = new Object[sorted.size()][4];

        for (int i = 0; i < sorted.size(); i++) {
            String name = sorted.get(i).getKey();
            int[] s = sorted.get(i).getValue();
            int draw = s.length > 2 ? s[2] : 0;
            rows[i] = new Object[]{name, s[0], s[1], draw};
        }

        JTable table = new JTable(rows, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table.setFont(new Font("Tahoma", Font.PLAIN, 16));
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 16));
        table.setEnabled(false);

        for (int col = 0; col < columns.length; col++) {
            table.getColumnModel().getColumn(col)
                    .setCellRenderer(centeredRenderer());
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(
                new Dimension(320, Math.min(240, 40 + rows.length * 28))
        );

        JDialog dialog = new JDialog(this, "Game History", true);
        dialog.setLayout(new BorderLayout(0, 10));
        dialog.add(scrollPane, BorderLayout.CENTER);

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                    dialog,
                    "Are you sure you want to clear all game history?",
                    "Clear History",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (choice == JOptionPane.YES_OPTION) {
                Score.clearScore();
                dialog.dispose();
            }
        });

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dialog.dispose());

        JPanel bottom = new JPanel();
        bottom.add(clearButton);
        bottom.add(closeButton);
        dialog.add(bottom, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private javax.swing.table.DefaultTableCellRenderer centeredRenderer() {
        javax.swing.table.DefaultTableCellRenderer renderer =
                new javax.swing.table.DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        return renderer;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StartPage().setVisible(true));
    }
}
