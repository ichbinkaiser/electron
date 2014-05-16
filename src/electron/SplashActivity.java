package electron;

import core.electron.R;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;

public class SplashActivity extends Activity 
{
	private LoadCheck loadcheck = new LoadCheck();
    @Override
    public void onCreate(Bundle savedinstancestate) 
    {
        super.onCreate(savedinstancestate);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splash);
        GameActivity.getSoundManager().initSounds(this);
        GameActivity.getSoundManager().loadSounds();
        loadcheck.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        getMenuInflater().inflate(R.menu.activity_splash, menu);
        return true;
    }
    
    public void showMain()
	{	
		Intent scoreIntent = new Intent(this, MainActivity.class);
		startActivity(scoreIntent);
		finish();
	}
    
    private class LoadCheck implements Runnable
    {
    	public void start()
    	{
    		Thread thread = new Thread(this);
    		thread.setName("LoadCheck");
    		thread.start();
    	}

		public void run() 
		{
			boolean done = false;
			while(!done)
			{
				if (GameActivity.getSoundManager().soundsloaded == GameActivity.getSoundManager().getSoundLibrarySize())
				{
					done = true;
					showMain();
				}
				
				try 
				{
					Thread.sleep(1000);
				} 
				catch (InterruptedException e) 
				{
					e.printStackTrace();
				}
			}
		}
    }
}
