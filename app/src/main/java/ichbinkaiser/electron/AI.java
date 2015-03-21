package ichbinkaiser.electron;

import android.graphics.Point;
import android.util.Log;

import java.util.ArrayList;

final class AI implements Runnable
{
    GameActivity gameactivity;
    ArrayList<Ball> ball = new ArrayList<>();
    Player user;
    int guardBoxLeft = 0, guardBoxRight = 0, guardBoxBottom = 0, guardBoxTop = 0; // maximum area the AI guards
    Quadrant quadrant;
    Point position = new Point(); // AI current location
    Point target = new Point(); // top balls threat

    AI(GameActivity gameactivity, ArrayList<Ball> ball, Player user, Player player, Quadrant quadrant) // constructor for multiplayer
    {
        this.gameactivity = gameactivity;
        this.ball = ball;
        this.user = user;
        this.quadrant = quadrant;
        player.position = position;

        switch (quadrant)
        {
            case TOPLEFT:
                guardBoxBottom = gameactivity.canvasHeight / 3;
                guardBoxRight = gameactivity.midpoint;
                break;
            case TOPRIGHT:
                guardBoxBottom = gameactivity.canvasHeight / 3;
                guardBoxLeft = gameactivity.midpoint;
                guardBoxRight = gameactivity.canvasWidth;
                break;
            case BOTTOMRIGHT:
                guardBoxTop = gameactivity.canvasHeight - gameactivity.canvasHeight / 3;
                guardBoxBottom = gameactivity.canvasHeight;
                guardBoxLeft = gameactivity.midpoint;
                guardBoxRight = gameactivity.canvasWidth;
        }
        start();
    }

    AI(GameActivity gameactivity, ArrayList<Ball> ball, Player user, Player player) // constructor for single player
    {
        this.gameactivity = gameactivity;
        this.ball = ball;
        this.user = user;
        this.quadrant = Quadrant.TOPLEFT;
        guardBoxBottom = gameactivity.canvasHeight / 3; // set AI guardline
        guardBoxRight = gameactivity.canvasWidth;
        player.position = position;
        start();
    }

    void targetLogic(Ball ball)
    {
        if (gameactivity.portrait) // if two player
        {
            if (ball.bump && ball.position.y < target.y)
                target.set(ball.position.x - gameactivity.pongWidth / 2, ball.position.y - gameactivity.pongHeight / 2); // check for priority target
        }

        else
        {
            switch (quadrant)
            {
                case TOPLEFT:
                    if (gameactivity.reversePosition)
                    {
                        if (ball.bump && ball.position.y < target.y && ball.position.x > gameactivity.midpoint)
                            target.set(ball.position.x - gameactivity.pongWidth / 2, ball.position.y + gameactivity.pongHeight / 2); // check for priority target within third quadrant
                    }

                    else
                    {
                        if (ball.bump && ball.position.y < target.y && ball.position.x < gameactivity.midpoint)
                            target.set(ball.position.x - gameactivity.pongWidth / 2, ball.position.y + gameactivity.pongHeight / 2); // check for priority target within second quadrant
                    }
                    break;
                case TOPRIGHT:
                    if (gameactivity.reversePosition)
                    {
                        if (ball.bump && ball.position.y < target.y && ball.position.x < gameactivity.midpoint)
                            target.set(ball.position.x - gameactivity.pongWidth / 2, ball.position.y + gameactivity.pongHeight / 2); // check for priority target within second quadrant
                    }

                    else
                    {
                        if (ball.bump && ball.position.y < target.y && ball.position.x > gameactivity.midpoint)
                            target.set(ball.position.x - gameactivity.pongWidth / 2, ball.position.y + gameactivity.pongHeight / 2); // check for priority target within third quadrant
                    }
                    break;
                case BOTTOMRIGHT:
                    if (user.position.x > gameactivity.midpoint) // check which quadrant player is located
                    {
                        if (!ball.bump && ball.position.y > target.y && ball.position.x < gameactivity.midpoint)
                            target.set(ball.position.x - gameactivity.pongWidth / 2, ball.position.y - gameactivity.pongHeight / 2); // check for priority target within fourth quadrant
                    }

                    else
                    {
                        if (!ball.bump && ball.position.y > target.y && ball.position.x > gameactivity.midpoint)
                            target.set(ball.position.x - gameactivity.pongWidth / 2, ball.position.y - gameactivity.pongHeight / 2); // check for priority target within first quadrant
                    }
                    break;
            }
        }
    }

    ////////////////////////// AI HUNTING LOGIC ////////////////////////////////////////////

    public void start()
    {
        Thread thread = new Thread(this);
        thread.setName("AI");
        thread.start();
    }

    public void run()
    {
        while (gameactivity.running)
        {
            //////////////////////////////// AI ZONING ////////////////////////////

            if (gameactivity.portrait)
                target.set(gameactivity.midpoint, gameactivity.canvasHeight); // two player logic
            else
            {
                switch (quadrant)
                {
                    case TOPLEFT: // if AI0
                        if (!gameactivity.reversePosition) // is not reverse positions
                        {
                            target.set(gameactivity.midpoint / 2, gameactivity.canvasHeight);
                            guardBoxLeft = 0;
                            guardBoxRight = gameactivity.midpoint;
                        }

                        else
                        {
                            target.set(gameactivity.midpoint + gameactivity.midpoint / 2, gameactivity.canvasHeight);
                            guardBoxLeft = gameactivity.midpoint;
                            guardBoxRight = gameactivity.canvasWidth;
                        }
                        break;

                    case TOPRIGHT: // if AI1
                        if (gameactivity.reversePosition)
                        {
                            target.set(gameactivity.midpoint / 2, gameactivity.canvasHeight);
                            guardBoxLeft = 0;
                            guardBoxRight = gameactivity.midpoint;
                        }

                        else
                        {
                            target.set(gameactivity.midpoint + gameactivity.midpoint / 2, gameactivity.canvasHeight);
                            guardBoxLeft = gameactivity.midpoint;
                            guardBoxRight = gameactivity.canvasWidth;
                        }
                        break;

                    case BOTTOMRIGHT: // if AI2
                        if (user.position.x > gameactivity.midpoint)
                        {
                            target.set(gameactivity.midpoint - gameactivity.midpoint / 2, 0);
                            guardBoxLeft = 0;
                            guardBoxRight = gameactivity.midpoint;
                        }

                        else
                        {
                            target.set(gameactivity.midpoint + gameactivity.midpoint / 2, 0);
                            guardBoxLeft = gameactivity.midpoint;
                            guardBoxRight = gameactivity.canvasWidth;
                        }
                }
            }

            for (Ball currentball : ball)
            {
                if (currentball.alive)
                    targetLogic(currentball); // send to AI targeting logic current balls location
            }

            ///////////////////////////////// AI MOVEMENT /////////////////////////////////////////

            if (position.x < target.x && position.x <= guardBoxRight) // AI move goingRight
            {
                if (Math.abs(target.x - position.x) > 10) // turbo mode
                    if (gameactivity.portrait)
                        position.x += 5;
                    else
                        position.x += 8;
                else
                    position.x++;
            }

            else if (position.x > target.x && position.x >= guardBoxLeft) // AI move left
            {
                if (Math.abs(target.x - position.x) > 10)
                    if (gameactivity.portrait)
                        position.x -= 5;
                    else
                        position.x -= 8;
                else
                    position.x--;
            }

            if (position.y < target.y && target.y <= guardBoxBottom) // AI move down
            {
                if (Math.abs(target.y - position.y) > 10)
                    position.y += 5;
                else
                    position.y++;
            }

            else if (position.y > target.y && position.y > guardBoxTop) // AI move up
            {
                if (Math.abs(target.y - position.y) > 10)
                    if (gameactivity.portrait)
                        position.y -= 8;
                    else
                        position.y -= 5;
                else
                    position.y--;
            }

            try
            {
                Thread.sleep(10);
            }

            catch (InterruptedException e)
            {
                e.printStackTrace();
                Log.e("AI", e.toString());
            }
        }
    }

    enum Quadrant
    {
        TOPLEFT, TOPRIGHT, BOTTOMRIGHT
    }
}