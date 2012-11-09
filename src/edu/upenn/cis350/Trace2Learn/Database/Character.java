package edu.upenn.cis350.Trace2Learn.Database;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Character {
    private long id;
    private Map<String, String> names;
    private List<String> tags = new ArrayList<String>();
    private List<Stroke> strokes = new ArrayList<Stroke>();
    
    public Character(){
    }
    
    public void setId(long id){
	this.id = id;
    }
    
    public long getId(){
	return this.id;
    }
    
    public void addName(String key, String value){
	this.names.put(key, value);
    }
    
    public Map<String, String> getNames(){
	return this.names;
    }
    
    public void addTag(String tag){
	this.tags.add(tag);
    }
    
    public List<String> getTags(){
	return this.tags;
    }
    
    public void addStroke(Stroke s){
	this.strokes.add(s);
    }
    
    public List<Stroke> getStrokes(){
	return this.strokes;
    }
    
}
