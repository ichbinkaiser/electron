package ichbinkaiser.electron;

import android.graphics.Point;
import android.util.Log;

final class Player implements Runnable
{
	GameActivity gameactivity;
	Point position = new Point();
	Point previousPosition = new Point(); // last position
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
			if (position.x != previousPosition.x)
			{
				goingRight = position.x > previousPosition.x;
			}

			previousPosition.set(position.x, position.y);

			try // get playerCount velocity
			{
				tspeedX = previousPosition.x;
				tspeedY = previousPosition.y;
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