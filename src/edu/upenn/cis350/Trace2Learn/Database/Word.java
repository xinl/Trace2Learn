package edu.upenn.cis350.Trace2Learn.Database;

import java.util.Map;
import java.util.Set;

public class Word {
    private long id;
    private Map<String, Set<String>> attributes;
    private Set<String> tags;
    private Set<Character> characters;
    
    public Word(){
	    id = -1;
	    attributes = null;
	    tags = null;
	    characters = null;
    }
    
    public void setId(long id){
	    this.id = id;
    }
    
    public long getId(){
	    return this.id;
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

    public void addCharacter(Character c){
    	this.characters.add(c);
    }

    public Set<Character> getCharacters(){
    	return this.characters;
    }
}
