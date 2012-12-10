package edu.upenn.cis350.Trace2Learn;

import edu.upenn.cis350.Trace2Learn.Database.Character;
import edu.upenn.cis350.Trace2Learn.Database.Stroke;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

/**
 * @author Ryan
 *
 */
public abstract class CharacterPane extends View {

	protected Paint paint;
	
	protected int backgroundColor = Color.LTGRAY;
	
	public CharacterPane(Context context) {
		super(context);
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(0xFFFF0000);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeJoin(Paint.Join.ROUND);
		paint.setStrokeCap(Paint.Cap.ROUND);
		paint.setStrokeWidth(12);
	}

	/**
	 * Draws the given stroke onto the screen
	 * @param canvas - the canvas on which to draw the stroke
	 * @param stroke - the stroke that should be drawn
	 */
	protected void drawStroke(Canvas canvas, Stroke stroke)
	{
		canvas.drawPath(stroke.toPath(), paint);
	}
	
	/**
	 * Draws the given character onto the screen
	 * @param canvas - the canvas on which to draw the stroke
	 * @param character - the character that should be drawn
	 */
	protected void drawCharacter(Canvas canvas, Character character)
	{
		character.draw(canvas);
	}
	
	public abstract void clearPane();
	
}
