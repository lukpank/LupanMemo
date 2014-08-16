package io.github.lukpank.lupanmemo;

import io.github.lukpank.lupanmemo.R;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.text.Html;
import android.text.method.LinkMovementMethod;


public class MainActivity extends Activity
	implements OnSharedPreferenceChangeListener, Board.EndOfGameListener {

	static final int BEST_RESULTS_REQUEST = 0;

	Board board;
	SharedPreferences sharedPref;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		board = (Board) findViewById(R.id.board);
		board.setEndOfGameListener(this);

		sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		sharedPref.registerOnSharedPreferenceChangeListener(this);

		if (! sharedPref.contains("board_state") ||
		    ! board.deserialize(
				sharedPref.getString("board_state", "")) ||
		    board.getActiveFields() == 0) {
			newGame();
		}
	}

	public void newGame()
	{
		int size;
		try {
			size = Integer.parseInt(
				sharedPref.getString("size_list", "605"));
		} catch (NumberFormatException e) {
			size = 605;
		}
		board.newGame(size);
		board.invalidate();

	}

	@Override
	protected void onPause ()
	{
		super.onPause();
		SharedPreferences.Editor prefEditor = sharedPref.edit();
		prefEditor.putString("board_state", board.serialize());
		prefEditor.commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private class MyLinkMovementMethod extends LinkMovementMethod {
		// a workaround to avoid crashing on my Nook device

		Activity activity;

		public MyLinkMovementMethod(Activity activity) {
			this.activity = activity;
		}

		@Override
		public boolean onTouchEvent(TextView widget, android.text.Spannable buffer, android.view.MotionEvent event)
		{
			try {
				return super.onTouchEvent(widget, buffer, event);
			} catch (android.content.ActivityNotFoundException e) {
				AlertDialog.Builder builder = new AlertDialog.Builder(activity);
				builder.setMessage(R.string.unsupported_link_type);
				builder.setPositiveButton(R.string.ok, null);
				builder.show();
				return false;
			}
		}
	}

	public void onEndOfGame(Board b) {
		bestResults(b.getMovesCnt());
	}

	public void bestResults(int score) {
		Intent intent = new Intent(this,
					   BestResultsActivity.class);
		Bundle b = new Bundle();
		b.putInt("board_size", board.getBoardSize());
		b.putInt("score", score);
		intent.putExtras(b);
		startActivityForResult(intent, BEST_RESULTS_REQUEST);
	}

	public void about() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.dialog_about, null);
		TextView text = (TextView) layout.findViewById(R.id.about_text);
		text.setText(Html.fromHtml(getString(R.string.message_about)));
		text.setMovementMethod(new MyLinkMovementMethod(this));
		builder.setView(layout);

		builder.setPositiveButton(R.string.ok, null);
		builder.show();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_new_game:
			newGame();
			return true;
		case R.id.action_settings:
			Intent intent = new Intent(this,
						   SettingsActivity.class);
			startActivity(intent);
			return true;
		case R.id.action_best_results:
			bestResults(0);
			return true;
		case R.id.action_about:
			about();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
					Intent data) {
		if (requestCode == BEST_RESULTS_REQUEST &&
		    resultCode == BestResultsActivity.RESULT_NEW_GAME) {
			newGame();
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals("size_list") && board.getMovesCnt() == 0) {
			newGame();
		}
	}

}
