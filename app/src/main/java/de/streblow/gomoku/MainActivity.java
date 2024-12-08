package de.streblow.gomoku;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private BoardView boardView;
    private boolean gameOver;
    private BoardState boardState;
    private SubMenu difficultyMenu;

    private char userTurn;
    private int boardSize;
    private int aiLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        boardView = (BoardView) findViewById(R.id.board);
        // Make to run the application only in portrait mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        gameOver = false;

        //parameters for the game should be configurable
        userTurn = 'X';
        boardSize = 15;
        aiLevel = 1; //corresponds to checked menu item difficulty_easy

        boardState = new BoardState(boardSize, 5, aiLevel,5);
        boardView.setBoardState(boardState);
        boardView.setGameActivity(this);

        // U : 2 players, 2 : X player and the user goes first, O : 1 player and the AI goes first
        if(userTurn == 'U') {
            boardState.setIsMultiPlayer();
            boardState.setUserTurn('X');
            boardState.setCurrentPlayer('X');
        } else if(userTurn == 'X') {
            boardState.setUserTurn('X');
            boardState.setAiTurn('O');
            boardState.setCurrentPlayer('X');
        } else {
            boardState.setUserTurn('O');
            boardState.setAiTurn('X');
            boardState.setCurrentPlayer('X');
            boardState.setIsFirstMoveTrue();
            boardState.aiMove();
            boardState.setIsFirstMoveFalse();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem difficultyMenuItem = menu.findItem(R.id.action_difficulty);
        difficultyMenu = difficultyMenuItem.getSubMenu();
        difficultyMenu.clearHeader();
        getMenuInflater().inflate(R.menu.menu_difficulty, difficultyMenu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_new_game) {
            FragmentManager fm = getSupportFragmentManager();
            NewGameFragment newgame = new NewGameFragment();
            newgame.listener = new NewGameFragment.OnDialogFragmentDismissedListener() {
                @Override
                public void onDialogFragmentDismissedListener(int difficulty, int mode) {
                    aiLevel = difficulty;
                    switch (aiLevel) {
                        case 1:
                            difficultyMenu.findItem(R.id.action_difficulty_easy).setChecked(true);
                            difficultyMenu.findItem(R.id.action_difficulty_medium).setChecked(false);
                            difficultyMenu.findItem(R.id.action_difficulty_hard).setChecked(false);
                            break;
                        case 2:
                            difficultyMenu.findItem(R.id.action_difficulty_easy).setChecked(false);
                            difficultyMenu.findItem(R.id.action_difficulty_medium).setChecked(true);
                            difficultyMenu.findItem(R.id.action_difficulty_hard).setChecked(false);
                            userTurn = 'O';
                            break;
                        case 3:
                            difficultyMenu.findItem(R.id.action_difficulty_easy).setChecked(false);
                            difficultyMenu.findItem(R.id.action_difficulty_medium).setChecked(false);
                            difficultyMenu.findItem(R.id.action_difficulty_hard).setChecked(true);
                            break;
                    }
                    switch (mode) {
                        case 1:
                            userTurn = 'X';
                            break;
                        case 2:
                            userTurn = 'O';
                            break;
                        case 3:
                            userTurn = 'U';
                            break;
                    }
                    rematchGame();
                }
            };
            newgame.show(fm, "NEWGAME");
            return true;
        }
        else if(item.getItemId() == R.id.action_rematch) {
            rematchGame();
        }
        else if(item.getItemId() == R.id.action_undo) {
            if(!gameOver) {
                if(userTurn == 'U') {
                    if (boardState.getMoveCounter() <= 1)
                        rematchGame();
                    else {
                        boardState.unMove();
                        boardView.invalidate();
                    }
                }
                else if (userTurn == 'X') {
                    if (boardState.getMoveCounter() <= 2)
                        rematchGame();
                    else {
                        boardState.unMove();
                        boardView.invalidate();
                    }
                }
                else if (userTurn == 'O') {
                    if (boardState.getMoveCounter() <= 3)
                        rematchGame();
                    else {
                        boardState.unMove();
                        boardView.invalidate();
                    }
                }
            }
        }
        else if(item.getItemId() == R.id.action_difficulty_easy) {
            boardState.setAILevel(1);
            item.setChecked(true);
            difficultyMenu.findItem(R.id.action_difficulty_medium).setChecked(false);
            difficultyMenu.findItem(R.id.action_difficulty_hard).setChecked(false);
        }
        else if(item.getItemId() == R.id.action_difficulty_medium) {
            boardState.setAILevel(2);
            item.setChecked(true);
            difficultyMenu.findItem(R.id.action_difficulty_easy).setChecked(false);
            difficultyMenu.findItem(R.id.action_difficulty_hard).setChecked(false);
        }
        else if(item.getItemId() == R.id.action_difficulty_hard) {
            boardState.setAILevel(3);
            item.setChecked(true);
            difficultyMenu.findItem(R.id.action_difficulty_easy).setChecked(false);
            difficultyMenu.findItem(R.id.action_difficulty_hard).setChecked(false);
        }
        else if(item.getItemId() == R.id.action_about) {
            AboutDialog about = new AboutDialog(this);
            about.setTitle(R.string.about_title);
            about.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void gameEnded(char c) {
        gameOver = true;
        String msg = "";
        if (userTurn == 'U')
            if (c == 'D')
                    msg = getResources().getString(R.string.game_end_tie);
                else
                    msg = String.format(getResources().getString(R.string.game_end_xo_won), c);
        else {
            if (c == 'D')
                msg = getResources().getString(R.string.game_end_tie);
            else if (userTurn == c)
                msg = getResources().getString(R.string.game_end_player_won);
            else
                msg = getResources().getString(R.string.game_end_player_lost);
        }
        String hint = getResources().getString(R.string.game_end_hint);
        new AlertDialog.Builder(this).setTitle(msg).setMessage(hint).show();
    }

    private void rematchGame() {
        gameOver = false;
        boardState = new BoardState(boardSize, 5, aiLevel,5);
        boardView.setBoardState(boardState);
        boardView.setGameActivity(this);

        // U : 2 players, X : 1 player and the user goes first, O : 1 player and the AI goes first
        if(userTurn == 'U') {
            boardState.setIsMultiPlayer();
            boardState.setUserTurn('X');
            boardState.setCurrentPlayer('X');
        } else if(userTurn == 'X') {
            boardState.setUserTurn('X');
            boardState.setAiTurn('O');
            boardState.setCurrentPlayer('X');
        } else {
            boardState.setUserTurn('O');
            boardState.setAiTurn('X');
            boardState.setCurrentPlayer('X');
            boardState.setIsFirstMoveTrue();
            boardState.aiMove();
            boardState.setIsFirstMoveFalse();
        }
        boardView.invalidate();
    }
}
