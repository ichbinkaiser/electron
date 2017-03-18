package ichbinkaiser.electron.entity;

import android.graphics.Point;

public class Trail {
    Point startPoint = new Point(); //start point
    Point endPoint = new Point(); //end point
    int life = 10; // animation index

    Trail(Point startPoint, Point endPoint) {
        this.startPoint.x = startPoint.x;
        this.startPoint.y = startPoint.y;
        this.endPoint.x = endPoint.x;
        this.endPoint.y = endPoint.y;
    }

    public int calcSize() // calculate trailer size
    {
        switch (life) {
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

    public void decrementLife() {
        life--;
    }

    public Point getStartPoint() {
        return startPoint;
    }

    public Point getEndPoint() {
        return endPoint;
    }

    public int getLife() {
        return life;
    }
}
