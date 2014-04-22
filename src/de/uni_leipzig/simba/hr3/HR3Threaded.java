package de.uni_leipzig.simba.hr3;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HR3Threaded extends HR3 {
	
	public int numThreads;
	
	public HR3Threaded(float threshold, int granularity, int numThreads)
    {
    	super(threshold,granularity);
    	this.numThreads = numThreads;
    }
	
	public static void main(String args[]) {
		HR3Threaded hr3threaded = new HR3Threaded(HR3.DEFAULT_THRESHOLD,HR3.DEFAULT_GRANULARITY,2);
		
		//generate data
        Set<Point> source = Util.generateData(-180, 180, 3, 10000);
        Set<Point> target = Util.generateData(-180, 180, 3, 10000);
        
        
        MainMemoryMapping result = new MainMemoryMapping();
        //run hr3 threaded
        long begin = System.currentTimeMillis();
        hr3threaded.run(source, target, result);
        System.out.println("Runtime (4 threads) :"+(System.currentTimeMillis()-begin));
		
        HR3  hr3 = new HR3(HR3.DEFAULT_THRESHOLD,HR3.DEFAULT_GRANULARITY);
        begin = System.currentTimeMillis();
        MainMemoryMapping standard = new MainMemoryMapping();
        hr3.run(source, target, standard);
        System.out.println("Runtime (1 thread) :"+(System.currentTimeMillis()-begin));
	
        
        System.out.println(result.size()+" - "+standard.size);
	}
	
	@Override
	public void run(Set<Point> sourceData, Set<Point> targetData, Mapping mapping) {
        Index source = assignCubes(sourceData);
        Index target = assignCubes(targetData);
        System.out.println("|source cubes|= "+source.cubes.keySet().size());
        System.out.println("|target cubes|= "+target.cubes.keySet().size());
        ExecutorService pool = Executors.newFixedThreadPool(this.numThreads);
        for (List<Integer> cubeIndex1 : source.cubes.keySet()) {
            pool.execute(new HR3DualSourceRunnable(cubeIndex1, source, target, this.threshold, this.granularity, mapping));
        }
        try {
			pool.shutdown();
        	pool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }
	
	@Override
	public void deduplicate(Set<Point> data, Mapping mapping) {
    	 Index index = assignCubes(data);
         System.out.println("|source cubes|= "+index.cubes.keySet().size());
         ExecutorService pool = Executors.newFixedThreadPool(this.numThreads); 
         for (List<Integer> cubeIndex : index.cubes.keySet()) {
        	 pool.execute(new HR3SingleSourceRunnable(cubeIndex, index, this.threshold, this.granularity, mapping));
         }
         try {
 			pool.shutdown();
         	pool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
    }
}

class HR3DualSourceRunnable implements Runnable {
	private List<Integer> cubeIndex1;
	private Index source;
    private Index target;
    private float threshold;
    private int granularity;
    private Mapping mapping;
    
    
    public HR3DualSourceRunnable(List<Integer> cubeIndex1, Index source, Index target, float threshold, int granularity, Mapping m) {
    	this.cubeIndex1 = cubeIndex1;
    	this.source = source;
        this.target = target;
        this.threshold = threshold;
        this.granularity = granularity;
        this.mapping = m;
    }
    
    public void run() {
    	float d; 
		Hypercube h1 = source.getCube(cubeIndex1);
        List<List<Integer>> cubes = HR3.getCubesToCompare(cubeIndex1,granularity);
        for (List<Integer> cubeIndex2 : cubes) {
        	Hypercube h2 = target.getCube(cubeIndex2);
            if (h2 != null) {
                for (Point a : h1.elements) {
                    for (Point b : h2.elements) {
                        d = HR3.computeDistance(a, b);
                        if (d <= threshold) {
                        	this.mapping.add(a.label, b.label, d);
                        }
                    }
                }
            }
        }
	}
}

class HR3SingleSourceRunnable implements Runnable {
	private List<Integer> cubeIndex1;
	private Index index;
    private float threshold;
    private int granularity;
    private Mapping mapping;
    
    public HR3SingleSourceRunnable(List<Integer> cubeIndex1, Index index, float threshold, int granularity, Mapping m) {
    	this.cubeIndex1 = cubeIndex1;
    	this.index = index;
        this.threshold = threshold;
        this.granularity = granularity;
        this.mapping = m;
    }
    
    
    public void run() {
    	float d; 
		Hypercube h1 = index.getCube(cubeIndex1);
        List<List<Integer>> cubes = HR3.getCubesToCompare(cubeIndex1,granularity);
        for (List<Integer> cubeIndex2 : cubes) {
            Hypercube h2 = index.getCube(cubeIndex2);
            // only run if the hypercube actually exists
            if (h2 != null) {
            	int cmp= HR3.compareCubes(cubeIndex1, cubeIndex2);
            	if (cmp<0) {
                    for (Point a : h1.elements) {
                        for (Point b : h2.elements) {
                            d = HR3.computeDistance(a, b);
                            if (d <= threshold) {
                                mapping.addSingleSource(a.label, b.label, d);
                            }
                        }
                    }
            	}
            	else if(cmp==0)
            	{
            		List<Point> points= new ArrayList<Point>(h1.elements);
                	for (int i=0; i<points.size()-1; i++) {
                        Point p1= points.get(i);
                		for (int j=i+1; j<points.size(); j++) {
                			Point p2= points.get(j);
                            d = HR3.computeDistance(p1, p2);
                            if (d <= threshold) {
                                mapping.addSingleSource(p1.label, p2.label, d);
                            }
                        }
                    }
            	}
            }
        }
    }
}
