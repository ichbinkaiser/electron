package ichbinkaiser.electron;

import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;

import ichbinkaiser.electron.GameActivity.MyDraw;

final class GameSurfaceThread extends Thread
{
    GameActivity gameactivity;
    SurfaceHolder myholder;
    MyDraw mydraw;

    public GameSurfaceThread(GameActivity gameactivity, SurfaceHolder holder, MyDraw drawmain)
    {
        this.gameactivity = gameactivity;
        setName("SurfaceView");
        myholder = holder;
        mydraw = drawmain;
        start();
    }

    public void run()
    {
        Canvas canvas = null;
        while (gameactivity.running)
        {
            try
            {
                canvas = myholder.lockCanvas(null);
                mydraw.onDraw(canvas);
            }

            catch (NullPointerException e)
            {
                Log.e(this.gameactivity.getLocalClassName(), e.toString());
            }

            finally
            {
                if (canvas != null)
                    myholder.unlockCanvasAndPost(canvas);
            }
        }
    }
}