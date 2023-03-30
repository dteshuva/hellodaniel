package edu.yu.cs.com1320.project.impl;

import java.io.IOException;
import java.util.Arrays;

import edu.yu.cs.com1320.project.BTree;
import edu.yu.cs.com1320.project.stage5.PersistenceManager;

public class BTreeImpl<Key extends Comparable<Key>,Value> implements BTree<Key,Value> {

        private static final int MAX = 4;
        private Node root; //root of the B-tree
        private Node leftMostExternalNode;
        private int height; //height of the B-tree
        private int n; //number of key-value pairs
        private PersistenceManager<Key, Value> manager;
        private Value v;

        public BTreeImpl() {
            this.root = new Node(0);
            this.leftMostExternalNode = this.root;
            this.height = 0;
            this.n = 0;
            this.manager = null;
        }
        private static final class Node
        {
            private int entryCount; // number of entries
            private Entry[] entries = new Entry[BTreeImpl.MAX];
            private Node next;
            private Node previous;
    
            // create a node with k entries
            Node(int k) {
                this.entryCount = k;
                this.entries = new Entry[MAX];
            }
    
            private void setNext(Node next)
            {
                this.next = next;
            }
            private Node getNext()
            {
                return this.next;
            }
            private void setPrevious(Node previous)
            {
                this.previous = previous;
            }
    
        }
        private static class Entry<Key extends Comparable, Value>
        {
            private Comparable key;
            private Object val;
            private Node child;
            private boolean onMemory;
    
            private Entry(Comparable key, Object val, Node child)
            {
                this.key = key;
                this.val = val;
                this.child = child;
                onMemory=true;
            }
            private void setStatus(boolean f){
                onMemory=f;
            }
        }
    
    public Value get(Key k) {
        if (k == null) {
            throw new IllegalArgumentException();
        }
        Entry entry = this.get(this.root, k, this.height);
        if(entry != null) {
            if (entry.val != null &&entry.val!=this.v) {
                this.deleteFile(k);  //will only delete a file if was written to disk previously- optional
                return (Value) entry.val;
             } else {
                if(entry.onMemory==true){
                    return null;}
                Value val = null;
            if(entry.val==this.v){
                try {
                    val = this.manager.deserialize(k);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                 }
                 if(val!=null)
                    entry.setStatus(true);
            entry.val = val;
           this.deleteFile(k);
            return (Value) val;
            }
         }
		return null;

    }
    private void deleteFile(Key k){
        try {
            this.manager.delete(k);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Entry get(Node currentNode, Key key, int height)
    {
        Entry[] entries = currentNode.entries;

        //current node is external (i.e. height == 0)
        if (height == 0)
        {
            for (int j = 0; j < currentNode.entryCount; j++)
            {
                if(isEqual(key, entries[j].key))
                {
                    //found desired key. Return its value
                    return entries[j];
                }
            }
            //didn't find the key
            return null;
        }

        //current node is internal (height > 0)
        else
        {
            for (int j = 0; j < currentNode.entryCount; j++)
            {
                //if (we are at the last key in this node OR the key we
                //are looking for is less than the next key, i.e. the
                //desired key must be in the subtree below the current entry),
                //then recurse into the current entry’s child
                if (j + 1 == currentNode.entryCount || less(key, entries[j + 1].key))
                {
                    return this.get(entries[j].child, key, height - 1);
                }
            }
            //didn't find the key
            return null;
        }
    }


    public Value put(Key k, Value v)
    {
        if (k == null)
        {
            throw new IllegalArgumentException("argument key to put() is null");
        }
        //if the key already exists in the b-tree, simply replace the value
         Entry<Key,Value> existEntry = (Entry<Key,Value>) this.get(this.root, k, this.height);
        if(existEntry != null) {
            try {
                if (existEntry.val != null) {
                    Value oldValue = (Value)existEntry.val;
                    existEntry.val = v;
                    existEntry.setStatus(true);
                    return oldValue;
                }
                existEntry.val =v;
                if(existEntry.onMemory==false){
                    existEntry.setStatus(true);
                return this.manager.deserialize((Key) existEntry.key); }
                return null;
             } catch (IOException e) {
                e.printStackTrace();
             }
        }
        return newNode(k, v);
    }
    private Value newNode(Key k, Value v){
        Node newNode = this.put(this.root, k, v, this.height);
        this.n++;
        if (newNode == null)
        {
            return null;
        }

        //split the root:
        //Create a new node to be the root.
        //Set the old root to be new root's first entry.
        //Set the node returned from the call to put to be new root's second entry
        Node newRoot = new Node(2);
        newRoot.entries[0] = new Entry(this.root.entries[0].key, null, this.root);
        newRoot.entries[1] = new Entry(newNode.entries[0].key, null, newNode);
        this.root = newRoot;
        //a split at the root always increases the tree height by 1
        this.height++;
        return null;
    }

    /**
     *
     * @param currentNode
     * @param key
     * @param val
     * @param height
     * @return null if no new node was created (i.e. just added a new Entry into an existing node). If a new node was created due to the need to split, returns the new node
     */
    private Node put(Node currentNode, Key key, Value val, int height)
    {
        int j;
        Entry newEntry = new Entry(key, val, null);

        //external node
        if (height == 0)
        {
            //find index in currentNode’s entry[] to insert new entry
            //we look for key < entry.key since we want to leave j
            //pointing to the slot to insert the new entry, hence we want to find
            //the first entry in the current node that key is LESS THAN
            for (j = 0; j < currentNode.entryCount; j++)
            {
                if (less(key, currentNode.entries[j].key))
                {
                    break;
                }
            }
        }

        // internal node
        else
        {
            //find index in node entry array to insert the new entry
            for (j = 0; j < currentNode.entryCount; j++)
            {
                //if (we are at the last key in this node OR the key we
                //are looking for is less than the next key, i.e. the
                //desired key must be added to the subtree below the current entry),
                //then do a recursive call to put on the current entry’s child
                if ((j + 1 == currentNode.entryCount) || less(key, currentNode.entries[j + 1].key))
                {
                    //increment j (j++) after the call so that a new entry created by a split
                    //will be inserted in the next slot
                    Node newNode = this.put(currentNode.entries[j++].child, key, val, height - 1);
                    if (newNode == null)
                    {
                        return null;
                    }
                    //if the call to put returned a node, it means I need to add a new entry to
                    //the current node
                    newEntry.key = newNode.entries[0].key;
                    newEntry.val = null;
                    newEntry.child = newNode;
                    break;
                }
            }
        }
        //shift entries over one place to make room for new entry
        for (int i = currentNode.entryCount; i > j; i--)
        {
            currentNode.entries[i] = currentNode.entries[i - 1];
        }
        //add new entry
        currentNode.entries[j] = newEntry;
        currentNode.entryCount++;
        if (currentNode.entryCount < BTreeImpl.MAX)
        {
            //no structural changes needed in the tree
            //so just return null
            return null;
        }
        else
        {
            //will have to create new entry in the parent due
            //to the split, so return the new node, which is
            //the node for which the new entry will be created
            return this.split(currentNode, height);
        }
    }

    /**
     * split node in half
     * @param currentNode
     * @return new node
     */
    private Node split(Node currentNode, int height)
    {
        Node newNode = new Node(BTreeImpl.MAX / 2);
        //by changing currentNode.entryCount, we will treat any value
        //at index higher than the new currentNode.entryCount as if
        //it doesn't exist
        currentNode.entryCount = BTreeImpl.MAX / 2;
        //copy top half of h into t
        for (int j = 0; j < BTreeImpl.MAX / 2; j++)
        {
            newNode.entries[j] = currentNode.entries[BTreeImpl.MAX / 2 + j];
        }
        //external node
        if (height == 0)
        {
            newNode.setNext(currentNode.getNext());
            newNode.setPrevious(currentNode);
            currentNode.setNext(newNode);
        }
        return newNode;
    }

    // comparison functions - make Comparable instead of Key to avoid casts
    private static boolean less(Comparable k1, Comparable k2)
    {
        return k1.compareTo(k2) < 0;
    }

    private static boolean isEqual(Comparable k1, Comparable k2)
    {
        return k1.compareTo(k2) == 0;
    }

    public void moveToDisk(Key k) throws Exception {
        Value val = get(k);
        Entry entry=this.get(this.root, k, this.height);
        if(entry==null||val==null){
            return;
        }
        if(entry.onMemory==false){
            return;
        }
        manager.serialize(k,val);
      //  put(k, val);
      put(k,this.v);
      entry.setStatus(false);

    }
    public void setPersistenceManager(PersistenceManager<Key,Value> pm) {
        this.manager = pm;
    }

}
