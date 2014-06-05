package electron;

import core.electron.R;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class ScoreActivity extends Activity 
{

	@Override
	public void onCreate(Bundle savedinstancestate) 
	{
		super.onCreate(savedinstancestate);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_score);
		Intent score = getIntent(); // retrieve score from game activity
		TextView text;
		text = (TextView) findViewById(R.id.textView1);
		text.setText("Your score is " + score.getStringExtra(GameActivity.getScore()));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		getMenuInflater().inflate(R.menu.activity_score, menu);
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		switch (item.getItemId()) 
		{
		case android.R.id.home:
		//	NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
