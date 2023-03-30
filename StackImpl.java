package edu.yu.cs.com1320.project.impl;
import edu.yu.cs.com1320.project.Stack;
public class StackImpl<T> implements Stack<T>{
    class Node<T>{
        T value;
        Node next;
        Node(T val){
            this.value=val;
            this.next=null;
        }
    }
    private Node head;
    private int size;
    public StackImpl(){
        this.head=null;
        this.size=0;
    }
    public void push(T element){
        if(element==null){
            throw new IllegalArgumentException();
        }
        Node link=new Node(element);
        if(this.size!=0){
            link.next=this.head;
        }
        this.head=link;
        size++;
    }

    /**
     * removes and returns element at the top of the stack
     * @return element at the top of the stack, null if the stack is empty
     */
    public T pop(){
        if(this.size==0){
            return null;
        }
        T prev=(T)this.head.value;
        this.head=this.head.next;
        size--;
        return prev;
    }

    /**
     *
     * @return the element at the top of the stack without removing it
     */
    public T peek(){
        if(this.size==0){
            return null;
        }
        return (T)this.head.value;
    }

    /**
     *
     * @return how many elements are currently in the stack
     */
    public int size(){
        return this.size;
    }
}