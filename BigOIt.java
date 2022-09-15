package edu.yu.introtoalgs;
import java.io.Console;
import java.nio.charset.Charset;
import java.lang.reflect.Constructor;
import java.lang.reflect.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import static java.lang.System.out;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
/** Defines the API for the BigOIt assignment: see the requirements
 * document for more information.
 *
 * Students MAY NOT change the constructor signature.  My test code will only
 * invoke the API defined below.
 *
 * @author Avraham Leff
 */

public class BigOIt extends BigOItBase{
  /** No-argument constructor.
   */
  private class Stopwatch {
    private final long start;
     Stopwatch(){
      this.start=System.currentTimeMillis();
    }
     long elapsedTime(){
      long now=System.currentTimeMillis();
      return now-start;
    }
  }
  public BigOIt() {
    int num=0;
    for(int i=0; i<1000; i++){
        for(int j=0; j<1000;j++)
        num=num*2+i;
    }
    // no-op implementation in this base class
  }
  
  /** Given the name of a class that implements the BigOMeasurable API, creates
   * and executes instances of the class, such that by measuring the resulting
   * performance, can return the "doubling ratio" for that algorithm's
   * performance.
   *
   * See extended discussion in Sedgewick, Chapter 1.4, on the topic of
   * doubling ratio experiments.
   *
   * @param bigOMeasurable name of the class for which we want to compute a
   * doubling ratio.  The client guarantees that the corresponding class
   * implements the BigOMeasurable API, and can be constructed with a
   * no-argument constructor.  This method is therefore able to first construct
   * instances of this class, invoke "setup(n)" for whatever values of "n" are
   * desired, and then invoke "execute()" to measure the performance of a
   * single invocation of the algorithm.  The client is responsible for
   * ensuring that invocation of setup(n) produces a suitably populated
   * (perhaps randomized) set of state scaled as a function of n.
   * @param timeOutInMs number of milliseconds allowed for the computation.  If
   * the implementation has not computed an answer by this time, it should
   * return NaN.
   * @return the doubling ratio for the specified algorithm if one can be
   * calculated, NaN otherwise.
   * @throws IllegalArgumentException if bigOMeasurable parameter doesn't
   * fulfil the contract specified above or if some characteristic of the
   * algorithm is at odds with the doubling ratio assumptions.
   */
  public double doublingRatio(String bigOMeasurable, long timeOutInMs){
    long begin=System.currentTimeMillis(),start,end,start2,end2,begin2;
    Constructor<?> c=null;
    BigOMeasurable obj=null;
    int count=0,n,count2;
    double t1,t2,d=0,ratioSum=0;

      try {
        c = Class.forName(bigOMeasurable).getConstructor(null);
      } catch (NoSuchMethodException e1) {
         throw new IllegalArgumentException("No such method");
      } catch (SecurityException e1) {
        throw new IllegalArgumentException("Security exception");
      } catch (ClassNotFoundException e1) {
        throw new IllegalArgumentException("No such class");
      }
    
       try {
        obj=(BigOMeasurable)c.newInstance(null);
      } catch (InstantiationException e) {
        throw new IllegalArgumentException("Instantiation problem");
      } catch (IllegalAccessException e) {
        throw new IllegalArgumentException("Access problem");
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("IAE");
      } catch (InvocationTargetException e) {
        throw new IllegalArgumentException("Invocation problem");
      }
    Callable<Double> task=() ->{
      return  0.0;
    } ;
    
     while((System.currentTimeMillis()-begin)<0.8*timeOutInMs&&count<=6000){
      n=2000;
      count2=0;
      begin2=System.currentTimeMillis();
     while((System.currentTimeMillis()-begin2)<0.2*timeOutInMs&&count2<=5){
      obj.setup(n);
      start=System.nanoTime();
      obj.execute();
      end=System.nanoTime();
      end=end-start;
      t1=end;
      int k=2*n;
      obj.setup(k);
      start2=System.nanoTime();
      obj.execute();
      end2=System.nanoTime();
      end2=end2-start2;
      t2=end2;
      if(t2!=0&t1!=0){
       d=t2/t1;
       if(d>1){
        ratioSum=ratioSum+d;
       }
      
      }
     // System.out.println(t2+" "+t1+" "+d);
     count++;
      count2++;
      n*=2;
    } 
  }  
    System.out.println("count: "+count+" "+ratioSum);
    if(count==0){
    return Double.NaN;
  }
    return ratioSum/count;
  }

} // class
