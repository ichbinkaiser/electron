package ichbinkaiser.electron.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import ichbinkaiser.electron.R;
import ichbinkaiser.electron.core.SoundManager;

public class SplashActivity extends Activity {
    Loader loader = new Loader();
    SoundManager soundManager = SoundManager.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splash);
        soundManager.initSounds(this);
        loader.start();
    }

    private void showMain() {
        Intent scoreIntent = new Intent(this, MainActivity.class);
        startActivity(scoreIntent);
        finish();
    }

    private class Loader implements Runnable {
        public void start() {
            Thread thread = new Thread(this);
            thread.setName("Loader");
            thread.setDaemon(true);
            thread.start();
        }

        public void run() {
            soundManager.loadSounds();
            while (!(soundManager.getSoundsLoaded() == soundManager.getSoundLibrary().length)) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            showMain(); // go to main menu
        }
    }
}
