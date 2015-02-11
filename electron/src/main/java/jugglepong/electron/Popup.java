package jugglepong.electron;

import android.graphics.Point;

import java.util.Random;

final class Popup 
{
    final static int SCOREUP = 0, LOSELIFE = 1, SOLO = 2;
	Point position = new Point();
    int life = 255; // animation index life
	int type; // popup message type
    int index = 0; // text message index
	
	Popup(Point position, int type, int indexsize)
	{
        Random rnd = new Random();
        if (indexsize > 0);
            index = rnd.nextInt(indexsize);

		this.type = type;
		this.position.x = position.x;

        if (type == SCOREUP)
            this.position.y = position.y + 255;
        else
            this.position.y = position.y - 255;
	}

	public int getLife()
	{
		return life--;
	}

}
