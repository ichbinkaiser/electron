package electron;

import android.graphics.Point;

final class Shockwave 
{
	private Point position = new Point();
	private int life; // animation index counter
	private int type; // shockwave type
	
	Shockwave(Point position, int type)
	{
		switch (type)
		{
		case 0: // extra small wave
			this.life = 11;
			break;
		case 1: // small wave
            this.life = 21;
			break;
		case 2: // medium wave
            this.life = 128;
			break;
		case 3: // large wave
            this.life = 252;
			break;
		}
		this.type = type;
		this.position.x = position.x;
		this.position.y = position.y;
	}
	
	public int getLife()
	{
        switch (type)
        {
            case 0 :
            case 1 :
                return life -= 1;
            case 2 :
            case 3 :
                return life -= 4;
            default :
                return 0;
        }
	}

	public int getType() 
	{
		return type;
	}

	public Point getPosition() 
	{
		return position;
	}
}
