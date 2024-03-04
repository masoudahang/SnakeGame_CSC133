package com.gamecodeschool.c17snake;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;

public interface Game {

    void newGame();


    boolean updateRequired();

    void update();

    void draw();

    void drawColorSize();

    //void drawPaused();

    void pause();

    void resume();
}
