package edu.upenn.cis350.Trace2Learn.Database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    
    public void removeAttribute(String key, String value) {
    	Set<String> values = this.attributes.get(key);
    	if (values == null) {
    		return;
    	}
    	values.remove(value);
    	attributes.put(key, values);
    }
    
    public void setAttributes(Map<String, Set<String>> attributes) {
    	this.attributes = attributes;
    }
    
    public Map<String, Set<String>> getAttributes() {
	    return this.attributes;
    }
    
    public void addTag(String tag){
	    this.tags.add(tag);
    }
    
    public void removeTag(String tag) {
    	this.tags.remove(tag);
    }

    public Set<String> getTags(){
	    return this.tags;
    }
    
    public void addStroke(Stroke s){
	    this.strokes.add(s);
    }
    
    public void setStrokes(List<Stroke> strokes) {
    	this.strokes = strokes;
    }
    
    public List<Stroke> getStrokes(){
	    return this.strokes;
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

}

