package jugglepong.electron;

import android.graphics.Point;
import android.util.Log;

import java.util.ArrayList;

final class AI implements Runnable
{
    final static int TOPLEFT = 0, TOPRIGHT = 1, BOTTOMRIGHT = 2;
	GameActivity gameactivity;
	ArrayList<Ball> ball = new ArrayList<Ball>();
	Player user;
	
	int guardboxleft = 0, guardboxright = 0, guardboxbottom = 0, guardboxtop = 0; // maximum area the AI guards
    int quadrant;

	Point position = new Point(); // AI current location
	Point target = new Point(); // top ball threat
	
	AI(GameActivity gameactivity, ArrayList<Ball> ball, Player user, Player player, int quadrant) // constructor for multiplayer
	{
		this.gameactivity = gameactivity;
		this.ball = ball;
		this.user = user;
		this.quadrant = quadrant;
		player.position = position;

        switch (quadrant)
        {
            case TOPLEFT:
                guardboxbottom = gameactivity.canvasheight / 3;
                guardboxright = gameactivity.midpoint;
                break;
            case TOPRIGHT:
                guardboxbottom = gameactivity.canvasheight / 3;
                guardboxleft = gameactivity.midpoint;
                guardboxright = gameactivity.canvaswidth;
                break;
            case BOTTOMRIGHT:
                guardboxtop = gameactivity.canvasheight - gameactivity.canvasheight / 3;
                guardboxbottom = gameactivity.canvasheight;
                guardboxleft = gameactivity.midpoint;
                guardboxright = gameactivity.canvaswidth;
        }
        start();
	}

    AI(GameActivity gameactivity, ArrayList<Ball> ball, Player user, Player player) // constructor for single player
    {
        this.gameactivity = gameactivity;
        this.ball = ball;
        this.user = user;
        this.quadrant = 0;
        guardboxbottom = gameactivity.canvasheight / 3; // set AI guardline
        guardboxright = gameactivity.canvaswidth;
        player.position = position;
        start();
    }

    ////////////////////////// AI HUNTING LOGIC ////////////////////////////////////////////

	void targetLogic(Ball ball)
	{
		if (gameactivity.portrait) // if two player
		{
			if (ball.bump && ball.position.y < target.y)
				target.set(ball.position.x - gameactivity.pongwidth / 2, ball.position.y - gameactivity.pongheight / 2); // check for priority target
		}

		else
		{
			switch (quadrant)
			{
			case TOPLEFT:
				if (gameactivity.reverseposition)
                {
                    if (ball.bump && ball.position.y < target.y && ball.position.x > gameactivity.midpoint)
                        target.set(ball.position.x - gameactivity.pongwidth / 2, ball.position.y + gameactivity.pongheight / 2); // check for priority target within third quadrant
                }

                else
				{
					if (ball.bump && ball.position.y < target.y && ball.position.x < gameactivity.midpoint)
						target.set(ball.position.x - gameactivity.pongwidth / 2, ball.position.y + gameactivity.pongheight / 2); // check for priority target within second quadrant
				}
				break;
			case TOPRIGHT:
				if (gameactivity.reverseposition)
				{
					if (ball.bump && ball.position.y < target.y && ball.position.x < gameactivity.midpoint)
						target.set(ball.position.x - gameactivity.pongwidth / 2, ball.position.y + gameactivity.pongheight / 2); // check for priority target within second quadrant
				}

				else
				{
					if (ball.bump && ball.position.y < target.y && ball.position.x > gameactivity.midpoint)
						target.set(ball.position.x - gameactivity.pongwidth / 2, ball.position.y + gameactivity.pongheight / 2); // check for priority target within third quadrant
				}
				break;
			case BOTTOMRIGHT:
				if (user.position.x > gameactivity.midpoint) // check which quadrant player is located
				{
					if (!ball.bump && ball.position.y > target.y && ball.position.x < gameactivity.midpoint)
						target.set(ball.position.x - gameactivity.pongwidth / 2, ball.position.y - gameactivity.pongheight / 2); // check for priority target within fourth quadrant
				}

				else
				{
					if (!ball.bump && ball.position.y > target.y && ball.position.x > gameactivity.midpoint)
						target.set(ball.position.x - gameactivity.pongwidth / 2, ball.position.y - gameactivity.pongheight / 2); // check for priority target within first quadrant
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
		while (gameactivity.running)
		{
            //////////////////////////////// AI ZONING ////////////////////////////

			if (gameactivity.portrait)
				target.set(gameactivity.midpoint, gameactivity.canvasheight); // two player logic
			else
			{
				switch (quadrant)
				{
                    case AI.TOPLEFT: // if AI0
                        if (!gameactivity.reverseposition) // is not reverse positions
                        {
                            target.set(gameactivity.midpoint / 2, gameactivity.canvasheight);
                            guardboxleft = 0;
                            guardboxright = gameactivity.midpoint;
                        }

                        else
                        {
                            target.set(gameactivity.midpoint + gameactivity.midpoint / 2, gameactivity.canvasheight);
                            guardboxleft = gameactivity.midpoint;
                            guardboxright = gameactivity.canvaswidth;
                        }
                        break;

                    case AI.TOPRIGHT: // if AI1
                        if (gameactivity.reverseposition)
                        {
                            target.set(gameactivity.midpoint / 2, gameactivity.canvasheight);
                            guardboxleft = 0;
                            guardboxright = gameactivity.midpoint;
                        }

                        else
                        {
                            target.set(gameactivity.midpoint + gameactivity.midpoint / 2, gameactivity.canvasheight);
                            guardboxleft = gameactivity.midpoint;
                            guardboxright = gameactivity.canvaswidth;
                        }
                        break;

                    case AI.BOTTOMRIGHT: // if AI2
                        if (user.position.x > gameactivity.midpoint)
                        {
                            target.set(gameactivity.midpoint - gameactivity.midpoint / 2, 0);
                            guardboxleft = 0;
                            guardboxright = gameactivity.midpoint;
                        }

                        else
                        {
                            target.set(gameactivity.midpoint + gameactivity.midpoint / 2, 0);
                            guardboxleft = gameactivity.midpoint;
                            guardboxright = gameactivity.canvaswidth;
                        }
				}
			}

			for (int ballcounter = 0; ballcounter < ball.size(); ballcounter++)
			{
                if(ball.get(ballcounter).alive)
					targetLogic(ball.get(ballcounter)); // send to AI targeting logic current ball location
			}

            ///////////////////////////////// AI MOVEMENT /////////////////////////////////////////

			if (position.x < target.x && position.x <= guardboxright) // AI move goingRight
			{
				if (Math.abs(target.x - position.x) > 10) // turbo mode
					if (gameactivity.portrait)
						position.x += 5; 
					else
						position.x += 8;
				else
					position.x++;
			}

			else if (position.x > target.x && position.x >= guardboxleft) // AI move left
			{
				if (Math.abs(target.x - position.x) > 10)
					if (gameactivity.portrait)
						position.x -= 5;
					else
						position.x -= 8;
				else
					position.x--;
			}

			if (position.y < target.y && target.y <= guardboxbottom) // AI move down
			{
				if (Math.abs(target.y - position.y) > 10)
					position.y += 5;
				else
					position.y++;
			}

			else if (position.y > target.y && position.y > guardboxtop) // AI move up
			{
				if (Math.abs(target.y - position.y) > 10)
					if (gameactivity.portrait)
                        position.y -= 8;
                    else
                        position.y -= 5;
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
}