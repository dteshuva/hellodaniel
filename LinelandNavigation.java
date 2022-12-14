package edu.yu.introtoalgs;
import java.util.*;
import java.util.NoSuchElementException;
/** Defines the API for the LinelandNavigation assignment: see the requirements
 * document for more information.
 *
 * Students MAY NOT change the constructor signature.  My test code will only
 * invoke the API defined below.
 *
 * @author Avraham Leff
 */

public class LinelandNavigation {
    private class Bag<Item> implements Iterable<Item> {
        private Node<Item> first;    // beginning of bag
        private int n;               // number of elements in bag

        // helper linked list class
        private class Node<Item> {
            private Item item;
            private Node<Item> next;
        }

        /**
         * Initializes an empty bag.
         */
        Bag() {
            first = null;
            n = 0;
        }

        /**
         * Adds the item to this bag.
         *
         * @param item the item to add to this bag
         */
        void add(Item item) {
            Node<Item> oldfirst = first;
            first = new Node<Item>();
            first.item = item;
            first.next = oldfirst;
            n++;
        }
        /**
         * Returns an iterator that iterates over the items in this bag in arbitrary order.
         *
         * @return an iterator that iterates over the items in this bag in arbitrary order
         */
        public Iterator<Item> iterator() {
            return new LinkedIterator(first);
        }

        // an iterator, doesn't implement remove() since it's optional
        private class LinkedIterator implements Iterator<Item> {
            private Node<Item> current;

            LinkedIterator(Node<Item> first) {
                current = first;
            }

            public boolean hasNext() {
                return current != null;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

            public Item next() {
                if (!hasNext()) throw new NoSuchElementException();
                Item item = current.item;
                current = current.next;
                return item;
            }
        } //end of Iterator
    } // end of Bag
    private class Digraph {
        private final int V;           // number of vertices in this digraph
        private int E;                 // number of edges in this digraph
        private Bag<Integer>[] adj;    // adj[v] = adjacency list for vertex v
        private int[] indegree;        // indegree[v] = indegree of vertex v

        /**
         * Initializes an empty digraph with <em>V</em> vertices.
         *
         * @param V the number of vertices
         * @throws IllegalArgumentException if {@code V < 0}
         */
          Digraph(int V) {
            if (V < 0) throw new IllegalArgumentException("Number of vertices in a Digraph must be non-negative");
            this.V = V;
            this.E = 0;
            indegree = new int[V];
            adj = (Bag<Integer>[]) new Bag[V];
            for (int v = 0; v < V; v++) {
                adj[v] = new Bag<Integer>();
            }
        }


        /**
         * Returns the number of vertices in this digraph.
         *
         * @return the number of vertices in this digraph
         */
        public int V() {
            return V;
        }

        /**
         * Returns the number of edges in this digraph.
         *
         * @return the number of edges in this digraph
         */
        public int E() {
            return E;
        }
        /**
         * Adds the directed edge v→w to this digraph.
         *
         * @param v the tail vertex
         * @param w the head vertex
         * @throws IllegalArgumentException unless both {@code 0 <= v < V} and {@code 0 <= w < V}
         */
        public void addEdge(int v, int w) {
            adj[v].add(w);
            indegree[w]++;
            E++;
        }

        /**
         * Returns the vertices adjacent from vertex {@code v} in this digraph.
         *
         * @param v the vertex
         * @return the vertices adjacent from vertex {@code v} in this digraph, as an iterable
         * @throws IllegalArgumentException unless {@code 0 <= v < V}
         */
        public Iterable<Integer> adj(int v) {
            return adj[v];
        }

    }
  private class BreadthFirstDirectedPaths {
    private static final int INFINITY = Integer.MAX_VALUE;
    private boolean[] marked;  // marked[v] = is there an s->v path?
    private int[] edgeTo;      // edgeTo[v] = last edge on shortest s->v path
    private int[] distTo;      // distTo[v] = length of shortest s->v path

    /**
     * Computes the shortest path from {@code s} and every other vertex in graph {@code G}.
     *
     * @param G the digraph
     * @param s the source vertex
     * @throws IllegalArgumentException unless {@code 0 <= v < V}
     */
     BreadthFirstDirectedPaths(Digraph G, int s) {
      marked = new boolean[G.V()];
      distTo = new int[G.V()];
      edgeTo = new int[G.V()];
      for (int v = 0; v < G.V(); v++)
        distTo[v] = INFINITY;
      bfs(G, s);
    }


    // BFS from single source
    private void bfs(Digraph G, int s) {
      Queue<Integer> q =new LinkedList<>();
      marked[s] = true;
      distTo[s] = 0;
      q.add(s);
      while (!q.isEmpty()) {
        int v = q.remove();
        for (int w : G.adj(v)) {
          if (!marked[w]) {
            edgeTo[w] = v;
            distTo[w] = distTo[v] + 1;
            marked[w] = true;
            q.add(w);
          }
        }
      }
    }

    /**
     * Is there a directed path from the source {@code s} (or sources) to vertex {@code v}?
     *
     * @param v the vertex
     * @return {@code true} if there is a directed path, {@code false} otherwise
     * @throws IllegalArgumentException unless {@code 0 <= v < V}
     */
     boolean hasPathTo(int v) {
      return marked[v];
    }

    /**
     * Returns the number of edges in a shortest path from the source {@code s}
     * (or sources) to vertex {@code v}?
     *
     * @param v the vertex
     * @return the number of edges in such a shortest path
     * (or {@code Integer.MAX_VALUE} if there is no such path)
     * @throws IllegalArgumentException unless {@code 0 <= v < V}
     */
     int distTo(int v) {
      return distTo[v];
    }
  } // end of bfs class


  /** Even though Lineland extends forward (and backwards) infinitely, for
   * purposes of this problem, the navigation goal cannot exceed this value
   */
  public final static int MAX_FORWARD = 1_000_000;
  private boolean [] mines;
  private int moves;
  private int goal;
  private int constant;
  private boolean noWay;
  private Digraph G;
  /** Constructor.  When the constructor completes successfully, the navigator
   * is positioned at position 0.
   *
   * @param g a positive value indicating the minimim valued position for a
   * successful navigation (a successful navigation can result in a position
   * that is greater than g).  The value of this parameter ranges from 1 to
   * MAX_FORWARD (inclusive).
   * @param m a positive integer indicating the exact number of positions that
   * must always be taken in a forward move. The value of this parameter cannot
   * exceed MAX_FORWARD.
   * @throws IllegalArgumentException if the parameter values violate the
   * specified semantics.
   */
  public LinelandNavigation(final int g, final int m) {
      if(g<1||g>MAX_FORWARD||m<1||m>MAX_FORWARD){
          throw new IllegalArgumentException("paramter didnt matech the javadoc requirements");
      }
      this.mines=new boolean[g+m];
      this.G=new Digraph(g+2*m-1);
      this.constant=m-1;
      this.moves=m;
      this.goal=g;
      this.noWay=false;
    // fill me in!
  }

  /** Adds a mined line segment to Lineland (an optional operation).
   *
   * NOTE: to simplify computation, assume that no two mined line segments can
   * overlap with one another, even at their end-points.  You need not test for
   * this (although if it's easy to do so, consider sprinkling asserts in your
   * code).
   *
   * @param start a positive integer representing the start (inclusive)
   * position of the line segment
   * @param end a positive integer represending the end (inclusive) position of
   * the line segment, must be greater or equal to start, and less than
   * MAX_FORWARD.
   */
  public void addMinedLineSegment(final int start, final int end) {
    // fill me in!
      if(start<1||end>=MAX_FORWARD||end<start){
          throw new IllegalArgumentException("parameters dont match javadoc requiremetns");
      }
      if(start>=this.goal+this.moves) return;
      for(int i=start; i<=(end>=(this.goal+this.moves)?(this.goal+this.moves-1):end); i++){
          this.mines[i]=true; } //when checking for mine in a position- index in the array is actual position
      if(end-start+1>=this.moves||end==this.moves||start==this.moves)
          noWay=true;
  }



  /** Determines the minimum number of moves needed to navigate from position 0
   * to position g or greater, where forward navigation must exactly m
   * positions at a time and backwards navigation can be any number of
   * positions.
   *
   * @return the minimum number of navigation moves necessary, or "0" if no
   * navigation is possible under the specified constraints.
   */
  // position is index+constant
  public final int solveIt() {
      if(this.noWay)
          return 0;
      for(int i=0; i<this.goal;i++){
          if(this.mines[i])
              continue;
          if(!this.mines[i+this.moves])
              this.G.addEdge(i+this.constant,i+this.constant+this.moves);
          for(int j=1; j<=this.constant; j++){
              if(i-j<1)
                  break;
              if(!this.mines[i-j])
                  this.G.addEdge(i+this.constant,i+this.constant-j);
          }
      }
      BreadthFirstDirectedPaths bfs=new BreadthFirstDirectedPaths(this.G,this.constant);
      int res=0;
      for(int i=this.goal; i<this.goal+this.moves;i++){
          if(bfs.hasPathTo(i+this.constant)){
              if(res==0){
                  res=bfs.distTo(i+this.constant); }
              else if(bfs.distTo(i+this.constant)<res) {
                  res= bfs.distTo(i+this.constant); }
          }
      }
    return res;
  }
} // LinelandNavigation
