package de.streblow.gomoku;

import java.util.StringTokenizer;

public class BoardState {

    int wChain = 5; // number of winning stones (e.g. 5 for 5-In-A-Row)
    int boardSize;
    char[][] board;
    int lastMoveX, lastMoveY;
    boolean gameOver;
    private char userTurn;
    private char aiTurn;
    private char currentPlayer;
    private AI ai;
    private boolean isFirstMove = false;
    private int maxRow; // bottom most occupied position
    private int minRow; // top most occupied position
    private int maxCol; // right most occupied position
    private int minCol; // left most occupied position
    private boolean isMultiPlayer = false;
    private String[] moves;
    private int moveCounter = 0;

    public BoardState(int boardSize, int aiLevel, int maxDepth) {
        this.boardSize = boardSize;
        board = new char[boardSize][boardSize];
        lastMoveX = -1;
        lastMoveY = -1;
        gameOver = false;
        // Initialize AI and board
        ai = new AI(aiLevel, maxDepth);
        moves = new String[boardSize * boardSize];
        initializeBoard();
        maxRow = boardSize/2;
        minRow = boardSize/2;
        maxCol = boardSize/2;
        minCol = boardSize/2;
    }

    // Copy constructor is used to create virtual state. When AI is thinking the best move,
    // AI creates virtual state so that it won't change the current state.
    public BoardState(BoardState other) {
        this.boardSize = other.boardSize;
        this.board = new char[boardSize][boardSize];
        this.isFirstMove = other.isFirstMove;
        for(int i = 0; i < boardSize; i++) {
            for(int j = 0; j < boardSize; j++) {
                this.board[i][j] = other.board[i][j];
            }
        }
        this.moves = other.moves;
    }

    private void initializeBoard() {
        for(int i = 0; i < boardSize; i++) {
            for(int j = 0; j < boardSize; j++) {
                board[i][j] = ' ';
            }
        }
    }

    public void setAILevel(int level) {
        ai.setAILevel(level);
    }

    public boolean getIsFirstMove(){
        return isFirstMove;
    }

    public void setIsFirstMoveTrue() { isFirstMove = true;}

    public void setIsFirstMoveFalse() {
        isFirstMove = false;
    }

    public void changeTurn() {
        currentPlayer = (currentPlayer == 'X' ? 'O' : 'X');
    }

    public char[][] getBoard() {
        return board;
    }

    public char getCharOfBox(int row, int col) {
        return board[row][col];
    }

    public void setUserTurn(char userTurn) {
        this.userTurn = userTurn;
    }

    public char getUserTurn() {
        return userTurn;
    }

    public void setAiTurn(char aiTurn) {
        this.aiTurn = aiTurn;
    }

    public char getAiTurn() {
        return aiTurn;
    }

    public int getBoardSize() {
        return boardSize;
    }

    public void setCurrentPlayer(char currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public char getCurrentPlayer() {
        return currentPlayer;
    }

    // This method is called every single move that whether user/AI makes.
    public boolean move(int row, int col) {
        board[row][col] = currentPlayer;
        maxRow = Math.max(maxRow, row);
        minRow = Math.min(minRow, row);
        maxCol = Math.max(maxCol, col);
        minCol = Math.min(minCol, col);
        gameOver = true;
        if(isEnded(row, col, 1, 0)) return true;
        if(isEnded(row, col, 0, 1)) return true;
        if(isEnded(row, col, 1, 1)) return true;
        if(isEnded(row, col, 1, -1)) return true;
        gameOver = false;
        moves[moveCounter] = row + "," + col;
        moveCounter++;
        changeTurn();
        return false;
    }

    // This method creates the new board using copy constructor and applies the move.
    // This doesn't affect the current board state.
    public BoardState applyMove(int row, int col) {
        BoardState afterMove = new BoardState(this);
        afterMove.setCurrentPlayer(currentPlayer);
        afterMove.setUserTurn(this.userTurn);
        afterMove.setAiTurn(this.aiTurn);
        afterMove.maxRow = maxRow;
        afterMove.minRow = minRow;
        afterMove.maxCol = maxCol;
        afterMove.minCol = minCol;
        afterMove.move(row, col);
        return afterMove;
    }

    // check whether the game ended or not
    // (dr, dc) : (0, 1) -> horizontal, (0, 1) -> vertical,
    // (1, 1) -> diagonal left to right, (1, -1) -> diagonal right to left
    // from center stone at (row, col), check positive side and negative side
    public boolean isEnded(int row, int col, int dr, int dc) {
        int count  = 1; // center stone should be also counted
        int r = row + dr;
        int c = col + dc;
        while(r >= 0 && r < boardSize && c >= 0 && c < boardSize && board[r][c] == currentPlayer) {
            count++;
            r += dr;
            c += dc;
        }

        if(count >= 5)  return true;

        r = row - dr;
        c = col - dc;
        while(r >= 0 && r < boardSize && c >= 0 && c < boardSize && board[r][c] == currentPlayer) {
            count++;
            r -= dr;
            c -= dc;
        }

        if(count >= 5)  return true;
        return false;
    }

    public boolean isLegalMove(int row, int col) {
        if (board[row][col] == ' ')
            return true;
        return false;
    }

    // This calls AI to find the best move.
    public boolean aiMove() {
        ai.move(this);
        String stringMove = ai.getMove();
        StringTokenizer tokens = new StringTokenizer(stringMove, ",");
        String row = tokens.nextToken();
        String col = tokens.nextToken();
        lastMoveX = Integer.parseInt(col);
        lastMoveY = Integer.parseInt(row);
        return move(lastMoveY, lastMoveX);
    }

    public int getLastMoveX() {
        return lastMoveX;
    }

    public int getLastMoveY() {
        return lastMoveY;
    }

    public int getMaxRow() {
        return maxRow;
    }

    public int getMinRow() {
        return minRow;
    }

    public int getMaxCol() {
        return maxCol;
    }

    public int getMinCol() {
        return minCol;
    }

    public boolean isBoardFilled() {
        for(int i = 0; i < boardSize; i++) {
            for(int j = 0; j < boardSize; j++) {
                if(board[i][j] == ' ')  return false;
            }
        }
        return true;
    }

    public void setIsMultiPlayer() {
        isMultiPlayer = true;
    }

    public boolean getIsMultiPlayer() {
        return isMultiPlayer;
    }

    // This method is called when the user wants to undo his move
    // The moves-array keep track of the history of moves.
    public void unMove() {
        lastMoveX = -1;
        lastMoveY = -1;
        if(isMultiPlayer) {
            moveCounter--;
            String deletedMove = moves[moveCounter];
            moves[moveCounter] = "";

            String[] deletedMoveToken = deletedMove.split(",");
            int deletedMoveRow = Integer.parseInt(deletedMoveToken[0]);
            int deletedMoveCol = Integer.parseInt(deletedMoveToken[1]);
            board[deletedMoveRow][deletedMoveCol] = ' ';

            if(moveCounter == 0) {
                lastMoveX = -1;
                lastMoveY = -1;
            } else {
                String newLastMoveMP = moves[moveCounter - 1];
                String[] newLastMoveMPToken = newLastMoveMP.split(",");
                int newLastMoveMPRow = Integer.parseInt(newLastMoveMPToken[0]);
                int newLastMoveMPCol = Integer.parseInt(newLastMoveMPToken[1]);
                lastMoveX = newLastMoveMPCol;
                lastMoveY = newLastMoveMPRow;
            }

            changeTurn();
            return;
        }

        moveCounter--;
        String deletedMove1 = moves[moveCounter];
        moves[moveCounter] = "";

        moveCounter--;
        String deletedMove2 = moves[moveCounter];
        moves[moveCounter] = "";

        String[] deletedMove1Token = deletedMove1.split(",");
        int deletedMove1Row = Integer.parseInt(deletedMove1Token[0]);
        int deletedMove1Col = Integer.parseInt(deletedMove1Token[1]);
        String[] deletedMove2Token = deletedMove2.split(",");
        int deletedMove2Row = Integer.parseInt(deletedMove2Token[0]);
        int deletedMove2Col = Integer.parseInt(deletedMove2Token[1]);

        board[deletedMove1Row][deletedMove1Col] = ' ';
        board[deletedMove2Row][deletedMove2Col] = ' ';

        if(moveCounter == 0) {
            lastMoveX = -1;
            lastMoveY = -1;
        } else {
            String newLastMoveSP = moves[moveCounter - 1];
            String[] newLastMoveSPToken = newLastMoveSP.split(",");
            int newLastMoveSPRow = Integer.parseInt(newLastMoveSPToken[0]);
            int newLastMoveSPCol = Integer.parseInt(newLastMoveSPToken[1]);
            lastMoveX = newLastMoveSPCol;
            lastMoveY = newLastMoveSPRow;
        }
    }

    public int getMoveCounter() {
        return moveCounter;
    }

}
