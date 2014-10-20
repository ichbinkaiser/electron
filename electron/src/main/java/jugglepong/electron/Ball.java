package jugglepong.electron;

import android.graphics.Point;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

final class Ball implements Runnable
{
	GameActivity gameactivity;
	ArrayList<Ball> ball = new ArrayList<Ball>();
	Player[] player;
	
	int hits = 0, speed = 5;
	Point position = new Point();
	Point pposition = new Point(); // previous position
	Random rnd = new Random();

	boolean bump; // is hit by player
	boolean goingleft; // is going left
	boolean collided; // has collided
    boolean supermode = false; // ball in super mode
    boolean alive = true; //

	int angle = rnd.nextInt(5); // angle of ball bounce
	int speedbonusx = 0, speedbonusy = 0;
	int spawnwave = 0;

	Ball(GameActivity gameactivity, ArrayList<Ball> ball, Player[] player, boolean newBall)
	{
		this.gameactivity = gameactivity;
		this.ball = ball;
		this.player = player;

        goingleft = rnd.nextBoolean();
        bump = rnd.nextBoolean();

        position.x = rnd.nextInt(gameactivity.canvaswidth);

        if (!bump)
            position.y = rnd.nextInt(150) + 5;
        else
            position.y = rnd.nextInt(150) + gameactivity.canvasheight - 150;

        if (newBall)
        {
            spawnwave = 5;
            GameActivity.soundmanager.playSound(8, 1);
        }
        start();
	}

	boolean checkCollision(Point object) // ball collision detection
	{
		return object.x <= position.x + gameactivity.ballsize - 1 && object.x >= position.x - gameactivity.ballsize - 1 && object.y <= position.y + gameactivity.ballsize - 1 && object.y >= position.y - gameactivity.ballsize - 1;
	}

	public void start()
	{
		Thread thread = new Thread(this);
		thread.setName("Ball");
		thread.start();
	}

	public void run()
	{
		while(gameactivity.running && alive)
		{
            ////////////////////////////////////// BALL TO PLAYER COLLISION DETECTION ////////////////////////////////

			for (int playercounter = 0; playercounter < player.length; playercounter++)
			{
				if (playercounter == 0 || playercounter == 3) // bottom player
				{
					if (position.y >= player[playercounter].position.y && position.y <= player[playercounter].position.y + gameactivity.pongheight && position.x >= player[playercounter].position.x && position.x <= player[playercounter].position.x + gameactivity.pongwidth && !bump) // bottom player to ball collision detection
					{
						angle = rnd.nextInt(speed);
						bump = true;
						hits++;
						gameactivity.shockwave.add(new Shockwave(position, Shockwave.EXTRA_SMALL_WAVE));

						goingleft =  (player[playercounter].goingRight);

						if (playercounter == 0)
						{
							gameactivity.gamescore =+ (1 + speed / 11);
							gameactivity.doShake(40);

							if (gameactivity.gamescore % 20 == 0 && gameactivity.ballcount < 0)
								ball.add(new Ball(gameactivity, ball, player, true));
						}
						GameActivity.soundmanager.playSound(1, 1);

						gameactivity.hitcounter++;

						if (hits > 0 && hits % 3 == 0) // increase ball speed for ever three hits
                            speed += 3;

						if (speedbonusx < player[playercounter].speedX / 10)
							speedbonusx = (player[playercounter].speedX / 20); // get ball speed bonus from directional velocity of player

						if (speedbonusy < player[playercounter].speedY / 10)
							speedbonusy = player[playercounter].speedY / 20;

						if (gameactivity.sologame)
							gameactivity.popup.add(new Popup(position, 2, 0)); // popup text in score++ in solo mode
					}
				}

				else // top players
				{
					if (position.y >= player[playercounter].position.y && position.y <= player[playercounter].position.y + gameactivity.pongheight && position.x >= player[playercounter].position.x && position.x <= player[playercounter].position.x + gameactivity.pongwidth && bump) // top player to ball collision detection
					{
						angle = rnd.nextInt(speed);
						bump = false;
						hits++;

						gameactivity.shockwave.add(new Shockwave(position, Shockwave.EXTRA_SMALL_WAVE));
						GameActivity.soundmanager.playSound(1, 1);

						if (hits > 0 && hits % 3 == 0)
							speed += 3;
					}
				}
			}

            /////////////////////////////// BALL TO BALL COLLISION DETECTION /////////////////////////////

			for (int ballcounter = 0; ballcounter < ball.size(); ballcounter++) // ball to ball collision detection
			{
                Ball currentball = ball.get(ballcounter);
				if (this != currentball && !collided) // if ball is not compared to itself and has not yet collided
				{
					if (checkCollision(currentball.position)) // ball collision detected
					{
						angle = rnd.nextInt(speed);

						GameActivity.soundmanager.playSound(SoundManager.HIT, 1);
                        goingleft = !((goingleft) && (!currentball.goingleft)); // go right if bumped ball is going left
                        currentball.goingleft = !goingleft; // reverse direction of the bumped ball
                        currentball.collided = true;
					}
				}
			}
			collided = false;

			pposition.x = position.x; // position tracing
			pposition.y = position.y;

            ////////////////////////// BALL MOVEMENT ///////////////////////////////

			if (!bump)
				position.y += speed + angle + speedbonusy;
			else
				position.y -= speed + angle + speedbonusy; // ball vertical movement
			
			if (goingleft)
				position.x += speed+speedbonusx;
			else
				position.x -= speed + speedbonusx; // ball horizontal movement

			if (spawnwave > 0) // spawn_wave animation
			{
				gameactivity.shockwave.add(new Shockwave(position, Shockwave.SMALL_WAVE));
				spawnwave--;
			}

            ////////////////////////// WORLD BOUNDS CONTROL ///////////////////////////////

			if (position.y < 0 || position.y > gameactivity.canvasheight) // ball has exceeded top or bottom bounds
			{
				if (!gameactivity.sologame) // if multiplayer
				{
					if (position.y < 0) // ball has reached top
					{
						gameactivity.life++;
						gameactivity.popup.add(new Popup(position, 0, gameactivity.extralifestrings.length));
                        GameActivity.soundmanager.playSound(SoundManager.LIFE_UP, 1);
					}

					else // ball has reached bottom
					{
						gameactivity.life--;
						gameactivity.popup.add(new Popup(position, 1, gameactivity.lostlifestrings.length));

                        GameActivity.soundmanager.playSound(SoundManager.DOWN, 1);
					}

					if (supermode)
						gameactivity.shockwave.add(new Shockwave(position, Shockwave.LARGE_WAVE));
					else
						gameactivity.shockwave.add(new Shockwave(position, Shockwave.MEDIUM_WAVE));

					gameactivity.doShake(100);
                    alive = false;
				}

				else // if solo
				{
					if (position.y < 0) // ball has reached top
					{
						angle = rnd.nextInt(speed);
						bump = false;
					}

					else
					{
						gameactivity.life--;
						
						if (supermode)
							gameactivity.shockwave.add(new Shockwave(position, Shockwave.LARGE_WAVE));
						else
							gameactivity.shockwave.add(new Shockwave(position, Shockwave.MEDIUM_WAVE));
						
						supermode = false;
						GameActivity.soundmanager.playSound(SoundManager.DOWN, 1);
						gameactivity.popup.add(new Popup(position, 1, gameactivity.lostlifestrings.length));
						gameactivity.doShake(100);
                        alive = false;
					}
				}
			}

			if (position.x < 0) // ball has reached left wall
			{
				angle = rnd.nextInt(speed);
				goingleft = true;
				GameActivity.soundmanager.playSound(SoundManager.POPWALL, 1);
			}

			if (position.x > gameactivity.canvaswidth) // ball has reached right wall
			{
				angle = rnd.nextInt(speed);
				goingleft = false;
                GameActivity.soundmanager.playSound(SoundManager.POPWALL, 1);
			}

            /////////// SUPER MODE CONTROLS //////////////

			if (speed == 11 && !supermode) // make super mode
			{
				GameActivity.soundmanager.playSound(SoundManager.DING, 1);
				gameactivity.shockwave.add(new Shockwave(position, Shockwave.MEDIUM_WAVE));
				supermode = true;
			}

			if (supermode) // draw super mode animation
			{
				gameactivity.trail.add(new Trail(pposition, position));
				gameactivity.shockwave.add(new Shockwave(position, Shockwave.EXTRA_SMALL_WAVE));
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

        gameactivity.ball.remove(this); // remove this dead ball
	}
}