package electron;

import android.graphics.Point;
import android.util.Log;

final class Player implements Runnable
{
	private GameActivity gameactivity;
	private Point position = new Point();
	private Point pposition = new Point(); // last position
	private boolean right = true; // is left direction
	private int speedY, speedX; // side speed
	
	Player(GameActivity gameactivity) 
	{
		this.gameactivity = gameactivity;
	}
	
	public void start()
	{
		Thread thread = new Thread(this);
		thread.setName("Player");
		thread.start();
	}

	public void run() 
	{
		int tspeedX, tspeedY; // speed placeholder
		while (gameactivity.isRunning())
		{
			if (position.x > pposition.x)
				right = true; // bottom player has moved right		
			else if (position.x < pposition.x)
				right = false; // bottom player has moved left
			
			pposition.set(position.x, position.y);

			try // get player velocity
			{
				tspeedX = pposition.x;
				tspeedY = pposition.y;
				Thread.sleep(10);
				tspeedX -= position.x;
				tspeedY -= position.y;
				speedX = Math.abs(tspeedX);
				speedY = Math.abs(tspeedY);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
				Log.e("Player", e.toString());
			}
		}
	}

	public Point getPosition()
	{
		return position;
	}

	public void setPosition(Point position)
	{
		this.position = position;
	}

	public boolean isRight()
	{
		return right;
	}

	public void setRight(boolean right)
	{
		this.right = right;
	}
	
	public int getSpeedX()
	{
		return speedX;
	}

	public int getSpeedY()
	{
		return speedY;
	}

	public void setPposition(Point pposition)
	{
		this.pposition = pposition;
	}
}