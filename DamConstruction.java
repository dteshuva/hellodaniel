package edu.yu.da;

import java.util.Arrays;

/** Defines the API for specifying and solving the DamConstruction problem (see
 * the requirements document).
 *
 * Students MAY NOT change the public API of this class, nor may they add ANY
 * constructor.
 *
 * @author Avraham Leff
 */

public class DamConstruction {
  private final int totalDams;
  private final int[] dams;
  private final int riverEnd;
  /** Constructor
   *
   * @param Y y-positions specifying dam locations, sorted by ascending
   * y-values.  Client maintains ownership of this parameter.  Y must contain
   * at least one element.
   * @param riverEnd the y-position of the river's end (a dam was previously
   * constructed both at this position and at position 0 and no evaluation will be
   * made of their construction cost): all values in Y are both greater than 0
   * and less than riverEnd.
   * @note students need not verify correctness of either parameter.  On the
   * other hand, for your own sake, I suggest that you add these (easy to do)
   * "sanity checks".
   */
  public DamConstruction(final int Y[], final int riverEnd) {
    // fill me in to taste
    if(Y == null || Y.length == 0 || riverEnd <=0)
        throw new IllegalArgumentException();
    totalDams = Y.length + 2;
    this.riverEnd=riverEnd;
    dams = new int[Y.length + 2];
    dams[0] = 0;
    dams[totalDams - 1] = riverEnd;
    int temp = 0;
    for (int i = 0; i < Y.length; i++) {
      if (Y[i] <= temp) {
        throw new IllegalArgumentException();
      }
      dams[i + 1] = Y[i];
      temp = Y[i];
    }
  } // constructor

  /** Solves the DamConstruction problem, returning the minimum possible cost
   * of evaluating the environmental impact of dam construction over all
   * possible construction sequences.
   *
   * @return the minimum possible evaluation cost.
   */

  public int solve(){
    int[][] dp = new int[totalDams][totalDams];
    for(int i= dams.length-1; i >= 0; --i){
      for(int j= i + 1; j < dams.length; ++j){
        for(int k= i + 1; k < j; ++k){
          dp[i][j] = Math.min(dp[i][j] == 0 ? Integer.MAX_VALUE : dp[i][j],
                  dams[j] - dams[i] + dp[i][k] + dp[k][j]);
        }
      }
    }
    return dp[0][dams.length - 1];
  }

  /** Returns the cost of applying the dam evaluation decisions in the
   * specified order against the dam locations and river end state supplied to
   * the constructor.
   *
   * @param evaluationSequence elements of the Y parameter supplied in the
   * constructor, possibly rearranged such that the ith element represents the
   * y-position that is to be the ith dam evaluated for the WPA.  Thus: if Y =
   * {2, 4, 6}, damDecisions may be {4, 6, 2}: this method will return the cost
   * of evaluating the entire set of y-positions when dam evaluation is done
   * first for position "4", then for position "6", finally for position "2".
   * @return the cost of dam evaluation for the entire sequence of dam
   * positions when performed in the specified order.
   * @fixme This method is conceptually a static method because it doesn't
   * depend on the optimal solution produced by solve().  OTOH: the
   * implementation does require access to both the Y array and "river end"
   * information supplied to the constructor.
   * @note the implementation of this method is (almost certainly) not the
   * dynamic programming algorithm used in solve().  This method is part of the
   * API to stimulate your thinking as you work through this assignment and to
   * exercise your software engineering muscles.
   * @notetoself is this assignment too easy without an API for returning the
   * "optimal evaluation sequence"?
   */
  public int cost(final int[] evaluationSequence) {
    int[] sequence=new int[evaluationSequence.length + 2];
    sequence[0]=0;
    sequence[sequence.length-1]=this.riverEnd;
    for(int i=0; i< evaluationSequence.length; i++){
      sequence[i+1]=evaluationSequence[i];
    }
    Arrays.sort(sequence);
    int totalCost=0, first=-1, last=-1;
    boolean[]used=new boolean[this.riverEnd+1];
    used[0]=true;
    used[this.riverEnd]=true;
    for(int i=0; i< evaluationSequence.length; i++){
      for(int j=Arrays.binarySearch(sequence, evaluationSequence[i]); j< sequence.length; j++){
        if(used[sequence[j]]){
          last=sequence[j];
          break;
        }
      }
      for(int j=Arrays.binarySearch(sequence, evaluationSequence[i]); j>=0; j--){
        if(used[sequence[j]]){
          first=sequence[j];
          break;
        }
      }
      used[evaluationSequence[i]]=true;
      totalCost += (last-first);
    }

    return totalCost;
  }
} // class
