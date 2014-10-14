package electron;

import java.util.ArrayList;
import java.util.Random;
import android.graphics.Point;
import android.util.Log;

final class Ball implements Runnable
{
	private GameActivity gameactivity;
	private ArrayList<Ball> ball = new ArrayList<Ball>();
	private Player[] player;
	
	private int hits = 0;
	private Point position = new Point();
	private Point pposition = new Point(); // previous position
	private Random rnd = new Random();
	private int speed = 5;
	private boolean bump; // is hit by player
	private boolean goingleft; // is going left
	private boolean collided; // has collided
	private int angle = rnd.nextInt(5); // angle of ball bounce
	private boolean supermode = false; // ball in super mode
	private boolean sleeping = false; // is thread sleeping
	private int speedbonusx = 0, speedbonusy = 0;
	private int spawnwave = 0;

	Ball(GameActivity gameactivity, ArrayList<Ball> ball, Player[] player) 
	{
		this.gameactivity = gameactivity;
		this.ball = ball;
		this.player = player;
	}

	private boolean checkCollision(Point object) // ball collision detection
	{
		return (((object.x <= getPosition().x + gameactivity.getBallSize() - 1) && (object.x >= getPosition().x - gameactivity.getBallSize() - 1) && ((object.y <= getPosition().y + gameactivity.getBallSize() - 1) && (object.y >= getPosition().y - gameactivity.getBallSize() - 1))));
	}

	public void setx() // random x start
	{
		position.x = rnd.nextInt(gameactivity.getCanvasWidth());
	}

	public void sety() // random y start
	{
		if (!isBump())
			position.y = rnd.nextInt(150) + 5;
		else
			position.y = rnd.nextInt(150) + gameactivity.getCanvasHeight() - 150;
	}

	public void randomizeStart() // randomize startup
	{
		int number = rnd.nextInt(3);
		goingleft =  (number > 1);

		number = rnd.nextInt(3);
		bump =  (number > 1);

		setx();
		sety();
	}

	public void start()
	{
		Thread thread = new Thread(this);
		thread.setName("Ball");
		thread.start();
	}

	public void run()
	{
		int new_angle;
		while(gameactivity.isRunning())
		{
			if (!sleeping)
			{
				for (int playercounter = 0; playercounter < player.length; playercounter++) // player collision logic
				{
					if ((playercounter == 0) || (playercounter == 3))
					{
						if ((position.y >= player[playercounter].getPosition().y) && (position.y <= player[playercounter].getPosition().y + gameactivity.getPongHeight()) && (position.x >= player[playercounter].getPosition().x) && (position.x <= player[playercounter].getPosition().x + gameactivity.getPongWidth()) && (!bump)) // bottom player to ball collision detection
						{
							new_angle = rnd.nextInt(speed);
							angle = new_angle;
							bump = true;
							hits++;
							gameactivity.getShockwave().add(new Shockwave(position, 0));

							goingleft =  (player[playercounter].isRight());

							if (playercounter == 0)
							{
								gameactivity.setGameScore(gameactivity.getGameScore() + (1 + speed / 11));
								gameactivity.doShake(40);

								if ((gameactivity.getGameScore() % 20 == 0) && (gameactivity.getBallCount() < 0))
								{
									ball.add(new Ball(gameactivity, ball, player));
                                    Ball latestball = ball.get(ball.size() - 1);
                                    latestball.randomizeStart();
                                    latestball.start();
                                    latestball.spawnwave = 5;
									GameActivity.getSoundManager().playSound(8, 1);
								}
							}
							GameActivity.getSoundManager().playSound(1, 1);

							gameactivity.setHitCounter(gameactivity.getHitCounter() + 1);

							if ((hits > 0) && (hits % 3 == 0)) speed += 3; // increase ball speed for ever three hits

							if (speedbonusx < player[playercounter].getSpeedX() / 10)
								speedbonusx = player[playercounter].getSpeedX() / 20; // get ball speed bonus from directional velocity of player
							
							if (speedbonusy < player[playercounter].getSpeedY() / 10)
								speedbonusy = player[playercounter].getSpeedY() / 20;

							if (gameactivity.isSoloGame())
								gameactivity.getPopup().add(new Popup(position, 2)); // popup text in score++ in solo mode
						}
					}
					else
					{
						if ((position.y >= player[playercounter].getPosition().y) && (position.y <= player[playercounter].getPosition().y + gameactivity.getPongHeight()) && (position.x >= player[playercounter].getPosition().x) && (position.x <= player[playercounter].getPosition().x + gameactivity.getPongWidth()) && (bump == true)) // top player to ball collision detection
						{
							new_angle = rnd.nextInt(speed);
							angle = new_angle;
							bump = false;
							hits++;

							gameactivity.getShockwave().add(new Shockwave(position,0));
							GameActivity.getSoundManager().playSound(1, 1);

							if ((hits > 0) && (hits % 3 == 0))
								speed += 3;
						}
					}
				}

				for (int ballcounter = 0; ballcounter < ball.size(); ballcounter++) // ball to ball collision detection
				{
                    Ball currentball = ball.get(ballcounter);
					if ((this != currentball) && (!collided)) // if ball is not compared to itself and has not yet collided
					{
						if (checkCollision(currentball.getPosition())) // ball collision detected
						{
							new_angle = rnd.nextInt(speed);
							angle = new_angle;

							GameActivity.getSoundManager().playSound(6, 1);
							if ((goingleft) && (!currentball.goingleft))
							{
								goingleft = false;
                                currentball.goingleft = true;
							}
							else 
							{
								goingleft = true;
                                currentball.goingleft = false;
							}
                            currentball.collided = true;
						}
					}
				}
				collided = false;
			}

			pposition.x = position.x;
			pposition.y = position.y;

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
				gameactivity.getShockwave().add(new Shockwave(position, 1));
				spawnwave--;
			}
			
			if ((position.y < 0) || (position.y > gameactivity.getCanvasHeight())) // ball has exceeded top or bottom bounds
			{
				if (!gameactivity.isSoloGame())
				{
					if (position.y < 0) // ball has reached top
					{
						gameactivity.setLife(gameactivity.getLife() + 1);
						gameactivity.getPopup().add(new Popup(position, 0));
					}
					else // ball has reached bottom
					{
						gameactivity.setLife(gameactivity.getLife() - 1);
						gameactivity.getPopup().add(new Popup(position, 1));
					}
					if (supermode)
						gameactivity.getShockwave().add(new Shockwave(position, 3));
					else
						gameactivity.getShockwave().add(new Shockwave(getPosition(), 2));
					
					supermode = false;
					GameActivity.getSoundManager().playSound(2, 1);
					gameactivity.doShake(100);

					try 
					{
						sleeping = true;
						Thread.sleep(1000);
						sleeping = false;
					} 
					catch (InterruptedException e) 
					{
						e.printStackTrace();
						Log.e("Ball", e.toString());
					}

					setx(); // reset ball
					sety();
					spawnwave = 5;
					speed = 5;
					hits = 0;
					angle = rnd.nextInt(5);
					speedbonusx = 0;
					speedbonusy = 0;
					GameActivity.getSoundManager().playSound(8, 1);
				}

				else
				{
					if (position.y < 0) // ball has reached top
					{
						angle=rnd.nextInt(speed);
						bump = false;
						GameActivity.getSoundManager().playSound(4, 1);
					}
					else
					{
						gameactivity.setLife(gameactivity.getLife() - 1);
						
						if (supermode)
							gameactivity.getShockwave().add(new Shockwave(position, 3));
						else
							gameactivity.getShockwave().add(new Shockwave(getPosition(), 2));
						
						supermode = false;
						GameActivity.getSoundManager().playSound(2, 1);
						gameactivity.getPopup().add(new Popup(position, 1));
						gameactivity.doShake(100);

						try 
						{
							setSleeping(true);
							Thread.sleep(1000);
							setSleeping(false);
						} 
						catch (InterruptedException e) 
						{
							e.printStackTrace();
							Log.e("Ball", e.toString());
						}

						setx(); // reset ball
						sety();
						spawnwave = 5;
						speed = 5;
						hits = 0;
						angle = rnd.nextInt(5);
						speedbonusx = 0;
						speedbonusy = 0;
						GameActivity.getSoundManager().playSound(8, 1);
					}
				}
			}

			if (position.x < 0) // ball has reached left wall
			{
				angle=rnd.nextInt(speed);
				goingleft = true;
				GameActivity.getSoundManager().playSound(4, 1);
			}

			if (position.x > gameactivity.getCanvasWidth()) // ball has reached right wall
			{
				angle=rnd.nextInt(speed);
				goingleft = false;
				GameActivity.getSoundManager().playSound(4, 1);
			}

			if ((speed == 11) && (!supermode)) // make super mode
			{
				GameActivity.getSoundManager().playSound(3, 1);
				gameactivity.getShockwave().add(new Shockwave(position, 2));
				supermode = true;
			}

			if (supermode) // draw super mode animation
			{
				gameactivity.getTrail().add(new Trail(pposition, position));
				gameactivity.getShockwave().add(new Shockwave(position, 0));
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
	}

	public boolean isBump() 
	{
		return bump;
	}

	public Point getPosition() 
	{
		return position;
	}

	public boolean isSleeping() 
	{
		return sleeping;
	}

	public void setSleeping(boolean sleep) 
	{
		this.sleeping = sleep;
	}
}