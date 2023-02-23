package edu.yu.da;

import java.util.*;

public class ArithmeticPuzzle extends ArithmeticPuzzleBase{
    class Chromosome{
        private char[] mapDigit;
        private int aug, add, sum, fitness;
        Chromosome(char[] map){
            mapDigit=map;
            aug=calcString(augend);
            add=calcString(addend);
            sum=calcString(ArithmeticPuzzle.this.sum);
            fitness=Math.abs(sum-add-aug);
        }

        int getFitness(){ return fitness; }
        int getAug(){ return this.aug; }
        int getAdd(){ return this.add; }
        int getSum(){ return  this.sum; }
        void reCalculateFitness(){
            aug=calcString(augend);
            add=calcString(addend);
            sum=calcString(ArithmeticPuzzle.this.sum);
            fitness=Math.abs(sum-add-aug);
        }

        char[]  getMap(){ return mapDigit; }

        void setMap(int dig, char ch){ this.mapDigit[dig]=ch; }

        private int getValue(Character ch){
            for(int i=0; i<this.mapDigit.length; i++){
                if(mapDigit[i]==ch)
                    return 1;
            }
            return -1;
        }
        private int calcString(String str){
            int sum=0;
            Character ch;
            for(int i=0; i<str.length(); i++){
                ch=str.charAt(i);
                sum+=Math.pow(10,str.length()-i-1)*getValue(ch);
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
                char[] map=new char[10];
                for(char ch : ArithmeticPuzzle.this.allChars){
                  dig=new Random().nextInt(10);
                  while(usedDigs[dig]==1){
                      dig=new Random().nextInt(10);
                  }
                  map[dig]=ch;
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
            int par1=r.nextInt(this.pop.size()),par2=r.nextInt(this.pop.size()); // 2 random chromosomes
            Chromosome p1=this.pop.get(par1),p2=this.pop.get(par2);
            mutationHelper(p1,r);
            mutationHelper(p2,r);

        }
        private void mutationHelper(Chromosome c,Random r){
            char [] arr=c.getMap();
            int ind=r.nextInt(allChars.size());
            int dig=r.nextInt(10);
            while(Character.isLetter(arr[dig]))
                dig=r.nextInt(10);  // getting a digit that isn't already in the chromosome
            int i=0;
            for(char ch : allChars){
                if(ind==i){
                    c.setMap(dig,ch);
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
                int par1=r.nextInt(pop.size()),par2=r.nextInt(pop.size()),ind;
                while(par1==par2){
                    par1=r.nextInt(pop.size()); //// random different chromosomes
                }
                Set<Character> arr1,arr2;
                Chromosome p1,p2;
                boolean invalid1,invalid2;
                /*
                Until population is full, every time get 2 random chromosomes and a random position and create out of it 2 new
                chromosomes,then add the ones which are valid- no duplicate digits
                 */
                while(pop.size()<this.size) {
                    p1 = this.pop.get(par1);
                    p2 = this.pop.get(par2);
                    ind=r.nextInt(10);
                    char[] xMap = p1.getMap(), yMap = p2.getMap(), mod1 = new char[10], mod2 = new char[10];
                    Set<Character> xSet = new HashSet<>();
                    for(int i=0; i<10; i++)
                        if(Character.isLetter(xMap[i]))
                            xSet.add(xMap[i]);
                    arr1 = new HashSet<>();
                    arr2 = new HashSet<>();
                    invalid1 = false;
                    invalid2 = false;
                    /*
                    after picking a random index, up do that index, add to mod1 p2's mapping pair and to mod2 p1's pairs
                    after the index do the opposite
                     */
                    for(int i=0; i<10; i++){
                        if(i<=ind){
                            mod1[i]=yMap[i];
                            mod2[i]=xMap[i];
                            if(Character.isLetter(yMap[i])&&!arr1.add(yMap[i]))
                                invalid1=true;
                            if(Character.isLetter(xMap[i])&&!arr2.add(xMap[i]))
                                invalid2=true;
                        }
                        else{
                            mod1[i]=xMap[i];
                            mod2[i]=yMap[i];
                            if(Character.isLetter(xMap[i])&&!arr1.add(xMap[i]))
                                invalid1=true;
                            if(Character.isLetter(yMap[i])&&!arr2.add(yMap[i]))
                                invalid2=true;
                        }
                        if(invalid1&&invalid2)
                            break;
                    }
                    Chromosome c;
                    if (!invalid1&&arr1.containsAll(allChars)){
                        c = new Chromosome(mod1);
                        this.pop.add(c);
                        updateHighestFit(c);
                    }
                    if (!invalid2&&arr2.containsAll(allChars)){
                        c = new Chromosome(mod2);
                        this.pop.add(c);
                        updateHighestFit(c);
                    }
                    par1=r.nextInt(pop.size());
                    par2=r.nextInt(pop.size());
                    while(par1==par2){
                        par1=r.nextInt(pop.size());
                    }
                } // end of while
            }
        }
        List<Chromosome> tournament(boolean isCross){
            List<Chromosome> selected = new ArrayList<>();
            int num= isCross==false ? this.size : this.size / 2;
            Chromosome bestFit=null;
            for (int i = 0; i < num; i++) {
                Chromosome best = null;
                for (int j = 0; j < 3; j++) {
                    int index = (int) (Math.random() * size);
                    Chromosome competitor = pop.get(index);
                    if (best == null || competitor.getFitness() < best.getFitness()) {
                        best = competitor;
                    }
                }
                if(bestFit == null || best.getFitness() < bestFit.getFitness()){
                    bestFit = best;
                }
                selected.add(best);
            }
            this.best = bestFit;
            this.highestFit = best.getFitness();
            return selected;
        }

        List<Chromosome> roulette(boolean isCross) {
            double[] probabilities = new double[this.size];
            List<Chromosome> selected = new ArrayList<>();
            Chromosome best = null, temp;
            double inverseFitnessSum = 0;

            // Compute the sum of the inverse fitness values
            for (Chromosome c : this.pop) {
                inverseFitnessSum += 1.0 / c.getFitness();
            }

            // Compute the probabilities based on the inverse fitness values
            for (int i = 0; i < this.size; i++) {
                double inverseFitness = 1.0 / this.pop.get(i).getFitness();
                probabilities[i] = inverseFitness / inverseFitnessSum;
            }

            int num = isCross ? this.size / 2 : this.size;
            int minFit = Integer.MAX_VALUE;

            for (int i = 0; i < num; i++) {
                temp = rouletteHelper(probabilities);
                selected.add(temp);
                if (temp.getFitness() < minFit) {
                    minFit = temp.getFitness();
                    best = temp;
                }
            }

            this.best = best;
            this.highestFit = minFit;
            return selected;
        }

        private Chromosome rouletteHelper(double[] probabilities) {
            double rand = Math.random();
            double slice = 0.0;

            for (int i = 0; i < this.size; i++) {
                slice += probabilities[i];
                if (rand <= slice) {
                    return this.pop.get(i);
                }
            }

            // Fallback if the loop above did not return a chromosome
            return this.pop.get(this.size - 1);
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
        private int augend, addend, sum;
        private int nGen;
        Solution(List<Character> ls, int augend, int addend, int sum, int x){
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
            return String.valueOf(this.augend);
        }

        @Override
        public String getAddend() {
            return String.valueOf(this.addend);
        }

        @Override
        public String getSum() {
            return String.valueOf(this.sum);
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
        if(augend==null || addend==null || sum==null)
            throw new IllegalArgumentException();
        if(augend.isBlank()||addend.isBlank()||sum.isBlank())
            throw new IllegalArgumentException();
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
        //System.out.println("shut up cutie p");
        int count=0;
        List<Character> sol=new ArrayList<>();
        SolutionI s;
        if(this.allChars.size()==1){
            for(char ch : allChars)
                sol.add(ch);
            for(int i=1; i<=9; i++)
                sol.add(' ');
            s=new Solution(sol, 0, 0, 0, 1);
            return s;
        }
        while(count< gac.getMaxGenerations()&&p.getHighestFit()!=0){ // may change highestfit to a method
            p.cross(gac);
            p.mutation(gac);
            count++;
        }
        if(p.getHighestFit()!=0){
            s=new Solution(sol, -1, -1, -1, 0);
            return s;
        }
        char[] map= p.getBest().getMap();
        for(int i=0; i<10; i++)
           sol.add(' ');
        for(int i=0; i< map.length; i++){
            if(Character.isLetter(map[i]))
                sol.add(i , map[i]);
        }
        s=new Solution(sol, p.getBest().getAug(), p.getBest().getAdd(), p.getBest().getSum(), count);
        return s;
    }
}
