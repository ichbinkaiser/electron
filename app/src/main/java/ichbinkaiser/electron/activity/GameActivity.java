package ichbinkaiser.electron.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import ichbinkaiser.electron.R;
import ichbinkaiser.electron.control.AI;
import ichbinkaiser.electron.control.Player;
import ichbinkaiser.electron.core.SoundManager;
import ichbinkaiser.electron.entity.Ball;
import ichbinkaiser.electron.entity.Popup;
import ichbinkaiser.electron.entity.Quadrant;
import ichbinkaiser.electron.entity.ShockWave;
import ichbinkaiser.electron.entity.Sound;
import ichbinkaiser.electron.entity.Trail;
import lombok.Getter;

public class GameActivity extends Activity {
    @Getter
    private static String score;
    protected PowerManager.WakeLock wakelock;
    private GameSurfaceThread gameSurfaceThread;
    private SurfaceHolder surfaceHolder;
    @Getter
    private int canvasHeight, canvasWidth, midpoint, centerLine;
    @Getter
    private int life = 50, gameScore = 0;
    @Getter
    private int ballCount = 3;
    @Getter
    private boolean running = true; // game running
    @Getter
    private int ballSize;
    @Getter
    private boolean portrait; // screen orientation
    @Getter
    private boolean reversePosition = false; // AI reverse position in doubles mode
    @Getter
    private boolean soloGame = false; // is solo game
    @Getter
    private int hitCounter = 0; // balls hit counter for AI friend guard zone switch
    @Getter
    private int playerCount; // number of players
    @Getter
    private int pongWidth, pongHeight; // ping pong bitmap height
    @Getter
    private List<Popup> popups = new CopyOnWriteArrayList<>(); // popups messages array list

    @Getter
    private List<ShockWave> shockWaves = new CopyOnWriteArrayList<>(); // shockWaves animation array list

    @Getter
    private List<Trail> trail = new CopyOnWriteArrayList<>(); // trail animation array list

    @Getter
    private List<Ball> balls = new CopyOnWriteArrayList<>();

    @Getter
    private Player[] players; // set Players array

    @Getter
    private AI[] ai; // set AI array

    @Getter
    private String[] extraLifeStrings = new String[]{
            "OH YEAH!",
            "WOHOOO!",
            "YEAH BABY!",
            "WOOOT!",
            "AWESOME!",
            "COOL!",
            "GREAT!",
            "YEAH!!",
            "WAY TO GO!",
            "YOU ROCK!"
    };

    @Getter
    private String[] lostLifeStrings = new String[]{
            "YOU SUCK!",
            "LOSER!",
            "GO HOME!",
            "REALLY?!",
            "WIMP!",
            "SUCKER!",
            "HAHAHA!",
            "YOU MAD?!",
            "DIE!",
            "BOOM!"
    };

    @Getter
    private String[] bumpStrings = new String[]{
            "BUMP!",
            "TOINK!",
            "BOINK!",
            "BAM!",
            "WABAM!"
    };

    @Getter
    private String[] zoomStrings = new String[]{
            "ZOOM!",
            "WOOSH!",
            "SUPER MODE!",
            "ZOOMBA!",
            "WARPSPEED!"
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(getLocalClassName(), "Activity started");

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.wakelock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "My Tag");
        this.wakelock.acquire();

        ballCount = getIntent().getIntExtra("BALLS_COUNT", -1); // retrieve balls count from main activity
        if (ballCount < 1) {
            ballCount = 3;
        }
        soloGame = getIntent().getBooleanExtra("SOLO_GAME", false);

        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            portrait = false;
            Log.i(getLocalClassName(), "Screen is landscape");
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
            portrait = true;
            Log.i(getLocalClassName(), "Screen is portrait");
        }

        if (soloGame) {
            playerCount = 1;
        } else if (portrait) {
            playerCount = 2;
        } else {
            playerCount = 4;
        }

        players = new Player[playerCount]; // set Players array
        ai = new AI[playerCount - 1]; // set AI array

        LinearLayout lLayout = new LinearLayout(getApplicationContext());
        GameScreen gameScreen = new GameScreen(getApplicationContext()); // set SurfaceView
        lLayout.addView(gameScreen);
        setContentView(lLayout);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(getLocalClassName(), "Activity stopped");
        this.wakelock.release();
    }

    public void onPause() {
        super.onPause();
        finish(); // disallow pausing
    }

    public void showScore() // show score screen
    {
        Intent scoreIntent = new Intent(this, ScoreActivity.class);
        scoreIntent.putExtra(score, Integer.toString(gameScore));
        startActivity(scoreIntent);
        Log.i(getLocalClassName(), "Game ended");
        finish();
    }

    public void doShake(int time) // phone vibrate
    {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(time);
    }

    public void adjustLife(boolean isIncrement) {
        if (isIncrement)
            life++;
        else
            life--;
    }

    public void addGameScore(int value) {
        gameScore += value;
    }

    public void incrementHitCounter() {
        hitCounter++;
    }

    static final class GameSurfaceThread extends Thread {
        GameActivity gameActivity;
        SurfaceHolder surfaceHolder;
        GameScreen gameScreen;

        public GameSurfaceThread(GameActivity gameActivity, SurfaceHolder holder, GameScreen gameScreen) {
            this.gameActivity = gameActivity;
            setName("SurfaceView");
            surfaceHolder = holder;
            this.gameScreen = gameScreen;
            start();
        }

        public void run() {
            Canvas canvas = null;
            while (gameActivity.running) {
                try {
                    canvas = surfaceHolder.lockCanvas(null);
                    gameScreen.screenDraw(canvas);
                } catch (NullPointerException e) {
                    Log.e(this.gameActivity.getLocalClassName(), e.toString());
                } finally {
                    if (canvas != null) {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }
    }

    private class GlobalThread implements Runnable {
        GlobalThread() {
            start();
        }

        public void start() {
            Thread thread = new Thread(this);
            thread.setName("GlobalThread");
            thread.setDaemon(true);
            thread.start();
        }

        public void run() {
            while (running) {
                if (life < 0) // game over condition
                {
                    running = false;
                    SoundManager.getInstance().playSound(Sound.RESTART);
                    showScore();
                }

                if (hitCounter == 10) // AI exchange places per trigger
                {
                    reversePosition = !reversePosition;
                    hitCounter = 0;
                }

                if (balls.size() < ballCount) {
                    balls.add(new Ball(GameActivity.this, balls, players, true));
                }

                try {
                    Thread.sleep(40);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Log.e("GlobalThread", e.toString());
                }
            }
        }
    }

    public class GameScreen extends SurfaceView implements Callback {
        Bitmap pongBottomRight; // bottom R pong image
        Bitmap pongBottomLeft; // bottom L pong image
        Bitmap pongTopRight; // top R pong image
        Bitmap pongTopLeft; // top L pong image
        Bitmap back; // background

        Paint pint = new Paint(); // balls paint
        Paint scoreText = new Paint();
        Paint popupText = new Paint();
        Paint ballTrail = new Paint(); // balls trail
        Paint circleStrokePaint = new Paint();
        Paint centerLinePaint = new Paint();

        int centerLineCounter = 32;
        boolean centerLineup;

        public GameScreen(Context context) {
            super(context);
            surfaceHolder = getHolder();
            surfaceHolder.addCallback(this);

            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);

            canvasWidth = metrics.widthPixels;
            canvasHeight = metrics.heightPixels;
            midpoint = canvasWidth / 2;
            centerLine = canvasHeight / 2;

            if (portrait) // create normal background
            {
                back = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.back), canvasWidth, canvasHeight, true);
                Log.i(getLocalClassName(), "Portrait background created");
            } else // create rotated background
            {
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                back = Bitmap.createBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.back), 0, 0, BitmapFactory.decodeResource(getResources(), R.drawable.back).getWidth(), BitmapFactory.decodeResource(getResources(), R.drawable.back).getHeight(), matrix, true);
                back = Bitmap.createScaledBitmap(back, canvasWidth, canvasHeight, true);
                Log.i(getLocalClassName(), "Landscape background created");
            }

            pongBottomRight = BitmapFactory.decodeResource(getResources(), R.drawable.pong); // create pong image for playerCount
            Log.i(getLocalClassName(), "pong bottom goingRight created");
            Matrix matrix = new Matrix();
            matrix.preScale(-1.0f, 1.0f);
            pongBottomLeft = Bitmap.createBitmap(pongBottomRight, 0, 0, pongBottomRight.getWidth(), pongBottomRight.getHeight(), matrix, true);
            Log.i(getLocalClassName(), "pong bottom left created");

            Matrix ai_matrix = new Matrix(); // create flipped clone of playerCount image for computer
            ai_matrix.preScale(1.0f, -1.0f);
            pongTopRight = Bitmap.createBitmap(pongBottomRight, 0, 0, pongBottomRight.getWidth(), pongBottomRight.getHeight(), ai_matrix, true);
            Log.i(getLocalClassName(), "pong top goingRight created");
            pongTopLeft = Bitmap.createBitmap(pongBottomLeft, 0, 0, pongBottomRight.getWidth(), pongBottomRight.getHeight(), ai_matrix, true);
            Log.i(getLocalClassName(), "pong top left created");

            Typeface myType = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);
            scoreText.setColor(Color.WHITE);
            scoreText.setTypeface(myType);

            popupText.setTypeface(myType);
            popupText.setTextAlign(Align.CENTER);

            centerLinePaint.setStrokeWidth(3);

            if (metrics.densityDpi == DisplayMetrics.DENSITY_LOW) // adjust to low DPI
            {
                popupText.setTextSize(8);
                scoreText.setTextSize(9);
                ballSize = 2;
                Log.i(getLocalClassName(), "Screen DPI is low, adjustment sizes set to small");
            } else {
                popupText.setTextSize(12);
                scoreText.setTextSize(15);
                ballSize = 4;
                Log.i(getLocalClassName(), "Screen DPI is not low, adjustment sizes set to normal");
            }

            pint.setColor(Color.WHITE);
            circleStrokePaint.setStyle(Paint.Style.STROKE);

            pongHeight = pongBottomRight.getHeight();
            pongWidth = pongBottomLeft.getWidth();

            if (balls.size() == 1) {
                Log.i(getLocalClassName(), "Ball initialized");
            } else {
                Log.i(getLocalClassName(), "Balls initialized");
            }

            if (portrait) {
                players[0] = new Player(GameActivity.this);
                players[0].getPosition().set(midpoint, canvasHeight - canvasHeight / 3);
                Log.i(getLocalClassName(), "Player0 initialized");

                if (!soloGame) {
                    players[1] = new Player(GameActivity.this);
                    players[1].getPosition().set(midpoint, canvasHeight / 3);
                    ai[0] = new AI(GameActivity.this, balls, players[0], players[1]); // pass parameters to AI object
                    Log.i(getLocalClassName(), "AI0 initialized");
                    Log.i(getLocalClassName(), "Player1 initialized");
                }
            } else {
                for (int playerCounter = 0; playerCounter < players.length; playerCounter++) // initialize playerCount and AI
                {
                    players[playerCounter] = new Player(GameActivity.this);
                    Log.i(getLocalClassName(), "Player" + Integer.toString(playerCounter) + " initialized");

                    if (playerCounter == 0) {
                        if (portrait) {
                            players[playerCounter].getPosition().set(midpoint / 2, canvasHeight / 3);
                        } else {
                            players[playerCounter].getPosition().set(midpoint - midpoint / 2, canvasHeight - canvasHeight / 3);
                        }
                    } else if (!soloGame) {
                        ai[playerCounter - 1] = new AI(GameActivity.this, balls, players[0], players[playerCounter], Quadrant.values()[playerCounter - 1]);
                        switch (playerCounter) {
                            case 1:
                                players[playerCounter].getPosition().set(midpoint / 2, canvasHeight / 3); // set AI start location to 2nd guardQuadrant
                                Log.i(getLocalClassName(), "AI0 initialized");
                                break;
                            case 2:
                                players[playerCounter].getPosition().set(midpoint + midpoint + 2, canvasHeight / 3); // set AI start location to 3rd guardQuadrant
                                Log.i(getLocalClassName(), "AI1 initialized");
                                break;
                            case 3:
                                players[playerCounter].getPosition().set(midpoint + midpoint + 2, canvasHeight - canvasHeight / 3); // set AI start location to 4th guardQuadrant
                                Log.i(getLocalClassName(), "AI2 initialized");
                        }
                    }
                }
            }

            for (int ballCounter = 0; ballCounter < ballCount; ballCounter++) // create instances of all balls
            {
                balls.add(new Ball(GameActivity.this, balls, players, false));
            }
            new GlobalThread();
        }

        public void surfaceDestroyed(SurfaceHolder holder) // when user leaves game
        {
            running = false;
            Log.i(getLocalClassName(), "Surface destroyed");
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Log.i(getLocalClassName(), "Surface changed");
        }

        public void surfaceCreated(SurfaceHolder holder) // when user enters game
        {
            gameSurfaceThread = new GameSurfaceThread(GameActivity.this, holder, this);
            Log.i(getLocalClassName(), "Surface created");
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                players[0].getPosition().x = (int) event.getX();

                if (soloGame) {
                    players[0].getPosition().y = (int) event.getY();
                } else if ((int) event.getY() >= centerLine) {
                    players[0].getPosition().y = (int) event.getY();
                }
            }
            return true;
        }

        protected void screenDraw(Canvas canvas) {
            canvas.drawBitmap(back, 0, 0, null);
            centerLinePaint.setColor(Color.argb(centerLineCounter, 255, 255, 255));

            if (centerLineCounter == 128 || centerLineCounter == 32) {
                centerLineup = !centerLineup;
            }

            if (centerLineup) {
                centerLineCounter++;
            } else {
                centerLineCounter--;
            }

            if (!soloGame) // draw centerLine
            {
                canvas.drawLine(0, centerLine + 5, canvasWidth, centerLine + 5, centerLinePaint);
                canvas.drawLine(0, centerLine - 5, canvasWidth, centerLine - 5, centerLinePaint);
            }

            for (int playerCounter = 0; playerCounter < players.length; playerCounter++) // draw playerCount
            {
                if (playerCounter == 0 || playerCounter == 3) // if playerCount is a bottom playerCount
                {
                    if (players[playerCounter].isGoingRight()) {
                        canvas.drawBitmap(pongBottomRight, players[playerCounter].getPosition().x, players[playerCounter].getPosition().y, null);
                    } else {
                        canvas.drawBitmap(pongBottomLeft, players[playerCounter].getPosition().x, players[playerCounter].getPosition().y, null);
                    }
                } else if (players[playerCounter].isGoingRight()) {
                    canvas.drawBitmap(pongTopRight, players[playerCounter].getPosition().x, players[playerCounter].getPosition().y, null);
                } else {
                    canvas.drawBitmap(pongTopLeft, players[playerCounter].getPosition().x, players[playerCounter].getPosition().y, null);
                }
            }

            for (Ball ball : balls) // balls drawer
            {
                if (ball.isAlive()) {
                    canvas.drawCircle(ball.getPosition().x, ball.getPosition().y, ballSize, pint);
                }
            }

            for (int index = 0; index < trail.size(); index++) // trail drawer
            {
                Trail currentTrail = trail.get(index);
                if (currentTrail.getLife() > 0) {
                    ballTrail.setStrokeWidth(ballSize - currentTrail.calcSize());
                    ballTrail.setColor(Color.argb(currentTrail.getLife() * 25, 255, 255, 255));
                    canvas.drawLine(currentTrail.getStartPoint().x, currentTrail.getStartPoint().y, currentTrail.getEndPoint().x, currentTrail.getEndPoint().y, ballTrail);
                    currentTrail.decrementLife();
                } else {
                    trail.remove(index);
                }
            }

            for (int index = 0; index < shockWaves.size(); index++) // shockWaves drawer
            {
                ShockWave currentShockWave = shockWaves.get(index);
                int currentShockWaveLife = currentShockWave.getLife();
                if (currentShockWaveLife > 0) // bump animation
                {
                    switch (currentShockWave.getType()) {
                        case EXTRA_SMALL_WAVE:
                            circleStrokePaint.setColor(Color.argb(currentShockWaveLife * 23, 255, 255, 255));
                            circleStrokePaint.setStrokeWidth(1);
                            canvas.drawCircle(currentShockWave.getPosition().x, currentShockWave.getPosition().y, 11 - currentShockWaveLife, circleStrokePaint);
                            break;
                        case SMALL_WAVE:
                            circleStrokePaint.setColor(Color.argb(currentShockWaveLife * 12, 255, 255, 255));
                            circleStrokePaint.setStrokeWidth(2);
                            canvas.drawCircle(currentShockWave.getPosition().x, currentShockWave.getPosition().y, 21 - currentShockWaveLife, circleStrokePaint);
                            break;
                        case MEDIUM_WAVE:
                            circleStrokePaint.setColor(Color.argb(currentShockWaveLife * 2, 255, 255, 255));
                            circleStrokePaint.setStrokeWidth(1);
                            canvas.drawCircle(currentShockWave.getPosition().x, currentShockWave.getPosition().y, 128 - currentShockWaveLife, circleStrokePaint);
                            break;
                        case LARGE_WAVE:
                            circleStrokePaint.setColor(Color.argb(currentShockWaveLife, 255, 255, 255));
                            circleStrokePaint.setStrokeWidth(1);
                            canvas.drawCircle(currentShockWave.getPosition().x, currentShockWave.getPosition().y, 252 - currentShockWaveLife, circleStrokePaint);
                    }
                } else {
                    shockWaves.remove(index);
                }
            }

            for (int index = 0; index < popups.size(); index++) // popups text drawer
            {
                Popup currentPopup = popups.get(index);
                if (currentPopup.getLife() > 0) // if popups text is to be shown
                {
                    int popupLife = currentPopup.getLife();
                    popupText.setColor(Color.argb(popupLife, 255, 255, 255)); // text fade effect

                    switch (currentPopup.getType()) {
                        case SCORE_UP:
                            canvas.drawText(extraLifeStrings[currentPopup.getIndex()], currentPopup.getPosition().x, currentPopup.getPosition().y - popupLife, popupText);
                            break;
                        case LOSE_LIFE:
                            canvas.drawText(lostLifeStrings[currentPopup.getIndex()], currentPopup.getPosition().x, currentPopup.getPosition().y + popupLife, popupText);
                            break;
                        case SOLO:
                            canvas.drawText("+1", currentPopup.getPosition().x, currentPopup.getPosition().y + currentPopup.getLife(), popupText);
                    }
                } else {
                    popups.remove(index);
                }
            }

            if (life > 0) {
                canvas.drawText("Ball Count: " + Integer.toString(balls.size()) + " " + "Score: " + Integer.toString(gameScore) + "  " + "Extra Life: " + Integer.toString(life), 10, canvasHeight - 10, scoreText);
            }
        }
    }
}