package electron;

import core.electron.R;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.util.Log;
import android.util.SparseIntArray;

final class SoundManager
{
	public SoundPool soundpool;
	private SparseIntArray sounds;
	private AudioManager  audiomanager;
	private Context context;
	public int soundsloaded = 0;
	private int[] soundlibrary = new int[] {R.raw.pop, R.raw.lifeup, R.raw.ding, R.raw.popwall, R.raw.down, R.raw.hit, R.raw.restart, R.raw.spawn};

	public int getSoundLibrarySize()
	{
		return soundlibrary.length;
	}

	public void initSounds(Context context)
	{
		this.context = context;
		soundpool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
		sounds = new SparseIntArray();
		audiomanager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
	}

	public void loadSounds() // load sounds to IntArray
	{
		soundsloaded = 0;
		soundpool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
			public void onLoadComplete(SoundPool soundPool, int sampleId,int status) {
				soundsloaded++;
				Log.i("SoundManager", "Sample" + Integer.toString(sampleId) + " loaded");
			}
		});
		
		for (int soundindex = 0; soundindex < soundlibrary.length; soundindex++)
		{
			sounds.put(soundindex + 1, soundpool.load(context, soundlibrary[soundindex], 1));
		}
	}

	public void playSound(int index,float speed)
	{
		if (soundsloaded==8)
		{
			float streamVolume = audiomanager.getStreamVolume(AudioManager.STREAM_MUSIC);
			streamVolume = streamVolume / audiomanager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			soundpool.play((Integer) sounds.get(index), streamVolume, streamVolume, 1, 0, speed);
		}
	}

	public void doCleanup()
	{
		soundpool.release();
	}
}
