package ichbinkaiser.electron.control;

import android.graphics.Point;
import android.util.Log;

import ichbinkaiser.electron.activity.GameActivity;

public class Player implements Runnable {
    GameActivity gameactivity;
    Point position = new Point();
    Point previousPosition = new Point(); // last position
    boolean goingRight = true; // is going right direction
    int speedY, speedX; // side speed

    public Player(GameActivity gameactivity) {
        this.gameactivity = gameactivity;
        start();
    }

    public void start() {
        Thread thread = new Thread(this);
        thread.setName("Player");
        thread.setDaemon(true);
        thread.start();
    }

    public void run() {
        ////////////// PLAYER MOVEMENT TRACKING ///////////////

        int tSpeedX, tSpeedY; // speed placeholder
        while (gameactivity.isRunning()) {
            if (position.x != previousPosition.x) {
                goingRight = position.x > previousPosition.x;
            }

            previousPosition.set(position.x, position.y);

            try // get playerCount velocity
            {
                tSpeedX = previousPosition.x;
                tSpeedY = previousPosition.y;
                Thread.sleep(10);
                tSpeedX -= position.x;
                tSpeedY -= position.y;
                speedX = Math.abs(tSpeedX);
                speedY = Math.abs(tSpeedY);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.e("Player", e.toString());
            }
        }
    }

    public GameActivity getGameactivity() {
        return gameactivity;
    }

    public void setGameactivity(GameActivity gameactivity) {
        this.gameactivity = gameactivity;
    }

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public Point getPreviousPosition() {
        return previousPosition;
    }

    public void setPreviousPosition(Point previousPosition) {
        this.previousPosition = previousPosition;
    }

    public boolean isGoingRight() {
        return goingRight;
    }

    public void setGoingRight(boolean goingRight) {
        this.goingRight = goingRight;
    }

    public int getSpeedY() {
        return speedY;
    }

    public void setSpeedY(int speedY) {
        this.speedY = speedY;
    }

    public int getSpeedX() {
        return speedX;
    }

    public void setSpeedX(int speedX) {
        this.speedX = speedX;
    }
}