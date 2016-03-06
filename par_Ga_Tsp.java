/*
 *
 * Title : TSP using parallel genetic algorithm
 *
 */


import java.util.*;
import java.util.concurrent.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class par_Ga_Tsp extends Frame {
    public static final boolean outputFlag = true;				//Flag to indicate outputting data to file
    public static final boolean guiFlag = true;					//Gui flag for closing the gui window
    public static final boolean threadedCitiesFlag = true;		//Flag to be considered for city setup
    public static final boolean threadedEvolveFlag = true;		//Flag to be considered for evolution 
    public static int cost;										//Cost of the path traversed
    public static final int width = 1000;						//Width of the frame in which simulation is shown
    public static final int height = 700;						//Height of the frame in which simulation is shown
    public static int numIter = 200;
    public static int numCities = 100;
    public static int popSize = 100*numCities;
    public static int numThreads = Runtime.getRuntime().availableProcessors(); //processors available to the Java virtual machine
    public static CyclicBarrier barrier; //Allows multiple thread to wait before proceding
	public static int start_node;//For fixing the start node from command line
    private static int[] cities, x, y; //X and Y coordinates of cities on diaplay screen
    private static chromosome current, population[];
    private static Random rand;
    private static int generation = 0;
    private static long startTime, endTime;

    public class citySetupThread implements Runnable {
	int start, end;
	
	//Constructor
	public citySetupThread(int s, int e) {
	    start = s;
	    end = e;
	}
	public void run() {
	    //run() method is called by the start() once the thread start running to go into running state
		
	    for (int j = start; j < end; j++) {
		x[j] = ThreadLocalRandom.current().nextInt(0, width - 100) + 40; //Randomly generated number between 1 to (width - 100)+40
		y[j] = ThreadLocalRandom.current().nextInt(0, height - 100) + 40;//Randomly generated number between 1 to (width - 100)+40
		
	    }

	    // Waiting for other thread to complete
	    try {
		barrier.await(); //wait for other threads to complete
	    } catch (InterruptedException ie) {
		return;
	    } catch (BrokenBarrierException bbe) {
		return;
	    }

	}
    }

    public class evolveThread implements Runnable {
	int start, end;
	
	//Constructor
	public evolveThread(int s, int e) {
	    start = s;
	    end = e;
	}
	public void run() {
	    // Take half of the population
	    int n = population.length/2, m;

	    for (m = start; m > end; m--) {
		int i, j;
		i = ThreadLocalRandom.current().nextInt(0, n);

		do {
		    j = ThreadLocalRandom.current().nextInt(0, n);//Randomly generated number between 1 to (width - 100)+40
		} while(i == j);

		population[m].crossover(population[i], population[j]);
		population[m].mutate(numCities);
	    }
	    
	    
	    try {
		barrier.await();//wait for other threads to complete
	    } catch (InterruptedException ie) {
		return;
	    } catch (BrokenBarrierException bbe) {
		return;
	    }

	}
    }

    public static void main(String args[]) {
	// We're timing this
	startTime = System.currentTimeMillis();

	// Read in number of iterations
	if (args.length > 0) {
	    try {
		//numIter = Integer.parseInt(args[0]);
		//numCities = Integer.parseInt(args[1]);
		//popSize = Integer.parseInt(args[2]);
		//numThreads = Integer.parseInt(args[3]);
		start_node = Integer.parseInt(args[0]);
	    } catch (NumberFormatException e) {
		System.err.println("Argument" + " must be an int.");
		System.exit(1);
	    }
	}

	// Using Merge sort instead of Tim sort for Arrays.sort()
	System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");

	// Set up thread pool
	final ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(numThreads*numIter);
	ThreadPoolExecutor tpool = new ThreadPoolExecutor(numThreads, numThreads, 10, 
							  TimeUnit.SECONDS,queue);//ThreadPoolExecuter class keeps a pool of worker threads.  it contains a queue that keeps tasks waiting to get executed.

	// Create a barrier for the threads
	barrier = new CyclicBarrier(numThreads + 1);

	
	par_Ga_Tsp k = new par_Ga_Tsp();
	k.init(k, tpool);

	// Done here, let's go home
	tpool.shutdown();

	// Stop timing
	endTime = System.currentTimeMillis();

	// Output time data to file
	if (outputFlag) {
	    try {
		String spacer = "    ";
		PrintWriter outStream = new PrintWriter(new BufferedWriter(new FileWriter("parout1.dat", true)));
		outStream.printf("%-8d %-8d %-8d %-8d %-8d %-8d %-8d\n", numIter, numCities, popSize, 
				 numThreads, (endTime - startTime),current.cost, start_node);
		outStream.close();
	    } catch (IOException ioe) {
		System.err.println("IOException: " + ioe.getMessage());
	    }
	}
	/*public class GraphingData extends JPanel {
    int[] data = {(endTime - startTime),cost
        
    };
    final int PAD = 20;

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        // Draw ordinate.
        g2.draw(new Line2D.Double(PAD, PAD, PAD, h-PAD));
        // Draw abcissa.
        g2.draw(new Line2D.Double(PAD, h-PAD, w-PAD, h-PAD));
        double xInc = (double)(w - 2*PAD)/(data.length-1);
        double scale = (double)(h - 2*PAD)/getMax();
        // Mark data points.
        g2.setPaint(Color.red);
        for(int i = 0; i < data.length; i++) {
            double x = PAD + i*xInc;
            double y = h - PAD - scale*data[i];
            g2.fill(new Ellipse2D.Double(x-2, y-2, 4, 4));
        }
    }

    private int getMax() {
        int max = -Integer.MAX_VALUE;
        for(int i = 0; i < data.length; i++) {
            if(data[i] > max)
                max = data[i];
        }
        return max;
    }

    public static void main(String[] args) {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(new GraphingData());
        f.setSize(400,400);
        f.setLocation(200,200);
        f.setVisible(true);
    }
*/
	
	if (!guiFlag) System.exit(0);
    }

    public void init(par_Ga_Tsp k, ThreadPoolExecutor tpool) {
	// Initialize data
	cities = new int[numCities];
	x = new int[numCities];
	y = new int[numCities];
	population = new chromosome[popSize];
	
	// Seed for deterministic output by putting a constant arg
	rand = new Random(8);

	//if(threadedCitiesFlag){
 if(false){
	    // Threaded city setup
	    int errorCities = 0, stepCities = 0;
	    stepCities = numCities/numThreads; //Assigning number of cities for each thread 
	    errorCities = numCities - stepCities*numThreads; //If the number of threads doesnot venly divide no of cities errorCities have no of remainder citiesf
           
	    // Split up work, assign to threads
	    for (int i = 1; i <= numThreads; i++) {
		int startCities = (i-1)*stepCities;
		int endCities = startCities + stepCities;

		// Execute thread pool
		if(i <= numThreads) endCities += errorCities;
		tpool.execute(new citySetupThread(startCities, endCities));
	    }

	    // Wait for other threads to complete
	    try {
		barrier.await();
	    } catch (InterruptedException ie) {
		return;
	    } catch (BrokenBarrierException bbe) {
		return;
	    }

	} else {
	    // Set up city topology, make sure no one falls off the edge
	    for (int i = 0; i < numCities; i++) {
		x[i] = rand.nextInt(width - 100) + 40;
		y[i] = rand.nextInt(height - 100) + 40;
		
	    }
		for (int i = 0; i < numCities; i++) 
	    System.out.println(x[i]+"\n");
	}

	// Set up population
	for (int i = 0; i < population.length; i++) {
	    population[i] = k.new chromosome(start_node);
	    population[i].mutate(numCities);
	}

	// Pick out the strongest
	Arrays.sort(population, population[0]);
	current = population[0];

	if (guiFlag) {
	    // Windowing stuff
	    k.setTitle("Parallel Traveling Salesman using Genetic Algorithm");
	    k.setBackground(Color.black);
	    k.setSize(width, height);
	    k.addWindowListener(new WindowAdapter(){
		    public void windowClosing(WindowEvent we){
			System.exit(0);
		    }
		});
	    k.setVisible(true);
	}

	// Loop through 
	for (int p = 0; p < numIter; p++) evolve(p, tpool);

	// Final paint job
	if (guiFlag) repaint();
    }

    public void evolve(int p, ThreadPoolExecutor tpool) {
	if (threadedEvolveFlag) {
	    // Threaded inner loop
	    int startEvolve = popSize - 1,
		endEvolve = (popSize - 1) - (popSize - 1)/numThreads;

	    // Split up work, assign to threads
	    for (int i = 0; i < numThreads; i++) {
		endEvolve = (popSize - 1) - (popSize - 1)*(i + 1)/numThreads + 1;
		tpool.execute(new evolveThread(startEvolve, endEvolve));
		startEvolve = endEvolve;
	    }

	    // Wait for others
	    try {
		barrier.await();
	    } catch (InterruptedException ie) {
		return;
	    } catch (BrokenBarrierException bbe) {
		return;
	    }

	} else {
	    // Get top half for random number bounds
	    int n = population.length/2, m;

	    // Go through entire population backwards, replace them with children of top half parents
	    for (m = population.length - 1; m > 1; m--) {
		// Two random parents, i and j
		int i = rand.nextInt(n), j;
		do {
		    j = rand.nextInt(n);
		} while(i == j);

		// Assign child genes from parents i and j, then mutate
		population[m].crossover(population[i], population[j]);
		population[m].mutate(numCities);
	    }
	}

	// Strongest child
	population[1].crossover(population[0], population[1]);
	population[1].mutate(numCities);
	population[0].mutate(numCities);

	// Pick out the strongest
	Arrays.sort(population, population[0]);
	current = population[0];
	generation++;

	// Redo our paint-job if needed
      	if (guiFlag) repaint();
    }

    public void paint(Graphics g) {
	// Line color
	g.setColor(Color.green);
	int i;
	// Fill in node graphic
	for (i = 0; i < cities.length; i++) g.fillRect(x[i] - 5, y[i] - 5, 7, 7);
	g.setColor(Color.yellow);
	// Set up edges
	for (i = 0; i < cities.length; i++) {
	    int icity = current.genes[i];
	    if (i != 0) {
		int last = current.genes[i - 1];
		g.drawLine(x[icity], y[icity], x[last], y[last]);
	    }
	}
	g.setColor(Color.red);
	g.drawLine(x[current.genes[i - 1]], y[current.genes[i - 1]], x[current.genes[0]], y[current.genes[0]]);
	// Printed information
	g.setColor(Color.yellow);
	FontMetrics fm = g.getFontMetrics();
	g.drawString("Generation: " + generation 
		     + "    Time: " + (endTime - startTime) + " ms"
		     + "    Overall Cost: " + current.cost,
		     8, height - fm.getHeight());
    }

    // Find distance between two cities
    public int distance(int m, int n) {
	if (m >= numCities) m = 0;
	if (n >= numCities) n = 0;

	int xdiff = x[m] - x[n];
	int ydiff = y[m] - y[n];

	return (int)Math.sqrt(xdiff*xdiff + ydiff*ydiff);
    }

    public class chromosome implements Comparator {
	int genes[];
	int cost;

	public chromosome(int start) {
		int i, j, k;
	    genes = new int[numCities];
	    b = new BitSet(numCities);
	    //for (int i = 0; i < numCities; i++) genes[i] = i;
		i = start;//start node
			genes[0] = i;
		for (k = 1; k < i; k++) 
		{
			genes[k] = k;
		}
		for (j = i; j < numCities; j++) 
		{
		if(genes[0] == j)
			genes[j] = 0;
		else
			genes[j] = j;
		}
		for (k = 0; k < numCities; k++) 
		//System.out.println(genes[k]);
	    cost = cost();
	}

	public int cost() {
	    int d = 0;

	    // Calculate distance to walk along path
	    for (int i = 1; i < genes.length; i++)
		d += distance(genes[i], genes[i - 1]);
	    return d;
	}

	public void mutate(int n) {
	    // Loop through numCities
	    while (--n >= 0) {
		int p, q, r, s;

		// Pick two random cities
		if (threadedEvolveFlag) p = ThreadLocalRandom.current().nextInt(0, numCities - 1);
		else p = rand.nextInt(numCities - 1);
		do { 
		    if (threadedEvolveFlag) q = ThreadLocalRandom.current().nextInt(0, numCities - 1);
		    else q = rand.nextInt(numCities - 1); 
		} while(q == p);

		// Scramble the cost function (distances initialize from p --> p + 1 == 1 shown inside
		// chromosome constructor)
		int old = distance(genes[p], genes[p + 1]) + distance(genes[q], genes[q + 1]);
		int guess = distance(genes[p], genes[q]) + distance(genes[p + 1], genes[q + 1]);

		// Negative feedback selection
		if (guess >= old) continue;

		// Adjust cost
		cost -= old - guess;

		// p must be less than q
		if (q < p) {
		    r = p;
		    p = q;
		    q = r;
		}

		// Start from random points, converge inward while swapping symmetrically
		for (; q > p; q--, p++) {
		    s = genes[p + 1];
		    genes[p + 1] = genes[q];
		    genes[q] = s;
		}
	    }
	}

	public int compare(Object a, Object b) {
	    // Use for sorting
	    return ((chromosome)a).cost - ((chromosome)b).cost;
	}

	public void crossover(chromosome dad, chromosome mom) {
	    int i;

	    if (threadedEvolveFlag) i = ThreadLocalRandom.current().nextInt(0, numCities);
	    else i = rand.nextInt(numCities);

	    // Start at a random city, find a closer city or march to the end
	    while (i < numCities - 1) {
		// Pick our child well
		int child = distance(dad.genes[i], mom.genes[i+1]);

		
		if (child < distance(dad.genes[i], dad.genes[i+1]) &&
		    child < distance(mom.genes[i], mom.genes[i+1])) {
		    mate(dad, mom, i);
		    break;
		}

		
		i++;
	    }
	}

	BitSet b;

	private void mate(chromosome dad, chromosome mom, int i) {
	    b.clear();

	    if (this == mom) {
		chromosome temp = mom;
		mom = dad;
		dad = temp;
	    }

	    // Assign father's part to child
	    for (int j = 0; j <= i; j++) {
		genes[j] = dad.genes[j];
		b.set(genes[j]);
	    }

	    int j, k = i + 1;

	    // Assign mother's part to child
	    for (j = i + 1; j < genes.length; j++) {
		if (b.get(mom.genes[j])) continue;
		genes[k] = mom.genes[j];
		b.set(genes[k++]);
	    }

	    j = 0;

	    // Iterate over till we hit a "zero" in the bitfield for mom, then 
	    // replace that one. Rinse and repeat.
	    while(k < genes.length && 
		  j < mom.genes.length) {
		while(j < mom.genes.length - 1 && 
		      b.get(mom.genes[j])) 
		    j++;
		genes[k] = mom.genes[j];
		k++;
		j++;
	    }

	    // Update cost for walking the path
	    cost = cost();
		//System.out.println(cost());
	}
    }
}
