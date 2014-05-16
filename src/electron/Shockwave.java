package electron;

import android.graphics.Point;

final class Shockwave 
{
	private Point position = new Point();
	private int counter; // animation index counter
	private int type; // shockwave type
	
	Shockwave(Point position, int type)
	{
		switch (type)
		{
		case 0: // extra small wave
			setCounter(11);
			break;
		case 1: // small wave
			setCounter(21);
			break;
		case 2: // medium wave
			setCounter(128);
			break;
		case 3: // large wave
			setCounter(252);
			break;
		}
		this.type = type;
		this.position.x = position.x;
		this.position.y = position.y;
	}
	
	public int getCounter() 
	{
		return counter;
	}

	public void setCounter(int counter) 
	{
		this.counter = counter;
	}

	public int getType() 
	{
		return type;
	}

	public void setType(int type) 
	{
		this.type = type;
	}

	public Point getPosition() 
	{
		return position;
	}

	public void setPosition(Point position) 
	{
		this.position = position;
	}
}
