package jugglepong.electron;

import android.graphics.Point;

final class Trail 
{
	Point startpoint = new Point(); //start point
	Point endpoint = new Point(); //end point
	int life = 10; // animation index
	
	Trail(Point startpoint, Point endpoint)
	{
		this.startpoint.x = startpoint.x;
		this.startpoint.y = startpoint.y;
		this.endpoint.x = endpoint.x;
		this.endpoint.y = endpoint.y;
	}
	
	int calcSize() // calculate trailer size
	{
		switch (life)
		{
            case 9:
            case 8:
            case 7:
                return 1;
            case 6:
            case 5:
            case 4:
                return 2;
            case 3:
            case 2:
            case 1:
                return 3;
            default:
                return 0;
		}
	}
}
