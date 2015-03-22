package ichbinkaiser.electron;

import android.graphics.Point;

final class ShockWave
{
	Point position = new Point();
	int life; // animation index counter
	Type type;

	ShockWave(Point position, Type type)
	{
		switch (type)
		{
			case EXTRA_SMALL_WAVE:
				this.life = 11;
				break;
			case SMALL_WAVE:
				this.life = 21;
				break;
			case MEDIUM_WAVE:
				this.life = 128;
				break;
			case LARGE_WAVE:
				this.life = 252;
		}

		this.type = type;
		this.position.x = position.x;
		this.position.y = position.y;
	}

	public int getLife()
	{
		if (type == Type.EXTRA_SMALL_WAVE || type == Type.SMALL_WAVE)
		{
			return life -= 1;
		}
		else
		{
			return life -= 4;
		}
	}

	enum Type
	{
		EXTRA_SMALL_WAVE, SMALL_WAVE, MEDIUM_WAVE, LARGE_WAVE
	}
}
