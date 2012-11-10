package edu.upenn.cis350.Trace2Learn.Database;

import java.util.Set;

public class Collection {
    private long id;
    private String name;
    private String description;
    private Set<Word> words;
    
    public Collection(){
	id = -1;
	name = null;
	description = null;
	words = null;
    }
    
    public void setId(long id){
	this.id = id;
    }
    
    public long getId(){
	return this.id;
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
    
    public void addWord(Word w){
	this.words.add(w);
    }
    
    public Set<Word> getWords(){
	return this.words;
    }
}