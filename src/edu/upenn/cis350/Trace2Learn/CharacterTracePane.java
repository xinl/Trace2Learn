/**
 * 
 */
package edu.upenn.cis350.Trace2Learn;

import edu.upenn.cis350.Trace2Learn.Database.Character;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.view.View;

/**
 * @author Ryan
 * 
 */
public class CharacterTracePane extends CharacterCreationPane {

	protected Character template;

	protected Paint paint;
	private static final float heightToStrokeRatio = 12F / 400F;

	private float templateTime = 0;
	private float timeLimit = 0;

	private float timePerStroke = 4000;

	private long lastTick;
	private int curStroke;

	private OnTraceCompleteListener onTraceCompleteListener = null;

	Thread refreshTimer;

	protected Handler handler;

	public CharacterTracePane(Context context) {
		super(context);
		paint = new Paint();
		paint.setColor(Color.GRAY);
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeJoin(Paint.Join.ROUND);
		paint.setStrokeCap(Paint.Cap.ROUND);
		handler = new Handler();
	}

	public void setTemplate(Character template) {
		this.template = template;
		setCurrentTraceStroke(0);
	}

	@Override
	public void clearPane() {
		super.clearPane();
		setCurrentTraceStroke(0);
	}

	/**
	 * Builds and starts the animation timer
	 */
	public void startTimer() {
		if (refreshTimer == null) {
			refreshTimer = new Thread() {
				Runnable update = new Runnable() {
					public void run() {
						invalidate();
					}
				};

				public void run() {
					while (templateTime < timeLimit) {
						handler.post(update);
						try {
							Thread.sleep(20);
						} catch (InterruptedException e) {
							break;
						}
					}
					refreshTimer = null;
				}
			};
			refreshTimer.start();
		}
	}

	protected void animateTick() {
		if (template != null) {
			long time = System.currentTimeMillis();
			long dif = time - lastTick;
			float strokeTime = dif / (timePerStroke * template.getNumberOfStrokes());
			templateTime = Math.min(templateTime + strokeTime, timeLimit);
		}
	}

	protected void setCurrentTraceStroke(int stroke) {
		if (template != null) {
			int numStrokes = template.getNumberOfStrokes();
			if (stroke < 0)
				stroke = 0;
			if (stroke >= numStrokes) {
				stroke = numStrokes;
				if (onTraceCompleteListener != null) {
					try {
						Thread.sleep(500); // wait a little so user can appreciate their work
					} catch (InterruptedException e) {
						// do nothing
					}
					onTraceCompleteListener.onTraceComplete(getRootView());
				}
			}
			float strokeLen = 1F / numStrokes;
			templateTime = stroke * strokeLen;
			timeLimit = (stroke + 1) * strokeLen;
			lastTick = System.currentTimeMillis();
			startTimer();
		}
		curStroke = stroke;
	}

	@Override
	protected void beginStroke(float newX, float newY) {
		super.beginStroke(newX, newY);
		completeStrokeAnimation();
	}

	@Override
	protected void completeStroke(float newX, float newY) {
		super.completeStroke(newX, newY);
		animateNextStroke();
	}

	protected void animateNextStroke() {
		setCurrentTraceStroke(curStroke + 1);
	}

	protected void completeStrokeAnimation() {
		templateTime = timeLimit;
	}

	@Override
	public void onDraw(Canvas canvas) {
		animateTick();
		// Log.i("DRAW", "TIME: " + timePerStroke);
		paint.setStrokeWidth(canvas.getHeight() * heightToStrokeRatio);
		canvas.drawColor(backgroundColor);
		if (template != null)
			template.draw(canvas, paint, templateTime);
		// Consider using a bitmap buffer so only new strokes are drawn.
		drawCharacter(canvas, character);
	}

	public interface OnTraceCompleteListener {
		public abstract void onTraceComplete(View v);
	}

	public void setOnTraceCompleteListener(OnTraceCompleteListener listener) {
		this.onTraceCompleteListener = listener;
	}

	public OnTraceCompleteListener getOnTraceCompleteListener() {
		return this.onTraceCompleteListener;
	}

}
