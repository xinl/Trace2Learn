package edu.upenn.cis350.Trace2Learn.Database;

import java.util.List;
import java.util.Map;

public class Word {
    long id;
    List<Long> charIds;
    Map<String, String> names;
    List<String> tags;
    
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
}