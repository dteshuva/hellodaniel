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

        void setMap(int ind1, int ind2){
            char temp=this.mapDigit[ind1];
            this.mapDigit[ind1]=this.mapDigit[ind2];
            this.mapDigit[ind2]=temp;
        }

        private int getValue(Character ch){
            for(int i=0; i<this.mapDigit.length; i++){
                if(mapDigit[i]==ch)
                    return i;
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
    } // end of chromosome
    private class Population{
        int size;
        Chromosome [] pop;
        int highestFit;
        boolean solutionFound;
        Chromosome best;
        // creating random populations of
        Population(int populationSize){
            this.solutionFound=false;
            this.highestFit=Integer.MAX_VALUE;
            this.size=populationSize;
            int dig;
            this.pop=new Chromosome[populationSize];
            Chromosome c;
            int count=0;
            while(count<populationSize){
                int[]usedDigs=new int[10];  // count of used digits
                char[] map=new char[10];  // mapping of digits to chars
                for(char ch : ArithmeticPuzzle.this.allChars){
                  dig=new Random().nextInt(10);
                  // if char is used already, re-pick a char until a not used one
                  while(usedDigs[dig]==1){
                      dig=new Random().nextInt(10);
                  }
                  map[dig]=ch;
                  usedDigs[dig]=1;
                }
                c=new Chromosome(map);
                pop[count]=c;
                count++;
                if(c.getFitness()==0){
                    this.best=c;
                    this.solutionFound=true;
                }
            }
        }
        void mutation(GeneticAlgorithmConfig gac){
            double rand;
            for(Chromosome c : this.pop) {
                rand = Math.random();
                if (rand > gac.getMutationProbability())
                    continue;
                Random r = new Random();
                int par1 = r.nextInt(10), par2 = r.nextInt(10); // 2 random genes
                while(par1==par2)
                    par1 = r.nextInt(10);
                c.setMap(par1, par2);
                c.reCalculateFitness();
                if (c.getFitness() == 0) {
                    this.solutionFound = true;
                    this.best = c;
               }
            }
        }
        void cross(GeneticAlgorithmConfig gac){
            double rand;
            for(int i=0; i<this.size-1; i+=2){
                rand = Math.random();
                if(rand>gac.getCrossoverProbability())
                    continue;
                this.pop[i]=crossover(this.pop[i], this.pop[i+1]);
                this.pop[i+1]=crossover(this.pop[i+1], this.pop[i]);
            }
        }
        private Chromosome crossover(Chromosome parent1, Chromosome parent2) {
            int len = 10;
            Random r = new Random();
            int crossoverPoint = r.nextInt(8) + 1;

            char[] childGenes = new char[len];
            Set<Character> usedDigits = new HashSet<>();
            // Copy genes before the crossover point from parent1 to child
            for (int i = 0; i < crossoverPoint; i++) {
                childGenes[i] = parent1.getMap()[i];
                usedDigits.add(parent1.getMap()[i]);

            }

            // Copy remaining genes from parent2 to child1 and from parent1 to child2
            int pos=crossoverPoint;
            for (int i = 0; i < len; i++) {
                char digit = parent2.getMap()[i];

                if (!usedDigits.contains(digit)) {
                    childGenes[pos] = digit;
                    usedDigits.add(digit);
                    pos++;
                }
            }
                Chromosome c = new Chromosome(childGenes);
                if (c.getFitness() == 0) {
                    this.solutionFound = true;
                    this.best = c;
                }
                return c;

        }


        void tournament(){
            Chromosome [] selected = new Chromosome[this.size];
            int comp = 3;

            for (int i = 0; i < this.size; i++) {
                Chromosome best = null;
                for (int j = 0; j < comp; j++) {
                    int index = (int) (Math.random() * size);
                    Chromosome competitor = pop[index];
                    if (best == null || competitor.getFitness() < best.getFitness()) {
                        best = competitor;
                    }
                }
                selected[i]=best;
            }
            this.pop=selected;
        }

        void roulette() {
            double[] probabilities = new double[this.size];
            Chromosome [] selected = new Chromosome[this.size];
            Chromosome temp;
            double inverseFitnessSum = 0;

            // Compute the sum of the inverse fitness values
            for (Chromosome c : this.pop) {
                inverseFitnessSum += 1.0 / c.getFitness();
            }

            // Compute the probabilities based on the inverse fitness values
            for (int i = 0; i < this.size; i++) {
                double inverseFitness = 1.0 / this.pop[i].getFitness();
                probabilities[i] = inverseFitness / inverseFitnessSum;
            }

            for (int i = 0; i < this.size; i++) {
                temp = rouletteHelper(probabilities);
                selected[i] = temp;
            }

            this.pop=selected;
        }

        private Chromosome rouletteHelper(double[] probabilities) {
            double rand = Math.random();
            double slice = 0.0;

            for (int i = 0; i < this.size; i++) {
                slice += probabilities[i];
                if (rand <= slice) {
                    return this.pop[i];
                }
            }

            // Fallback if the loop above did not return a chromosome
            return this.pop[this.size-1];
        }

        Chromosome getBest(){
            return this.best;
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
            s=new Solution(sol, this.augend, this.addend, this.sum, 1);
            return s;
        }
        while(count< gac.getMaxGenerations() && !p.solutionFound){ // may change highestfit to a method
            if(gac.getSelectionType()== GeneticAlgorithmConfig.SelectionType.ROULETTE){
                p.roulette();
            }
            else{
                p.tournament();
            }
            p.cross(gac);
            p.mutation(gac);
            count++;
        }
        if(!p.solutionFound){
            s=new Solution(sol, this.augend, this.addend, this.sum, 0);
            return s;
        }
        char[] map= p.getBest().getMap();
        for(int i=0; i<10; i++)
           sol.add(' ');
        for(int i=0; i< map.length; i++){
            if(Character.isLetter(map[i]))
                sol.set(i , map[i]);
        }
        s=new Solution(sol, this.augend, this.addend, this.sum, count);
        return s;
    }
}
