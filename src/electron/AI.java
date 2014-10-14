package electron;

import java.util.ArrayList;
import android.graphics.Point;
import android.util.Log;

final class AI implements Runnable
{
	private GameActivity gameactivity;
	private ArrayList<Ball> ball = new ArrayList<Ball>();
	private Player user;
	private int id;
	
	private int guardboxleft, guardboxright, guardboxbottom, guardboxtop; // maximum area the AI guards
	private Point position = new Point(); // AI current location
	private Point target = new Point(); // top ball threat
	private boolean friend = false;
	
	AI(GameActivity gameactivity, ArrayList<Ball> ball, Player user, Player player, int id)
	{
		this.gameactivity = gameactivity;
		this.ball = ball;
		this.user = user;
		this.id = id;
		player.setPosition(position);
	}

	private void targetLogic(Ball ball)
	{
		if (gameactivity.isPortrait()) // if two player
		{
			if ((ball.isBump()) && (ball.getPosition().y < target.y))
				target.set(ball.getPosition().x - gameactivity.getPongWidth() / 2, ball.getPosition().y - gameactivity.getPongHeight() / 2); // check for priority target			
		}
		else
		{
			switch (id)
			{
			case 0:
				if (!gameactivity.isReversePosition())
				{
					if ((ball.isBump()) && (ball.getPosition().y < target.y) && (ball.getPosition().x < gameactivity.getMidpoint()))
						target.set(ball.getPosition().x - gameactivity.getPongWidth() / 2, ball.getPosition().y + gameactivity.getPongHeight() / 2); // check for priority target within second quadrant
				}
				else
				{
					if ((ball.isBump()) && (ball.getPosition().y < target.y) && (ball.getPosition().x > gameactivity.getMidpoint()))
						target.set(ball.getPosition().x - gameactivity.getPongWidth() / 2, ball.getPosition().y + gameactivity.getPongHeight() / 2); // check for priority target within third quadrant		
				}
				break;
			case 1:
				if (gameactivity.isReversePosition())
				{
					if ((ball.isBump()) && (ball.getPosition().y < target.y) && (ball.getPosition().x < gameactivity.getMidpoint()))
						target.set(ball.getPosition().x - gameactivity.getPongWidth() / 2, ball.getPosition().y + gameactivity.getPongHeight() / 2); // check for priority target within second quadrant
				}
				else
				{
					if ((ball.isBump()) && (ball.getPosition().y < target.y) && (ball.getPosition().x > gameactivity.getMidpoint()))
						target.set(ball.getPosition().x - gameactivity.getPongWidth() / 2, ball.getPosition().y + gameactivity.getPongHeight() / 2); // check for priority target within third quadrant	
				}
				break;
			case 2:
				if (user.getPosition().x > gameactivity.getMidpoint()) // check which quadrant player is located
				{
					if ((!ball.isBump()) && (ball.getPosition().y > target.y) && (ball.getPosition().x < gameactivity.getMidpoint()))
						target.set(ball.getPosition().x - gameactivity.getPongWidth() / 2, ball.getPosition().y - gameactivity.getPongHeight() / 2); // check for priority target within fourth quadrant	
				}
				else
				{
					if ((!ball.isBump()) && (ball.getPosition().y > target.y) && (ball.getPosition().x > gameactivity.getMidpoint()))
						target.set(ball.getPosition().x - gameactivity.getPongWidth() / 2, ball.getPosition().y - gameactivity.getPongHeight() / 2); // check for priority target within first quadrant		
				}
				break;
			}
		}
	}

	public void start()
	{
		Thread thread = new Thread(this);
		thread.setName("AI");
		thread.start();
	}

	public void run()
	{
		while(gameactivity.isRunning()) // AI Thread
		{
			if (gameactivity.isPortrait()) 
				target.set(gameactivity.getMidpoint(), gameactivity.getCanvasHeight()); // two player logic
			else
			{
				switch (id)
				{
				case 0: // if AI0
					if (!gameactivity.isReversePosition()) // is not reverse positions
					{
						target.set(gameactivity.getMidpoint() / 2, gameactivity.getCanvasHeight());
						guardboxleft = 0;
						guardboxright = gameactivity.getMidpoint();
					}
					else
					{
						target.set(gameactivity.getMidpoint() + gameactivity.getMidpoint() / 2, gameactivity.getCanvasHeight());
						guardboxleft = gameactivity.getMidpoint();
						guardboxright = gameactivity.getCanvasWidth();
					}
					break;
				case 1: // if AI1
					if (gameactivity.isReversePosition())
					{
						target.set(gameactivity.getMidpoint() / 2, gameactivity.getCanvasHeight());
						guardboxleft = 0;
						guardboxright = gameactivity.getMidpoint();
					}
					else
					{
						target.set(gameactivity.getMidpoint() + gameactivity.getMidpoint() / 2, gameactivity.getCanvasHeight());
						guardboxleft = gameactivity.getMidpoint();
						guardboxright = gameactivity.getCanvasWidth();
					}
					break;
				case 2: // if AI2
					if (user.getPosition().x > gameactivity.getMidpoint())
					{
						target.set(gameactivity.getMidpoint() - gameactivity.getMidpoint() / 2, 0);
						guardboxleft = 0;
						guardboxright = gameactivity.getMidpoint();
					}
					else
					{
						target.set(gameactivity.getMidpoint() + gameactivity.getMidpoint() / 2, 0);
						guardboxleft = gameactivity.getMidpoint();
						guardboxright = gameactivity.getCanvasWidth();
					}
					break;
				}
			}

			for (int ballcounter = 0; ballcounter < ball.size(); ballcounter++)
			{
				if (!ball.get(ballcounter).isSleeping())
					targetLogic(ball.get(ballcounter)); // send to AI targeting logic current ball location
			}

			if ((position.x < target.x) && (position.x <= guardboxright)) // AI move right
			{
				if (Math.abs(target.x - position.x) > 10) // turbo mode
					if (gameactivity.isPortrait())
						position.x += 5; 
					else
						position.x += 8;
				else
					position.x++;
			}

			else if ((position.x > target.x) && (position.x >= guardboxleft)) // AI move left
			{
				if (Math.abs(target.x - position.x) > 10)
					if (gameactivity.isPortrait())
						position.x -= 5;
					else
						position.x -= 8;
				else
					position.x--;
			}

			if ((position.y < target.y) && (target.y <= guardboxbottom)) // AI move down
			{
				if (Math.abs(target.y - position.y) > 10)
					position.y += 5;
				else
					position.y++;
			}

			else if ((position.y > target.y) && (position.y > guardboxtop)) // AI move up
			{
				if (Math.abs(target.y - position.y) > 10)
					if (gameactivity.isPortrait()) position.y -= 8; else position.y -= 5;
				else
					position.y--;
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

	public void setGuardBoxTop(int guardboxtop) 
	{
		this.guardboxtop = guardboxtop;
	}

	public void setGuardBoxBottom(int guardboxbottom) 
	{
		this.guardboxbottom = guardboxbottom;
	}

	public void setGuardBoxLeft(int guardboxleft) 
	{
		this.guardboxleft = guardboxleft;
	}

	public void setGuardBoxRight(int guardboxright) 
	{
		this.guardboxright = guardboxright;
	}

	public void setFriend(boolean isfriend) 
	{
		this.friend = isfriend;
	}
}