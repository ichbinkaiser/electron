package electron;

import android.graphics.Point;

final class Trail 
{
	private Point startpoint = new Point(); //start point
	private Point endpoint = new Point(); //end point
	private int counter = 10; // animation index
	
	Trail(Point startpoint, Point endpoint)
	{
		this.startpoint.x = startpoint.x;
		this.startpoint.y = startpoint.y;
		this.endpoint.x = endpoint.x;
		this.endpoint.y = endpoint.y;
	}
	
	int calcSize()
	{
		switch (getCounter())
		{
		case 9:
			return 1;
		case 8:
			return 1;
		case 7:
			return 1;
		case 6:
			return 2;
		case 5:
			return 2;
		case 4:
			return 2;
		case 3:
			return 3;
		case 2:
			return 3;
		case 1:
			return 3;
		default:
			return 0;
		}
	}
	
	public int getCounter() 
	{
		return counter;
	}

	public void setCounter(int counter) 
	{
		this.counter = counter;
	}

	public Point getStartPoint() 
	{
		return startpoint;
	}

	public void setStartPoint(Point startpoint) 
	{
		this.startpoint = startpoint;
	}

	public Point getEndPoint() 
	{
		return endpoint;
	}

	public void setEndPoint(Point endpoint) 
	{
		this.endpoint = endpoint;
	}
}
