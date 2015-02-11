package jugglepong.electron;

import android.graphics.Point;
import android.util.Log;

final class Player implements Runnable
{
	GameActivity gameactivity;
	Point position = new Point();
	Point pposition = new Point(); // last position
	boolean goingRight = true; // is going right direction
	int speedY, speedX; // side speed
	
	Player(GameActivity gameactivity) 
	{
		this.gameactivity = gameactivity;
        start();
	}
	
	public void start()
	{
		Thread thread = new Thread(this);
		thread.setName("Player");
		thread.start();
	}

	public void run() 
	{
        ////////////// PLAYER MOVEMENT TRACKING ///////////////

		int tspeedX, tspeedY; // speed placeholder
		while (gameactivity.running)
		{
            if (position.x != pposition.x)
                goingRight = position.x > pposition.x;

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
}