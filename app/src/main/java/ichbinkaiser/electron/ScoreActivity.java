package ichbinkaiser.electron;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class ScoreActivity extends Activity
{

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_score);
		Intent score = getIntent(); // retrieve score from game activity
		TextView text;
		text = (TextView) findViewById(R.id.textView1);
		text.setText("Your score is " + score.getStringExtra(GameActivity.score));
	}
}
