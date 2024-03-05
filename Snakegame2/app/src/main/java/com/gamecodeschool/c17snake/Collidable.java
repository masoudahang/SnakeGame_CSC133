package com.gamecodeschool.c17snake;

import android.graphics.Point;

public interface Collidable {
    boolean detectDeath();
    boolean checkDinner(Point l);
}
