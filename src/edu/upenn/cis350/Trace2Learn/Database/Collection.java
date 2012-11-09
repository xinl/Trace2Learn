package edu.upenn.cis350.Trace2Learn.Database;

import java.util.List;

public class Collection {
    long id;
    List<Long> wordIds;
    String name;
    String description;
    
    public Collection(){
    }
    
    public void setId(long id){
	this.id = id;
    }
    
    public long getId(){
	return this.id;
    }
    
    public void addWordId(long id){
	this.wordIds.add(id);
    }
    
    public List<Long> getWordIds(){
	return this.wordIds;
    }
    
    public void setName(String name){
	this.name = name;
    }
    
    public String getName(){
	return this.name;
    }
    
    public void setDescription(String description){
	this.description = description;
    }
    
    public String getDescription(){
	return this.description;
    }
}
