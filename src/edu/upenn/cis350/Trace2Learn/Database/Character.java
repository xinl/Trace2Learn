package edu.upenn.cis350.Trace2Learn.Database;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import android.util.Pair;

public class Character {
    private long id;
    private Set<Pair<String, String>> attributes;
    private Set<String> tags;
    private List<Stroke> strokes = new ArrayList<Stroke>();
    
    public Character(){
    }
    
    public void setId(long id){
	this.id = id;
    }
    
    public long getId(){
	return this.id;
    }
    
    public void addAttribute(String key, String value){
	Pair<String, String> attribute = new Pair<String, String>(key, value);
	attributes.add(attribute);
    }
    
    public Set<Pair<String, String>> getAttributes(){
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