package ichbinkaiser.electron;

import android.graphics.Point;

import java.util.Random;

final class Popup
{
	Point position = new Point();
	int life = 255; // animation index life
	Type type; // popups message type
	int index = 0; // text message index

	Popup(Point position, Type type, int indexSize)
	{
		Random rnd = new Random();
		if (indexSize > 0)
		{
			;
		}
		index = rnd.nextInt(indexSize);

		this.type = type;
		this.position.x = position.x;

		if (type == Type.SCORE_UP)
		{
			this.position.y = position.y + 255;
		}
		else
		{
			this.position.y = position.y - 255;
		}
	}

	public int getLife()
	{
		return life--;
	}

	enum Type
	{
		SCORE_UP, LOSE_LIFE, SOLO
	}

}
