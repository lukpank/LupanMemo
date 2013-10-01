package com.example.lupan.memo;

import android.os.Bundle;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.preference.PreferenceManager;
import java.text.DateFormat;
import java.util.Date;
import android.view.Menu;
import android.widget.TextView;

public class BestResultsActivity extends Activity {

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

	int board_size;
	int your_score;
	int your_pos = -1;
	SharedPreferences sharedPref;
	Result[] results = new Result[11];
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_best_results);

		Bundle b = getIntent().getExtras();
		board_size = b.getInt("board_size");
		your_score = b.getInt("score");

		sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		setBestResults(sharedPref.getString(
			String.format("best_results_%d", board_size), "100:0:Lupan\n150:100000:Hello:World"));
		fillTable();
	}

	private void setBestResults(String results_string)
	{
		String[] lines = results_string.split("\n");
		int i = 0, score;
		long date;
		String name;
		your_pos = -1;
		Result your_result = new Result(your_score,
						System.currentTimeMillis(),
						getString(R.string.your_result));
		for (String line : lines) {
			if (i > 9) {
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
			if (i > 9) {
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
 
		for (int i = 0; i <= 11; i++) {
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
	}

	// @Override
	// public boolean onCreateOptionsMenu(Menu menu) {
	// 	// Inflate the menu; this adds items to the action bar if it is present.
	// 	getMenuInflater().inflate(R.menu.best_results, menu);
	// 	return true;
	// }

}
