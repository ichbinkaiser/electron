package ichbinkaiser.electron;

import android.graphics.Point;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

final class Ball implements Runnable
{
    GameActivity gameActivity;
    ArrayList<Ball> balls = new ArrayList<>();
    Player[] player;

    int hits = 0, speed = 5;
    Point position = new Point();
    Point prevPosition = new Point(); // previous position
    Random rnd = new Random();

    boolean bump; // is hit by player
    boolean goingLeft; // is going left
    boolean collided; // has collided
    boolean superMode = false; // balls in super mode
    boolean alive = true; //

    int angle = rnd.nextInt(5); // angle of balls bounce
    int speedBonusX = 0, speedBonusY = 0;
    int spawnWave = 0;

    Ball(GameActivity gameActivity, ArrayList<Ball> balls, Player[] player, boolean newBall)
    {
        this.gameActivity = gameActivity;
        this.balls = balls;
        this.player = player;

        goingLeft = rnd.nextBoolean();
        bump = rnd.nextBoolean();

        position.x = rnd.nextInt(gameActivity.canvasWidth);

        if (!bump)
        {
            position.y = rnd.nextInt(150) + 5;
        }
        else
        {
            position.y = rnd.nextInt(150) + gameActivity.canvasHeight - 150;
        }

        if (newBall)
        {
            spawnWave = 5;
            GameActivity.SOUNDMANAGER.playSound(SoundManager.Sound.SPAWN, 1);
        }
        start();
    }

    boolean checkCollision(Point object) // balls collision detection
    {
        return object.x <= position.x + gameActivity.ballSize - 1 && object.x >= position.x - gameActivity.ballSize - 1 && object.y <= position.y + gameActivity.ballSize - 1 && object.y >= position.y - gameActivity.ballSize - 1;
    }

    public void start()
    {
        Thread thread = new Thread(this);
        thread.setName("Ball");
        thread.start();
    }

    public void run()
    {
        while (gameActivity.running && alive)
        {
            ////////////////////////////////////// BALL TO PLAYER COLLISION DETECTION ////////////////////////////////

            for (int playercounter = 0; playercounter < player.length; playercounter++)
            {
                if (playercounter == 0 || playercounter == 3) // bottom player
                {
                    if (position.y >= player[playercounter].position.y && position.y <= player[playercounter].position.y + gameActivity.pongHeight && position.x >= player[playercounter].position.x && position.x <= player[playercounter].position.x + gameActivity.pongWidth && !bump) // bottom player to balls collision detection
                    {
                        angle = rnd.nextInt(speed);
                        bump = true;
                        hits++;
                        gameActivity.shockWaves.add(new ShockWave(position, ShockWave.Type.EXTRA_SMALL_WAVE));

                        goingLeft = (player[playercounter].goingRight);

                        if (playercounter == 0)
                        {
                            gameActivity.gameScore = +(1 + speed / 11);
                            gameActivity.doShake(40);

                            if (gameActivity.gameScore % 20 == 0 && gameActivity.ballCount < 0)
                            {
                                balls.add(new Ball(gameActivity, balls, player, true));
                            }
                        }
                        GameActivity.SOUNDMANAGER.playSound(SoundManager.Sound.POP, 1);

                        gameActivity.hitCounter++;

                        if (hits > 0 && hits % 3 == 0) // increase balls speed for ever three hits
                        {
                            speed += 3;
                        }

                        if (speedBonusX < player[playercounter].speedX / 10)
                        {
                            speedBonusX = (player[playercounter].speedX / 20); // get balls speed bonus from directional velocity of player
                        }

                        if (speedBonusY < player[playercounter].speedY / 10)
                        {
                            speedBonusY = player[playercounter].speedY / 20;
                        }

                        if (gameActivity.soloGame)
                        {
                            gameActivity.popups.add(new Popup(position, Popup.Type.SOLO, 0)); // popups text in score++ in solo mode
                        }
                    }
                }

                else // top players
                {
                    if (position.y >= player[playercounter].position.y && position.y <= player[playercounter].position.y + gameActivity.pongHeight && position.x >= player[playercounter].position.x && position.x <= player[playercounter].position.x + gameActivity.pongWidth && bump) // top player to balls collision detection
                    {
                        angle = rnd.nextInt(speed);
                        bump = false;
                        hits++;

                        gameActivity.shockWaves.add(new ShockWave(position, ShockWave.Type.EXTRA_SMALL_WAVE));
                        GameActivity.SOUNDMANAGER.playSound(SoundManager.Sound.POP, 1);

                        if (hits > 0 && hits % 3 == 0)
                        {
                            speed += 3;
                        }
                    }
                }
            }

            /////////////////////////////// BALL TO BALL COLLISION DETECTION /////////////////////////////

            for (Ball currentBall : balls) // balls to balls collision detection
            {
                if (this != currentBall && !collided) // if balls is not compared to itself and has not yet collided
                {
                    if (checkCollision(currentBall.position)) // balls collision detected
                    {
                        angle = rnd.nextInt(speed);
                        GameActivity.SOUNDMANAGER.playSound(SoundManager.Sound.HIT, 1);
                        goingLeft = !goingLeft && !currentBall.goingLeft; // go right if bumped balls is going left
                        currentBall.goingLeft = !goingLeft; // reverse direction of the bumped balls
                        currentBall.collided = true;
                    }
                }
            }
            collided = false;

            prevPosition.x = position.x; // position tracing
            prevPosition.y = position.y;

            ////////////////////////// BALL MOVEMENT ///////////////////////////////

            if (!bump)
            {
                position.y += speed + angle + speedBonusY;
            }
            else
            {
                position.y -= speed + angle + speedBonusY; // balls vertical movement
            }

            if (goingLeft)
            {
                position.x += speed + speedBonusX;
            }
            else
            {
                position.x -= speed + speedBonusX; // balls horizontal movement
            }

            if (spawnWave > 0) // spawn_wave animation
            {
                gameActivity.shockWaves.add(new ShockWave(position, ShockWave.Type.SMALL_WAVE));
                spawnWave--;
            }

            ////////////////////////// WORLD BOUNDS CONTROL ///////////////////////////////

            if (position.y < 0 || position.y > gameActivity.canvasHeight) // balls has exceeded top or bottom bounds
            {
                if (!gameActivity.soloGame) // if multi-player
                {
                    if (position.y < 0) // balls has reached top
                    {
                        gameActivity.life++;
                        gameActivity.popups.add(new Popup(position, Popup.Type.SCOREUP, gameActivity.extraLifeStrings.length));
                        GameActivity.SOUNDMANAGER.playSound(SoundManager.Sound.LIFE_UP, 1);
                    }

                    else // balls has reached bottom
                    {
                        gameActivity.life--;
                        gameActivity.popups.add(new Popup(position, Popup.Type.LOSELIFE, gameActivity.lostLifeStrings.length));

                        GameActivity.SOUNDMANAGER.playSound(SoundManager.Sound.DOWN, 1);
                    }

                    if (superMode)
                    {
                        gameActivity.shockWaves.add(new ShockWave(position, ShockWave.Type.LARGE_WAVE));
                    }
                    else
                    {
                        gameActivity.shockWaves.add(new ShockWave(position, ShockWave.Type.MEDIUM_WAVE));
                    }

                    gameActivity.doShake(100);
                    alive = false;
                }

                else // if solo
                {
                    if (position.y < 0) // balls has reached top
                    {
                        angle = rnd.nextInt(speed);
                        bump = false;
                    }

                    else
                    {
                        gameActivity.life--;

                        if (superMode)
                        {
                            gameActivity.shockWaves.add(new ShockWave(position, ShockWave.Type.LARGE_WAVE));
                        }
                        else
                        {
                            gameActivity.shockWaves.add(new ShockWave(position, ShockWave.Type.MEDIUM_WAVE));
                        }

                        superMode = false;
                        GameActivity.SOUNDMANAGER.playSound(SoundManager.Sound.DOWN, 1);
                        gameActivity.popups.add(new Popup(position, Popup.Type.LOSELIFE, gameActivity.lostLifeStrings.length));
                        gameActivity.doShake(100);
                        alive = false;
                    }
                }
            }

            if (position.x < 0) // balls has reached left wall
            {
                angle = rnd.nextInt(speed);
                goingLeft = true;
                GameActivity.SOUNDMANAGER.playSound(SoundManager.Sound.POPWALL, 1);
            }

            if (position.x > gameActivity.canvasWidth) // balls has reached right wall
            {
                angle = rnd.nextInt(speed);
                goingLeft = false;
                GameActivity.SOUNDMANAGER.playSound(SoundManager.Sound.POPWALL, 1);
            }

            /////////// SUPER MODE CONTROLS //////////////

            if (speed == 11 && !superMode) // make super mode
            {
                GameActivity.SOUNDMANAGER.playSound(SoundManager.Sound.DING, 1);
                gameActivity.shockWaves.add(new ShockWave(position, ShockWave.Type.MEDIUM_WAVE));
                superMode = true;
            }

            if (superMode) // draw super mode animation
            {
                gameActivity.trail.add(new Trail(prevPosition, position));
                gameActivity.shockWaves.add(new ShockWave(position, ShockWave.Type.EXTRA_SMALL_WAVE));
            }

            try
            {
                Thread.sleep(40);
            }

            catch (InterruptedException e)
            {
                e.printStackTrace();
                Log.e("Ball", e.toString());
            }
        }

        try
        {
            Thread.sleep(1000);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
            Log.e("Ball", e.toString());
        }

        gameActivity.balls.remove(this); // remove this dead balls
    }
}