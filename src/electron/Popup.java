package electron;

import java.util.Random;
import android.graphics.Point;

final class Popup 
{
	private Point position = new Point();
	private int counter = 255; // animation index counter
	private int type; // popup message type
	private Random rnd = new Random();
	private int textindex = rnd.nextInt(10); // random text index
	
	Popup(Point position, int type)
	{
		this.setType(type);
		this.getPosition().x = position.x;
		
		switch(type)
		{
		case 0:
			this.getPosition().y = position.y + 255;
			break;
		case 1:
			this.getPosition().y = position.y - 255;
			break;
		case 2:
			this.getPosition().y = position.y - 255;
			break;
		}
	}

	public int getTextIndex() 
	{
		return textindex;
	}

	public int getlife()
	{
		return counter--;
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
}
