package ichbinkaiser.electron;

import android.graphics.Point;
import android.util.Log;

import java.util.List;

final class AI implements Runnable
{
	GameActivity gameActivity;
	List<Ball> balls;
	Player user;
	int guardBoxLeft = 0, guardBoxRight = 0, guardBoxBottom = 0, guardBoxTop = 0; // maximum area the AI guards
	Quadrant guardQuadrant;
	Point position = new Point(); // AI current location
	Point target = new Point(); // top balls threat

	AI(GameActivity gameActivity, List<Ball> balls, Player user, Player player, Quadrant guardQuadrant) // constructor for multiplayer
	{
		this.gameActivity = gameActivity;
		this.balls = balls;
		this.user = user;
		this.guardQuadrant = guardQuadrant;
		player.position = position;

		switch (guardQuadrant)
		{
			case TOP_LEFT:
				guardBoxBottom = gameActivity.canvasHeight / 3;
				guardBoxRight = gameActivity.midpoint;
				break;
			case TOP_RIGHT:
				guardBoxBottom = gameActivity.canvasHeight / 3;
				guardBoxLeft = gameActivity.midpoint;
				guardBoxRight = gameActivity.canvasWidth;
				break;
			case BOTTOM_RIGHT:
				guardBoxTop = gameActivity.canvasHeight - gameActivity.canvasHeight / 3;
				guardBoxBottom = gameActivity.canvasHeight;
				guardBoxLeft = gameActivity.midpoint;
				guardBoxRight = gameActivity.canvasWidth;
		}
		start();
	}

	AI(GameActivity gameActivity, List<Ball> balls, Player user, Player player) // constructor for single playerCount
	{
		this.gameActivity = gameActivity;
		this.balls = balls;
		this.user = user;
		this.guardQuadrant = Quadrant.TOP_LEFT;
		guardBoxBottom = gameActivity.canvasHeight / 3; // set AI guard line
		guardBoxRight = gameActivity.canvasWidth;
		player.position = position;
		start();
	}

	void targetLogic(Ball ball)
	{
		if (gameActivity.portrait) // if two playerCount
		{
			if (ball.bump && ball.position.y < target.y)
			{
				target.set(ball.position.x - gameActivity.pongWidth / 2, ball.position.y - gameActivity.pongHeight / 2); // check for priority target
			}
		}

		else
		{
			switch (guardQuadrant)
			{
				case TOP_LEFT:
					if (gameActivity.reversePosition)
					{
						if (ball.bump && ball.position.y < target.y && ball.position.x > gameActivity.midpoint)
						{
							target.set(ball.position.x - gameActivity.pongWidth / 2, ball.position.y - gameActivity.pongHeight / 2); // check for priority target within third guardQuadrant
						}
					}

					else
					{
						if (ball.bump && ball.position.y < target.y && ball.position.x < gameActivity.midpoint)
						{
							target.set(ball.position.x - gameActivity.pongWidth / 2, ball.position.y - gameActivity.pongHeight / 2); // check for priority target within second guardQuadrant
						}
					}
					break;
				case TOP_RIGHT:
					if (gameActivity.reversePosition)
					{
						if (ball.bump && ball.position.y < target.y && ball.position.x < gameActivity.midpoint)
						{
							target.set(ball.position.x - gameActivity.pongWidth / 2, ball.position.y - gameActivity.pongHeight / 2); // check for priority target within second guardQuadrant
						}
					}

					else
					{
						if (ball.bump && ball.position.y < target.y && ball.position.x > gameActivity.midpoint)
						{
							target.set(ball.position.x - gameActivity.pongWidth / 2, ball.position.y - gameActivity.pongHeight / 2); // check for priority target within third guardQuadrant
						}
					}
					break;
				case BOTTOM_RIGHT:
					if (user.position.x > gameActivity.midpoint) // check which guardQuadrant playerCount is located
					{
						if (!ball.bump && ball.position.y > target.y && ball.position.x < gameActivity.midpoint)
						{
							target.set(ball.position.x - gameActivity.pongWidth / 2, ball.position.y - gameActivity.pongHeight / 2); // check for priority target within fourth guardQuadrant
						}
					}

					else
					{
						if (!ball.bump && ball.position.y > target.y && ball.position.x > gameActivity.midpoint)
						{
							target.set(ball.position.x - gameActivity.pongWidth / 2, ball.position.y - gameActivity.pongHeight / 2); // check for priority target within first guardQuadrant
						}
					}
			}
		}
	}

	////////////////////////// AI HUNTING LOGIC ////////////////////////////////////////////

	public void start()
	{
		Thread thread = new Thread(this);
		thread.setName("AI");
		thread.start();
	}

	public void run()
	{
		while (gameActivity.running)
		{
			//////////////////////////////// AI ZONING ////////////////////////////

			if (gameActivity.portrait)
			{
				target.set(gameActivity.midpoint, gameActivity.canvasHeight); // two playerCount logic
			}
			else
			{
				switch (guardQuadrant)
				{
					case TOP_LEFT: // if AI0
						if (!gameActivity.reversePosition) // is not reverse positions
						{
							target.set(gameActivity.midpoint / 2, gameActivity.canvasHeight);
							guardBoxLeft = 0;
							guardBoxRight = gameActivity.midpoint;
						}

						else
						{
							target.set(gameActivity.midpoint + gameActivity.midpoint / 2, gameActivity.canvasHeight);
							guardBoxLeft = gameActivity.midpoint;
							guardBoxRight = gameActivity.canvasWidth;
						}
						break;

					case TOP_RIGHT: // if AI1
						if (gameActivity.reversePosition)
						{
							target.set(gameActivity.midpoint / 2, gameActivity.canvasHeight);
							guardBoxLeft = 0;
							guardBoxRight = gameActivity.midpoint;
						}

						else
						{
							target.set(gameActivity.midpoint + gameActivity.midpoint / 2, gameActivity.canvasHeight);
							guardBoxLeft = gameActivity.midpoint;
							guardBoxRight = gameActivity.canvasWidth;
						}
						break;

					case BOTTOM_RIGHT: // if AI2
						if (user.position.x > gameActivity.midpoint)
						{
							target.set(gameActivity.midpoint - gameActivity.midpoint / 2, 0);
							guardBoxLeft = 0;
							guardBoxRight = gameActivity.midpoint;
						}

						else
						{
							target.set(gameActivity.midpoint + gameActivity.midpoint / 2, 0);
							guardBoxLeft = gameActivity.midpoint;
							guardBoxRight = gameActivity.canvasWidth;
						}
				}
			}

			for (Ball ball : balls)
			{
				if (ball.alive)
				{
					targetLogic(ball); // send to AI targeting logic current balls location
				}
			}

			///////////////////////////////// AI MOVEMENT /////////////////////////////////////////

			if (position.x < target.x && position.x <= guardBoxRight) // AI move goingRight
			{
				if (Math.abs(target.x - position.x) > 8) // turbo mode
				{
					if (gameActivity.portrait)
					{
						position.x += 5;
					}
					else
					{
						position.x += 8;
					}
				}
				else
				{
					position.x++;
				}
			}

			else if (position.x > target.x && position.x >= guardBoxLeft) // AI move left
			{
				if (gameActivity.portrait && Math.abs(target.x - position.x) > 4)
				{
					position.x -= 5;
				}
				else if (Math.abs(target.x - position.x) > 7)
				{
					position.x -= 8;
				}
				else
				{
					position.x--;
				}
			}

			if (position.y < target.y && target.y <= guardBoxBottom) // AI move down
			{
				if (Math.abs(target.y - position.y) > 4)
				{
					position.y += 5;
				}
				else
				{
					position.y++;
				}
			}

			else if (position.y > target.y && position.y > guardBoxTop) // AI move up
			{
				if (gameActivity.portrait && Math.abs(target.y - position.y) > 7)
				{
					position.y -= 8;
				}
				else if (Math.abs(target.y - position.y) > 4)
				{
					position.y -= 5;
				}
				else
				{
					position.y--;
				}
			}

			try
			{
				Thread.sleep(10);
			}

			catch (InterruptedException e)
			{
				e.printStackTrace();
				Log.e("AI", e.toString());
			}
		}
	}

	enum Quadrant
	{
		TOP_LEFT, TOP_RIGHT, BOTTOM_RIGHT
	}
}