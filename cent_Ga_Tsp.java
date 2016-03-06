/*
 * Title - Centralized genetic algorithm for travelling salesman problem
 *
 */
 import java.util.*;
 import java.io.*;
 import java.awt.*;
 import java.util.concurrent.*;
 import java.awt.event.*;
 import java.util.ArrayList;
 import java.util.Collections;
 
 //Main class 
 public class cent_Ga_Tsp extends Frame{
 
	private static int[] nodes; 				//Cities to be visited 
	private static int[] x;						//X coordinates of the cities on display screen
	private static int[] y;						//Y coordinates of the cities on display screen
	public static final int fheight = 700;		//Height of the display frame
	public static final int fwidth = 1000;		//Width of display frame
	public static int noIter = 200;				//Number of iterations per generation
	public static int noCity = 100;		//Number of cities to be traversed(can be given by user also)
	public static int start_node;
	public static int pSize = 100*noCity/2;		//Population size of each generation
	private static int gen = 0;					//No of generations
	private static chromosome chrom_curr;		//Current chromosome value
	public static int cost;						//Cost of the path traversed by the salesman
    private static chromosome chrom_population[];		
    private static Random rand;					//Random number for city weights to be generated
	private static long start, end;				//Start and end time of simulation
	public static final boolean output = true;
	
	//main function
	public static void main(String args[]) {
	// Read in number of iterations
	if (args.length > 0) {
	    try {
		//noIter = Integer.parseInt(args[0]);
		//noCity = Integer.parseInt(args[1]);
		start_node = Integer.parseInt(args[0]);
		pSize = 100*noCity/2;
	    } catch (NumberFormatException e) {
		System.err.println("Error in Argument");
		System.exit(1);
	    }
	}

	cent_Ga_Tsp n = new cent_Ga_Tsp();
	n.init(n);
	//System.exit(0);
    }
	 
	 //init() for the the initialization of city coordinates and weights
	 public void init(cent_Ga_Tsp n) {
	// Initialize data
	nodes = new int[noCity];			      //Creating city nodes array 
	x = new int[noCity];					  //Creating x coordinate array 
	y = new int[noCity];                      // Creating y coordinate array
	chrom_population = new chromosome[pSize]; // Creating  array of population objects for chrmosome class eaquals to population size
	
	// Seed for deterministic output
	rand = new Random(8); //To generate same sequence of random numbers

	// Set up city topology
	for (int i = 0; i < noCity; i++) {
	    x[i] = rand.nextInt(fwidth - 100) + 40;
	    y[i] = rand.nextInt(fheight - 100) + 40;
		
	}
	 
	// Set up population
	for (int i = 0; i < chrom_population.length; i++) {
	    chrom_population[i] = n.new chromosome(start_node);
	    chrom_population[i].mutate(noCity);
	}

	// Pick out the best
	Arrays.sort(chrom_population, chrom_population[0]);
	chrom_curr = chrom_population[0];

	// Window display
	n.setTitle("Centralized Traveling Salesman using Genetic Algorithm");
	n.setBackground(Color.black);
	n.resize(fwidth, fheight);
	n.addWindowListener(new WindowAdapter(){
		    public void windowClosing(WindowEvent we){
			System.exit(0);
		    }
		});
	n.show();

	// Timer start
	start = System.currentTimeMillis();

	// Loop through and evolve
	for (int p = 0; p < noIter; p++) evolve(p);

	// Timer end
	end = System.currentTimeMillis();

	// Final paint job
	repaint();

	// Output time data to file
	if (output) {
	    try {
		PrintWriter outStream = new PrintWriter(new BufferedWriter(new FileWriter("serialout1.dat", true)));
		//outStream.println(noCity + "    " + (end - start));
		outStream.printf("%-8d %-8d %-8d %-8d %-8d %-8d\n", noIter, noCity, pSize 
				 , (end - start),chrom_curr.cost, start_node);
		outStream.close();
	    } catch (IOException ioe) {
		System.err.println("IOException: " + ioe.getMessage());
	    }
	}
	if (!output) System.exit(0);

    }
	/*
	 *Function to calculate distances between individual cities by Euclidian distance formula
	 *returns the Euclidean distance
	 */
	public int distance(int c1, int c2) {
	if (c1 >= noCity) c1 = 0;
	if (c2 >= noCity) c2 = 0;

	int xdiff = x[c1] - x[c2];
	int ydiff = y[c1] - y[c2];

	return (int)Math.sqrt(xdiff*xdiff + ydiff*ydiff);
    }
	
	/*
	 *Function to perform crossover between the parents in the population
	 */
	public void evolve(int p) {
	    // Half of population selected for crossover
	    int n = chrom_population.length/2, m;
		
	    // Generate random parents of the population and wait till two different random parents are generated
	    for (m = chrom_population.length - 1; m > 1; m--) {
		int i = rand.nextInt(n), j;

		do {
		    j = rand.nextInt(n);
		} while(i == j);
		
		chrom_population[m].crossover(chrom_population[i],chrom_population[j]);//Crossover between two different parents
		chrom_population[m].mutate(noCity);//Perform random mutation of cities
	    }

	    chrom_population[1].crossover(chrom_population[0],chrom_population[1]);//Perform crossover between the remaining population at 0 and 1 position
	    chrom_population[1].mutate(noCity);//Perform random mutation of cities
	    chrom_population[0].mutate(noCity);//Perform random mutation of cities
		
	    // Pick out the strongest
	    Arrays.sort(chrom_population,chrom_population[0]);
	    chrom_curr = chrom_population[0];
	    gen++;

	    // Try not to give the user seizures
	    if (p % 50 == 0) repaint();
    }
	
	/* Function to paint the screen, nodes and the links between them
	 */
	public void paint(Graphics g) {
	g.setColor(Color.green);
	int i;
	for (i = 0; i < nodes.length; i++) g.fillRect(x[i] - 5, y[i] - 5, 7, 7);//Draw the nodes on the display and connect it through lines
	g.setColor(Color.yellow);
	for (i = 0; i < nodes.length; i++) {
	    int icity = chrom_curr.genes[i];
	    if (i != 0) {
		int last = chrom_curr.genes[i-1];
		g.drawLine(x[icity], y[icity], x[last], y[last]);//Draw lines between nodes
	    }
		
	}
	g.setColor(Color.red);
	g.drawLine(x[chrom_curr.genes[i-1]], y[chrom_curr.genes[i-1]], x[chrom_curr.genes[0]], y[chrom_curr.genes[0]]);//draw lines betwen the first and last node in the hamiltonian circuit
	// Printed information
	g.setColor(Color.yellow);
	FontMetrics fm = g.getFontMetrics();
	g.drawString("Generation: " + gen 
		     + "   Time (ms): " + (end - start) + "   Overall Cost: " + chrom_curr.cost,
		     8, fheight - fm.getHeight());
    }
	
	
	//Chromosome class consisting of genes and the cost of the path traversed as member variables
	public class chromosome implements Comparator {
	int genes[];
	int cost;
	BitSet b;
	//Function to initialize chromosomes 
	public chromosome(int start) {
		int i, j, k;
	    genes = new int[noCity];
	    b = new BitSet(noCity);
		i = start;//start node
			genes[0] = i;
		for (k = 1; k < i; k++) 
		{
			genes[k] = k;
		}
		for (j = i; j < noCity; j++) 
		{
		if(genes[0] == j)
			genes[j] = 0;
		else
			genes[j] = j;
		}
		for (k = 0; k < noCity; k++) 
		//System.out.println(genes[k]);
		
		
	    cost = cost();
	
	}
	//Function to calculate the sum of cost or distance of path traversed till now incrementally
	public int cost() {
	    int d = 0;
	    for (int i = 1; i < genes.length; i++)
		{
		d += distance(genes[i], genes[i - 1]);
		}
	    return d;
	}
	
	//Function to perform random mutation
	public void mutate(int n) {
	    // CRITICAL SECTION
	    while (--n >= 0) {
		int i1 = rand.nextInt(noCity-1), j1, k1;

		do { 
		    j1 = rand.nextInt(noCity-1); 
		} while(j1 == i1);

		int old = distance(genes[i1], genes[i1 + 1]) + distance(genes[j1], genes[j1 + 1]);
		int guess = distance(genes[i1], genes[j1]) + distance(genes[i1 + 1], genes[j1 + 1]);

		if (guess >= old) continue;

		cost -= old - guess;

		if (j1 < i1) {
		    k1 = i1;  i1 = j1;  j1 = k1;
		}

		for (; j1 > i1; j1--, i1++) {
		    int i2 = genes[i1 + 1];
		    genes[i1 + 1] = genes[j1];
		    genes[j1] = i2;
		}
	    }
	}

	public int compare(Object o1, Object o2) {
	    return ((chromosome)o1).cost - ((chromosome)o2).cost;
	}
	
	//Function to perform crossover operation between two randomly selected different parents
	public void crossover(chromosome dad, chromosome mom) {
	    int i = rand.nextInt(noCity);

	    
	    while (i < noCity - 1) {
		int child = distance(dad.genes[i], mom.genes[i+1]);

		if (child < distance(dad.genes[i], dad.genes[i+1]) &&
		    child < distance(mom.genes[i], mom.genes[i+1])) {
		    mate(dad, mom, i);
		    break;
		}

		i++;
	    }
	}

	
	//Function for mating and exchange of genetic information
	private void mate(chromosome p1, chromosome p2, int i) {
	    b.clear();

	    if (this == p2) {
		chromosome temp = p2;
		p2 = p1;
		p1 = temp;
	    }

	    for (int j = 0; j <= i; j++) {
		genes[j] = p1.genes[j];
		b.set(genes[j]);
	    }

	    int j, k = i + 1;

	    for (j = i + 1; j < genes.length; j++) {
		if (b.get(p2.genes[j])) continue;
		genes[k] = p2.genes[j];
		b.set(genes[k++]);
	    }

	    j = 0;

	    while (k < genes.length) {
		while (b.get(p2.genes[j])) j++;
		genes[k++] = p2.genes[j++];
	    }

	    cost = cost();//current cost
		//System.out.println(cost());
	}
    }
	}