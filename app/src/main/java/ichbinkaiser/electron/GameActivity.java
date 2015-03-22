package ichbinkaiser.electron;

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
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import java.util.ArrayList;

public class GameActivity extends Activity
{
    static String score;
    static SoundManager SOUNDMANAGER = new SoundManager(); // global sound manager
    protected PowerManager.WakeLock wakelock;
    int canvasHeight, canvasWidth, midpoint, centerLine;
    int life = 50, gameScore = 0;
    int ballCount = 3;
    boolean running = true; // game running
    int ballSize;
    boolean portrait; // screen orientation
    boolean reversePosition = false; // AI reverse position in doubles mode
    boolean soloGame = false; // is solo game
    int hitCounter = 0; // balls hit counter for AI friend guard zone switch
    int players; // number of players
    int pongWidth, pongHeight; // ping pong bitmap height
    ArrayList<Popup> popups = new ArrayList<>(); // popups messages array list
    ArrayList<ShockWave> shockWaves = new ArrayList<>(); // shockWaves animation array list
    ArrayList<Trail> trail = new ArrayList<>(); // trail animation array list
    ArrayList<Ball> balls = new ArrayList<>();
    GameSurfaceThread gameSurfaceThread;
    SurfaceHolder surfaceHolder;
    Player[] player; // set Players array
    AI[] ai; // set AI array
    String[] extraLifeStrings = new String[]{"OH YEAH!", "WOHOOO!", "YEAH BABY!", "WOOOT!", "AWESOME!", "COOL!", "GREAT!", "YEAH!!", "WAY TO GO!", "YOU ROCK!"};
    String[] lostLifeStrings = new String[]{"YOU SUCK!", "LOSER!", "GO HOME!", "REALLY?!", "WIMP!", "SUCKER!", "HAHAHA!", "YOU MAD?!", "DIE!", "BOOM!"};
    String[] bumpStrings = new String[]{"BUMP!", "TOINK!", "BOINK!", "BAM!", "WABAM!"};
    String[] zoomStrings = new String[]{"ZOOM!", "WOOSH!", "SUPER MODE!", "ZOOMBA!", "WARPSPEED!"};

    @Override
    public void onCreate(Bundle savedinstancestate)
    {
        super.onCreate(savedinstancestate);
        Log.i(getLocalClassName(), "Activity started");

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.wakelock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "My Tag");
        this.wakelock.acquire();

        ballCount = getIntent().getIntExtra("BALLS_COUNT", -1); // retrieve balls count from main activity
        if (ballCount < 1)
        {
            ballCount = 3;
        }
        soloGame = getIntent().getBooleanExtra("SOLO_GAME", false);

        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            portrait = false;
            Log.i(getLocalClassName(), "Screen is landscape");
        }

        else
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
            portrait = true;
            Log.i(getLocalClassName(), "Screen is portrait");
        }

        if (soloGame)
        {
            players = 1;
        }
        else if (portrait)
        {
            players = 2;
        }
        else
        {
            players = 4;
        }

        player = new Player[players]; // set Players array
        ai = new AI[players - 1]; // set AI array

        LinearLayout lLayout = new LinearLayout(getApplicationContext());
        GameScreen gameScreen = new GameScreen(getApplicationContext()); // set SurfaceView
        lLayout.addView(gameScreen);
        setContentView(lLayout);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Log.i(getLocalClassName(), "Activity stopped");
        this.wakelock.release();
    }

    public void onPause()
    {
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

    private AI.Quadrant getEnumFromPlayerNumber(int playerNumber)
    {
        switch (playerNumber)
        {
            case 1:
                return AI.Quadrant.TOPLEFT;
            case 2:
                return AI.Quadrant.TOPRIGHT;
            case 3:
                return AI.Quadrant.BOTTOMRIGHT;
            default:
                return AI.Quadrant.TOPLEFT;
        }
    }

    private class GlobalThread implements Runnable
    {
        GlobalThread()
        {
            start();
        }

        public void start()
        {
            Thread thread = new Thread(this);
            thread.setName("GlobalThread");
            thread.start();
        }

        public void run()
        {
            while (running)
            {
                if (life < 0) // game over condition
                {
                    SOUNDMANAGER.playSound(SoundManager.Sound.RESTART, 1);
                    showScore();
                }

                if (hitCounter == 10) // AI exchange places per trigger
                {
                    reversePosition = !reversePosition;
                    hitCounter = 0;
                }

                if (balls.size() < ballCount)
                {
                    balls.add(new Ball(GameActivity.this, balls, player, true));
                }

                try
                {
                    Thread.sleep(40);
                }

                catch (InterruptedException e)
                {
                    e.printStackTrace();
                    Log.e("GlobalThread", e.toString());
                }
            }
        }
    }

    public class GameScreen extends SurfaceView implements Callback
    {
        Bitmap pongBottomRight; // bottom R pong image
        Bitmap pongBottomLeft; // bottom L pong image
        Bitmap pongTopRight; // top R pong image
        Bitmap pongTopLeft; // top L pong image
        Bitmap back; // background

        Paint pint = new Paint(); // balls paint
        Paint scoretext = new Paint();
        Paint popuptext = new Paint();
        Paint balltrail = new Paint(); // balls trail
        Paint circleStrokePaint = new Paint();
        Paint centerlinepaint = new Paint();

        int centerlinecounter = 32;
        boolean centerlineup;

        public GameScreen(Context context)
        {
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
            }

            else // create rotated background
            {
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                back = Bitmap.createBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.back), 0, 0, BitmapFactory.decodeResource(getResources(), R.drawable.back).getWidth(), BitmapFactory.decodeResource(getResources(), R.drawable.back).getHeight(), matrix, true);
                back = Bitmap.createScaledBitmap(back, canvasWidth, canvasHeight, true);
                Log.i(getLocalClassName(), "Landscape background created");
            }

            pongBottomRight = BitmapFactory.decodeResource(getResources(), R.drawable.pong); // create pong image for player
            Log.i(getLocalClassName(), "pong bottom goingRight created");
            Matrix matrix = new Matrix();
            matrix.preScale(-1.0f, 1.0f);
            pongBottomLeft = Bitmap.createBitmap(pongBottomRight, 0, 0, pongBottomRight.getWidth(), pongBottomRight.getHeight(), matrix, true);
            Log.i(getLocalClassName(), "pong bottom left created");

            Matrix ai_matrix = new Matrix(); // create flipped clone of player image for computer
            ai_matrix.preScale(1.0f, -1.0f);
            pongTopRight = Bitmap.createBitmap(pongBottomRight, 0, 0, pongBottomRight.getWidth(), pongBottomRight.getHeight(), ai_matrix, true);
            Log.i(getLocalClassName(), "pong top goingRight created");
            pongTopLeft = Bitmap.createBitmap(pongBottomLeft, 0, 0, pongBottomRight.getWidth(), pongBottomRight.getHeight(), ai_matrix, true);
            Log.i(getLocalClassName(), "pong top left created");

            Typeface myType = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);
            scoretext.setColor(Color.WHITE);
            scoretext.setTypeface(myType);

            popuptext.setTypeface(myType);
            popuptext.setTextAlign(Align.CENTER);

            centerlinepaint.setStrokeWidth(3);

            if (metrics.densityDpi == DisplayMetrics.DENSITY_LOW) // adjust to low DPI
            {
                popuptext.setTextSize(8);
                scoretext.setTextSize(9);
                ballSize = 2;
                Log.i(getLocalClassName(), "Screen DPI is low, adjustment sizes set to small");
            }

            else
            {
                popuptext.setTextSize(12);
                scoretext.setTextSize(15);
                ballSize = 4;
                Log.i(getLocalClassName(), "Screen DPI is not low, adjustment sizes set to normal");
            }

            pint.setColor(Color.WHITE);
            circleStrokePaint.setStyle(Paint.Style.STROKE);

            pongHeight = pongBottomRight.getHeight();
            pongWidth = pongBottomLeft.getWidth();

            if (balls.size() == 1)
            {
                Log.i(getLocalClassName(), "Ball initialized");
            }
            else
            {
                Log.i(getLocalClassName(), "Balls initialized");
            }

            if (portrait)
            {
                player[0] = new Player(GameActivity.this);
                player[0].position.set(midpoint, canvasHeight - canvasHeight / 3);
                Log.i(getLocalClassName(), "Player0 initialized");

                if (!soloGame)
                {
                    player[1] = new Player(GameActivity.this);
                    player[1].position.set(midpoint, canvasHeight / 3);
                    ai[0] = new AI(GameActivity.this, balls, player[0], player[1]); // pass parameters to AI object
                    Log.i(getLocalClassName(), "AI0 initialized");
                    Log.i(getLocalClassName(), "Player1 initialized");
                }
            }

            else
            {
                for (int playercounter = 0; playercounter < player.length; playercounter++) // initialize player and AI
                {
                    player[playercounter] = new Player(GameActivity.this);
                    Log.i(getLocalClassName(), "Player" + Integer.toString(playercounter) + " initialized");

                    if (playercounter == 0)
                    {
                        if (portrait)
                        {
                            player[playercounter].position.set(midpoint / 2, canvasHeight / 3);
                        }
                        else
                        {
                            player[playercounter].position.set(midpoint - midpoint / 2, canvasHeight - canvasHeight / 3);
                        }
                    }

                    else if (!soloGame)
                    {
                        ai[playercounter - 1] = new AI(GameActivity.this, balls, player[0], player[playercounter], getEnumFromPlayerNumber(playercounter - 1));
                        switch (playercounter)
                        {
                            case 1:
                                player[playercounter].position.set(midpoint / 2, canvasHeight / 3); // set AI start location to 2nd guardQuadrant
                                Log.i(getLocalClassName(), "AI0 initialized");
                                break;
                            case 2:
                                player[playercounter].position.set(midpoint + midpoint + 2, canvasHeight / 3); // set AI start location to 3rd guardQuadrant
                                Log.i(getLocalClassName(), "AI1 initialized");
                                break;
                            case 3:
                                player[playercounter].position.set(midpoint + midpoint + 2, canvasHeight - canvasHeight / 3); // set AI start location to 4th guardQuadrant
                                Log.i(getLocalClassName(), "AI2 initialized");
                        }
                    }
                }
            }

            for (int ballcounter = 0; ballcounter < ballCount; ballcounter++) // create instances of all balls
            {
                balls.add(new Ball(GameActivity.this, balls, player, false));
            }
            new GlobalThread();
        }

        public void surfaceDestroyed(SurfaceHolder holder) // when user leaves game
        {
            running = false;
            Log.i(getLocalClassName(), "Surface destroyed");
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
        {
            Log.i(getLocalClassName(), "Surface changed");
        }

        public void surfaceCreated(SurfaceHolder holder) // when user enters game
        {
            gameSurfaceThread = new GameSurfaceThread(GameActivity.this, holder, this);
            Log.i(getLocalClassName(), "Surface created");
        }

        @Override
        public boolean onTouchEvent(@NonNull MotionEvent event)
        {
            if (event.getAction() == MotionEvent.ACTION_MOVE)
            {
                player[0].position.x = (int) event.getX();

                if (soloGame)
                {
                    player[0].position.y = (int) event.getY();
                }
                else if ((int) event.getY() >= centerLine)
                {
                    player[0].position.y = (int) event.getY();
                }
            }
            return true;
        }

        @Override
        protected void onDraw(Canvas canvas)
        {
            canvas.drawBitmap(back, 0, 0, null);
            centerlinepaint.setColor(Color.argb(centerlinecounter, 255, 255, 255));

            if (centerlinecounter == 128 || centerlinecounter == 32)
            {
                centerlineup = !centerlineup;
            }

            if (centerlineup)
            {
                centerlinecounter++;
            }
            else
            {
                centerlinecounter--;
            }

            if (!soloGame) // draw centerLine
            {
                canvas.drawLine(0, centerLine + 5, canvasWidth, centerLine + 5, centerlinepaint);
                canvas.drawLine(0, centerLine - 5, canvasWidth, centerLine - 5, centerlinepaint);
            }

            for (int playercounter = 0; playercounter < player.length; playercounter++) // draw player
            {
                if (playercounter == 0 || playercounter == 3) // if player is a bottom player
                {
                    if (player[playercounter].goingRight)
                    {
                        canvas.drawBitmap(pongBottomRight, player[playercounter].position.x, player[playercounter].position.y, null);
                    }
                    else
                    {
                        canvas.drawBitmap(pongBottomLeft, player[playercounter].position.x, player[playercounter].position.y, null);
                    }
                }
                else if (player[playercounter].goingRight)
                {
                    canvas.drawBitmap(pongTopRight, player[playercounter].position.x, player[playercounter].position.y, null);
                }
                else
                {
                    canvas.drawBitmap(pongTopLeft, player[playercounter].position.x, player[playercounter].position.y, null);
                }
            }

            for (Ball currentBall : balls) // balls drawer
            {
                if (currentBall.alive)
                {
                    canvas.drawCircle(currentBall.position.x, currentBall.position.y, ballSize, pint);
                }
            }

            for (int index = 0; index < trail.size(); index++) // trail drawer
            {
                Trail currentTrail = trail.get(index);
                if (currentTrail.life > 0)
                {
                    balltrail.setStrokeWidth(ballSize - currentTrail.calcSize());
                    balltrail.setColor(Color.argb(currentTrail.life * 25, 255, 255, 255));
                    canvas.drawLine(currentTrail.startPoint.x, currentTrail.startPoint.y, currentTrail.endPoint.x, currentTrail.endPoint.y, balltrail);
                    currentTrail.life--;
                }
                else
                {
                    trail.remove(index);
                }
            }

            for (int index = 0; index < shockWaves.size(); index++) // shockWaves drawer
            {
                ShockWave currentShockWave = shockWaves.get(index);
                int currentShockWaveLife = currentShockWave.getLife();
                if (currentShockWaveLife > 0) // bump animation
                {
                    switch (currentShockWave.type)
                    {
                        case EXTRA_SMALL_WAVE:
                            circleStrokePaint.setColor(Color.argb(currentShockWaveLife * 23, 255, 255, 255));
                            circleStrokePaint.setStrokeWidth(1);
                            canvas.drawCircle(currentShockWave.position.x, currentShockWave.position.y, 11 - currentShockWaveLife, circleStrokePaint);
                            break;
                        case SMALL_WAVE:
                            circleStrokePaint.setColor(Color.argb(currentShockWaveLife * 12, 255, 255, 255));
                            circleStrokePaint.setStrokeWidth(2);
                            canvas.drawCircle(currentShockWave.position.x, currentShockWave.position.y, 21 - currentShockWaveLife, circleStrokePaint);
                            break;
                        case MEDIUM_WAVE:
                            circleStrokePaint.setColor(Color.argb(currentShockWaveLife * 2, 255, 255, 255));
                            circleStrokePaint.setStrokeWidth(1);
                            canvas.drawCircle(currentShockWave.position.x, currentShockWave.position.y, 128 - currentShockWaveLife, circleStrokePaint);
                            break;
                        case LARGE_WAVE:
                            circleStrokePaint.setColor(Color.argb(currentShockWaveLife, 255, 255, 255));
                            circleStrokePaint.setStrokeWidth(1);
                            canvas.drawCircle(currentShockWave.position.x, currentShockWave.position.y, 252 - currentShockWaveLife, circleStrokePaint);
                    }
                }
                else
                {
                    shockWaves.remove(index);
                }
            }

            for (int index = 0; index < popups.size(); index++) // popups text drawer
            {
                Popup currentPopup = popups.get(index);
                if (currentPopup.getLife() > 0) // if popups text is to be shown
                {
                    int popupLife = currentPopup.getLife();
                    popuptext.setColor(Color.argb(popupLife, 255, 255, 255)); // text fade effect

                    switch (currentPopup.type)
                    {
                        case SCOREUP:
                            canvas.drawText(extraLifeStrings[currentPopup.index], currentPopup.position.x, currentPopup.position.y - popupLife, popuptext);
                            break;
                        case LOSELIFE:
                            canvas.drawText(lostLifeStrings[currentPopup.index], currentPopup.position.x, currentPopup.position.y + popupLife, popuptext);
                            break;
                        case SOLO:
                            canvas.drawText("+1", currentPopup.position.x, currentPopup.position.y + currentPopup.getLife(), popuptext);
                    }
                }
                else
                {
                    popups.remove(index);
                }
            }

            if (life > 0)
            {
                canvas.drawText("Ball Count: " + Integer.toString(balls.size()) + " " + "Score: " + Integer.toString(gameScore) + "  " + "Extra Life: " + Integer.toString(life), 10, canvasHeight - 10, scoretext);
            }
        }
    }
}