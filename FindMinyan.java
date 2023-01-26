package edu.yu.da;

/** Defines the API for specifying and solving the FindMinyan problem (see the
 * requirements document).  Also defines an inner interface, and uses it as
 * part of the ArithmeticPuzzleI API definition.
 *
 * Students MAY NOT change the public API of this class, nor may they add ANY
 * constructor.
 *
 * @author Avraham Leff
 */


import java.util.*;

public class FindMinyan {
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
      first = new Node<>();
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
      public Item next() {
        if (!hasNext()) throw new NoSuchElementException();
        Item item = current.item;
        current = current.next;
        return item;
      }
    } //end of Iterator
  } // end of Bag

  class DirectedEdge {
    private final int v;
    private final int w;
    private final int weight;

    DirectedEdge(int v, int w, int weight) {
      this.v = v;
      this.w = w;
      this.weight = weight;
    }

    /**
     * Returns the tail vertex of the directed edge.
     *
     * @return the tail vertex of the directed edge
     */
     int from() {
      return v;
    }

    /**
     * Returns the head vertex of the directed edge.
     *
     * @return the head vertex of the directed edge
     */
     int to() {
      return w;
    }

    /**
     * Returns the weight of the directed edge.
     *
     * @return the weight of the directed edge
     */
     int weight() {
      return weight;
    }
  } //end of edge class
  class EdgeWeightedDigraph {
    private final int V;                // number of vertices in this digraph
    private int E;                      // number of edges in this digraph
    private Bag<DirectedEdge>[] adj;    // adj[v] = adjacency list for vertex v
    private HashMap<Integer,HashSet<Integer>> edges;
    /**
     * Initializes an empty edge-weighted digraph with {@code V} vertices and 0 edges.
     *
     * @param V the number of vertices
     * @throws IllegalArgumentException if {@code V < 0}
     */
     EdgeWeightedDigraph(int V) {
      this.V = V;
      this.E = 0;
      this.edges=new HashMap<>();
      adj = (Bag<DirectedEdge>[]) new Bag[V+1];
      for (int v = 1; v <= V; v++){
          adj[v] = new Bag<>();
          this.edges.put(v,new HashSet<>());
      }

    }
    /**
     * Returns the number of vertices in this edge-weighted digraph.
     *
     * @return the number of vertices in this edge-weighted digraph
     */
     int V() {
      return V;
    }

    /**
     * Adds the directed edge {@code e} to this edge-weighted digraph.
     *
     * @param e the edge
     * @throws IllegalArgumentException unless endpoints of edge are between {@code 0}
     *                                  and {@code V-1}
     */
    void addEdge(DirectedEdge e) {
      int v = e.from();
      int w = e.to();
      if(this.edges.get(v).contains(w)||this.edges.get(w).contains(v))
          throw new IllegalArgumentException("edge can't be added twice");
      adj[v].add(e);
      adj[w].add(new DirectedEdge(w,v,e.weight()));
      this.edges.get(v).add(w);
      this.edges.get(w).add(v);
      E++;
    }
    /**
     * Returns the directed edges incident from vertex {@code v}.
     *
     * @param v the vertex
     * @return the directed edges incident from vertex {@code v} as an Iterable
     * @throws IllegalArgumentException unless {@code 0 <= v < V}
     */
    Iterable<DirectedEdge> adj(int v) {
      return adj[v];
    }
  }// end of edge weighted graph

   class Dijkstra {
    private int[] distTo;          // distTo[v] = distance  of shortest s->v path
    private DirectedEdge[] edgeTo;    // edgeTo[v] = last edge on shortest s->v path
    private PQ<Integer> pq;    // priority queue of vertices
    private int[]paths;         // number of possible paths to get to vertex v

    /**
     * Computes a shortest-paths tree from the source vertex {@code s} to every other
     * vertex in the edge-weighted digraph {@code G}.
     *
     * @param G the edge-weighted digraph
     * @param s the source vertex
     */
     Dijkstra(EdgeWeightedDigraph G, int s) {
      distTo = new int[G.V()+1];
      edgeTo = new DirectedEdge[G.V()+1];
      this.paths=new int[G.V()+1];
      this.paths[s]=1;
      for (int v = 1; v < G.V()+1; v++)
        distTo[v] = Integer.MAX_VALUE;
      distTo[s] = 0;

      // relax vertices in order of distance from s
      pq = new PQ<>(G.V());
      pq.insert(s, distTo[s]);
      while (!pq.isEmpty()) {
        int v = pq.delMin();
        for (DirectedEdge e : G.adj(v))
          relax(e);
      }
    }

    // relax edge e and update pq if changed
    private void relax(DirectedEdge e) {
      int v = e.from(), w = e.to();
      if (distTo[w] > distTo[v] + e.weight()) {
        distTo[w] = distTo[v] + e.weight();
        edgeTo[w] = e;
        paths[w]=paths[v];
        if (pq.contains(w)) pq.decreaseKey(w, distTo[w]);
        else pq.insert(w, distTo[w]);
      }
      else if(distTo[w] == distTo[v] + e.weight()&&edgeTo[v].to()!=edgeTo[w].from()&&edgeTo[w].to()!=edgeTo[v].from()){
        paths[w]+=paths[v];
      }
    }

    /**
     * Returns the length of a shortest path from the source vertex {@code s} to vertex {@code v}.
     *
     * @param v the destination vertex
     * @return the length of a shortest path from the source vertex {@code s} to vertex {@code v};
     * {@code Double.POSITIVE_INFINITY} if no such path
     * @throws IllegalArgumentException unless {@code 0 <= v < V}
     */
    int distTo(int v) {
      return distTo[v];
    }

    /**
     * Returns true if there is a path from the source vertex {@code s} to vertex {@code v}.
     *
     * @param v the destination vertex
     * @return {@code true} if there is a path from the source vertex
     * {@code s} to vertex {@code v}; {@code false} otherwise
     * @throws IllegalArgumentException unless {@code 0 <= v < V}
     */
     boolean hasPathTo(int v) {
      return distTo[v] < Integer.MAX_VALUE;
    }
    /* Function gets v, a destination vertex, and returns how many shortest paths there
       are to reach this vertex
     */
      protected int numberOfPaths(int v){
        return this.paths[v];
      }
  }

  /** Constructor: clients specify the number of cities involved in the
   * problem.  Cities are numbered 1..n, and for convenience, the "start" city
   * is labelled as "1", and the goal city is labelled as "n".
   *
   * @param nCities number of cities, must be greater than 1.
   */
  private int nCities;
  private EdgeWeightedDigraph cities;
  private Set<Integer> minyanim;
  private int tripNum=0;
  private int tripDur=0;
  private boolean shortCut;
  public FindMinyan(final int nCities) {
    if(nCities<=1)
      throw new IllegalArgumentException();
    this.nCities=nCities;
    this.cities=new EdgeWeightedDigraph(this.nCities);
    this.minyanim=new HashSet<>();
    this.shortCut=false;
    // fill me in!
  }

  /** Defines a highway leading (bi-directionally) between two cities, of
   * specified duration.
   *
   * @param city1 identifies a 1 <= city <= n, must differ from city2
   * @param city2 identifies a 1 <= city <= n, must differ from city1
   * @param duration the bi-directional duration of a trip between the two
   * cities on this highway, must be non-negative
   */
  // may check if a highway exists already
  public void addHighway(final int city1, final int duration, final int city2) {
    // fill me in!
    if(city1==city2)
      throw new IllegalArgumentException("Cities must differ");
    if(city1<1||city1>this.nCities||city2<1||city2>this.nCities)
      throw new IllegalArgumentException("Cities must be in range 1 to n");
    if(duration<0)
      throw new IllegalArgumentException("duration must be nonnegative");
    this.cities.addEdge(new DirectedEdge(city1,city2,duration));
  }

  /** Specifies that a minyan can be found in the specified city.
   *
   * @param city identifies a 1 <= city <= n
   */
  public void hasMinyan(final int city) {
    if(city<1||city>this.nCities)
      throw new IllegalArgumentException("city isn't in the range");
    this.minyanim.add(city);
    if(city==1||city==this.nCities)
        shortCut=true;
    // fill me in!
  }

  /** Find a solution to the FindMinyan problem based on state specified by the
   * constructor, addHighway(), and hasMinyan() API.  Clients access the
   * solution through the shortestDuration() and nShortestDurationTrips() APIs.
   */
  public void solveIt() {
    // fill me in!
    if(this.minyanim.size()==0)
      return;
    Dijkstra sp=new Dijkstra(this.cities,1);
    if(shortCut){
        this.tripDur=sp.hasPathTo(this.nCities)?sp.distTo(this.nCities):0;
        this.tripNum    =sp.hasPathTo(this.nCities)?sp.numberOfPaths(this.nCities):0;
        return;
    }
    Dijkstra sp2=new Dijkstra(this.cities,this.nCities);
    int[][]distances=new int[this.minyanim.size()][2];
    int c=0,min=Integer.MAX_VALUE,paths=0;
    for(int n : this.minyanim){
      distances[c][0]=sp.hasPathTo(n)?sp.distTo(n):0;
      if(sp.hasPathTo(n)){ // checking from minyan to goal only if
        distances[c][1]=sp2.hasPathTo(n)?sp2.distTo(n):0;
        if(sp2.hasPathTo(n)&&distances[c][0]+distances[c][1]<min){ //updating minimum minyan
          min=distances[c][0]+distances[c][1];   // if goal is reachable from minyan and total duration is less than the minimum
          paths=sp.numberOfPaths(n)*sp2.numberOfPaths(n);
        }
        else if(sp2.hasPathTo(n)&&distances[c][0]+distances[c][1]==min){
            paths+=sp.numberOfPaths(n)*sp2.numberOfPaths(n); }
      }
      c++;
    }
      this.tripNum=paths;
      if(min<Integer.MAX_VALUE)
        this.tripDur=min;
  }

  /** Returns the duration of the shortest trip satisfying the FindMinyan
   * constraints.  
   *
   * @return duration of the shortest trip, undefined if client hasn't
   * previously invoked solveIt().
   */
  public int shortestDuration() {
    return this.tripDur;
  }

  /** Returns the number of distinct trips that satisfy the FindMinyan
   * constraints.
   * 
   * @return number of shortest duration trips, undefined if client hasn't
   * previously invoked solveIt()..
   */
  public int numberOfShortestTrips() {
    return this.tripNum;
  }
  
} // FindMinyan
