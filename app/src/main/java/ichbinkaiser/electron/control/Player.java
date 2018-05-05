package ichbinkaiser.electron.control;

import android.graphics.Point;
import android.util.Log;

import ichbinkaiser.electron.activity.GameActivity;
import lombok.Getter;
import lombok.Setter;

public class Player implements Runnable {

    @Getter
    private boolean goingRight = true; // is going right direction

    @Getter
    private int speedY, speedX; // side speed

    @Getter
    @Setter
    private Point position = new Point();

    @Getter
    @Setter
    private Point previousPosition = new Point(); // last position

    private GameActivity gameactivity;

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
}