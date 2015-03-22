package ichbinkaiser.electron;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;

import ichbinkaiser.electron.GameActivity.GameScreen;

final class GameSurfaceThread extends Thread
{
    GameActivity gameActivity;
    SurfaceHolder surfaceHolder;
    GameScreen gameScreen;

    public GameSurfaceThread(GameActivity gameActivity, SurfaceHolder holder, GameScreen gameScreen)
    {
        this.gameActivity = gameActivity;
        setName("SurfaceView");
        surfaceHolder = holder;
        this.gameScreen = gameScreen;
        start();
    }

    @SuppressLint("WrongCall")
    public void run()
    {
        Canvas canvas = null;
        while (gameActivity.running)
        {
            try
            {
                canvas = surfaceHolder.lockCanvas(null);
                gameScreen.onDraw(canvas);
            }

            catch (NullPointerException e)
            {
                Log.e(this.gameActivity.getLocalClassName(), e.toString());
            }

            finally
            {
                if (canvas != null)
                {
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }
}