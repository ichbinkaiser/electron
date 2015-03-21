package ichbinkaiser.electron;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.util.Log;
import android.util.SparseIntArray;

final class SoundManager
{
    SoundPool soundpool;
    int soundsLoaded = 0;
    SparseIntArray sounds;
    AudioManager audioManager;
    Context context;
    int[] soundLibrary = new int[]{R.raw.pop, R.raw.lifeup, R.raw.ding, R.raw.popwall, R.raw.down, R.raw.hit, R.raw.restart, R.raw.spawn};

    public void initSounds(Context context)
    {
        this.context = context;
        soundpool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        sounds = new SparseIntArray();
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    public void loadSounds() // load sounds to IntArray
    {
        soundsLoaded = 0;
        soundpool.setOnLoadCompleteListener(new OnLoadCompleteListener()
        {
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status)
            {
                soundsLoaded++;
                Log.i("SoundManager", "Sample" + Integer.toString(sampleId) + " loaded");
            }
        });

        for (int soundindex = 0; soundindex < soundLibrary.length; soundindex++)
        {
            sounds.put(soundindex + 1, soundpool.load(context, soundLibrary[soundindex], 1));
        }
    }

    public void playSound(Sound sound, float speed)
    {
        float streamVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        streamVolume = streamVolume / audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        soundpool.play(sounds.get(sound.ordinal() + 1), streamVolume, streamVolume, 1, 0, speed);
    }

    public void doCleanup()
    {
        soundpool.release();
    }

    public static enum Sound
    {
        POP, LIFE_UP, DING, POPWALL, DOWN, HIT, RESTART, SPAWN;
    }
}
