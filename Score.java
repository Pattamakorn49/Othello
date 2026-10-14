import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;

public class Score {
    private static final String DATA_FOLDER = System.getProperty("user.dir");
    private static final File SCORE_FILE = new File(DATA_FOLDER, "score.txt");
    private static final File SAVE_FILE = new File(DATA_FOLDER, "game_save.txt");

    // Create both data files at startup so they are always in the same folder.
    public static void ensureFiles() {
        try {
            if (!SCORE_FILE.exists()) {
                SCORE_FILE.createNewFile();
            }
            if (!SAVE_FILE.exists()) {
                SAVE_FILE.createNewFile();
            }
        } catch (IOException e) {
            System.out.println("Could not create data files: " + e.getMessage());
        }
    }

    // score array = { win, loss, draw }
    public static Map<String, int[]> readScore() {
        ensureFiles();
        Map<String, int[]> scores = new LinkedHashMap<>();
        if (!SCORE_FILE.exists()) return scores;

        try {
            for (String line : Files.readAllLines(SCORE_FILE.toPath(), StandardCharsets.UTF_8)) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] data = line.split("\\|");

                if (data.length == 3) {
                    try {
                        scores.put(data[0], new int[]{
                                Integer.parseInt(data[1]),
                                Integer.parseInt(data[2]),
                                0
                        });
                    } catch (NumberFormatException ignored) {
                    }
                } else if (data.length == 4) {
                    try {
                        scores.put(data[0], new int[]{
                                Integer.parseInt(data[1]),
                                Integer.parseInt(data[2]),
                                Integer.parseInt(data[3])
                        });
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Could not read score.txt: " + e.getMessage());
        }

        return scores;
    }

    public static void writeScore(Map<String, int[]> scores) {
        ensureFiles();
        try (Writer writer = new OutputStreamWriter(
                new FileOutputStream(SCORE_FILE), StandardCharsets.UTF_8)) {

            for (Map.Entry<String, int[]> entry : scores.entrySet()) {
                int[] s = entry.getValue();
                int draw = s.length > 2 ? s[2] : 0;

                writer.write(entry.getKey() + "|"
                        + s[0] + "|"
                        + s[1] + "|"
                        + draw
                        + System.lineSeparator());
            }
        } catch (IOException e) {
            System.out.println("Could not write score.txt: " + e.getMessage());
        }
    }

    public static void clearScore() {
        ensureFiles();
        try (Writer writer = new OutputStreamWriter(
                new FileOutputStream(SCORE_FILE), StandardCharsets.UTF_8)) {
            writer.write("");
        } catch (IOException e) {
            System.out.println("Could not clear score.txt: " + e.getMessage());
        }
    }

    private static void addResult(String playerName, int index) {
        if (playerName == null || playerName.trim().isEmpty()) return;

        Map<String, int[]> scores = readScore();
        scores.computeIfAbsent(playerName, k -> new int[]{0, 0, 0})[index]++;
        writeScore(scores);
    }
    
    public static void addWin(String n)  { addResult(n, 0); }
    public static void addLoss(String n) { addResult(n, 1); }
    public static void addDraw(String n) { addResult(n, 2); }

    public static String getScoreText() {
        Map<String, int[]> scores = readScore();
        if (scores.isEmpty()) return "No game history yet";

        StringBuilder text = new StringBuilder();
        text.append(String.format("%-12s %6s %6s %6s%n",
                "Player", "Win", "Loss", "Draw"));
        text.append("--------------------------------\n");

        for (Map.Entry<String, int[]> entry : scores.entrySet()) {
            int[] s = entry.getValue();
            int draw = s.length > 2 ? s[2] : 0;

            text.append(String.format("%-12s %6d %6d %6d%n",
                    entry.getKey(), s[0], s[1], draw));
        }

        return text.toString();
    }

    public static class SavedGame {
        public final String playerX;
        public final String playerO;
        public final char[][] board;
        public final char currentPlayer;

        public SavedGame(String playerX, String playerO,
                         char[][] board, char currentPlayer) {
            this.playerX = playerX;
            this.playerO = playerO;
            this.board = board;
            this.currentPlayer = currentPlayer;
        }
    }

    public static void saveGame(String playerX, String playerO,
                                char[][] board, char currentPlayer) {
        ensureFiles();
        try (Writer writer = new OutputStreamWriter(
                new FileOutputStream(SAVE_FILE), StandardCharsets.UTF_8)) {

            writer.write("PlayerX=" + playerX + System.lineSeparator());
            writer.write("PlayerO=" + playerO + System.lineSeparator());
            writer.write("Board=" + flattenBoard(board) + System.lineSeparator());
            writer.write("CurrentPlayer=" + currentPlayer + System.lineSeparator());
        } catch (IOException e) {
            System.out.println("Could not save game_save.txt: " + e.getMessage());
        }
    }


    public static void clearSavedGame() {
        ensureFiles();
        try (Writer writer = new OutputStreamWriter(
                new FileOutputStream(SAVE_FILE), StandardCharsets.UTF_8)) {
            writer.write("");
        } catch (IOException e) {
            System.out.println("Could not clear game_save.txt: " + e.getMessage());
        }
    }

    public static SavedGame loadGame() {
        ensureFiles();
        if (!SAVE_FILE.exists() || SAVE_FILE.length() == 0) return null;

        try {
            String playerX = "";
            String playerO = "";
            String boardText = "";
            char currentPlayer = 'X';

            for (String line : Files.readAllLines(SAVE_FILE.toPath(), StandardCharsets.UTF_8)) {
                if (line.startsWith("PlayerX=")) {
                    playerX = line.substring("PlayerX=".length());
                } else if (line.startsWith("PlayerO=")) {
                    playerO = line.substring("PlayerO=".length());
                } else if (line.startsWith("Board=")) {
                    boardText = line.substring("Board=".length());
                } else if (line.startsWith("CurrentPlayer=")) {
                    String value = line.substring("CurrentPlayer=".length());
                    if (!value.isEmpty()) currentPlayer = value.charAt(0);
                }
            }

            if (playerX.isEmpty() || playerO.isEmpty()
                    || boardText.length() != GameState.SIZE * GameState.SIZE
                    || (currentPlayer != 'X' && currentPlayer != 'O')) {
                return null;
            }

            return new SavedGame(playerX, playerO,
                    unflattenBoard(boardText), currentPlayer);
        } catch (IOException e) {
            return null;
        }
    }

    public static boolean hasSavedGame() {
        SavedGame game = loadGame();
        return game != null && !isFinished(game.board, game.currentPlayer);
    }

    private static boolean isFinished(char[][] board, char currentPlayer) {
        GameState state = new GameState();
        state.loadSavedState(board, currentPlayer);
        return state.isGameOver();
    }

    private static String flattenBoard(char[][] board) {
        StringBuilder text = new StringBuilder(GameState.SIZE * GameState.SIZE);

        for (int row = 0; row < GameState.SIZE; row++) {
            for (int col = 0; col < GameState.SIZE; col++) {
                text.append(board[row][col]);
            }
        }

        return text.toString();
    }

    private static char[][] unflattenBoard(String text) {
        char[][] board = new char[GameState.SIZE][GameState.SIZE];
        int index = 0;

        for (int row = 0; row < GameState.SIZE; row++) {
            for (int col = 0; col < GameState.SIZE; col++) {
                board[row][col] = text.charAt(index++);
            }
        }

        return board;
    }
}
