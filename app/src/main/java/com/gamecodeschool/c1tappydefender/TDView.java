package com.gamecodeschool.c1tappydefender;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;

/**
 * Created by Ab on 11/24/2016.
 */

public class TDView extends SurfaceView implements Runnable {
    // Game objects
    private PlayerShip player;
    private EnemyShip enemy1;
    private EnemyShip enemy2;
    private EnemyShip enemy3;
    // Make some random space dust
    public ArrayList<SpaceDust> dustList = new ArrayList<>();

    // For drawing
    private Paint paint;
    private Canvas canvas;
    private SurfaceHolder ourHolder;

    volatile boolean playing;
    Thread gameThread = null;

    private float distanceRemaining;
    private long timeTaken;
    private long timeStarted;
    private long fastestTime;

    private int screenX;
    private int screenY;
    private Context context;

    private boolean gameEnded;

    public TDView(Context context, int x, int y) {
        super(context);
        this.context = context;
        //Initialize our drawing Opjects
        ourHolder = getHolder();
        paint = new Paint();

        screenX = x;
        screenY = y;

        //Initialize the Player & enemy Ships
//        player = new PlayerShip(context, x, y);
//        enemy1 = new EnemyShip(context, x, y);
//        enemy2 = new EnemyShip(context, x, y);
//        enemy3 = new EnemyShip(context, x, y);
//
//        int numSpecs = 500;
//        for (int i = 0; i < numSpecs; i++) {
//            // Where will the dust spawn?
//            SpaceDust spec = new SpaceDust(x, y);
//            dustList.add(spec);
//        }
        startGame();

    }

    @Override
    public void run() {
        while (playing) {
            update();
            draw();
            control();
        }

    }

    private void update() {
        // Collision detection on new positions
        // Before move because we are testing last frames
        // position which has just been drawn
        boolean hitDetected = false ;
        if (Rect.intersects(player.getHitbox(), enemy1.getHitbox())) {
            hitDetected = true;
            enemy1.setX(-100);
        }
        if (Rect.intersects(player.getHitbox(), enemy2.getHitbox())) {
            enemy2.setX(-100);
            hitDetected = true;
        }
        if (Rect.intersects(player.getHitbox(), enemy3.getHitbox())) {
            enemy3.setX(-100);
            hitDetected = true;
        }

        if (hitDetected) {
            player.reduceShieldStrength();
            if (player.getShieldStrength() < 0) {
                //game Over
                gameEnded = true;
            }
        }
        //Update the Player
        player.update();
        // Update the enemies
        enemy1.update(player.getSpeed());
        enemy2.update(player.getSpeed());
        enemy3.update(player.getSpeed());

        for (SpaceDust sd : dustList) {
            sd.update(player.getSpeed());
        }
        if (!gameEnded) {
            //subtract distance to home planet based on current speed
            distanceRemaining -= player.getSpeed();
            //How long has the player been flying
            timeTaken = System.currentTimeMillis() - timeStarted;

        }

        //Completed the Game
        if (distanceRemaining < 0) {
            //check for new fastest time
            if (timeTaken < fastestTime) {
                fastestTime = timeTaken;
            }
            // avoid ugly negative numbers
            // in the HUD
            distanceRemaining = 0;

            //Now End the Game
            gameEnded = true;
        }
    }

    private void draw() {
        if (ourHolder.getSurface().isValid()) {
            //First we lock the area of memory we will be drawing to
            canvas = ourHolder.lockCanvas();
            // Rub out the last frame
            canvas.drawColor(Color.argb(255, 0, 0, 0));
            // For debugging
            // Switch to white pixels
            paint.setColor(Color.argb(255, 255, 255, 255));
            // Draw Hit boxes
            canvas.drawRect(player.getHitbox().left,
                    player.getHitbox().top,
                    player.getHitbox().right,
                    player.getHitbox().bottom,
                    paint);
            canvas.drawRect(enemy1.getHitbox().left,
                    enemy1.getHitbox().top,
                    enemy1.getHitbox().right,
                    enemy1.getHitbox().bottom,
                    paint);
            canvas.drawRect(enemy2.getHitbox().left,
                    enemy2.getHitbox().top,
                    enemy2.getHitbox().right,
                    enemy2.getHitbox().bottom,
                    paint);
            canvas.drawRect(enemy3.getHitbox().left,
                    enemy3.getHitbox().top,
                    enemy3.getHitbox().right,
                    enemy3.getHitbox().bottom,
                    paint);
            //white space of dust
            paint.setColor(Color.argb(255, 255, 255, 255));
            //draw the dust form our ArrayList
            for (SpaceDust sd : dustList) {
                canvas.drawPoint(sd.getX(), sd.getY(), paint);
            }
            // Draw the player
            canvas.drawBitmap(player.getBitmap(), player.getX(), player.getY(), paint);
            canvas.drawBitmap(enemy1.getBitmap(), enemy1.getX(), enemy1.getY(), paint);
            canvas.drawBitmap(enemy2.getBitmap(), enemy2.getX(), enemy2.getY(), paint);
            canvas.drawBitmap(enemy3.getBitmap(), enemy3.getX(), enemy3.getY(), paint);
            if (!gameEnded) {
                // Draw the hud
                paint.setTextAlign(Paint.Align.LEFT);
                paint.setColor(Color.argb(255, 225, 225, 225));
                paint.setTextSize(25);
                canvas.drawText("Fastest: " + fastestTime + "s", 10, 20, paint);
                canvas.drawText("Time:" + timeTaken + "s", screenX / 2, 20, paint);
                canvas.drawText("Distance:" + distanceRemaining / 1000 + " KM", screenX / 3, screenY - 20, paint);
                canvas.drawText("Shield:" + player.getShieldStrength(), 10, screenY - 20, paint);
                canvas.drawText("Speed:" + player.getSpeed() * 60 + " MPS", (screenX / 3) * 2, screenY - 20, paint);
            } else {
                // Show pause screen
                paint.setTextSize(80);
                paint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText("Game Over", screenX / 2, 100, paint);
                paint.setTextSize(25);
                canvas.drawText("fastest: " + fastestTime + " s", screenX / 2, 160, paint);
                canvas.drawText("Time: " + timeTaken + "s", screenX / 2, 200, paint);
                canvas.drawText("Distance remaining:" + distanceRemaining/1000 + " KM",screenX/2, 240, paint);
                paint.setTextSize(80);
                canvas.drawText("Tap to replay!", screenX/2, 350, paint);
            }
            // Unlock and draw the scene
            ourHolder.unlockCanvasAndPost(canvas);
        }
    }

    private void control() {
        try {
            gameThread.sleep(17);
        } catch (InterruptedException e) {
        }
    }


    // Clean up our thread if the game is interrupted or the player quits
    public void pause() {
        playing = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
        }
    }

    // Make a new thread and start it
    // Execution moves to our R
    public void resume() {
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // There are many different events in MotionEvent
        // We care about just 2 - for now.
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            // Has the player lifted their finger up?
            case MotionEvent.ACTION_UP:
                player.stopBoosting();
                break;
            // Has the player touched the screen?
            case MotionEvent.ACTION_DOWN:
                player.setBoosting();
                // If we are currently on the pause screen, start a new game
                if (gameEnded) {
                    startGame();
                }
                break;
        }
        return true;
    }

    private void startGame() {
        // Initialize game Opjects
        player = new PlayerShip(context, screenX, screenY);
        enemy1 = new EnemyShip(context, screenX, screenY);
        enemy2 = new EnemyShip(context, screenX, screenY);
        enemy3 = new EnemyShip(context, screenX, screenY);

        int numSpecs = 40;
        for (int i = 0; i < numSpecs; i++) {
            //Where will the dust Spawn
            SpaceDust spec = new SpaceDust(screenX, screenY);
            dustList.add(spec);
        }
        // Reset time and distance
        distanceRemaining = 100000; //10 KM
        timeTaken = 0;

        //Get Start Time
        timeStarted = System.currentTimeMillis();

        gameEnded = false;
    }
}
