import java.awt.*;
import javax.swing.*;

public class StartPage extends JFrame {
    private JTextField player1Field;
    private JTextField player2Field;

    public StartPage() {
        Score.ensureFiles();
        setTitle("Othello");
        setSize(350, 420);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("OTHELLO");
        title.setFont(new Font("Tahoma", Font.BOLD, 30));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        player1Field = new JTextField();
        player1Field.setBorder(BorderFactory.createTitledBorder("Player 1 (Black)"));
        player1Field.setMaximumSize(new Dimension(300, 50));

        player2Field = new JTextField();
        player2Field.setBorder(BorderFactory.createTitledBorder("Player 2 (White)"));
        player2Field.setMaximumSize(new Dimension(300, 50));

        JButton startButton = new JButton("START");
        startButton.setMaximumSize(new Dimension(150, 40));
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startButton.addActionListener(e -> startGame());

        JButton historyButton = new JButton("HISTORY");
        historyButton.setMaximumSize(new Dimension(150, 40));
        historyButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        historyButton.addActionListener(e -> showHistory());

        panel.add(Box.createVerticalStrut(25));
        panel.add(title);
        panel.add(Box.createVerticalStrut(25));
        panel.add(player1Field);
        panel.add(Box.createVerticalStrut(15));
        panel.add(player2Field);
        panel.add(Box.createVerticalStrut(20));
        panel.add(startButton);
        panel.add(Box.createVerticalStrut(10));
        panel.add(historyButton);

        add(panel);
    }

    private void startGame() {
        if (Score.hasSavedGame()) {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "An unfinished Othello game was found.\n"
                            + "Would you like to continue the previous game?",
                    "Saved Game",
                    JOptionPane.YES_NO_OPTION
            );

            if (choice == JOptionPane.YES_OPTION) {
                Score.SavedGame savedGame = Score.loadGame();

                if (savedGame != null) {
                    GamePanel game = new GamePanel(
                            savedGame.playerX,
                            savedGame.playerO,
                            savedGame,
                            this
                    );
                    game.setVisible(true);
                    setVisible(false);
                    return;
                }
            }
        }

        String player1 = player1Field.getText().trim();
        String player2 = player2Field.getText().trim();

        if (player1.isEmpty() || player2.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter names for both players"
            );
            return;
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
