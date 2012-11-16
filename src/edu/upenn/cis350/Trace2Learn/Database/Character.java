package edu.upenn.cis350.Trace2Learn.Database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;

public class Character {
    private long id;
    private long order;
    private Map<String, Set<String>> attributes;
    private Set<String> tags;
    private List<Stroke> strokes;
  
    
    public Character() {
    	id = -1;
    	order = 0;
    	attributes = new HashMap<String, Set<String>>();
    	tags = new HashSet<String>();
    	strokes = new ArrayList<Stroke>();
    }
    
    public void setId(long id){
	    this.id = id;
    }
    
    public long getId(){
	    return this.id;
    }
    
    public void setOrder(long order){
	    this.order = order;
    }
    
    public long getOrder() {
    	return this.order;
    }
    
    public void addAttribute(String key, String value) {
	    Set<String> values = this.attributes.get(key);
	    if (values == null) {
	    	values = new HashSet<String>();
	    }
	    values.add(value);
	    attributes.put(key, values);
    }
    
    public void addAttributes(String key, Set<String> values) {
    	attributes.put(key, new HashSet<String>(values));
    }
    
    public void removeAttribute(String key, String value) {
    	Set<String> values = this.attributes.get(key);
    	if (values == null) {
    		return;
    	}
    	values.remove(value);
    	if (values.isEmpty()) {
    		attributes.remove(key);
    	} else {
    		attributes.put(key, values);
    	}
    }
    
    public void removeAttributes(String key) {
    	attributes.remove(key);
    }
    
    public void setAttributes(Map<String, Set<String>> attributes) {
    	this.attributes = deepCopy(attributes);
    }
    
    public Map<String, Set<String>> getAttributes() {
	    return deepCopy(this.attributes);
    }
    
    public void addTag(String tag){
	    this.tags.add(tag);
    }
    
    public void removeTag(String tag) {
    	this.tags.remove(tag);
    }

    public Set<String> getTags(){
	    return new HashSet<String>(this.tags);
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
    
    @Override
    public boolean equals(Object o) {
    	if (o == null) return false;
    	if (o == this) return true;
    	if (!(o instanceof Character)) return false;
    	Character that = (Character) o;
    	if (this.id != that.id) return false;
    	if (this.order != that.order) return false;
    	if (!this.strokes.equals(that.strokes)) return false;
    	if (!this.tags.equals(that.tags)) return false;
    	if (!this.attributes.equals(that.attributes)) return false;
    	return true;
    }
    
    @Override
    public String toString() {
    	return "\nCHAR\tTags: " + tags.toString() + "\t" +
               "Attributes: " + attributes.toString() + "\t" +
    		   "Id: " + id + "\t" + "Order: " + order;
    }
    
    private Map<String, Set<String>> deepCopy(Map<String, Set<String>> original) {
    	Map<String, Set<String>> newMap = new HashMap<String, Set<String>>();
    	for (String key : original.keySet()) {
    		newMap.put(key, new HashSet<String>(original.get(key)));
    	}
    	return newMap;
    }
    
    /* ugly legacy code */
    
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
	public void draw(Canvas canvas, Paint paint, float left, float top, float width, float height)
	{
		Matrix matrix = new Matrix();
		Log.i("DRAW", "Scale: " + width + " " + height);
		Log.i("DRAW", "Strokes: " + strokes.size());
		matrix.postScale(width, height);
		matrix.postTranslate(left,  top);
		List<Stroke> strokes = getStrokes();
		for(Stroke stroke : strokes)
		{
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
	public void draw(Canvas canvas, Paint paint, float left, float top, float width, float height, float time)
	{
		Matrix matrix = new Matrix();
		Log.i("DRAW", "Scale: " + width + " " + height);
		Log.i("DRAW", "Strokes: " + strokes.size());
		Log.i("DRAW", "Time: " + time);
		matrix.postScale(width, height);
		matrix.postTranslate(left,  top);
		
		List<Stroke> strokes = getStrokes();
		
		float strokeTime = 1F/strokes.size();
		float coveredTime = 0;
		for(Stroke stroke : strokes)
		{
			if(coveredTime > time) break;
			float sTime = time - coveredTime;
			if(sTime > strokeTime) sTime = strokeTime;
			Path path = stroke.toPath(matrix, sTime/strokeTime);
			canvas.drawPath(path, paint);
			coveredTime += strokeTime;
		}
	}

}

