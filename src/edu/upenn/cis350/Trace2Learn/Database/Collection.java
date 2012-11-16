package edu.upenn.cis350.Trace2Learn.Database;


import java.util.ArrayList;
import java.util.List;

public class Collection {
    private long id;
    private long order;
    private String name;
    private String description;
    private List<Word> words;
    
    public Collection(){
	    id = -1;
	    order = -1;
	    name = null;
	    description = null;
	    words = new ArrayList<Word>() ;
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
    
    public void setWords(List<Word> words) {
    	this.words = new ArrayList<Word>(words);
    }
    
    public List<Word> getWords(){
	    return new ArrayList<Word>(this.words);
    }
    
    public void removeWord(int location){
    	this.words.remove(location);
    }
    
    public List<Long> getWordIds() {
		List<Long> ids = new ArrayList<Long>();
		if (words != null) {
			for (Word c : words){
				ids.add(c.getId());
			}
		}
		return ids;
	}
    
    public int size() {
    	return words.size();
    }
    
    @Override
    public boolean equals(Object o) {
    	if (o == null) return false;
    	if (o == this) return true;
    	if (!(o instanceof Collection)) return false;
    	Collection that = (Collection) o;
    	if (this.id != that.id) return false;
    	if (this.order != that.order) return false;
    	if (words.size() != that.words.size()) return false;
    	for (int i = 0; i < words.size(); i++) {
    		if (this.words.get(i).getId() != that.words.get(i).getId()) {
    			return false;
    		}
    	}
    	return true;
    }
    
    @Override
    public String toString() {
    	return "\nCOLLECTION\tName: " + name + "\t" +
               "Description: " + description + "\t" +
    		   "Id: " + id + "\t" + "Order: " + order +
    		   "\tWords: " + words;
    }
}

