package edu.upenn.cis350.Trace2Learn;

import edu.upenn.cis350.Trace2Learn.Database.Character;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;

/**
 * @author Ryan This class displays a character which is animated stroke by stroke.
 */
public class CharacterPlaybackPane extends CharacterViewPane {

	protected Character character;
	protected int currentStroke = 0;
	protected boolean animated;
	protected long lastTick;
	protected float animationLength;

	protected Thread refreshTimer;
	protected Handler handler;

	private float _elapsedTime;

	public CharacterPlaybackPane(Context context, boolean animated, float animationLength) {
		super(context);
		resetPlayback();
		this.animated = animated;
		this.animationLength = animationLength;

		handler = new Handler();

	}

	public CharacterPlaybackPane(Context context, boolean animated) {
		this(context, false, 60);
	}

	public CharacterPlaybackPane(Context context) {
		this(context, false);
	}

	public void setCharacter(Character character) {
		this.character = character;
		resetPlayback();
	}

	public void clearPane() {
		character = null;
	}

	/**
	 * Builds and starts the animation timer
	 */
	public void startTimer() {
		if (refreshTimer == null) {
			refreshTimer = new Thread() {
				Runnable _update = new Runnable() {
					public void run() {
						invalidate();
					}
				};

				public void run() {
					while (animated && _elapsedTime < animationLength) {
						handler.post(_update);
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

	/**
	 * If the timer is running, stop the timer
	 */
	public void stopTimer() {
		if (refreshTimer != null) {
			refreshTimer.interrupt();
			try {
				refreshTimer.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			refreshTimer = null;
		}
	}

	/**
	 * Toggles whether or not the character should be drawn stroke by stroke
	 * 
	 * @param animate
	 *            whether the character should be animated
	 */
	public void setAnimated(boolean animate) {
		resetPlayback();
		animated = animate;
		if (animated) {
			startTimer();
		} else {
			stopTimer();
		}
	}

	/**
	 * Toggles whether or not the character should be drawn stroke by stroke
	 * 
	 * @param animate
	 *            whether the character should be animated
	 * @param length
	 *            the duration of the animation
	 */
	public void setAnimated(boolean animate, float length) {
		setAnimated(animate);
		setAnimationLength(length);
	}

	public void setAnimationLength(float length) {
		if (length > 0)
			animationLength = length;
	}

	/**
	 * Resets playback to the first stroke (nothing shown).
	 */
	public void resetPlayback() {
		currentStroke = 0;
		lastTick = System.currentTimeMillis();
		_elapsedTime = 0;
	}

	/**
	 * Adds the next stroke to the screen.
	 */
	public void stepPlayback() {
		currentStroke++;
		if (currentStroke > character.getNumberOfStrokes())
			currentStroke = character.getNumberOfStrokes();
	}

	/**
	 * Sets the current stroke to be drawn. The value will be clamped to the actual number of strokes.
	 * 
	 * @param stroke
	 *            - the number of strokes that should be drawn
	 * @return the actual stroke the view is set to.
	 */
	public int setCurrentStroke(int stroke) {
		if (character == null || stroke < 0) {
			currentStroke = 0;
		} else if (stroke > character.getNumberOfStrokes()) {
			currentStroke = character.getNumberOfStrokes();
		} else {
			currentStroke = stroke;
		}

		return currentStroke;
	}

	protected void animateTick() {
		long ticks = System.currentTimeMillis() - lastTick;
		_elapsedTime += ticks / 1000F;
		lastTick = System.currentTimeMillis();
	}

	@Override
	public void onDraw(Canvas canvas) {
		animateTick();

		float time = _elapsedTime / animationLength;

		canvas.drawColor(_backgroundColor);

		if (character != null) {
			if (animated) {
				character.draw(canvas, time);
			} else {
				character.draw(canvas);
			}

		}
	}

}
