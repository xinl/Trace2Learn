package edu.upenn.cis350.Trace2Learn.Database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Word {
    private long id;
    long order;
    private Map<String, Set<String>> attributes;
    private Set<String> tags;
    private List<Character> characters;
    
    public Word(){
    	id = -1;
    	order = 0;
    	attributes = new HashMap<String, Set<String>>();
    	tags = new HashSet<String>();
	    characters = new ArrayList<Character>();
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
    
    public void addAttributes(String key, Set<String> values) {
    	attributes.put(key, new HashSet<String>(values));
    }
    
    public void removeAttribute(String key, String value) {
    	Set<String> values = this.attributes.get(key);
    	if (values == null) {
    		return;
    	}
    	values.remove(value);
    	if (values.isEmpty()) {
    		attributes.remove(key);
    	} else {
    		attributes.put(key, values);
    	}
    }
    
    public void removeAttributes(String key) {
    	attributes.remove(key);
    }
    
    public void setAttributes(Map<String, Set<String>> attributes) {
    	this.attributes = deepCopy(attributes);
    }
    
    public Map<String, Set<String>> getAttributes() {
	    return deepCopy(this.attributes);
    }
    
    public void addTag(String tag){
	    this.tags.add(tag);
    }
    
    public void removeTag(String tag) {
    	this.tags.remove(tag);
    }

    public Set<String> getTags(){
	    return new HashSet<String>(this.tags);
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
    
    @Override
    public boolean equals(Object o) {
    	if (o == null) return false;
    	if (o == this) return true;
    	if (!(o instanceof Word)) return false;
    	Word that = (Word) o;
    	if (this.id != that.id) return false;
    	if (this.order != that.order) return false;
    	if (characters.size() != that.characters.size()) return false;
    	for (int i = 0; i < characters.size(); i++) {
    		if (this.characters.get(i).getId() !=
    				that.characters.get(i).getId()) {
    			return false;
    		}
    	}
    	if (!this.tags.equals(that.tags)) return false;
    	if (!this.attributes.equals(that.attributes)) return false;
    	return true;
    }
    
    @Override
    public String toString() {
    	return "\nWORD\tTags: " + tags.toString() + "\t" +
               "Attributes: " + attributes.toString() + "\t" +
    		   "Id: " + id + "\t" + "Order: " + order + "\t" +
    		   "Characters: " + characters.toString();
    }
    
    private Map<String, Set<String>> deepCopy(Map<String, Set<String>> original) {
    	Map<String, Set<String>> newMap = new HashMap<String, Set<String>>();
    	for (String key : original.keySet()) {
    		newMap.put(key, new HashSet<String>(original.get(key)));
    	}
    	return newMap;
    }
}
