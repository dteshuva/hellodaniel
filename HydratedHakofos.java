package edu.yu.introtoalgs;

/** Defines the API for the HydratedHakofos assignment: see the requirements
 * document for more information.
 *
 * Students MAY NOT change the constructor signature.  My test code will only
 * invoke the API defined below.
 *
 * @author Avraham Leff
 */

public class HydratedHakofos extends BigOMeasurable {
  private int sum;
  private int[]available;
  private int[]required;
  public HydratedHakofos() {
    this.sum=0;
  }

  /** Determines whether or not a table exists such that hakofos, beginning
   * from this table, can successfully traverse the entire circuit without
   * getting dehydrated ("negative water level").  If multiple tables are
   * valid, return the lowest-numbered table (numbered 1..n).  Otherwise,
   * return the sentinel value of "-1".
   *
   * @param waterAvailable an array, indexed from 0..n-1, such that the ith
   * element represents the amount of water available at the ith table.
   * @param waterRequired an array, indexed from 0..n-1, such that the ith
   * element represents the amount of water required to travel from the ith
   * table to the next table.
   * @return a number from 1..n or -1 as appropriate.
   *
   * NOTE: if the client supplies arrays of differing lengths, or if the arrays
   * are null, or empty, or if the client supplies non-positive values in
   * either of these arrays, the result is undefined.  In other words: you
   * don't have to check for these conditions (unless you want to prevent
   * errors during development).
   */
  public int doIt(int[] waterAvailable, int[] waterRequired) {
      if(!isPossible(waterAvailable, waterRequired)){
        return -1;
      }
      int index,count=0;
      for(int i=0; i<waterAvailable.length; i++){ // when implementing changes, dont forget that 
      if(waterAvailable[i]<waterRequired[i])   //// you increase i by what you want minus 1
        continue;
        index=i;
        count=0;
        this.sum=0;
        while(count<waterAvailable.length){ // you may change the while to a for lood
          sum+=(waterAvailable[index]-waterRequired[index]);
          if(sum<0)
          break;
          index++;
          count++;
          if(index==waterAvailable.length)
          index=0;
        }
        if(count==waterAvailable.length){
          return (i+1);
        }
      }
      
      return -1;
  } // doIt

  protected boolean isPossible(int[]waterAvailable,int[] waterRequired){
    int sumA=0,sumR=0;
    for(int i=0; i<waterAvailable.length; i++){
      sumA+=waterAvailable[i];
      sumR+=waterRequired[i];
    }
    return (sumA>=sumR);

  }
  public void setup(int n){
    this.available=new int[n];
    this.required=new int[n];
    int random;
    for(int i=0; i<n; i++){
      random=(int)(Math.random()*100);
      available[i]=random;
      random=(int)(Math.random()*100);
      required[i]=random;
  }

  }

  @Override
  public void execute() {
    int num=this.doIt(this.available, this.required);
   // System.out.println(available.length+" , "+num);
    
  }

}
