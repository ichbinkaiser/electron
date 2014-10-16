package electron;

import core.electron.R;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Window;
import android.view.WindowManager;

public class SplashActivity extends Activity 
{
	Loader loader = new Loader();

    @Override
    public void onCreate(Bundle savedinstancestate) 
    {
        super.onCreate(savedinstancestate);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splash);
        GameActivity.soundmanager.initSounds(this);
        loader.start();
    }
    
    public void showMain()
	{	
		Intent scoreIntent = new Intent(this, MainActivity.class);
		startActivity(scoreIntent);
		finish();
	}
    
    private class Loader implements Runnable
    {
    	public void start()
    	{
    		Thread thread = new Thread(this);
    		thread.setName("Loader");
    		thread.start();
    	}

		public void run() 
		{
            GameActivity.soundmanager.loadSounds();
			while(!(GameActivity.soundmanager.soundsloaded == GameActivity.soundmanager.soundlibrary.length))
			{
				try 
				{
					Thread.sleep(1000);
				}

				catch (InterruptedException e) 
				{
					e.printStackTrace();
				}
			}
            showMain(); // go to main menu
		}
    }
}
