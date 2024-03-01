package com.gamecodeschool.c17snake;

import android.graphics.Paint;
import android.graphics.Canvas;

public interface GameObj {
    void draw(Canvas canvas, Paint paint );
    void reset(int h, int w);
    void move();

}
