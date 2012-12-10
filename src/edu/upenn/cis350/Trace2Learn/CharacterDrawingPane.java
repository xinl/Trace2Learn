package edu.upenn.cis350.Trace2Learn;

import edu.upenn.cis350.Trace2Learn.Database.Character;
import edu.upenn.cis350.Trace2Learn.Database.Stroke;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;

public class CharacterDrawingPane extends CharacterPane {

	protected Character character;
	private Stroke currentStroke;
	private float prevX, prevY;
	private static final float TOUCH_TOLERANCE = 4;

	public CharacterDrawingPane(Context c) {
		super(c);
		character = new Character();
	}

	/**
	 * The method that is called every time a new stroke begins This is where the creation of a new stroke should be handled
	 * 
	 * @param newX
	 *            The x coordinate where the stroke begins
	 * @param newY
	 *            The y coordinate where the stroke ends
	 */
	protected void beginStroke(float newX, float newY) {
		currentStroke = new Stroke(newX, newY);
		character.addStroke(currentStroke);
	}

	/**
	 * The method that is called every time a new point is sampled for the current stroke
	 * 
	 * @param newX
	 *            The x coordinate of the sample point
	 * @param newY
	 *            The y coordinate of the sample point
	 */
	protected void updateStroke(float newX, float newY) {
		currentStroke.addPoint(newX, newY);
	}

	/**
	 * The method that is called every when the stroke is completed This is where the completion of a stroke should be handled
	 * 
	 * @param newX
	 *            The x coordinate of the sample point
	 * @param newY
	 *            The y coordinate of the sample point
	 */
	protected void completeStroke(float newX, float newY) {
		// consider drawing just one stroke
		currentStroke.addPoint(newX, newY);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawColor(backgroundColor);

		// Consider using a bitmap buffer so only new strokes are drawn.
		drawCharacter(canvas, character);
	}

	/**
	 * Returns a copy on the drawn character We make a copy so that we do not inadvertently make changes to a saved character if the pane is revisited
	 * 
	 * @return a copy of the on screen character
	 */
	public Character getCharacter() {
		return character;
	}

	public void setCharacter(Character character) {
		this.character = character;
		invalidate();
	}

	public void clearPane() {
		this.character = new Character();
		invalidate();
	}

	/**
	 * Notifies the pane that a new stroke has been started on the screen
	 * 
	 * @param x
	 *            - the screen coordinate of the point in pixels
	 * @param y
	 *            - the screen coordinate of the point in pixels
	 */
	private void touchStart(float x, float y) {
		// Scale the pixels to a 1x1 square
		beginStroke(x / getWidth(), y / getHeight());
	}

	/**
	 * Notifies the pane that an additional point has been sampled in the current stroke
	 * 
	 * @param x
	 *            The screen coordinate of the point in pixels
	 * @param y
	 *            The screen coordinate of the point in pixels
	 */
	private void touchMove(float x, float y) {
		float dx = Math.abs(x - prevX);
		float dy = Math.abs(y - prevY);
		// Don't sample if the movement is negligible in size
		if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
			// Scale the pixels to a 1x1 square
			updateStroke(x / getWidth(), y / getHeight());
		}
	}

	/**
	 * Notifies the pane that the last point has been sampled in the current stroke
	 * 
	 * @param x
	 *            The screen coordinate of the point in pixels
	 * @param y
	 *            The screen coordinate of the point in pixels
	 */
	private void touchUp(float x, float y) {
		completeStroke(x / getWidth(), y / getHeight());
	}

	/**
	 * Handles the touch event and logs a new point in the stroke
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX();
		float y = event.getY();

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			touchStart(x, y);
			invalidate();
			break;
		case MotionEvent.ACTION_MOVE:
			touchMove(x, y);
			invalidate();
			break;
		case MotionEvent.ACTION_UP:
			touchUp(x, y);
			invalidate();
			break;
		}
		prevX = x;
		prevY = y;
		return true;
	}

}
