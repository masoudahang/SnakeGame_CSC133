package com.gamecodeschool.c17snake;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.io.IOException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.WindowManager;
import androidx.core.content.res.ResourcesCompat;

class SnakeGame extends SurfaceView implements Runnable, Game {

    // Objects for the game loop/thread
    private Thread mThread = null;

    // Control pausing between updates
    private long mNextFrameTime;
    private boolean isFirstPause = true;

    // Is the game currently playing and or paused?
    private volatile boolean mPlaying = false;
    private volatile boolean mPaused = true;

    // for playing sound effects
    private SoundPool mSP;
    private int mEat_ID = -1;
    private int mCrashID = -1;

    // The size in segments of the playable area
    private final int NUM_BLOCKS_WIDE = 40;
    private int mNumBlocksHigh;

    // How many points does the player have
    private int mScore;

    //Pause button rendering objects
    private Rect mPauseButtonRect;
    private Paint mPauseButtonPaint;

    // Objects for drawing
    private Canvas mCanvas;
    private final SurfaceHolder mSurfaceHolder;
    private final Paint mPaint;

    // Typeface object to hold the custom font
    private Typeface mCustomFont;

    // A snake ssss
    private Snake mSnake;
    // And an apple
    private Apple mApple;
    private Bitmap mBackgroundBitmap;

    // This is the constructor method that gets called
    // from SnakeActivity
    protected SnakeGame(Context context, Point size) {
        super(context);

        // Refactored
        fontTryCatch(context);

        // Create the size of the button
        createPauseButton(size);

        // Refactored
        loadBackgroundImage(context, size);

        // Refactored
        soundPool();

        // Refactored
        tryCatch(context);

        // Initialize the drawing objects
        mSurfaceHolder = getHolder();
        mPaint = new Paint();

        // Refactored
        callConstructors(context, size);

        // Create the pause button
        createPauseButton();
    }

    // Refactored
    @Override
    public void fontTryCatch(Context context) {
        try {
            // Load the custom font
            mCustomFont = ResourcesCompat.getFont(context, R.font.font);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Refactored
    @Override
    public void loadBackgroundImage(Context context, Point size) {
        // Load the background image
        mBackgroundBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.background);

        // Scale the image to match the screen size
        mBackgroundBitmap = Bitmap.createScaledBitmap(mBackgroundBitmap, size.x, size.y, true);
    }

    // Method to create and draw the pause button
    @Override
    public void createPauseButton() {

        int buttonWidth = 400;
        int buttonHeight = 100;
        int buttonLeft = 800;
        int buttonTop = 50;

        // Create a Rect object representing the pause button's bounds
        mPauseButtonRect = new Rect(buttonLeft, buttonTop, buttonLeft + buttonWidth, buttonTop + buttonHeight);

        // Define the appearance of the pause button (e.g., color)
        mPauseButtonPaint = new Paint();
        mPauseButtonPaint.setColor(Color.RED); // Adjust color as needed
    }

    // Overload
    // Create the size of the button
    protected void createPauseButton(Point size) {
        int buttonWidth = size.x / 6;
        int buttonHeight = size.y / 12;
        int buttonLeft = (size.x - buttonWidth) / 2;
        int buttonTop = size.y / 10;

        mPauseButtonRect = new Rect(buttonLeft, buttonTop, buttonLeft + buttonWidth, buttonTop + buttonHeight);
        mPauseButtonPaint = new Paint();
        mPauseButtonPaint.setColor(Color.RED);
    }

    //Refactored
    @Override
    public void soundPool() {
        // Initialize the SoundPool
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        mSP = new SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttributes)
                .build();
    }

    //Refactored
    @Override
    public void tryCatch(Context context) {
        try {
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor;

            // Prepare the sounds in memory
            descriptor = assetManager.openFd("get_apple.ogg");
            mEat_ID = mSP.load(descriptor, 0);

            descriptor = assetManager.openFd("snake_death.ogg");
            mCrashID = mSP.load(descriptor, 0);

        } catch (IOException e) {
            // Error
        }
    }

    //Refactored
    @Override
    public void callConstructors(Context context, Point size) {
        // Work out how many pixels each block is
        int blockSize = size.x / NUM_BLOCKS_WIDE;
        mNumBlocksHigh = size.y / blockSize;

        // Call the constructors of our two game objects
        mApple = new Apple(context,
                new Point(NUM_BLOCKS_WIDE,
                        mNumBlocksHigh),
                blockSize);

        mSnake = new Snake(context,
                new Point(NUM_BLOCKS_WIDE,
                        mNumBlocksHigh),
                blockSize);
    }

    // Handles the game loop
    @Override
    public void run() {
        while (mPlaying) {
            if(!mPaused) {
                // Update 10 times a second
                if (updateRequired()) {
                    update();
                }
            }

            draw();
        }
    }

    // Check to see if it is time for an update
    @Override
    public boolean updateRequired() {

        // Run at 10 frames per second
        final long TARGET_FPS = 10;
        // There are 1000 milliseconds in a second
        final long MILLIS_PER_SECOND = 1000;

        // Are we due to update the frame
        if(mNextFrameTime <= System.currentTimeMillis()){

            // Setup when the next update will be triggered
            mNextFrameTime =System.currentTimeMillis()
                    + MILLIS_PER_SECOND / TARGET_FPS;

            // Return true so that the update and draw
            // methods are executed
            return true;
        }

        return false;
    }

    @Override
    public void newGame() {

        // Reset the snake and spawn the apple if it's not paused and it's the first pause
        if (!mPaused && isFirstPause) {
            mSnake.reset(NUM_BLOCKS_WIDE, mNumBlocksHigh);
            mApple.spawn();
        }

        isFirstPause = mPaused;
        mNextFrameTime = System.currentTimeMillis();
    }

    // Update the newGame() method to set isFirstPause to true
    @Override
    public void update() {
        if (!mPaused) {
            mSnake.move();

            if (mSnake.checkDinner(mApple.getLocation())) {
                mApple.spawn();
                mScore++;
                mSP.play(mEat_ID, 1, 1, 0, 0, 1);
            }

            if (mSnake.detectDeath()) {
                // Reset the score and the game if snake dies
                resetGame();
            }
        }
    }

    private void resetGame() {
        if (!mPaused) {
            mScore = 0;
            mApple.spawn();
            mApple.hide(); // Hide the apple upon resetting the game
            mSnake.reset(NUM_BLOCKS_WIDE, mNumBlocksHigh);
            mSnake.hide(); // Hide the snake upon resetting the game
            isFirstPause = true; // Set isFirstPause to true upon resetting the game
            mPaused = true; // Set mPaused to true upon resetting the game
        }
    }

    @Override
    public void draw() {
        // Get a lock on the canvas
        if (mSurfaceHolder.getSurface().isValid()) {
            mCanvas = mSurfaceHolder.lockCanvas();

            // Draw the background image
            mCanvas.drawBitmap(mBackgroundBitmap, 0, 0, null);

            // Draw the score
            drawColorSize();

            // Draw the names
            drawNames();

            // Refactored
            drawConditions();

            // Unlock the canvas and reveal the graphics for this frame
            mSurfaceHolder.unlockCanvasAndPost(mCanvas);
        }
    }

    // Refactored
    @Override
    public void drawConditions() {
        if (isFirstPause && mPaused) {
            // Draw the "Tap to play" prompt if the game is initially paused
            drawPaused();
        } else {
            // Draw the pause button only if the game is paused and not rendering "Tap to play"
            drawPauseButton(mCanvas, mPaint);

            if (!mPaused) {
                // Draw the apple only if the game is not paused
                mApple.draw(mCanvas, mPaint);
            }
        }
    }

    // Refactored
    @Override
    public void drawColorSize() {

        // Set the size and color of the mPaint for the text
        mPaint.setColor(Color.argb(255, 255, 255, 255));
        mPaint.setTextSize(120);

        // Draw the score
        mCanvas.drawText("" + mScore, 20, 120, mPaint);

        // Draw the apple and the snake
        mApple.draw(mCanvas, mPaint);
        mSnake.draw(mCanvas, mPaint);
    }

    // Refactored
    @Override
    public void drawPaused() {
        // Set the size and color of the mPaint for the text
        mPaint.setColor(Color.argb(255, 203, 67, 53));
        mPaint.setTextSize(250);
        // Set the custom font to the Paint object
        mPaint.setTypeface(mCustomFont);

        if (isFirstPause && mPaused) {
            //Refactored
            drawTapToPlay();
        }

        drawNames();
    }

    // Refactored
    @Override
    public void drawTapToPlay() {
        // Draw the "Tap to play" message if the game is initially paused
        String message = getResources().getString(R.string.tap_to_play);

        // Get the width and height of the message
        float messageWidth = mPaint.measureText(message);
        float messageHeight = mPaint.getFontMetrics().bottom - mPaint.getFontMetrics().top;

        // Get the screen dimensions
        Point screenDimensions = getScreenDimensions();
        int screenWidth = screenDimensions.x;
        int screenHeight = screenDimensions.y;

        // Calculate the position to center the text horizontally and vertically
        float centerX = (screenWidth - messageWidth) / 2;
        float centerY = (screenHeight + messageHeight) / 2;

        // Draw the "Tap to play" message centered on the screen
        mCanvas.drawText(message, centerX, centerY, mPaint);
    }

    // Draws names of the students who worked together to make the code better
    @Override
    public void drawNames() {
        mPaint.setColor(Color.argb(255, 255, 255, 255));
        mPaint.setTextSize(30);

        // Get the screen dimensions
        Point screenDimensions = getScreenDimensions();
        int screenWidth = screenDimensions.x;

        // Calculate the x-coordinate to position the names
        int xCoordinate = screenWidth - 250; // Adjust this value as needed

        mCanvas.drawText(getResources().getString(R.string.name1),
                xCoordinate, 50, mPaint);
        mCanvas.drawText(getResources().getString(R.string.name2),
                xCoordinate, 85, mPaint);
    }

    // Method to get screen dimensions
    private Point getScreenDimensions() {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;
        return new Point(screenWidth, screenHeight);
    }

    // Didn't Refactor since it makes the code of this class even longer because we have
    // to initialize variables outside of the method in order to be able to refactor
    // Original: Method to draw the pause button
    private void drawPauseButton(Canvas canvas, Paint paint) {
        // Set color for the button background
        paint.setColor(Color.argb(255, 203, 67, 53));

        // Get screen dimensions
        Point screenDimensions = getScreenDimensions();
        int screenWidth = screenDimensions.x;
        int screenHeight = screenDimensions.y;

        // Define the size and position of the button relative to screen dimensions
        int buttonWidth = screenWidth / 9;
        int buttonHeight = screenHeight / 20;
        int buttonLeft = (screenWidth - buttonWidth) / 2;
        int buttonTop = screenHeight / 10;

        // Draw a rounded rectangle representing the button
        canvas.drawRoundRect(
                buttonLeft, buttonTop,
                buttonLeft + buttonWidth, buttonTop + buttonHeight,
                25, 25, paint
        );

        // Set color and size for the button text
        paint.setColor(Color.WHITE);
        paint.setTextSize(buttonHeight * 0.9f);

        // Determine the text to be displayed based on the pause state
        String buttonText = mPaused ? "Resume" : "Pause";

        // Calculate the position to center the text within the button
        float textWidth = paint.measureText(buttonText);
        float textX = buttonLeft + (buttonWidth - textWidth) / 2;
        float textY = buttonTop + (float) buttonHeight / 2 + paint.getTextSize() / 3; // Adjust for vertical centering

        // Draw the determined text at the center of the button
        canvas.drawText(buttonText, textX, textY, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if ((motionEvent.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
            if (mPaused) {
                // If the game is paused, resume the game
                mPaused = false;
                newGame();
            } else if (mPauseButtonRect.contains((int) motionEvent.getX(), (int) motionEvent.getY())) {
                // If the pause button is touched, pause the game
                mPaused = true;
            } else {
                // If the game is running and not paused, handle snake movement
                mSnake.switchHeading(motionEvent);
            }
            return true;
        }
        return true;
    }

    // Stop the thread
    @Override
    public void pause() {
        mPlaying = false;
        try {
            mThread.join();
        } catch (InterruptedException e) {
            // Error
        }
    }

    // Start the thread
    @Override
    public void resume() {
        mPlaying = true;
        mThread = new Thread(this);
        mThread.start();
    }
}