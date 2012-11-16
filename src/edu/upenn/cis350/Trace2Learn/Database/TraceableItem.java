package edu.upenn.cis350.Trace2Learn.Database;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public abstract class TraceableItem {
	private long id;
    long order;
    private Map<String, Set<String>> attributes;
    private Set<String> tags;
    
    public TraceableItem() {
    	id = -1;
    	order = 0;
    	attributes = new HashMap<String, Set<String>>();
    	tags = new HashSet<String>();
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
    
    public void setTags(Set<String> tags) {
    	this.tags = new HashSet<String>(tags);
    }
    
    private Map<String, Set<String>> deepCopy(Map<String, Set<String>> original) {
    	Map<String, Set<String>> newMap = new HashMap<String, Set<String>>();
    	for (String key : original.keySet()) {
    		newMap.put(key, new HashSet<String>(original.get(key)));
    	}
    	return newMap;
    }
    
    @Override
    public boolean equals(Object o) {
    	if (o == null) return false;
    	if (o == this) return true;
    	if (!(o instanceof TraceableItem)) return false;
    	TraceableItem that = (TraceableItem) o;
    	if (this.id != that.id) return false;
    	if (this.order != that.order) return false;
    	if (!this.tags.equals(that.tags)) return false;
    	if (!this.attributes.equals(that.attributes)) return false;
    	return true;
    }
    
    @Override
    public String toString() {
    	return tags.toString() + "\t" +
               "Attributes: " + attributes + "\t" +
		       "Id: " + id + "\t" + "Order: " + order + "\t";
    }
    /** 
	 * The ratio for determining how large a stroke should be given the size
	 * of the canvas
	 */
	private static final float _heightToStroke = 8F/400F;
	
	/**
	 * Configures the paint options given the size of the canvas
	 * @param height - the height of the canvas on which the paint options will
	 * 				   be used
	 * @return The configured paint options
	 */
	private Paint buildPaint(float height) {
		
		Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(0xFFFF0000);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(height*_heightToStroke);
        
        return paint;
	}	
	
	/**
	 * Draws the item in the canvas provided
	 * @param canvas
	 */
	public void draw(Canvas canvas) {
		Paint paint = buildPaint(canvas.getHeight());
        
        draw(canvas, paint);
	}
	
	/**
	 * Draws the item in the canvas provided in a animation percentage
	 * The time is a normalized step from 0 to 1, 0 being not shown at all
	 * and 1 being completely drawn.
	 * @param canvas - the canvas to draw on
	 * @param time - the time in the animation from 0 to 1
	 */
	public void draw(Canvas canvas, float time) {
		Paint paint = buildPaint(canvas.getHeight());
        
        draw(canvas, paint, time);
	}
	
	/**
	 * Draws the item in the canvas provided, using the provided paint brush
	 * @param canvas - the canvas to draw on
	 * @param paint - the drawing settings for the item
	 */
	public void draw(Canvas canvas, Paint paint) {
		Rect bounds = canvas.getClipBounds();
		draw(canvas, paint, bounds.left, bounds.top, bounds.width(), bounds.height());
	}
	
	/**
	 * Draws the item in the canvas provided in a animation percentage, using the provided paint brush
	 * The time is a normalized step from 0 to 1, 0 being not shown at all
	 * and 1 being completely drawn.
	 * @param canvas - the canvas to draw on
	 * @param paint - the drawing settings for the item
	 * @param time - the time in the animation from 0 to 1
	 */
	public void draw(Canvas canvas, Paint paint, float time) {
		Rect bounds = canvas.getClipBounds();
		draw(canvas, paint, bounds.left, bounds.top, bounds.width(), bounds.height(), time);
	}
	
	/**
	 * Draws the item in the canvas provided within the provided bounding box
	 * The time is a normalized step from 0 to 1, 0 being not shown at all
	 * and 1 being completely drawn.
	 * @param canvas - the canvas to draw on
	 * @param left - the left bound in which the item should be drawn
	 * @param top - the top bound in which the item should be drawn
	 * @param width - the width of the bounding box in which the item should be drawn
	 * @param height - the height of the bounding box in which the item should be drawn
	 * @param time - the time in the animation from 0 to 1
	 */
	public void draw(Canvas canvas, float left, float top, float width,
			float height, float time) {
		Paint paint = buildPaint(height);
        
        draw(canvas, paint, left, top, width, height, 1);
	}
	
	/**
	 * Draws the item in the canvas provided within the provided bounding box
	 * @param canvas - the canvas to draw on
	 * @param left - the left bound in which the item should be drawn
	 * @param top - the top bound in which the item should be drawn
	 * @param width - the width of the bounding box in which the item should be drawn
	 * @param height - the height of the bounding box in which the item should be drawn
	 */
	public void draw(Canvas canvas, float left, float top,
			float width, float height) {
		Paint paint = buildPaint(height);
        
        draw(canvas, paint, left, top, width, height);
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
	public void draw(Canvas canvas, Paint paint, float left,
			float top, float width, float height) {
		draw(canvas, paint, left, top, width, height, 1F);
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
	public abstract void draw(Canvas canvas, Paint paint, float left,
			float top, float width, float height, float time);
}