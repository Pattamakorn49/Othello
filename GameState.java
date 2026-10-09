public class GameState {
    public static final int SIZE = 8;
    private char[][] board = new char[SIZE][SIZE];
    private char currentPlayer = 'X'; // X = Black, O = White
    private boolean gameOver = false;
    private char passedPlayer = ' ';

    public GameState() {
        reset();
    }
    public void reset() {
        board = new char[SIZE][SIZE];

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                board[row][col] = ' ';
            }
        }
        board[3][3] = 'O';
        board[3][4] = 'X';
        board[4][3] = 'X';
        board[4][4] = 'O';

        currentPlayer = 'X';
        gameOver = false;
        passedPlayer = ' ';
    }
    public char[][] getBoard() {
        char[][] copy = new char[SIZE][SIZE];
        for (int row = 0; row < SIZE; row++) {
            System.arraycopy(board[row], 0, copy[row], 0, SIZE);
        }
        return copy;
    }
    public char getCurrentPlayer() {
        return currentPlayer;
    }
    public boolean isGameOver() {
        return gameOver;
    }
    public char getPassedPlayer() {
        return passedPlayer;
    }
    public boolean makeMove(int row, int col) {
        if (gameOver || !isValidMove(row, col)) return false;
        passedPlayer = ' ';
        char player = currentPlayer;
        char opponent = getOpponent(player);
        board[row][col] = player;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                flipDirection(row, col, dr, dc, player, opponent);
            }
        }
        currentPlayer = opponent;
        if (!hasValidMove(currentPlayer)) {
            if (hasValidMove(player)) {
                passedPlayer = opponent;   // opponent is skipped
                currentPlayer = player;
            } else {
                gameOver = true;
            }
        }
        return true;
    }
    public boolean isValidMove(int row, int col) {
        if (!inBounds(row, col) || board[row][col] != ' ' || gameOver) {
            return false;
        }
        char opponent = getOpponent(currentPlayer);
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                if (capturesInDirection(row, col, dr, dc, currentPlayer, opponent)) {
                    return true;
                }
            }
        }
        return false;
    }
    public boolean hasValidMove(char player) {
        if (player != 'X' && player != 'O') return false;
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (isValidMoveForPlayer(row, col, player)) {
                    return true;
                }
            }
        }
        return false;
    }
    public char checkWinner() {
        if (!gameOver) return ' ';
        int x = countDisc('X');
        int o = countDisc('O');
        if (x > o) return 'X';
        if (o > x) return 'O';
        return ' ';
    }
    public boolean isFull() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (board[row][col] == ' ') return false;
            }
        }
        return true;
    }
    public int countDisc(char player) {
        int count = 0;
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (board[row][col] == player) count++;
            }
        }
        return count;
    }
    public void loadSavedState(char[][] savedBoard, char savedCurrentPlayer) {
        if (savedBoard == null || savedBoard.length != SIZE) return;
        for (char[] row : savedBoard) {
            if (row == null || row.length != SIZE) return;
        }
        if (savedCurrentPlayer != 'X' && savedCurrentPlayer != 'O') return;

        board = new char[SIZE][SIZE];
        for (int row = 0; row < SIZE; row++) {
            System.arraycopy(savedBoard[row], 0, board[row], 0, SIZE);
        }
        currentPlayer = savedCurrentPlayer;
        passedPlayer = ' ';
        gameOver = isFull() ||
                (!hasValidMove('X') && !hasValidMove('O'));
        if (!gameOver && !hasValidMove(currentPlayer)) {
            char other = getOpponent(currentPlayer);
            if (hasValidMove(other)) {
                currentPlayer = other;
            } else {
                gameOver = true;
            }
        }
    }
    private boolean isValidMoveForPlayer(int row, int col, char player) {
        if (!inBounds(row, col) || board[row][col] != ' ') return false;
        char opponent = getOpponent(player);
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                if (capturesInDirection(row, col, dr, dc, player, opponent)) {
                    return true;
                }
            }
        }

        return false;
    }
    private boolean capturesInDirection(int row,int col,int dr,int dc,char player,char opponent) {
        int r = row + dr;
        int c = col + dc;
        boolean foundOpponent = false;
        while (inBounds(r, c) && board[r][c] == opponent) {
            foundOpponent = true;
            r += dr;
            c += dc;
        }
        return foundOpponent && inBounds(r, c) && board[r][c] == player;
    }
    private void flipDirection(int row, int col,int dr, int dc,char player, char opponent) {
        if (!capturesInDirection(row, col, dr, dc, player, opponent)) return;
        int r = row + dr;
        int c = col + dc;
        while (inBounds(r, c) && board[r][c] == opponent) {
            board[r][c] = player;
            r += dr;
            c += dc;
        }
    }
    private char getOpponent(char player) {
        return player == 'X' ? 'O' : 'X';
    }
    private boolean inBounds(int row, int col) {
        return row >= 0 && row < SIZE && col >= 0 && col < SIZE;
    }
}
