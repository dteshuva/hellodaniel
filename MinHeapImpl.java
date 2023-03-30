package edu.yu.cs.com1320.project.impl;
import edu.yu.cs.com1320.project.MinHeap;
import edu.yu.cs.com1320.project.impl.BTreeImpl;
public class MinHeapImpl<E extends Comparable<E>> extends MinHeap<E>{
    public MinHeapImpl(){
        this.elements=(E[]) new Comparable[1];
    } 
    public MinHeapImpl(BTreeImpl table){
        this.elements=(E[]) new Comparable[1];
        this.table=table;
    }

    @Override
    public void reHeapify(E element) {
        this.upHeap(getArrayIndex(element));
        this.downHeap(getArrayIndex(element));
    }
    @Override  // see what happens if element isn't in the heap or in the heap more than once
    protected int getArrayIndex(E element) {
        for(int i=1;i<=this.count;i++){
            if(element.equals(elements[i])){
                return i;
            }
        }
        return -1;
    }
    @Override
    protected void doubleArraySize() {
        E[] temp =(E[]) new Comparable[this.count!=0 ? this.elements.length*2 : 2];
        if(temp.length!=2){
            for (int i = 0; i <= this.count; i++) {
               temp[i] = this.elements[i];
            }
        }
        this.elements = temp;
    }
    
}