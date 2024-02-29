package com.gamecodeschool.c17snake;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;


public abstract class GameObject {

    protected Point location;
    protected int mSegmentSize;
    protected int size;
    protected Point mSpawnRange;
    protected Point mMoveRange;

    //Constructor
    public GameObject(Context context, Point location, int size) {
        this.size = size;
        this.location = new Point(-10, -10);
        mSegmentSize = size;
        mSpawnRange = location;
        mMoveRange = location;

    }

    // Let SnakeGame know where the apple is
    // SnakeGame can share this with the snake
    public Point getLocation() {
        return location;
    }
    public abstract void spawn();

    public abstract void draw(Canvas canvas, Paint paint);

}
