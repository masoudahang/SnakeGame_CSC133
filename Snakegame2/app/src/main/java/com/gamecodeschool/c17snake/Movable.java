package com.gamecodeschool.c17snake;

import android.content.Context;
import android.view.MotionEvent;

public interface Movable {
    void move();
    void headMovement(Context context, int ss);
    void movingLoop();
    void switchHeading(MotionEvent motionEvent);

}
