package com.example.lupan.memo;

import com.example.lupan.memo.R;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.view.Menu;
import android.view.MenuItem;
import android.util.Log;


public class MainActivity extends Activity implements OnSharedPreferenceChangeListener {

	Board board;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		board = (Board) findViewById(R.id.board);

		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		sharedPref.registerOnSharedPreferenceChangeListener(this);

		board.setSize(Integer.parseInt(sharedPref.getString("size_list", "605")));

		Log.i("lupan", "onCreate");
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			Intent intent = new Intent(this,
						   SettingsActivity.class);
			startActivity(intent);
			return true;
		case R.id.action_new_game:
			board.newGame();
			board.invalidate();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals("size_list")) {
			board.setSize(Integer.parseInt(sharedPreferences.getString("size_list", "605")));
			board.invalidate();
		}
	}

}
