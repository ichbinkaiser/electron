package ichbinkaiser.electron.control;

import android.graphics.Point;
import android.util.Log;

import java.util.List;

import ichbinkaiser.electron.activity.GameActivity;
import ichbinkaiser.electron.entity.Ball;
import ichbinkaiser.electron.entity.Quadrant;
import lombok.Getter;

public class AI implements Runnable {

    @Getter
    private int guardBoxLeft = 0,
            guardBoxRight = 0,
            guardBoxBottom = 0,
            guardBoxTop = 0; // maximum area the AI guards

    @Getter
    private Quadrant guardQuadrant;

    @Getter
    private Point position = new Point(); // AI current location

    @Getter
    private Point target = new Point(); // top balls threat

    private List<Ball> balls;
    private Player user;
    private GameActivity gameActivity;

    public AI(GameActivity gameActivity,
              List<Ball> balls,
              Player user,
              Player player,
              Quadrant guardQuadrant) // constructor for multiplayer
    {
        this.gameActivity = gameActivity;
        this.balls = balls;
        this.user = user;
        this.guardQuadrant = guardQuadrant;
        player.setPosition(position);

        switch (guardQuadrant) {
            case TOP_LEFT:
                guardBoxBottom = gameActivity.getCanvasHeight() / 3;
                guardBoxRight = gameActivity.getMidpoint();
                break;
            case TOP_RIGHT:
                guardBoxBottom = gameActivity.getCanvasHeight() / 3;
                guardBoxLeft = gameActivity.getMidpoint();
                guardBoxRight = gameActivity.getCanvasWidth();
                break;
            case BOTTOM_RIGHT:
                guardBoxTop = gameActivity.getCanvasHeight() - gameActivity.getCanvasHeight() / 3;
                guardBoxBottom = gameActivity.getCanvasHeight();
                guardBoxLeft = gameActivity.getMidpoint();
                guardBoxRight = gameActivity.getCanvasWidth();
        }
        start();
    }

    public AI(GameActivity gameActivity,
              List<Ball> balls,
              Player user,
              Player player) // constructor for single playerCount
    {
        this.gameActivity = gameActivity;
        this.balls = balls;
        this.user = user;
        this.guardQuadrant = Quadrant.TOP_LEFT;
        guardBoxBottom = gameActivity.getCanvasHeight() / 3; // set AI guard line
        guardBoxRight = gameActivity.getCanvasWidth();
        player.setPosition(position);
        start();
    }

    void targetLogic(Ball ball) {
        if (gameActivity.isPortrait()) // if two playerCount
        {
            if (ball.isBump() && ball.getPosition().y < target.y) {
                target.set(ball.getPosition().x - gameActivity.getPongWidth() / 2,
                        ball.getPosition().y - gameActivity.getPongHeight() / 2); // check for priority target
            }
        } else {
            switch (guardQuadrant) {
                case TOP_LEFT:
                    if (gameActivity.isReversePosition()) {
                        if (ball.isBump() && ball.getPosition().y < target.y
                                && ball.getPosition().x > gameActivity.getMidpoint()) {
                            target.set(ball.getPosition().x - gameActivity.getPongWidth() / 2,
                                    ball.getPosition().y - gameActivity.getPongHeight() / 2); // check for priority target within third guardQuadrant
                        }
                    } else {
                        if (ball.isBump() && ball.getPosition().y < target.y
                                && ball.getPosition().x < gameActivity.getMidpoint()) {
                            target.set(ball.getPosition().x - gameActivity.getPongWidth() / 2,
                                    ball.getPosition().y - gameActivity.getPongHeight() / 2); // check for priority target within second guardQuadrant
                        }
                    }
                    break;
                case TOP_RIGHT:
                    if (gameActivity.isReversePosition()) {
                        if (ball.isBump() && ball.getPosition().y < target.y
                                && ball.getPosition().x < gameActivity.getMidpoint()) {
                            target.set(ball.getPosition().x - gameActivity.getPongWidth() / 2,
                                    ball.getPosition().y - gameActivity.getPongHeight() / 2); // check for priority target within second guardQuadrant
                        }
                    } else {
                        if (ball.isBump() && ball.getPosition().y < target.y
                                && ball.getPosition().x > gameActivity.getMidpoint()) {
                            target.set(ball.getPosition().x - gameActivity.getPongWidth() / 2,
                                    ball.getPosition().y - gameActivity.getPongHeight() / 2); // check for priority target within third guardQuadrant
                        }
                    }
                    break;
                case BOTTOM_RIGHT:
                    if (user.getPosition().x > gameActivity.getMidpoint()) // check which guardQuadrant playerCount is located
                    {
                        if (!ball.isBump() && ball.getPosition().y > target.y
                                && ball.getPosition().x < gameActivity.getMidpoint()) {
                            target.set(ball.getPosition().x - gameActivity.getPongWidth() / 2,
                                    ball.getPosition().y - gameActivity.getPongHeight() / 2); // check for priority target within fourth guardQuadrant
                        }
                    } else {
                        if (!ball.isBump() && ball.getPosition().y > target.y
                                && ball.getPosition().x > gameActivity.getMidpoint()) {
                            target.set(ball.getPosition().x - gameActivity.getPongWidth() / 2,
                                    ball.getPosition().y - gameActivity.getPongHeight() / 2); // check for priority target within first guardQuadrant
                        }
                    }
            }
        }
    }

    ////////////////////////// AI HUNTING LOGIC ////////////////////////////////////////////

    public void start() {
        Thread thread = new Thread(this);
        thread.setName("AI");
        thread.setDaemon(true);
        thread.start();
    }

    public void run() {
        while (gameActivity.isRunning()) {
            //////////////////////////////// AI ZONING ////////////////////////////

            if (gameActivity.isPortrait()) {
                target.set(gameActivity.getMidpoint(),
                        gameActivity.getCanvasHeight()); // two playerCount logic
            } else {
                switch (guardQuadrant) {
                    case TOP_LEFT: // if AI0
                        if (!gameActivity.isReversePosition()) { // is not reverse positions
                            target.set(gameActivity.getMidpoint() / 2,
                                    gameActivity.getCanvasHeight());
                            guardBoxLeft = 0;
                            guardBoxRight = gameActivity.getMidpoint();
                        } else {
                            target.set(gameActivity.getMidpoint() + gameActivity.getMidpoint() / 2,
                                    gameActivity.getCanvasHeight());
                            guardBoxLeft = gameActivity.getMidpoint();
                            guardBoxRight = gameActivity.getCanvasWidth();
                        }
                        break;

                    case TOP_RIGHT: // if AI1
                        if (gameActivity.isReversePosition()) {
                            target.set(gameActivity.getMidpoint() / 2,
                                    gameActivity.getCanvasHeight());
                            guardBoxLeft = 0;
                            guardBoxRight = gameActivity.getMidpoint();
                        } else {
                            target.set(gameActivity.getMidpoint() + gameActivity.getMidpoint() / 2,
                                    gameActivity.getCanvasHeight());
                            guardBoxLeft = gameActivity.getMidpoint();
                            guardBoxRight = gameActivity.getCanvasWidth();
                        }
                        break;

                    case BOTTOM_RIGHT: // if AI2
                        if (user.getPosition().x > gameActivity.getMidpoint()) {
                            target.set(gameActivity.getMidpoint() - gameActivity.getMidpoint() / 2,0);
                            guardBoxLeft = 0;
                            guardBoxRight = gameActivity.getMidpoint();
                        } else {
                            target.set(gameActivity.getMidpoint() + gameActivity.getMidpoint() / 2, 0);
                            guardBoxLeft = gameActivity.getMidpoint();
                            guardBoxRight = gameActivity.getCanvasWidth();
                        }
                }
            }

            for (Ball ball : balls) {
                if (ball.isAlive()) {
                    targetLogic(ball); // send to AI targeting logic current balls location
                }
            }

            ///////////////////////////////// AI MOVEMENT /////////////////////////////////////////

            if (position.x < target.x && position.x <= guardBoxRight) { // AI move goingRight
                if (Math.abs(target.x - position.x) > 8) { // turbo mode
                    if (gameActivity.isPortrait()) {
                        position.x += 5;
                    } else {
                        position.x += 8;
                    }
                } else {
                    position.x++;
                }
            } else if (position.x > target.x && position.x >= guardBoxLeft) { // AI move left
                if (gameActivity.isPortrait() && Math.abs(target.x - position.x) > 4) {
                    position.x -= 5;
                } else if (Math.abs(target.x - position.x) > 7) {
                    position.x -= 8;
                } else {
                    position.x--;
                }
            }

            if (position.y < target.y && target.y <= guardBoxBottom) { // AI move down
                if (Math.abs(target.y - position.y) > 4) {
                    position.y += 5;
                } else {
                    position.y++;
                }
            } else if (position.y > target.y && position.y > guardBoxTop) { // AI move up
                if (gameActivity.isPortrait() && Math.abs(target.y - position.y) > 7) {
                    position.y -= 8;
                } else if (Math.abs(target.y - position.y) > 4) {
                    position.y -= 5;
                } else {
                    position.y--;
                }
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.e("AI", e.toString());
            }
        }
    }
}