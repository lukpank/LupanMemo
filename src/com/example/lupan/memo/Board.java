package com.example.lupan.memo;

import com.example.lupan.memo.R;

import android.view.View;
import android.view.MotionEvent;
import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect; // (left, top, right, bottom)
import android.util.Log;


public class Board extends View {

	int xcnt = 5;
	int ycnt = 6;

	static final int[] icons = {
		R.drawable.animals_bumble_bee,
		R.drawable.animals_butterfly,
		R.drawable.animals_cat,
		R.drawable.animals_elephant,
		R.drawable.animals_owl,
		R.drawable.animals_rabbit,
		R.drawable.bookmarks,
		R.drawable.cards_club,
		R.drawable.cards_diamond,
		R.drawable.cards_heart,
		R.drawable.cards_spade,
		R.drawable.dialog_information_2,
		R.drawable.dialog_password_2,
		R.drawable.disc_00,
		R.drawable.eye,
		R.drawable.food_strawberry_with_light_shadow,
		R.drawable.mail_attachment_2,
		R.drawable.mail_unread_3,
		R.drawable.media_playlist_shuffle_3,
		R.drawable.object_locked_2,
		R.drawable.pictogram_din_m000_general,
		R.drawable.pictograms_aem_0022_electric_general,
		R.drawable.pictograms_aem_0047_read_operators_manual,
		R.drawable.pictograms_aem_0056_radiation_hazard,
		R.drawable.pictograms_aem_0063_wear_safety_boots,
		R.drawable.pictograms_road_signs_airplane_roadsign_2,
		R.drawable.script_error,
		R.drawable.start_here,
		R.drawable.user_away_2,
		R.drawable.water_drop_1
	};

	public Board(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	Paint hidden_paint, visible_paint, textPaint;
	Rect[][] rects;
	int[][] field_icons;

	// fields status with possible values
	int[][] field_status;
	final int HIDDEN_FIELD = 0;
	final int UNCOVERED_FIELD = 1;
	final int EMPTY_FIELD = 2;

	int active_fields; // all HIDDEN_FIELD + all UNCOVERED_FIELDS
	int uncovered_cnt;  // how many fields are uncovered
	int moves_cnt;

	// coordinates of uncovered fields
	int[] xs = new int[2];
	int[] ys = new int[2];

	private void init() {
		hidden_paint = new Paint();
		hidden_paint.setColor(0x404040ff);
		visible_paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		visible_paint.setStyle(Paint.Style.STROKE);
		textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		textPaint.setTextAlign(Paint.Align.CENTER);
		textPaint.setTextSize(18);
		setSize(xcnt, ycnt);
	}

	public void setSize(int ycnt_xcnt) {
		setSize(ycnt_xcnt % 100, ycnt_xcnt / 100);
	}

	public void setSize(int xcnt, int ycnt)
	{
		if (xcnt < 1 || ycnt < 1) {
			return;
		}

		this.xcnt = xcnt;
		this.ycnt = ycnt;
		rects = new Rect[xcnt][];
		field_status = new int[xcnt][];
		field_icons = new int[xcnt][];
		for (int i = 0; i < xcnt; i++) {
			rects[i] = new Rect[ycnt];
			field_status[i] = new int[ycnt];
			field_icons[i] = new int[ycnt];
		}

		newGame();
		onSizeChanged(widget_w, widget_h, widget_w, widget_h);
	}

	public void newGame() {
		for (int i = 0; i < xcnt; i++) {
			for (int j = 0; j < ycnt; j++) {
				field_status[i][j] = HIDDEN_FIELD;
			}
		}
		active_fields = xcnt * ycnt;
		uncovered_cnt = 0;
		moves_cnt = 0;

		Randomizer r_icons = new Randomizer(icons.length);
		Randomizer r_fields = new Randomizer(xcnt * ycnt);
		for (int i = 0; i < xcnt * ycnt / 2; i++) {
			int icon = r_icons.getRandomValue();
			int f1 = r_fields.getRandomValue();
			int x1 = f1 / ycnt;
			int y1 = f1 % ycnt;
			field_icons[x1][y1] = icon;
			int f2 = r_fields.getRandomValue();
			int x2 = f2 / ycnt;
			int y2 = f2 % ycnt;
			field_icons[x2][y2] = icon;
		}
	}

	int x;
	int y;
	int w;
	int h;
	Rect rect;
	int widget_w, widget_h; // w and h from last call of onSizeChanged

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
		widget_w = w;
		widget_h = h;
		int w1 = w - getPaddingLeft() - getPaddingRight();
		int h1 = h - getPaddingTop() - getPaddingBottom();
		int size = Math.min((w1 / xcnt), (h1 / ycnt));
		this.w = xcnt * size;
		this.h = ycnt * size;
		this.x = getPaddingLeft() + (w1 - this.w) / 2;
		this.y = getPaddingTop() + (h1 - this.h) / 2;
		rect = new Rect(this.x, this.y,
				this.x + this.w, this.y + this.h);
		for (int x = 0; x < xcnt; x++) {
			for (int y = 0; y < ycnt; y++) {
				rects[x][y] = new Rect(
				       this.x + x * size + 2,
				       this.y + y * size + 2,
				       this.x + (x + 1) * size - 2,
				       this.y + (y + 1) * size - 2);
			}
		}
	}

	private void drawRect(Canvas canvas, int x, int y)
	{
		if (field_status[x][y] == EMPTY_FIELD) {
			return;
		}
		if (field_status[x][y] == HIDDEN_FIELD) {
			canvas.drawRect(rects[x][y], hidden_paint);
		}
		canvas.drawRect(rects[x][y], visible_paint);
		if (field_status[x][y] == UNCOVERED_FIELD) {
			Resources res = getContext().getResources();
			Bitmap bitmap = BitmapFactory.decodeResource(
				              res, icons[field_icons[x][y]]);
			canvas.drawBitmap(bitmap, null, rects[x][y],
					  visible_paint);
		}
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);

		for (int x = 0; x < xcnt; x++) {
			for (int y = 0; y < ycnt; y++) {
				drawRect(canvas, x, y);
			}
		}
		if (active_fields == 0) {
			canvas.drawText(String.format(
				"You won using %d moves!", moves_cnt),
					x + w/2, y + h/2, textPaint);
		}
	}

	private void uncover(int x, int y) {
		field_status[x][y] = UNCOVERED_FIELD;
		xs[uncovered_cnt] = x;
		ys[uncovered_cnt] = y;
		uncovered_cnt++;
	}

	private void remove_uncovered_fields() {
		field_status[xs[0]][ys[0]] = EMPTY_FIELD;
		field_status[xs[1]][ys[1]] = EMPTY_FIELD;
		uncovered_cnt = 0;
		active_fields -= 2;
	}

	private void hide_uncovered_fields() {
		field_status[xs[0]][ys[0]] = HIDDEN_FIELD;
		field_status[xs[1]][ys[1]] = HIDDEN_FIELD;
		uncovered_cnt = 0;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if ((event.getAction() == MotionEvent.ACTION_UP)) {
			int x1 = (int)event.getX();
			int y1 = (int)event.getY();
			int x, y = 0;
			boolean found = false;

			for (x = 0; x < xcnt; x++) {
				for (y = 0; y < ycnt; y++) {
					if (rects[x][y].contains(x1, y1)) {
						found = true;
						break;
					}
				}
				if (found) {
					break;
				}
			}
			if (!found || field_status[x][y] != HIDDEN_FIELD) {
				return true;
			}

			switch (uncovered_cnt) {
			case 0:
				moves_cnt += 1;
				uncover(x, y);
				break;
			case 1:
				uncover(x, y);
				if (field_icons[xs[0]][ys[0]] ==
				    field_icons[xs[1]][ys[1]]) {
					remove_uncovered_fields();
				}
				break;
			case 2:
				moves_cnt += 1;
				hide_uncovered_fields();
				uncover(x, y);
				break;
			}
			invalidate();
		}
		return true;
	}

	/**
	 * Serialize Board state into a string
	 */
	public String serialize()
	{
		StringBuffer buf = new StringBuffer();

		buf.append("0:"); // format's version number
		buf.append(String.format("%d:", xcnt));
		buf.append(String.format("%d:", ycnt));
		buf.append(String.format("%d:", moves_cnt));

		for (int x = 0; x < xcnt; x++) {
			for (int y = 0; y < ycnt; y++) {
				buf.append(String.format("%d:",
							 field_icons[x][y]));
				buf.append(String.format("%d:",
							 field_status[x][y]));
			}
		}
		return buf.toString();
	}

	/**
	 * Deserialize Board state from a string
	 */
	public boolean deserialize(String str)
	{
		String[] tokens = str.split(":");
		int[] values = new int[tokens.length];
		for (int i = 0; i < tokens.length; i++) {
			try {
				values[i] = Integer.parseInt(tokens[i]);
			} catch (NumberFormatException e) {
				return false;
			}
		}
		if (values.length < 4) {
			return false;
		}
		int version = values[0];
		if (version != 0) {
			return false;
		}
		int xcnt = values[1];
		int ycnt = values[2];

		if (xcnt < 1 || ycnt < 1 || xcnt * ycnt / 2 > icons.length ||
		    values.length < 2 + 2 * xcnt * ycnt) {
			// empty or not enough icons or not enough values
			return false;
		}
		setSize(xcnt, ycnt);
		moves_cnt = values[3];

		active_fields = xcnt * ycnt;
		uncovered_cnt = 0;
		int idx = 4;
		for (int x = 0; x < xcnt; x++) {
			for (int y = 0; y < ycnt; y++) {
				field_icons[x][y] = values[idx++];
				field_status[x][y] = values[idx++];
				if (field_icons[x][y] < 0 || field_icons[x][y] >= icons.length ||
				    field_status[x][y] < 0 || field_status[x][y] > 2) {
					setSize(xcnt, ycnt);
					return false;
				}
				if (field_status[x][y] == EMPTY_FIELD) {
					active_fields--;
				}
				if (field_status[x][y] == UNCOVERED_FIELD) {
					if (uncovered_cnt == 2) {
						setSize(xcnt, ycnt);
						return false;
					}
					xs[uncovered_cnt] = x;
					ys[uncovered_cnt] = y;
					uncovered_cnt++;
				}
			}
		}
		return true;
	}
}
