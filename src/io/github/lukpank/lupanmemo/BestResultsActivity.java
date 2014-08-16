package io.github.lukpank.lupanmemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import io.github.lukpank.lupanmemo.R;

import java.text.DateFormat;
import java.util.Date;



public class BestResultsActivity extends Activity {

	public static final int RESULT_NEW_GAME = Activity.RESULT_FIRST_USER;

	class Result {
		public int score;
		public long date;
		public String name;
		
		public Result(int score, long date, String name) {
			this.score = score;
			this.date = date;
			this.name = name;
		}

		public String getDate() {
			DateFormat df = DateFormat.getDateInstance();
			return df.format(new Date(date));

		}
	}

	static final int BEST_RESULTS_CNT = 10;

	int board_size;
	int your_score;
	int your_pos = -1;
	int ask = 1;
	SharedPreferences sharedPref;
	Result[] results = new Result[BEST_RESULTS_CNT + 1];
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_best_results);

		Bundle b = getIntent().getExtras();
		board_size = b.getInt("board_size");
		your_score = b.getInt("score");

		if (savedInstanceState != null) {
			ask = savedInstanceState.getInt("ask");
			if (ask == 0) {
				your_pos = savedInstanceState.getInt("your_pos");
			}
		}

		sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		setBestResults(sharedPref.getString(
			String.format("best_results_%d", board_size), ""));
		fillTable();
		if (your_pos >= 0 && your_pos < BEST_RESULTS_CNT && ask == 1) {
			askPlayerName();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt("ask", ask);
		if (ask == 0) {
			outState.putInt("your_pos", your_pos);
		}
	}

	private void setBestResults(String results_string)
	{
		String[] lines = results_string.split("\n");
		int i = 0, score;
		long date;
		String name;
		Result your_result = new Result(your_score,
						System.currentTimeMillis(),
						getString(R.string.your_result));
		for (String line : lines) {
			if (i >= BEST_RESULTS_CNT) {
				break;
			}
			String[] fields = line.split(":", 3);
			try {
				score = Integer.parseInt(fields[0]);
				date = Long.parseLong(fields[1]);
				name = fields[2];
			} catch (NumberFormatException e) {
				continue;
			}
			if (your_score > 0 && your_pos < 0 && your_score < score) {
				your_pos = i;
				results[i++] = your_result;
			}
			if (i >= BEST_RESULTS_CNT) {
				break;
			}
			results[i++] = new Result(score, date, name);
		}

		if (your_score > 0 && your_pos < 0) {
			your_pos = i;
			results[i] = your_result;
		}
	}

	/**
	 * fill GUI table with values from results array
	 */
	private void fillTable()
	{
		Resources res = getResources();
		String pkg = getApplicationContext().getPackageName();
 
		for (int i = 0; i < BEST_RESULTS_CNT + 1; i++) {
			if (results[i] == null) {
				break;
			}
			
			int score = res.getIdentifier(String.format("score%d", i + 1), "id", pkg);
			if (score != 0) {
				TextView text = (TextView) findViewById(score);
				text.setText(Integer.toString(results[i].score));
			}
			int date = res.getIdentifier(String.format("date%d", i + 1), "id", pkg);
			if (date != 0) {
				TextView text = (TextView) findViewById(date);
				text.setText(results[i].getDate());
			}
			int name = res.getIdentifier(String.format("name%d", i + 1), "id", pkg);
			if (name != 0) {
				TextView text = (TextView) findViewById(name);
				text.setText(results[i].name);
			}
		}

		if (your_pos >= 0) {
			int row = res.getIdentifier(String.format("tableRow%d", your_pos + 1), "id", pkg);
			if (row != 0) {
				findViewById(row).setBackgroundColor(Color.YELLOW);
			}

			int score = res.getIdentifier(String.format("score%d", your_pos + 1), "id", pkg);
			if (score != 0) {
				TextView text = (TextView) findViewById(score);
				text.setTextColor(Color.BLACK);
			}
			int date = res.getIdentifier(String.format("date%d", your_pos + 1), "id", pkg);
			if (date != 0) {
				TextView text = (TextView) findViewById(date);
				text.setTextColor(Color.BLACK);
			}
			int name = res.getIdentifier(String.format("name%d", your_pos + 1), "id", pkg);
			if (name != 0) {
				TextView text = (TextView) findViewById(name);
				text.setTextColor(Color.BLACK);
			}

		}

		Button button = (Button) findViewById(R.id.button1);
		if (your_score > 0) {
			button.setText(R.string.start_new_game);
		} else {
			button.setText(R.string.back_to_game);
		}
	}

	private void askPlayerName() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.dialog_player_name, null);
		final EditText text = (EditText) layout.findViewById(R.id.playerName);
		text.setText(sharedPref.getString("player_name", ""));
		builder.setView(layout);

		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					saveBestResults(text.getText().toString().replace('\n', ' '));
				}
			});
		builder.show();

	}

	private void saveBestResults(String player_name) {
		Resources res = getResources();
		String pkg = getApplicationContext().getPackageName();

		results[your_pos].name = player_name;
		int name = res.getIdentifier(String.format("name%d", your_pos + 1), "id", pkg);
		if (name != 0) {
			TextView text = (TextView) findViewById(name);
			text.setText(player_name);
		}
		
		SharedPreferences.Editor prefEditor = sharedPref.edit();
		prefEditor.putString("player_name", player_name);
		prefEditor.putString(String.format("best_results_%d", board_size),
				     serializeResults());
		prefEditor.commit();
		ask = 0;
	}

	private String serializeResults() {
		StringBuffer buf = new StringBuffer();

		for (int i = 0; i < BEST_RESULTS_CNT; i++) {
			if (results[i] == null) {
				break;
			}
			buf.append(String.format("%d:%d:%s\n", results[i].score, results[i].date,
						 results[i].name));
		}
		return buf.toString();
	}

	public void onButtonClick(View view) {
		setResult((your_score > 0) ?
			  RESULT_NEW_GAME : Activity.RESULT_OK);
		finish();
	}

	// @Override
	// public boolean onCreateOptionsMenu(Menu menu) {
	// 	// Inflate the menu; this adds items to the action bar if it is present.
	// 	getMenuInflater().inflate(R.menu.best_results, menu);
	// 	return true;
	// }

}
