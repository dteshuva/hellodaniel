package edu.yu.introtoalgs;

/* Implements PrimeCalculator interface by using exactly two threads to
 * partition the range of primes between them.  Each thread uses the "naive"
 * SerialPrimes algorithm to solve its part of the problem.
 *
 * Students may not change the constructor signature or add any other
 * constructor!
 *
 * @author Avraham Leff
 */

public class TwoThreadPrimes extends BigOMeasurable {

  /** Constructor
   *
   */
  private int n;
  public TwoThreadPrimes() {
    // your code (if any) goes here
    
  }

 // @Override
  public int nPrimesInRange(final long start, final long end) {
    // your code (if any) goes here
    if(end<start)
    throw new IllegalArgumentException("Start can't be greater than end");
    if(end>=Long.MAX_VALUE|| start<2)
    throw new IllegalArgumentException("End must be less than LONG.max value and start greater than 1");
    class PrimeThread extends Thread{
      private long s;
      private long finish;
      protected int count;

      PrimeThread(long x, long y){
        this.s=x;
        this.finish=y;
        this.count=0;
      }
      public void run(){
        
       for(long i=this.s; i<=this.finish; i++){
         if(isPrime(i)) this.count++;
       }
      }
      private boolean isPrime(long n){
        for(int i=2; i<=Math.sqrt(n); i++ ){
          if(n%i==0)
          return false;
        }
        return true;
      }
    } // end of thread
    int answer=0;
     PrimeThread t1=new PrimeThread(start, (start+end)/2);
     t1.start();
     PrimeThread t2=new PrimeThread(1+(start+end)/2, end);
     t2.start();
     try {
      t1.join();
    } catch (InterruptedException e) { e.printStackTrace(); }
    answer+=t1.count;
     try {
      t2.join();
    } catch (InterruptedException e) { e.printStackTrace();   }
    answer+=t2.count;
    return answer;
  }

  @Override
  public void setup(int x){
     this.n=x;
  }

  @Override
  public void execute() {
    // TODO Auto-generated method stub
      int count=this.nPrimesInRange(2, this.n);
  }
}
