package edu.yu.introtoalgs;
import java.util.*;

/** Defines the API for the WealthTransfer assignment: see the requirements
 * document for more information.
 *
 * Students MAY NOT change the constructor signature.  My test code will only
 * invoke the API defined below.
 *
 * @author Avraham Leff
 */

public class WealthTransfer {
  private int[][]parents; // first argument represents the parent, second one is proportion of
                            // wealth intended to be transferred to them, third one is 1 if squared and 0 if not
  private List<Integer>[] kids; // array of list of children of each node- not necessary
  private int[]given; // this array specifies the proportion of wealth that has been transferred by a parent- should be 100/0 at the end
  private boolean[][]comparison; // for each node, first line states whether it invoked "intendtotransfer",
                                 // second line states whether it invoked setRequiredWealth
  private Map<Integer,Set<Double>> minimumParentWealth; // key- parent, value- set of all minimum wealth required- not necessary
  private Map<Integer,Integer> wealthRequirements;
  private int size;
  private boolean[]marked;
  /** Constructor: specifies the size of the population.
   *
   * @param populationSize a positive integer specifying the number of people
   * in the population.  Members of the population are uniquely identified by
   * an integer 1..populationSize.  Initial wealth transfer must be initiated
   * by the person with id of "1".
   */
  public WealthTransfer(final int populationSize) {
    this.size=populationSize;
    this.wealthRequirements=new HashMap<>();
    this.parents=new int[populationSize][3];
    this.kids=new ArrayList[populationSize];
    for(int i=0; i<populationSize; i++) this.kids[i]=new ArrayList<>();
    this.given=new int[populationSize];
    this.comparison=new boolean[populationSize][2];
    this.minimumParentWealth=new HashMap<>();


    // fill me in
  } // constructor

  /** Specifies that one person want to make a wealth transfer to another
   * person.
   *
   * @param from specifies who is doing the wealth transfer, must correspond to
   * a valid population id
   * @param to specifies who is receiving the wealth transfer, must correspond
   * to a valid population id, and can't be identical to "from"
   * @param percentage the percentage of "from"'s wealth that will be
   * transferred to "to": must be an integer between 1..100
   * @param isWealthSquared if true, the wealth received is the square of the
   * money transferred
   * @throws IllegalArgumentException if the parameter Javadoc specifications
   * aren't satisfied or if this "from" has previously specified a wealth
   * transfer to this "to".
   */
  public void intendToTransferWealth(final int from, final int to, final int percentage, final boolean isWealthSquared) {
    if (from == to || percentage > 100 || percentage < 1 || from>this.size||from<1 ||to>this.size||to<2) {
      throw new IllegalArgumentException("Javadoc specifications aren't satisfied");
    }
    if (this.parents[to - 1][0] != 0) {
      throw new IllegalArgumentException("wealth transfer has been specified to " + to + " already");
    }
    if (this.comparison[from - 1][1]) {
      throw new IllegalArgumentException(from + " isn't allowed to transfer money");
    }
    if(this.given[from-1]+percentage>100){
      throw new IllegalArgumentException("Cannot transfer more than 100%");
    }
    this.parents[to-1][0]=from;
    this.parents[to-1][1]=percentage;
    this.parents[to-1][2]= isWealthSquared?1:0;
    this.kids[from-1].add(to);
    this.given[from-1]+=percentage;
    this.comparison[from-1][0]=true;
  }

  /** Specifies the wealth that the person must have in order for the overall
   * wealth transfer problem to be considered solved.
   *
   * @param id must correspond to a member of the population from 2..populationSize
   * @param wealth the wealth that the designated person must have as a result
   * of wealth transfers, must be positive.
   * @throw IllegalArgumentException if parameter Javadoc specifications aren't
   * met.
   */
  public void setRequiredWealth(final int id, final int wealth) {
    if(id<2||id>this.size||wealth<1) { throw new IllegalArgumentException("Javadoc specifications aren't satisfied");}
    if(this.comparison[id-1][0]||this.comparison[id-1][1]) {
      throw new IllegalArgumentException("required wealth can't be assigned to it");
    }
    this.wealthRequirements.put(id,wealth);
    this.comparison[id-1][1]=true;
  }

  /** Solves the wealth transfer problem by determining the MINIMAL amount of
   * wealth that "person with id of 1" must transfer such that all members of
   * the population receive the wealth that they have been promised.
   *
   * The amount of wealth that a person has been promised is specified by
   * invocations of setRequiredWealth().  The amount of wealth that a person
   * actually receives is specified by invocations of intendToTransferWealth().
   * The "person with id of 1" initiates all wealth transfers between members
   * of the population.  This method returns the minimum amount of that
   * initiating wealth transfer that will satisfy the remaining population's
   * needs.
   *
   * NOTE: at the time that this method is invoked, all persons transfering
   * wealth MUST be on record as intending to transfer 100 percent of their
   * wealth.  If this pre-condition doesn't hold, the implementation MUST throw
   * an IllegalStateException in lieu of solving the problem.
   *
   * @return the minimum amount transfered by person with id #1: must be
   * accurate to four digits after the decimal point.
   */
  public double solveIt() {
    for(int i=1; i<this.size; i++){
      if(this.given[i]!=0&&this.given[i]!=100){
        throw new IllegalStateException("node "+(i+1)+"didn't transfer all of his wealth");
      }
    }
    List<Double> results=new ArrayList<>();
    double max=0,temp;
    for(Integer n : this.wealthRequirements.keySet()){
      temp=calculatePath(n,this.wealthRequirements.get(n));
      if(temp>max)
        max=temp;
    }
    return max;
  }
  private double calculatePath(int ind,double amount){ // in method above, pass index+1
    if(ind==0){
      throw new IllegalArgumentException("this path doesn't receive money from 1");
    }
    if(ind==1){
      return amount;
    }
    double res;
    if(this.parents[ind-1][2]==1){
      res=Math.sqrt(amount);
    }
    else{
      res=amount;
    }
    double divisor=(double)this.parents[ind-1][1]/100;
    res=res/divisor;
    return calculatePath(this.parents[ind-1][0],res);
  }
} // class
