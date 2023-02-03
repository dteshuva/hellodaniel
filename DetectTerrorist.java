package edu.yu.da;

/** Defines the API for specifying and solving the DetectTerrorist problem (see
 * the requirements document).
 *
 * Students MAY NOT change the public API of this class, nor may they add ANY
 * constructor.
 *
 * @author Avraham Leff
 */

public class DetectTerrorist {

  /** Constructor: represents passengers to be detected as an array in which
   * the ith value is the weight of the ith passenger.  After the constructor
   * completes, clients can invoke getTerrorist() for a O(1) lookup cost.
   *
   * @param passengers an array of passenger weights, indexed 0..n-1.  All
   * passengers that are not terrorists have the same weight: that weight is
   * greater than the weight of the terrorist.  Exactly one passenger is a
   * terrorist.
   */
  // 5 5 5 5 2 5 5 5 5 5
  private int res=-1;
  public DetectTerrorist(final int[] passengers) {
    // fill me in!
    if(passengers==null)
      throw new IllegalArgumentException();
    if(passengers.length==1){
      res=0;
      return;
    }
    if(passengers.length==2||passengers.length==0)
      throw new IllegalArgumentException("terrorist cannot be found");
    this.res=detection(passengers,0,passengers.length-1);
  }   // constructor
  private int detection(int [ ] passengers,int start, int end){
    int sum1,sum2;
    if(end-start==2){
      return solveThree(passengers[start],passengers[start+1],passengers[end],start,start+1,end);
    }
    if(end-start==3){
      return solveFour(passengers[start],passengers[start+1],passengers[end-1], passengers[end],start,start+1,end-1,end);
    }
    sum1=passengers[0]+passengers[1];
    for(int i=0; i<passengers.length-3; i+=2){
      sum2=passengers[i+2]+passengers[i+3];
      if(sum1!=sum2)
        return solveFour(passengers[i], passengers[i+1],passengers[i+2],passengers[i+3],i,i+1,i+2,i+3);
      sum1=sum2;
    }
    return passengers.length-1;
  }

  /*
  Function gets 3 integers and their corresponding indices.
  Function return the index of the number which is different than the other 2
   */
  private int solveThree(int a, int b, int c,int indA,int indB,int indC){
    int sum1=a+b,sum2=b+c,sum3=a+c;
    if(sum1==sum2&&sum3==sum1)
      return -1;
    if(sum1==sum2&&sum3>sum1)
      return indB;
    if(sum2>sum3)
      return indA;
    return indC;
  }
  /*
  Function gets 4 integers and their corresponding indices.
  Function return the index of the number which is different than the other 3
   */
  private int solveFour(int a, int b, int c,int d,int indA,int indB,int indC,int indD){
    int sum1=a+b,sum2=c+d,sum3=b+c;
    if(sum1==sum2&&sum3==sum1)
      return -1;
    if(sum3>sum1)
      return indA;
    if(sum3<sum1)
      return indC;
    if(sum3<sum2)
      return indB;
    return indD;
  }
  /** Returns the index of the passenger who has been determined to be a
   * terrorist.
   *
   * @return the index of the terrorist element.
   */
  public int getTerrorist() {
    return res;
  }

} // DetectTerrorist
