package electron;

import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;
import electron.GameActivity.MyDraw;

final class GameSurfaceThread extends Thread 
{
	private GameActivity gameactivity;
	private boolean flag; // game is running
	private SurfaceHolder myholder;
	private MyDraw mydraw;
	
	public GameSurfaceThread(GameActivity gameactivity, SurfaceHolder holder , MyDraw drawmain)
	{
		this.gameactivity = gameactivity;
		setName("SurfaceView");
		myholder = holder;
		mydraw = drawmain;
	}

	public void setFlag (boolean myFlag)
	{
		flag = myFlag;
	}
	public void run()
	{
		Canvas canvas = null;
		while(flag)
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
				if(canvas != null)
					myholder.unlockCanvasAndPost(canvas);
			}
		}
	}
}