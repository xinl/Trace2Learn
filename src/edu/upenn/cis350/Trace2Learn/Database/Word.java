package edu.upenn.cis350.Trace2Learn.Database;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Paint;

public class Word extends TraceableItem {
    private List<Character> characters;
    
    public Word(){
    	super();
	    characters = new ArrayList<Character>();
    }

    public void addCharacter(Character c){
    	this.characters.add(c);
    }

    public List<Character> getCharacters(){
    	return new ArrayList<Character>(this.characters);
    }
    
    public void removeCharacter(int location) {
    	this.characters.remove(location);
    }
    
    public int length() {
    	return characters.size();
    }
    

	public List<Long> getCharacterIds() {
		List<Long> ids = new ArrayList<Long>();
		if (characters != null) {
			for (Character c : characters){
				ids.add(c.getId());
			}
		}
		return ids;
	}
    
    @Override
    public boolean equals(Object o) {
    	if (super.equals(o) == false) return false;
    	if (! (o instanceof Word)) return false;
    	Word that = (Word) o;
    	for (int i = 0; i < characters.size(); i++) {
    		if (this.characters.get(i).getId() !=
    				that.characters.get(i).getId()) {
    			return false;
    		}
    	}
    	return true;
    }
    
    @Override
    public String toString() {
    	return "\nWORD\t" + super.toString() + "Characters: " + characters;
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
		// TODO add animation code
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
	@Override
	public void draw(Canvas canvas, Paint paint, float left,
			float top, float width, float height) {
		int i = 0;
		float charWidth = width / characters.size();
		for(Character character: characters) {
			character.draw(canvas, paint, left + charWidth*i, top, charWidth, height);
			i++;
		}
	}

}
