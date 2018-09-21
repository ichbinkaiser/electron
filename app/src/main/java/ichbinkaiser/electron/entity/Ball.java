package ichbinkaiser.electron.entity;

import android.graphics.Point;
import android.util.Log;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import ichbinkaiser.electron.activity.GameActivity;
import ichbinkaiser.electron.control.Player;
import ichbinkaiser.electron.core.SoundManager;
import lombok.Getter;

public class Ball implements Runnable {

    @Getter
    private Point position = new Point();

    @Getter
    private boolean bump; // is hit by playerCount

    @Getter
    private boolean alive = true;

    private GameActivity gameActivity;
    private List<Ball> balls;
    private Player[] players;
    private Point prevPosition = new Point(); // previous position
    private Random rnd = new Random();
    private SoundManager soundManager = SoundManager.getInstance();

    private boolean goingLeft; // is going left
    private boolean collided; // has collided
    private boolean superMode = false; // balls in super mode
    private int angle = rnd.nextInt(5); // angle of balls bounce
    private int speedBonusX = 0, speedBonusY = 0;
    private int spawnWave = 0;
    private int hits = 0, speed = 5;

    public Ball(GameActivity gameActivity, List<Ball> balls, Player[] players, boolean newBall) {
        this.gameActivity = gameActivity;
        this.balls = balls;
        this.players = players;

        goingLeft = rnd.nextBoolean();
        bump = rnd.nextBoolean();

        position.x = rnd.nextInt(gameActivity.getCanvasWidth());

        if (!bump) {
            position.y = rnd.nextInt(150) + 5;
        } else {
            position.y = rnd.nextInt(150) + gameActivity.getCanvasHeight() - 150;
        }

        if (newBall) {
            spawnWave = 5;
            soundManager.playSound(Sound.SPAWN);
        }
        start();
    }

    boolean checkCollision(Point object) // balls collision detection
    {
        return object.x <= position.x + gameActivity.getBallSize() - 1
                && object.x >= position.x - gameActivity.getBallSize() - 1
                && object.y <= position.y + gameActivity.getBallSize() - 1
                && object.y >= position.y - gameActivity.getBallSize() - 1;
    }

    public void start() {
        Thread thread = new Thread(this);
        thread.setName("Ball");
        thread.setDaemon(true);
        thread.start();
    }

    public void run() {
        while (gameActivity.isRunning() && alive) {
            ////////////////////////////////////// BALL TO PLAYER COLLISION DETECTION ////////////////////////////////

            for (int playerCounter = 0; playerCounter < players.length; playerCounter++) {
                if (playerCounter == 0 || playerCounter == 3) // bottom playerCount
                {
                    if (position.y >= players[playerCounter].getPosition().y && position.y <= players[playerCounter].getPosition().y + gameActivity.getPongHeight() && position.x >= players[playerCounter].getPosition().x && position.x <= players[playerCounter].getPosition().x + gameActivity.getPongWidth() && !bump) // bottom playerCount to balls collision detection
                    {
                        angle = rnd.nextInt(speed);
                        bump = true;
                        hits++;
                        gameActivity.getShockWaves().add(new ShockWave(position, ShockWaveType.EXTRA_SMALL_WAVE));

                        goingLeft = (players[playerCounter].isGoingRight());

                        if (playerCounter == 0) // if human players
                        {
                            gameActivity.addGameScore(1 + speed / 11);
                            gameActivity.doShake(40);

                            if (gameActivity.getGameScore() % 20 == 0 && gameActivity.getBallCount() < 0) {
                                balls.add(new Ball(gameActivity, balls, players, true));
                            }
                        }
                        soundManager.playSound(Sound.POP);

                        gameActivity.incrementHitCounter();

                        if (hits > 0 && hits % 3 == 0) // increase balls speed for ever three hits
                        {
                            speed += 3;
                        }

                        if (speedBonusX < players[playerCounter].getSpeedX() / 10) {
                            speedBonusX = (players[playerCounter].getSpeedX() / 20); // get balls speed bonus from directional velocity of playerCount
                        }

                        if (speedBonusY < players[playerCounter].getSpeedY() / 10) {
                            speedBonusY = players[playerCounter].getSpeedY() / 20;
                        }

                        if (gameActivity.isSoloGame()) {
                            gameActivity.getPopups().add(new Popup(position, PopupType.SOLO, 0)); // popups text in score++ in solo mode
                        }
                    }
                } else // top playerCount
                {
                    if (position.y >= players[playerCounter].getPosition().y && position.y <= players[playerCounter].getPosition().y + gameActivity.getPongHeight() && position.x >= players[playerCounter].getPosition().x && position.x <= players[playerCounter].getPosition().x + gameActivity.getPongWidth() && bump) // top playerCount to balls collision detection
                    {
                        angle = rnd.nextInt(speed);
                        bump = false;
                        hits++;

                        gameActivity.getShockWaves().add(new ShockWave(position, ShockWaveType.EXTRA_SMALL_WAVE));
                        soundManager.playSound(Sound.POP);

                        if (hits > 0 && hits % 3 == 0) {
                            speed += 3;
                        }
                    }
                }
            }

            /////////////////////////////// BALL TO BALL COLLISION DETECTION /////////////////////////////

            for (Ball ball : balls) // balls to balls collision detection
            {
                if (this != ball && !collided) // if balls is not compared to itself and has not yet collided
                {
                    if (checkCollision(ball.position)) // balls collision detected
                    {
                        angle = rnd.nextInt(speed);
                        soundManager.playSound(Sound.HIT);
                        goingLeft = !goingLeft && !ball.goingLeft; // go right if bumped balls is going left
                        ball.goingLeft = !goingLeft; // reverse direction of the bumped balls
                        ball.collided = true;
                    }
                }
            }
            collided = false;

            prevPosition.x = position.x; // position tracing
            prevPosition.y = position.y;

            ////////////////////////// BALL MOVEMENT ///////////////////////////////

            if (!bump) {
                position.y += speed + angle + speedBonusY;
            } else {
                position.y -= speed + angle + speedBonusY; // balls vertical movement
            }

            if (goingLeft) {
                position.x += speed + speedBonusX;
            } else {
                position.x -= speed + speedBonusX; // balls horizontal movement
            }

            if (spawnWave > 0) // spawn_wave animation
            {
                gameActivity.getShockWaves().add(new ShockWave(position, ShockWaveType.SMALL_WAVE));
                spawnWave--;
            }

            ////////////////////////// WORLD BOUNDS CONTROL ///////////////////////////////

            if (position.y < 0 || position.y > gameActivity.getCanvasHeight()) // balls has exceeded top or bottom bounds
            {
                if (!gameActivity.isSoloGame()) // if multi-playerCount
                {
                    if (position.y < 0) // balls has reached top
                    {
                        gameActivity.adjustLife(true);
                        gameActivity.getPopups().add(new Popup(position, PopupType.SCORE_UP, gameActivity.getExtraLifeStrings().length));
                        soundManager.playSound(Sound.LIFE_UP);
                    } else // balls has reached bottom
                    {
                        gameActivity.adjustLife(false);
                        gameActivity.getPopups().add(new Popup(position, PopupType.LOSE_LIFE, gameActivity.getLostLifeStrings().length));

                        soundManager.playSound(Sound.DOWN);
                    }

                    if (superMode) {
                        gameActivity.getShockWaves().add(new ShockWave(position, ShockWaveType.LARGE_WAVE));
                    } else {
                        gameActivity.getShockWaves().add(new ShockWave(position, ShockWaveType.MEDIUM_WAVE));
                    }

                    gameActivity.doShake(100);
                    alive = false;
                } else // if solo
                {
                    if (position.y < 0) // balls has reached top
                    {
                        angle = rnd.nextInt(speed);
                        bump = false;
                    } else {
                        gameActivity.adjustLife(false);

                        if (superMode) {
                            gameActivity.getShockWaves().add(new ShockWave(position, ShockWaveType.LARGE_WAVE));
                        } else {
                            gameActivity.getShockWaves().add(new ShockWave(position, ShockWaveType.MEDIUM_WAVE));
                        }

                        superMode = false;
                        soundManager.playSound(Sound.DOWN);
                        gameActivity.getPopups().add(new Popup(position, PopupType.LOSE_LIFE, gameActivity.getLostLifeStrings().length));
                        gameActivity.doShake(100);
                        alive = false;
                    }
                }
            }

            if (position.x < 0) // balls has reached left wall
            {
                angle = rnd.nextInt(speed);
                goingLeft = true;
                soundManager.playSound(Sound.POP_WALL);
            }

            if (position.x > gameActivity.getCanvasWidth()) // balls has reached right wall
            {
                angle = rnd.nextInt(speed);
                goingLeft = false;
                soundManager.playSound(Sound.POP_WALL);
            }

            /////////// SUPER MODE CONTROLS //////////////

            if (speed == 11 && !superMode) // make super mode
            {
                soundManager.playSound(Sound.DING);
                gameActivity.getShockWaves().add(new ShockWave(position, ShockWaveType.MEDIUM_WAVE));
                superMode = true;
            }

            if (superMode) // draw super mode animation
            {
                gameActivity.getTrail().add(new Trail(prevPosition, position));
                gameActivity.getShockWaves().add(new ShockWave(position, ShockWaveType.EXTRA_SMALL_WAVE));
            }

            try {
                Thread.sleep(40);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.e("Ball", e.toString());
            }
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.e("Ball", e.toString());
        }

        gameActivity.getBalls().remove(this); // remove this dead balls
    }
}