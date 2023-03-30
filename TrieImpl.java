package edu.yu.cs.com1320.project.impl;
import edu.yu.cs.com1320.project.Trie;
import edu.yu.cs.com1320.project.Stack;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
public class TrieImpl<Value> implements Trie<Value> {
    private final int alphabetSize = 36; 
    private Node root;
    private class Node<Value>{
        private Set<Value>values=new HashSet<>();
        private Node[]links=new Node[36];
    }
    public TrieImpl(){
        this.root=new Node<>();
    }
    public void put(String key, Value val){
        if(key==null){
            throw new IllegalArgumentException();
        }
        this.root=put(root,key.toLowerCase(),val,0);
    }
    private Node put(Node x, String key, Value val, int d){
        if(x==null){
            x=new Node();
        }
        if(d==key.length()){
            x.values.add(val);
            return x;
        }
        int i=getIndex(key.charAt(d));
        x.links[i]=put(x.links[i],key,val,d+1);
        return x;
    }
    private int getIndex(Character c){
        if(Character.isDigit(c)){
            return (int)c-48;
        }
        else{
            return (int)c-87;
        }
    }
    // Remove the given value from the node of the given key (do not remove the value from other nodes in the Trie)
    // @param key
    // @param val
     //@return the value which was deleted. If the key did not contain the given value, return null.
    public Value delete(String key, Value val){  
        if(key==null || val==null){
            throw new IllegalArgumentException();
        }
        key=key.toLowerCase();
        List<Value> v=get(key);
        Node x=get(root,key,0);
        if(v==null||v.isEmpty()){
            return null;
        }
        if(!v.remove(val)){
            return null;
        }
        if(v.isEmpty()){
            deleteAll(key);
        }
        x.values.remove(val);
        return val;
    }
    /**
     * Delete the subtree rooted at the last character of the prefix.
     * Search is CASE INSENSITIVE.
     * @param prefix
     * @return a Set of all Values that were deleted.
     */
    public Set<Value> deleteAllWithPrefix(String prefix){
        if(prefix==null){
            throw new IllegalArgumentException();
        }
        prefix=prefix.toLowerCase();
        Set<String> allKeys=collectKeys(prefix);
        Set<Value> deletedValues=new HashSet();
        for(String key : allKeys){
            deletedValues.addAll(deleteAll(key));
        }
        return deletedValues;
    }
    public Set<Value> deleteAll(String key){
        if(key==null){
            throw new IllegalArgumentException();
        }
        key=key.toLowerCase();
        List<Value>vals=get(key);
        Set<Value>deleted=new HashSet();
        for(Value v : vals){
            deleted.add(v);
        }
        delete(key);
        return deleted;
    }
    //deleting a word from the trieImpl a
    private void delete(String Key){
        this.root=delete(root,Key,0);
    }
    private Node delete(Node x, String Key, int d){
        if(x==null){
            return null;
        }
        if(Key.length()==d){
            x.values.clear();
        }
        else{
            int i=getIndex(Key.charAt(d));
            x.links[i]=delete(x.links[i], Key, d+1);
        }
        if(x.values!=null&&!x.values.isEmpty()){
            return x;
        }
        for(int c=0; c<this.alphabetSize; c++){
            if(x.links[c]!=null){
                return x;
            }
        }
        return null;
    }
    public List<Value> getAllSorted(String key, Comparator<Value> comparator){
        if(comparator==null||key==null){
            throw new IllegalArgumentException();
        }
        key=key.toLowerCase();
        List<Value>vals=get(key);
        if(vals==null||vals.isEmpty()){
            return new ArrayList();
        }
        vals.sort(comparator);
        return vals;
    }
    //get a prefix and returns all the keys which has the prefix
    private Set<String> collectKeys(String prefix){
        Node x=get(root,prefix,0);
        if(x==null||prefix.equals("")){
            return new HashSet<>();
        }
        Set<String> keys=new HashSet();
        findKeys(x, keys, prefix);
        return keys;
    }
    //add to a set all the words that contain the prefix
    private void findKeys(Node x,Set <String> keys,String key){
        if(x==null){
            return;
        }
        if(!x.values.isEmpty()){
            keys.add(key);
        }
        Character c='0';
        for(int i=0; i<this.alphabetSize;i++){
            findKeys(x.links[i], keys, key+c);
            if(c=='9'){
                c='a';
            }
            else{
                c++;
            }
        }

    }
    /**
     * get all matches which contain a String with the given prefix, sorted in descending order.
     * For example, if the key is "Too", you would return any value that contains "Tool", "Too", "Tooth", "Toodle", etc.
     * Search is CASE INSENSITIVE.
     * @param prefix
     * @param comparator used to sort values
     * @return a List of all matching Values containing the given prefix, in descending order
     */
    public List<Value> getAllWithPrefixSorted(String prefix, Comparator<Value> comparator){
        if(comparator==null||prefix==null){
            throw new IllegalArgumentException();
        }
        prefix=prefix.toLowerCase();
        Node x=get(root,prefix,0);
        if(x==null||prefix.equals("")){
            return new ArrayList();
        }
        Set<Value>values=new HashSet<>();
        findValues(values, x);
        List<Value>vals=new ArrayList<>();
        for(Value v : values){
            vals.add(v);
        }

        vals.sort(comparator);
        return vals;
    }
    //adding to a list all values of node x and its descendants
    private void findValues(Set<Value>allVal,Node x){
        if(x==null){
            return;
        }
        if(!x.values.isEmpty()){
            allVal.addAll(x.values);
        }
        for(int i=0;i<this.alphabetSize;i++){
            findValues(allVal, x.links[i]);
        }
    }
    private List<Value> get(String key) {
        Node x = get(root, key, 0);
        if (x == null) {
            return new ArrayList<>();
        }
        List<Value>allVal=new ArrayList();
        allVal.addAll(x.values);
        return allVal;
    }
    private Node get(Node x, String key, int d){ // Return value associated with key in the subtrie rooted at x.
        if (x == null){
             return null;
        }
        if (d == key.length()){ 
            return x;
        }
        int i=getIndex(key.charAt(d));
        return get(x.links[i], key, d+1);
    }
}