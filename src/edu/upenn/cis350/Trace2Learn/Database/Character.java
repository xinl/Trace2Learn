package edu.upenn.cis350.Trace2Learn.Database;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;

public class Character extends TraceableItem {
    
	private List<Stroke> strokes;

    public Character() {
    	super();
    	strokes = new ArrayList<Stroke>();
    }
        
    public void addStroke(Stroke s){
	    this.strokes.add(s);
    }
    
    public void setStrokes(List<Stroke> strokes) {
    	this.strokes = new ArrayList<Stroke>(strokes);
    }
    
    public List<Stroke> getStrokes(){
	    return new ArrayList<Stroke>(this.strokes);
    }
    
    public int getNumberOfStrokes() {
    	return strokes.size();
    }
    
    @Override
    public boolean equals(Object o) {
    	if (super.equals(o) == false) return false;
    	if (! (o instanceof Character)) return false;
    	Character that = (Character) o;
    	if (!this.strokes.equals(that.strokes)) return false;
    	return true;
    }
    
    @Override
    public String toString() {
    	return "\nCHAR\t" + super.toString();
    }
    
    /**
	 * Draws the item in the canvas provided, using the provided paint brush
	 * within the provided bounding box
	 * @param canvas - the canvas to draw on
	 * @param paint - the drawing settings for the item
	 * @param left - the left bound in which the item should be drawn
	 * @param top - the top bound in which the item should be drawn
	 * @param width - the width of the bounding box in which the item should be drawn
	 * @param height - the height of the bounding box in which the item should be drawn
	 */
	@Override
	public void draw(Canvas canvas, Paint paint, float left,
			float top, float width, float height) {
		Matrix matrix = new Matrix();
		// Log.i("DRAW", "Scale: " + width + " " + height);
		// Log.i("DRAW", "Strokes: " + strokes.size());
		matrix.postScale(width, height);
		matrix.postTranslate(left,  top);
		List<Stroke> strokes = getStrokes();
		for(Stroke stroke : strokes) {
			Path path = stroke.toPath(matrix);
			canvas.drawPath(path, paint);
		}
	}
	
	/**
	 * Draws the item in the canvas provided, using the provided paint brush
	 * within the provided bounding box
	 * The time is a normalized step from 0 to 1, 0 being not shown at all
	 * and 1 being completely drawn.
	 * @param canvas - the canvas to draw on
	 * @param paint - the drawing settings for the item
	 * @param left - the left bound in which the item should be drawn
	 * @param top - the top bound in which the item should be drawn
	 * @param width - the width of the bounding box in which the item should be drawn
	 * @param height - the height of the bounding box in which the item should be drawn
	 * @param time - the time in the animation from 0 to 1
	 */
	@Override
	public void draw(Canvas canvas, Paint paint, float left,
			float top, float width, float height, float time) {
		Matrix matrix = new Matrix();
		// Log.i("DRAW", "Scale: " + width + " " + height);
		// Log.i("DRAW", "Strokes: " + strokes.size());
		// Log.i("DRAW", "Time: " + time);
		matrix.postScale(width, height);
		matrix.postTranslate(left,  top);
		
		float strokeTime = 1F/strokes.size();
		float coveredTime = 0;
		for(Stroke stroke : strokes) {
			if(coveredTime > time) break;
			float sTime = time - coveredTime;
			if(sTime > strokeTime) sTime = strokeTime;
			Path path = stroke.toPath(matrix, sTime/strokeTime);
			canvas.drawPath(path, paint);
			coveredTime += strokeTime;
		}
	}

}

