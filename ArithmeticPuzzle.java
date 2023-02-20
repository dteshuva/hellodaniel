package edu.yu.da;

import java.util.*;

public class ArithmeticPuzzle extends ArithmeticPuzzleBase{
    class Chromosome{
        private Map<Character,Integer> mapDigit;
        private int aug, add, sum, fitness;
        Chromosome(Map<Character,Integer> map){
            mapDigit=map;
            aug=calcString(augend);
            add=calcString(addend);
            sum=calcString(ArithmeticPuzzle.this.sum);
            fitness=Math.abs(sum-add-aug);
        }

        int getFitness(){ return fitness; }

        void reCalculateFitness(){
            aug=calcString(augend);
            add=calcString(addend);
            sum=calcString(ArithmeticPuzzle.this.sum);
            fitness=Math.abs(sum-add-aug);
        }

        Map<Character,Integer>  getMap(){ return mapDigit; }

        private int calcString(String str){
            int sum=0;
            Character ch;
            for(int i=0; i<str.length(); i++){
                ch=str.charAt(i);
                sum+=Math.pow(10,str.length()-i-1)*mapDigit.get(ch);
            }
            return sum;
        }
    }
    private class Population{
        int size;
        List<Chromosome> pop;
        int highestFit;
        Chromosome best;
        // creating random populations of
        Population(int populationSize){
            this.highestFit=Integer.MAX_VALUE;
            this.size=populationSize;
            int dig;
            this.pop=new ArrayList<>();
            Chromosome c;
            while(pop.size()<populationSize){
                int[]usedDigs=new int[10];
                HashMap<Character,Integer> map=new HashMap<>();
                for(char ch : ArithmeticPuzzle.this.allChars){
                  dig=new Random().nextInt(10);
                  while(usedDigs[dig]==1){
                      dig=new Random().nextInt(10);
                  }
                  map.put(ch,dig);
                  usedDigs[dig]=1;
                }
                c=new Chromosome(map);
                pop.add(c);
                updateHighestFit(c);

            }
        }
        void mutation(GeneticAlgorithmConfig gac){
            if(allChars.size()==10)  // may change it to be an edge case
                return;
            double rand = Math.random();
            if(rand>gac.getMutationProbability())
                return;
            Random r=new Random();
            int par1=r.nextInt(this.size),par2=r.nextInt(this.size); // 2 random chromosomes
            Chromosome p1=this.pop.get(par1),p2=this.pop.get(par2);
            mutationHelper(p1,r);
            mutationHelper(p2,r);

        }
        private void mutationHelper(Chromosome c,Random r){
            boolean[] arr=new boolean[10];
            for(int ch : c.getMap().values())
                arr[ch]=true;
            int ind=r.nextInt(allChars.size());
            int dig=r.nextInt(10);
            while(arr[dig])
                dig=r.nextInt(10);  // getting a digit that isn't already in the chromosome
            int i=0;
            for(char ch : allChars){
                if(ind==i){
                    c.getMap().put(ch,dig);
                    break;
                }
                i++;
            }
            c.reCalculateFitness();
            updateHighestFit(c);
        }
        void cross(GeneticAlgorithmConfig gac){
            double rand = Math.random();
            if(rand>gac.getCrossoverProbability()){
                this.pop= gac.getSelectionType()== GeneticAlgorithmConfig.SelectionType.TOURNAMENT ? this.tournament(false) : this.roulette(false);
            }
            else{
                this.pop= gac.getSelectionType()== GeneticAlgorithmConfig.SelectionType.TOURNAMENT ? this.tournament(true) : this.roulette(true);
                Random r=new Random();
                int par1=r.nextInt(this.size),par2=r.nextInt(this.size),ind,j;
                while(par1==par2){
                    par1=r.nextInt(this.size); //// random different chromosomes
                }
                int[] arr1,arr2;
                Chromosome p1,p2;
                boolean invalid1,invalid2;
                /*
                Until population is full, every time get 2 random chromosomes and a random position and create out of it 2 new
                chromosomes,then add the ones which are valid- no duplicate digits
                 */
                while(pop.size()<this.size) {
                    p1 = this.pop.get(par1);
                    p2 = this.pop.get(par2);
                    if(allChars.size()>2)
                        ind = r.nextInt(allChars.size() - 2) + 1;   // check for edge cases of only 1/2 letters
                    else
                        ind = r.nextInt(2);
                    Map<Character, Integer> xMap = p1.getMap(), yMap = p2.getMap(), mod1 = new HashMap<>(), mod2 = new HashMap<>();
                    Set<Character> xSet = xMap.keySet();
                    j = 0;
                    arr1 = new int[10];
                    arr2 = new int[10];
                    invalid1 = false;
                    invalid2 = false;
                    /*
                    after picking a random index, up do that index, add to mod1 p2's mapping pair and to mod2 p1's pairs
                    after the index do the opposite
                     */
                    for (Character ch : xSet) {
                        if (j <= ind) {
                            mod1.put(ch, yMap.get(ch));
                            mod2.put(ch, xMap.get(ch));
                            arr1[yMap.get(ch)]++;
                            arr2[xMap.get(ch)]++;
                            // following condition means that there is a multiple occurrence of the same digit - not valid
                            if (arr1[yMap.get(ch)] > 1)
                                invalid1 = true;
                            if (arr2[xMap.get(ch)] > 1)
                                invalid2 = true;
                        } else {
                            mod1.put(ch, xMap.get(ch));
                            mod2.put(ch, yMap.get(ch));
                            arr1[xMap.get(ch)]++;
                            arr2[yMap.get(ch)]++;
                            if (arr1[xMap.get(ch)] > 1)
                                invalid1 = true;
                            if (arr2[yMap.get(ch)] > 1)
                                invalid2 = true;
                        }
                        if (invalid1 && invalid2)
                            break;
                        j++;
                    }
                    Chromosome c;
                    if (!invalid1){
                        c = new Chromosome(mod1);
                        this.pop.add(c);
                        updateHighestFit(c);
                    }
                    if (!invalid2){
                        c = new Chromosome(mod2);
                        this.pop.add(c);
                        updateHighestFit(c);
                    }
                    par1=r.nextInt(this.size);
                    par2=r.nextInt(this.size);
                    while(par1==par2){
                        par1=r.nextInt(this.size);
                    }
                } // end of while
            }
        }
        List<Chromosome> tournament(boolean isCross){
            List<Chromosome> selected=new ArrayList<>();
            int num= isCross==false ? this.size : this.size / 2, minFit=Integer.MAX_VALUE;
            Chromosome best=null, temp;
            for(int i=1; i<=num; i++){
                temp=tournamentHelper();
                selected.add(temp);
                if(temp.getFitness()<minFit){
                    minFit=temp.getFitness();
                    best=temp;
                }
            }
            this.best=best;
            this.highestFit=minFit;
            return selected;
        }
        private Chromosome tournamentHelper(){
            int c=0,rand,min,temp;
            int thres=6;
            Chromosome sol;
            Random r=new Random();
            rand=r.nextInt(this.size);
            min=this.pop.get(rand).getFitness();
            sol=this.pop.get(rand);
            while(c<thres){
                rand=r.nextInt(this.size);
                temp=this.pop.get(rand).getFitness();
                if(temp<min){
                    min=temp;
                    sol=this.pop.get(rand);
                }
                c++;
            }
            return sol;
        }
        List<Chromosome> roulette(boolean isCross){
            double[] probabilities=new double[this.size];
            List<Chromosome> selected=new ArrayList<>();
            Chromosome best=null, temp;
            int sumFitness=0;
            // getting total fitness
            for(Chromosome c : this.pop){
                sumFitness+=c.getFitness();
            }
            int i=0;
            for(Chromosome c : this.pop){
                probabilities[i]=c.getFitness()/sumFitness;
                i++;
            }
            int num= isCross==false ? this.size : this.size / 2, minFit=Integer.MAX_VALUE;
            for(i=1; i<=num; i++){
                temp=rouletteHelper(probabilities);
                selected.add(temp);
                if(temp.getFitness()<minFit){
                    minFit=temp.getFitness();
                    best=temp;
                }
            }
            this.best=best;
            this.highestFit=minFit;
            return selected;
        }
        private Chromosome rouletteHelper(double[]probabilities){
            double rand = Math.random(); // Generate a random number between 0 and 1
            double slice = 0;
            int i=0;
            for(Chromosome c : this.pop){
                slice+=probabilities[i];
                if(rand <= slice)
                    return c;
                i++;
            }
            return null;
        }
        int getHighestFit(){
            return this.highestFit;
        }
        Chromosome getBest(){
            return this.best;
        }

        private void updateHighestFit(Chromosome c){
            if(c.getFitness()>this.highestFit){
                this.highestFit=c.getFitness();
                this.best=c;
            }
        }
    }
    private class Solution implements SolutionI{
        private List<Character> solution;
        private String augend, addend, sum;
        private int nGen;
        Solution(List<Character> ls, String augend, String addend, String sum, int x){
            this.solution=ls;
            this.augend=augend;
            this.addend=addend;
            this.sum=sum;
            this.nGen=x;
        }
        @Override
        public List<Character> solution() {
            return this.solution;
        }

        @Override
        public String getAugend() {
            return this.augend;
        }

        @Override
        public String getAddend() {
            return this.addend;
        }

        @Override
        public String getSum() {
            return this.sum;
        }

        @Override
        public int nGenerations() {
            return this.nGen;
        }
    }
    /**
     * Constructor.  Specifies the arithmetic puzzle to be solved in terms of an
     * augend, addend, and sum.
     * <p>
     * Representation: all characters are in the range A-Z, with each letter
     * represents a unique digit.  The puzzle to be solved specifies that the
     * augend and addend (each representing a number in base 10) sum to the
     * specified sum (also a number in base 10).  Each of these numbers is
     * represented with the most significant letter (digit) in position 0, next
     * most significant letter (digit) in position 1, and so on.  The numbers
     * need not be the same length: an "empty" digit is represented by the
     * "space" character.
     * <p>
     * Addition: Augend + Addend = Sum
     *
     * @param augend
     * @param addend
     * @param sum
     */
    String augend, addend, sum;
    Set<Character> allChars;
    Population p;
    SolutionI sol;
    public ArithmeticPuzzle(String augend, String addend, String sum) {
        super(augend, addend, sum);
        this.augend=augend;
        this.addend=addend;
        this.sum=sum;
        allChars=new HashSet<>();
        addChars(augend);
        addChars(addend);
        addChars(sum);
    }
    // updates character set to contain all distinct characters
    private void addChars(String str){
       for(int i=0; i<str.length(); i++){
           this.allChars.add(str.charAt(i));
       }
    }
    @Override
    public SolutionI solveIt(GeneticAlgorithmConfig gac) {
        p=new Population(gac.getInitialPopulationSize());
        int count=0;
        List<Character> sol=new ArrayList<>();
        SolutionI s;
        if(this.allChars.size()==1){
            for(char ch : allChars)
                sol.add(ch);
            for(int i=1; i<=9; i++)
                sol.add(' ');
            s=new Solution(sol, this.augend, this.addend, this.sum, 1);
            return s;
        }
        while(count< gac.getMaxGenerations()&&p.getHighestFit()!=0){ // may change highestfit to a method
            p.cross(gac);

            p.mutation(gac);
            count++;
        }
        if(p.getHighestFit()!=0){
            s=new Solution(sol, this.augend, this.addend, this.sum, 0);
            return s;
        }
        Map<Character, Integer> map= p.getBest().getMap();
        for(int i=0; i<10; i++)
           sol.add(' ');
        for(char ch : map.keySet()){
            sol.add(map.get(ch), ch);
        }
        s=new Solution(sol, this.augend, this.addend, this.sum, count);
        return s;
    }
}
