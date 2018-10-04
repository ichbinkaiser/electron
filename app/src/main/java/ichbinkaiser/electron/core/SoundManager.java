package ichbinkaiser.electron.core;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.util.Log;
import android.util.SparseIntArray;

import ichbinkaiser.electron.R;
import ichbinkaiser.electron.entity.Sound;
import lombok.Getter;

public class SoundManager {
    @Getter
    private final int[] soundLibrary = {
            R.raw.pop,
            R.raw.lifeup,
            R.raw.ding,
            R.raw.popwall,
            R.raw.down,
            R.raw.hit,
            R.raw.restart,
            R.raw.spawn
    };

    @Getter
    private int soundsLoaded = 0;

    private SoundPool soundpool;
    private SparseIntArray sounds;
    private AudioManager audioManager;
    private Context context;
    private static final SoundManager INSTANCE = new SoundManager();

    private SoundManager() {
        // This is to be instantiated internally
    }

    public void initSounds(Context context) {
        this.context = context;
        soundpool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        sounds = new SparseIntArray();
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    public void loadSounds() // load sounds to IntArray
    {
        soundsLoaded = 0;
        soundpool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
            soundsLoaded++;
            Log.i("SoundManager", "Sample" + Integer.toString(sampleId) + " loaded");
        });

        for (int soundIndex = 0; soundIndex < soundLibrary.length; soundIndex++) {
            sounds.put(soundIndex + 1, soundpool.load(context, soundLibrary[soundIndex], 1));
        }
    }

    public void playSound(Sound sound) {
        float streamVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        streamVolume = streamVolume / audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        soundpool.play(sounds.get(sound.ordinal() + 1),
                streamVolume,
                streamVolume,
                1,
                0,
                1f);
    }

    public SoundManager getInstance() {
        return INSTANCE;
    }

    public void doCleanup() {
        soundpool.release();
    }
}
