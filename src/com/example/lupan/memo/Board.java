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


public class Board extends View {

	interface EndOfGameListener {
		void onEndOfGame(Board b);
	}

	static final int[] icons = {
		R.drawable.animals_bumble_bee,
		R.drawable.animals_butterfly,
		R.drawable.animals_cat,
		R.drawable.animals_elephant,
		R.drawable.animals_owl,
		R.drawable.animals_rabbit,
		R.drawable.banana_label,
		R.drawable.blueberry_muffin,
		R.drawable.cards_club,
		R.drawable.cards_diamond,
		R.drawable.cards_heart,
		R.drawable.cards_spade,
		R.drawable.cd_grey,
		R.drawable.cherries,
		R.drawable.coffe_tea_01,
		R.drawable.dialog_information_2,
		R.drawable.eye,
		R.drawable.food_fried_egg_sunny,
		R.drawable.food_grapes,
		R.drawable.food_kiwi,
		R.drawable.food_strawberry_with_light_shadow,
		R.drawable.frog_crazy_eyes,
		R.drawable.green_ladybug,
		R.drawable.green_pear,
		R.drawable.lemon_1,
		R.drawable.little_penguin,
		R.drawable.mail_attachment_2,
		R.drawable.start_here,
		R.drawable.startled_fish,
		R.drawable.water_drop_1
	};

	public Board(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	EndOfGameListener listener;

	public void setEndOfGameListener(EndOfGameListener listener)
	{
		this.listener = listener;
	}

	public int getMovesCnt() {
		return moves_cnt;
	}

	public int getBoardSize() {
		return ycnt * 100 + xcnt;
	}

	int xcnt = 0; /** number of rows on the board */
	int ycnt = 0; /** number of columns on the board */

	Paint visible_paint, text_paint;
	Bitmap pattern_bitmap;
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
		visible_paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		visible_paint.setStyle(Paint.Style.STROKE);
		text_paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		text_paint.setTextAlign(Paint.Align.CENTER);
		text_paint.setTextSize(18);
		Resources res = getContext().getResources();
		pattern_bitmap = BitmapFactory.decodeResource(
	              res, R.drawable.pattern);

	}

	/**
	 * Set number of columns (xcnt) and number of rows (ycnt)
	 * of the board
	 *
	 * @param xcnt number of columns
	 * @param ycnt number of rows
	 */
	private void setSize(int xcnt, int ycnt)
	{
		if (xcnt < 1 || ycnt < 1 ||
		    xcnt == this.xcnt && ycnt == this.ycnt) {
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

		onSizeChanged(widget_w, widget_h, widget_w, widget_h);
	}

	/**
	 * start an new Game, the argument must be of the form:
	 * (ycnt * 100 + xcnt)
	 */
	public void newGame(int ycnt_xcnt) {
		newGame(ycnt_xcnt % 100, ycnt_xcnt / 100);
	}

	/**
	 * start an new Game for the board
	 *
	 * @param xcnt number of columns
	 * @param ycnt number of rows
	 */
	public void newGame(int xcnt, int ycnt) {
		setSize(xcnt, ycnt);
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

	int board_x; /** x orign of drawing the board */
	int board_y; /** y orign of drawing the board */
	int board_w; /** width of the board minus padding */
	int board_h; /** height of the board minus padding */
	int widget_w, widget_h; // w and h from last call of onSizeChanged

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
		if (xcnt == 0 || ycnt == 0) {
			return;
		}
		widget_w = w;
		widget_h = h;
		int w1 = w - getPaddingLeft() - getPaddingRight();
		int h1 = h - getPaddingTop() - getPaddingBottom();
		int size = Math.min((w1 / xcnt), (h1 / ycnt));
		board_w = xcnt * size;
		board_h = ycnt * size;
		board_x = getPaddingLeft() + (w1 - board_w) / 2;
		board_y = getPaddingTop() + (h1 - board_h) / 2;
		for (int x = 0; x < xcnt; x++) {
			for (int y = 0; y < ycnt; y++) {
				rects[x][y] = new Rect(
				       board_x + x * size + 2,
				       board_y + y * size + 2,
				       board_x + (x + 1) * size - 2,
				       board_y + (y + 1) * size - 2);
			}
		}
	}

	/**
	 * Draw rectangle, a single field of the board
	 */
	private void drawRect(Canvas canvas, int x, int y)
	{
		if (field_status[x][y] == EMPTY_FIELD) {
			return;
		}
		if (field_status[x][y] == HIDDEN_FIELD) {
			canvas.drawBitmap(pattern_bitmap, null, rects[x][y],
					  visible_paint);

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

	Rect bounds = new Rect();

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
			String msg = String.format(
				getContext().getString(R.string.you_won),
				moves_cnt);
			text_paint.getTextBounds(msg, 0, msg.length(), bounds);
			while (bounds.width() >= board_w) {
				text_paint.setTextSize(text_paint.getTextSize() - 1);
				text_paint.getTextBounds(msg, 0, msg.length(),
							 bounds);
			}
			canvas.drawText(msg, board_x + board_w/2,
					board_y + board_h/2, text_paint);
		}
	}

	/**
	 * Uncover field of the given coordinates
	 */
	private void uncover(int x, int y) {
		field_status[x][y] = UNCOVERED_FIELD;
		xs[uncovered_cnt] = x;
		ys[uncovered_cnt] = y;
		uncovered_cnt++;
	}

	/**
	 * Remove oncovered fields (having identical icons)
	 */
	private void remove_uncovered_fields() {
		field_status[xs[0]][ys[0]] = EMPTY_FIELD;
		field_status[xs[1]][ys[1]] = EMPTY_FIELD;
		uncovered_cnt = 0;
		active_fields -= 2;
		if (active_fields == 0) {
			if (listener != null) {
				listener.onEndOfGame(this);
			}
		}
	}

	/**
	 * Hide uncovered fields
	 */
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
		    values.length < 4 + 2 * xcnt * ycnt) {
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
					newGame(xcnt, ycnt);
					return false;
				}
				if (field_status[x][y] == EMPTY_FIELD) {
					active_fields--;
				}
				if (field_status[x][y] == UNCOVERED_FIELD) {
					if (uncovered_cnt == 2) {
						newGame(xcnt, ycnt);
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
