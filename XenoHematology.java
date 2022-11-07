package edu.yu.introtoalgs;

import java.util.*;

/** Defines the API for the XenoHematology assignment: see the requirements
 * document for more information.
 *
 * Students MAY NOT change the constructor signature.  My test code will only
 * invoke the API defined below.
 *
 * @author Avraham Leff
 */

public class XenoHematology extends BigOMeasurable{
   private int[] id; // parent link (site indexed)
   private int[] sz; // size of component for roots (site indexed)
   private int size; // number of components
   private HashMap<Integer,HashSet<Integer>> incomp; //

  /** Constructor: specifies the size of the xeno population.
   *
   * @param populationSize a non-negative integer specifying the number of
   * aliens in the xeno population.  Members of the population are uniquely
   * identified by an integer 0..populationSize -1.
   */
  public XenoHematology(){
    this.size=10;
  }
  public XenoHematology(final int populationSize) {
    if(populationSize<0){
      throw new IllegalArgumentException("Population size cannot be negative");
    }
    this.id = new int[populationSize];
   for (int i = 0; i < populationSize; i++) id[i] = i;
    this.sz = new int[populationSize];
    for (int i = 0; i < populationSize; i++) sz[i] = 1;
    this.incomp=new HashMap<>();
    for(int i=0; i< populationSize; i++) incomp.put(i, new HashSet<>());
    this.size=populationSize;
    // fill me in!
  } // constructor

  /** Specifies that xeno1 and xeno2 are incompatible.  Once specified
   * as incompatible, the pair can never be specified as being
   * "compatible".  In that case, don't throw an exception, simply
   * treat the method invocation as a "no-op".  A xeno is always
   * compatible with itself, is never incompatible with itself:
   * directives to the contrary should be treated as "no-op"
   * operations.
   *
   * Both parameters must correspond to a member of the population.
   *
   * @param xeno1 non-negative integer that uniquely specifies a member of the
   * xeno population, differs from xeno2
   * @param xeno2 non-negative integer that uniquely specifies a member of the
   * xeno population.
   * @throws IllegalArgumentException if the supplied values are
   * incompatible with the @param Javadoc.
   */
  public void setIncompatible(int xeno1, int xeno2) {
    if(xeno1<0||xeno2<0 || xeno1>=this.size ||xeno2>=this.size){
      throw new IllegalArgumentException("Parameters didn't match to the requirements");
    }
    if(xeno1==xeno2) {return ;}
    if(areCompatible(xeno1, xeno2)){ return; }
    int r1=find(xeno1),r2=find(xeno2);
    this.incomp.get(r1).add(xeno2);
    this.incomp.get(r2).add(xeno1);
    Set<Integer> notComp1=this.incomp.get(r1);
    Set<Integer> notComp2=this.incomp.get(r2);
    for(int n : notComp1) this.setCompatible(xeno2, n);
    for(int n : notComp2) this.setCompatible(xeno1, n);
     // 1 incomp with 2-10
    // 11 incomp with 12-22
    //if compative you return, else you add each one to the other set and add following conditions
    // fill me in!
  }

  /** Specifies that xeno1 and xeno2 are compatible.  Once specified
   * as compatible, the pair can never be specified as being
   * "incompatible".  In that case, don't throw an exception, simply
   * treat the method invocation as a "no-op".  A xeno is always
   * compatible with itself, is never incompatible with itself:
   * directives to the contrary should be treated as "no-op"
   * operations.
   *
   * Both parameters must correspond to a member of the population.
   *
   * @param xeno1 non-negative integer that uniquely specifies a member of the
   * xeno population.
   * @param xeno2 non-negative integer that uniquely specifies a member of the
   * xeno population
   * @throws IllegalArgumentException if the supplied values are
   * incompatible with the @param Javadoc.
   */
  public void setCompatible(int xeno1, int xeno2) { //o(n) worst case
    if(xeno1<0||xeno2<0 || xeno1>=this.size ||xeno2>=this.size){
      throw new IllegalArgumentException("Parameters didn't match to the requirements");
    }
    if(xeno1==xeno2) return;
    if(areIncompatible(xeno1, xeno2)){return;}
    int r1=find(xeno1),r2=find(xeno2),root,other;
    if(r1==r2) return;
    if(sz[r1]<sz[r2]){
      id[r1]=r2;
      sz[r2]+=sz[r1];
      root=r2;
      other=r1;
    }
    else{
      root=r1;
      other=r2;
      id[r2]=r1;
      sz[r1]+=sz[r2];
    }
   
    if(this.incomp.get(root).size()>=this.incomp.get(other).size()){ 
      this.incomp.get(root).addAll(this.incomp.get(other));
      this.incomp.remove(other);
    }
    else{
      HashSet<Integer> temp=this.incomp.get(other);
      temp.addAll(this.incomp.get(root));
      this.incomp.put(root, temp);
      this.incomp.remove(other);
    }
  }

  /** Returns true iff xeno1 and xeno2 are compatible from a hematology
   * perspective, false otherwise (including if we don't know one way or the
   * other).  Both parameters must correspond to a member of the population.
   *
   * @param xeno1 non-negative integer that uniquely specifies a member of the
   * xeno population, differs from xeno2
   * @param xeno2 non-negative integer that uniquely specifies a member of the
   * xeno population
   * @return true iff compatible, false otherwise
   * @throws IllegalArgumentException if the supplied values are
   * incompatible with the @param Javadoc
   */
  public boolean areCompatible(int xeno1, int xeno2) { //almost o(1)
    if(xeno1<0||xeno2<0 || xeno1>=this.size ||xeno2>=this.size){
      throw new IllegalArgumentException("Parameters didn't match to the requirements");
    }
    if(xeno1==xeno2) return true;
    return find(xeno1)==find(xeno2);
  }

  /** Returns true iff xeno1 and xeno2 are incompatible from a hematology
   * perspective, false otherwise (including if we don't know one way or the
   * other).  Both parameters must correspond to a member of the population.
   *
   * @param xeno1 non-negative integer that uniquely specifies a member of the
   * xeno population, differs from xeno2
   * @param xeno2 non-negative integer that uniquely specifies a member of the
   * xeno population
   * @return true iff compatible, false otherwise
   * @throws IllegalArgumentException if the supplied values are
   * incompatible with the @param Javadoc.
   */
  public boolean areIncompatible(int xeno1, int xeno2) { //roughly o(1)
    if(xeno1<0||xeno2<0 || xeno1>=this.size ||xeno2>=this.size){
      throw new IllegalArgumentException("Parameters didn't match to the requirements");
    }
    if(xeno1==xeno2) return false;
    int r1=find(xeno1),r2=find(xeno2);
    if(r1==r2) return false;
    if(this.incomp.get(r1).contains(r2) || this.incomp.get(r2).contains(r1)){
      this.incomp.get(r1).add(r2);
      this.incomp.get(r2).add(r1);
      return true;
    } 
    return false;               // replace to taste!
  }
  private int find(int p) // almost o(1)
 { // Follow links to find a root.
 while (p != id[p]){
  id[p]=id[id[p]];
  p = id[p];
 }
 return p;
 }
 // 5- 11 1
 @Override
  public void setup(int populationSize){
     this.id = new int[populationSize];
   for (int i = 0; i < populationSize; i++) id[i] = i;
    this.sz = new int[populationSize];
    for (int i = 0; i < populationSize; i++) sz[i] = 1;
    this.incomp=new HashMap<>();
    for(int i=0; i< populationSize; i++) incomp.put(i, new HashSet<>());
    this.size=populationSize;
     int ind=this.size/2;
     for(int i=0; i<ind; i++) this.setIncompatible(ind, i);
     for(int i=ind+2; i<this.size; i++) this.setIncompatible(ind+1, i);

  }
  @Override
  public void execute() {
     
    this.setIncompatible(this.size/2-5, 3+this.size/2);
  }
 
 // 1 - 2 3 5 7 8
 // 2 - 3 7 9
 // 2 - 2 3 5 7 8 9
} // XenoHematology
