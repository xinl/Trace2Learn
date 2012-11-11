package edu.upenn.cis350.Trace2Learn.Database;

import java.util.ArrayList;
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
    	attributes = null;
    	tags = null;
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
    
    public long getOrder(){
	    return this.order;
    }
    
    public void addAttribute(String key, String value){
	    Set<String> values = this.attributes.get(key);
	    values.add(value);
	    attributes.put(key, values);
    }
    
    public Map<String, Set<String>> getAttributes(){
	    return this.attributes;
    }
    
    public void addTag(String tag){
	    this.tags.add(tag);
    }
    

    public Set<String> getTags(){
	    return this.tags;
    }
    
    public void addStroke(Stroke s){
	    this.strokes.add(s);
    }
    
    public List<Stroke> getStrokes(){
	    return this.strokes;
    }

}

