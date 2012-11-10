package edu.upenn.cis350.Trace2Learn.Database;

import java.util.List;
import java.util.Set;
import android.util.Pair;

public class Word {
    private long id;
    private List<Long> charIds;
    private Set<Pair<String, String>> attributes;
    private Set<String> tags;
    
    public Word(){
    }
    
    public void setId(long id){
	this.id = id;
    }
    
    public long getId(){
	return this.id;
    }
    
    public void addCharId(long id){
	this.charIds.add(id);
    }
    
    public List<Long> getCharIds(){
	return this.charIds;
    }
    
    public void addAttribute(String key, String value){
	Pair<String, String> attribute = new Pair<String, String>(key, value);
	this.attributes.add(attribute);
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
}