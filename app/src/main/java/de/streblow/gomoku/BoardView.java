package de.streblow.gomoku;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class BoardView extends View {

    private static final int LINE_THICK = 1; //2
    private static final int STROKE_WIDTH = 2; //3
    private static final int MARGIN = 5;
    private int width, height;
    private Paint gridPaint, oPaint, xPaint, markerPaint;
    private MainActivity activity;
    private int squareLength, boxLength;
    private int boardSize;
    private BoardState boardState;
    private int markerSize;
    private int paddingSize;
    private int lineThick;
    private int strokeWidth;

    public BoardView(Context context) {
        super(context);
    }

    public BoardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        // lines
        gridPaint = new Paint();
        gridPaint.setColor(Color.argb(0xff,0xaf,0xaf,0xff));
        gridPaint.setStyle(Paint.Style.FILL);

        // 'O' shape and 'X' shape
        oPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        oPaint.setColor(Color.RED);
        oPaint.setStyle(Paint.Style.STROKE);
        oPaint.setStrokeWidth(STROKE_WIDTH);
        xPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        xPaint.setColor(Color.BLUE);
        xPaint.setStyle(Paint.Style.STROKE);
        xPaint.setStrokeWidth(STROKE_WIDTH);

        // marker of last move
        markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        markerPaint.setColor(Color.YELLOW);
        markerPaint.setStyle(Paint.Style.FILL);
    }

    public void setGameActivity(MainActivity gameContainer) {
        this.activity = gameContainer;
    }

    // This is called to set the view to Game activity
    public void setBoardState(BoardState boardState) {
        this.boardState = boardState;
        boardSize = boardState.getBoardSize();
    }

    // measure the screen and the value is used my
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        height = MeasureSpec.getSize(heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        squareLength = Math.min(width, height);
        boxLength =  squareLength / boardSize;
        squareLength = boxLength * boardSize;

        paddingSize = Math.max(MARGIN, boxLength / 6);
        markerSize = Math.max(paddingSize + 2, boxLength / 4);

        lineThick = LINE_THICK;
        strokeWidth = STROKE_WIDTH;
        if(width > 400) {
            lineThick = LINE_THICK + 1;
            strokeWidth = STROKE_WIDTH + 4;
        }
        oPaint.setStrokeWidth(strokeWidth);
        xPaint.setStrokeWidth(strokeWidth);

        super.onMeasure(
                MeasureSpec.makeMeasureSpec(squareLength, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(squareLength, MeasureSpec.EXACTLY));
    }

    // This method is called each time invalidate() is called
    @Override
    protected void onDraw(Canvas canvas) {
        drawGrid(canvas);
        drawPieces(canvas);
    }

    // Detect user interaction with screen (detect user putting stones)
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if((event.getAction() == MotionEvent.ACTION_DOWN) && (!boardState.gameOver)) {
            int x = (int) (event.getX() / boxLength);
            int y = (int) (event.getY() / boxLength);
            // if there are 2 players, no need to implement AI
            if(boardState.getIsMultiPlayer()) {
                if(boardState.isLegalMove(y, x)) {
                    boolean userWin = boardState.move(y, x);
                    boardState.lastMoveX = x;
                    boardState.lastMoveY = y;
                    invalidate();
                    if(userWin)
                        activity.gameEnded(boardState.getCurrentPlayer());
                    if(boardState.isBoardFilled()) {
                        activity.gameEnded('D');
                    }
                }
                return super.onTouchEvent(event);
            }
            // if it is user's turn
            if(!boardState.getIsMultiPlayer()&& boardState.getUserTurn() == boardState.getCurrentPlayer()) {
                if(boardState.isLegalMove(y, x)) {
                    boolean userWin = boardState.move(y, x);
                    boardState.lastMoveX = x;
                    boardState.lastMoveY = y;
                    invalidate();
                    // if user win
                    if(userWin)
                        activity.gameEnded(boardState.getUserTurn());
                    else if(boardState.isBoardFilled()) {
                        activity.gameEnded('D');
                    } else {
                        // move on to ai move after user made a move
                        boolean aiWin = boardState.aiMove();
                        boardState.lastMoveX = boardState.getLastMoveX();
                        boardState.lastMoveY = boardState.getLastMoveY();
                        invalidate();
                        if(aiWin)
                            activity.gameEnded((boardState.getAiTurn()));
                    }
                    if(boardState.isBoardFilled())
                        activity.gameEnded('D');
                }
            }
        }
        return super.onTouchEvent(event);
    }

    // draw the grid runs from top to down and left to right
    private void drawGrid(Canvas canvas) {
        // x coordinates start from left and
        // y coordinates start from top
        for(int i = 0; i < boardSize; i++) {
            // vertical lines
            // the line has thickness, that's why there is left and right
            float left = boxLength * i;
            float right = left + lineThick;
            float top = 0;
            float bottom = squareLength;

            canvas.drawRect(left, top, right, bottom, gridPaint);

            // horizontal lines
            float left2 = 0;
            float right2 = squareLength;
            float top2 = boxLength * i;
            float bottom2 = top2 + lineThick;

            canvas.drawRect(left2, top2, right2, bottom2, gridPaint);
        }
        canvas.drawRect(squareLength - lineThick, 0,
                boxLength * boardSize, squareLength, gridPaint);
        canvas.drawRect(0, squareLength - lineThick,
                squareLength, squareLength, gridPaint);
    }

    // draw each pieces by accessing char list in boardState
    private void drawPieces(Canvas canvas) {
        for(int i = 0; i < boardSize; i++) {
            for(int j = 0; j < boardSize; j++) {
                drawEachBox(canvas, boardState.getCharOfBox(i,j), i, j);
            }
        }
    }

    // draw 'O' and 'X' at the given position on canvas
    private void drawEachBox(Canvas canvas, char c, int y, int x) {
        if(c == 'O') {
            // drawing circle to the center of box
            float cx = (boxLength * x) + lineThick + (boxLength - lineThick) / 2;
            float cy = (boxLength * y) + lineThick + (boxLength - lineThick) / 2;
            canvas.drawCircle(cx, cy, (boxLength - lineThick - 2 * paddingSize) / 2 , oPaint);
        } else if(c == 'X') {
            // this is from upper left to lower right of 'X'
            float startX = (boxLength * x) + lineThick + paddingSize;
            float startY = (boxLength * y) + lineThick + paddingSize;
            float endX = startX + boxLength - lineThick - paddingSize * 2;
            float endY = startY + boxLength - lineThick - paddingSize * 2;

            canvas.drawLine(startX, startY, endX, endY, xPaint);

            // this is from upper right to lower left of 'X'
            float startX2 = (boxLength * (x + 1)) - paddingSize;
            float startY2 = (boxLength * y) + lineThick + paddingSize;
            float endX2 = startX2 - boxLength + lineThick + paddingSize * 2;
            float endY2 = startY2 + boxLength - lineThick - paddingSize * 2;

            canvas.drawLine(startX2, startY2, endX2, endY2, xPaint);
        }
        // draw marker for last move
        if(boardState.lastMoveX == x && boardState.lastMoveY == y) {
            // drawing marker edges
            markerPaint.setColor(c == 'O' ? Color.RED : Color.BLUE);
            float left, top, right, bottom;
            left = boxLength * x;
            right = left + markerSize;
            top = boxLength * y;
            bottom = top + lineThick + 1;
            canvas.drawRect(left, top, right, bottom, markerPaint);
            left = boxLength * (x + 1) - markerSize;
            right = left + markerSize;
            canvas.drawRect(left, top, right, bottom, markerPaint);
            top = boxLength * (y + 1) - lineThick - 1;
            bottom = top + lineThick + 1;
            canvas.drawRect(left, top, right, bottom, markerPaint);
            left = boxLength * x;
            right = left + markerSize;
            canvas.drawRect(left, top, right, bottom, markerPaint);

            left = boxLength * x;
            right = left + lineThick + 1;
            top = boxLength * y;
            bottom = top + markerSize;
            canvas.drawRect(left, top, right, bottom, markerPaint);
            top = boxLength * (y + 1) - markerSize;
            bottom = top + markerSize;
            canvas.drawRect(left, top, right, bottom, markerPaint);
            left = boxLength * (x + 1) - lineThick - 1;
            right = left + lineThick + 1;
            canvas.drawRect(left, top, right, bottom, markerPaint);
            top = boxLength * y;
            bottom = top + markerSize;
            canvas.drawRect(left, top, right, bottom, markerPaint);
        }
    }

}