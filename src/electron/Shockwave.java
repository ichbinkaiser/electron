package electron;

import android.graphics.Point;

final class Shockwave 
{
    final static int EXTRA_SMALL_WAVE = 0, SMALL_WAVE = 1, MEDIUM_WAVE = 2, LARGE_WAVE = 3;
	Point position = new Point();
	int life; // animation index counter
	int type; // shockwave type
	
	Shockwave(Point position, int type)
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
        switch (type)
        {
            case 0:
            case 1:
                return life -= 1;
            case 2:
            case 3:
                return life -= 4;
            default:
                return 0;
        }
	}
}
