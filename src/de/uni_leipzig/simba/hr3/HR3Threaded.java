package de.uni_leipzig.simba.hr3;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HR3Threaded extends HR3 {
	public int numThreads;
	private ExecutorService pool;
	public boolean useMapMerge;
	public int elementsCountForThreading;
	public boolean extremePool;

	public HR3Threaded(float threshold, int granularity, int numThreads)
	{
		super(threshold,granularity);
		this.numThreads = numThreads;
		this.pool = Executors.newFixedThreadPool(this.numThreads);
		this.useMapMerge = false;
		this.extremePool = false;
		this.elementsCountForThreading = 10000;
	}

	public HR3Threaded(float threshold, int granularity, int numThreads, boolean useMapMerge)
	{
		super(threshold,granularity);
		this.numThreads = numThreads;
		this.pool = Executors.newFixedThreadPool(this.numThreads);
		this.useMapMerge = useMapMerge;
		this.elementsCountForThreading = 10000;
		this.extremePool = false;
	}

	public HR3Threaded(float threshold, int granularity, int numThreads, boolean useMapMerge, int elementsCountForThreading, boolean extremePool)
	{
		super(threshold,granularity);
		this.numThreads = numThreads;
		this.pool = Executors.newFixedThreadPool(this.numThreads);
		this.useMapMerge = useMapMerge;
		this.elementsCountForThreading = elementsCountForThreading;
		this.extremePool = extremePool;
	}

	public static void main(String args[]) {
		int threads = 32;
		int curThreads = 0;
		int idx = 0;
		long begin = 0;
		long duration = 0;
		HR3Threaded hr3threaded = null;
		HR3 hr3 = null;
		MainMemoryMapping result = null;

		//generate data
		Set<Point> source = Util.generateData(0, 180.f, 2, 50000);
		Set<Point> target = Util.generateData(0, 180.f, 2, 50000);

		Vector<Integer>	times = new Vector<Integer>();
		Vector<Integer>	results = new Vector<Integer>();
		Vector<String>	descriptions = new Vector<String>();

		//run hr3 threaded with merge
		for (curThreads=threads ; curThreads>1 ; curThreads/=4) {
			hr3threaded = new HR3Threaded(HR3.DEFAULT_THRESHOLD, HR3.DEFAULT_GRANULARITY, curThreads, true);
			result = new MainMemoryMapping();

			begin = System.currentTimeMillis();
			hr3threaded.run(source, target, result);
			duration = System.currentTimeMillis()-begin;

			times.add((int)duration);
			descriptions.add("threads "+curThreads+", merge");
			results.add(result.size());
			System.out.println("Runtime ("+curThreads+" threads, merge): "+duration);
		}

		//run hr3 threaded with merge, extreme pool
		for (curThreads=threads ; curThreads>1 ; curThreads/=4) {
			hr3threaded = new HR3Threaded(HR3.DEFAULT_THRESHOLD, HR3.DEFAULT_GRANULARITY,
				curThreads, true, 10000, true);
			result = new MainMemoryMapping();

			begin = System.currentTimeMillis();
			hr3threaded.run(source, target, result);
			duration = System.currentTimeMillis()-begin;

			times.add((int)duration);
			descriptions.add("threads "+curThreads+", merge, extreme pool");
			results.add(result.size());
			System.out.println("Runtime ("+curThreads+" threads, merge, extreme pool): "+duration);
		}

		//run hr3 threaded without merge
		for (curThreads=threads ; curThreads>1 ; curThreads/=4) {
			hr3threaded = new HR3Threaded(HR3.DEFAULT_THRESHOLD, HR3.DEFAULT_GRANULARITY, curThreads, false);
			result = new MainMemoryMapping();

			begin = System.currentTimeMillis();
			hr3threaded.run(source, target, result);
			duration = System.currentTimeMillis()-begin;

			times.add((int)duration);
			descriptions.add("threads "+curThreads+", w/o merge");
			results.add(result.size());
			System.out.println("Runtime ("+curThreads+" threads, w/o merge): "+duration);
		}

		//run hr3 threaded without merge, extreme pool
		for (curThreads=threads ; curThreads>1 ; curThreads/=4) {
			hr3threaded = new HR3Threaded(HR3.DEFAULT_THRESHOLD, HR3.DEFAULT_GRANULARITY,
				curThreads, true, 10000, true);
			result = new MainMemoryMapping();

			begin = System.currentTimeMillis();
			hr3threaded.run(source, target, result);
			duration = System.currentTimeMillis()-begin;

			times.add((int)duration);
			descriptions.add("threads "+curThreads+", w/o merge, extreme pool");
			results.add(result.size());
			System.out.println("Runtime ("+curThreads+" threads, w/o merge, extreme pool): "+duration);
		}

		//run hr3 single
		hr3 = new HR3(HR3.DEFAULT_THRESHOLD,HR3.DEFAULT_GRANULARITY);
		result = new MainMemoryMapping();

		begin = System.currentTimeMillis();
		hr3.run(source, target, result);
		duration = System.currentTimeMillis()-begin;

		descriptions.add("single, w/o merge");
		times.add((int)duration);
		results.add(result.size());

		//System.out.println("Runtime (1 thread): "+(System.currentTimeMillis()-begin));
		//System.out.println(result.size()+" - "+standard.size);
		
		String csv = "";

		for (int i=0 ; i<descriptions.size() ; i++) {
			csv += descriptions.elementAt(i);
			if (i < times.size()-1)
				csv += ";";
		}
		csv += "\n";

		for (int i=0 ; i<times.size() ; i++) {
			csv += times.elementAt(i);
			if (i < results.size()-1)
				csv += ";";
		}
		csv += "\n";

		for (int i=0 ; i<results.size() ; i++) {
			csv += results.elementAt(i);
			if (i < results.size()-1)
				csv += ";";
		}
		csv += "\n";

		System.out.print("\n"+csv+"\n");
	}

	@Override
	public void run(Set<Point> sourceData, Set<Point> targetData, Mapping mapping) {
		long begin = 0;
		Index source = assignCubes(sourceData);
		Index target = assignCubes(targetData);

		System.out.println("|source cubes|= "+source.cubes.keySet().size());
		System.out.println("|target cubes|= "+target.cubes.keySet().size());
		Vector<Mapping> m = new Vector<Mapping>();

		begin = System.currentTimeMillis();

		for (List<Integer> cubeIndex1 : source.cubes.keySet()) {
			this.pool.execute(new HR3DualSourceRunnable(cubeIndex1,
							source,
							target,
							this.threshold,
							this.granularity,
							this.pool,
							mapping,
							this.useMapMerge,
							this.elementsCountForThreading));

		}

		try {
			pool.shutdown();
			pool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// System.out.println("Comparsion took "+(System.currentTimeMillis()-begin)+"s");
	}

	@Override
	public void deduplicate(Set<Point> data, Mapping mapping) {
		Index index = assignCubes(data);
		System.out.println("|source cubes|= "+index.cubes.keySet().size());
		for (List<Integer> cubeIndex : index.cubes.keySet()) {
			this.pool.execute(new HR3SingleSourceRunnable(
						cubeIndex,
						index,
						this.threshold,
						this.granularity,
						mapping));
		}
		try {
			this.pool.shutdown();
			this.pool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

class HR3PointCompareRunnable implements Runnable {
	private Point point;
	private Hypercube cube;
	private Mapping mapping;
	private float threshold;

	public HR3PointCompareRunnable(Point source, Hypercube cube, float threshold, Mapping m) {
		this.point = source;
		this.cube = cube;
		this.mapping = m;
		this.threshold = threshold;
	}

	public void run() {
		// if (this.cube.elements.size() > 100)
		//	System.out.println("runging point compare for "+this.cube.elements.size());
		String targetLabel = null;
		double targetSimilarity = -1.0;

		for (Point b : this.cube.elements) {
			float d = HR3.computeDistance(this.point, b);
			if (d <= this.threshold) {
				targetLabel = b.label;
				targetSimilarity = d;
			}
		}

		if (targetLabel != null)
			this.mapping.add(this.point.label, targetLabel, targetSimilarity);
	}
}

class HR3DualSourceRunnable implements Runnable {
	private List<Integer> cubeIndex1;
	private Index source;
	private Index target;
	private float threshold;
	private int granularity;
	private Mapping mapping;
	private ExecutorService pool;

	public static boolean useMapMergeStatic = false;
	public static int elementsCountForThreadingStatic = 100000;
	public boolean useMapMerge;
	public int elementsCountForThreading;

	public HR3DualSourceRunnable(List<Integer> cubeIndex1, Index source, Index target, float threshold, int granularity, ExecutorService pool, Mapping m) {
		this.cubeIndex1 = cubeIndex1;
		this.source = source;
		this.target = target;
		this.threshold = threshold;
		this.granularity = granularity;
		this.pool = pool;
		this.mapping = m;
		this.useMapMerge = useMapMergeStatic;
		this.elementsCountForThreading = elementsCountForThreadingStatic;
	}

	public HR3DualSourceRunnable(List<Integer> cubeIndex1, Index source, Index target, float threshold, int granularity, ExecutorService pool, Mapping m, boolean useMapMerge, int elementsCountForThreading) {
		this.cubeIndex1 = cubeIndex1;
		this.source = source;
		this.target = target;
		this.threshold = threshold;
		this.granularity = granularity;
		this.pool = pool;
		this.mapping = m;
		this.useMapMerge = useMapMerge;
		this.elementsCountForThreading = elementsCountForThreading;
	}

	public static int threadCount = 0;

	public synchronized void increaseThreadCount() {
		HR3DualSourceRunnable.threadCount++;
	}

	public synchronized void decreaseThreadCount() {
		HR3DualSourceRunnable.threadCount--;
	}


	public void run() {
		//increaseThreadCount();

		float d; 
		Hypercube h1 = source.getCube(cubeIndex1);
		List<List<Integer>> cubes = HR3.getCubesToCompare(cubeIndex1,granularity);
		ExecutorService pool = this.pool;
		Mapping usedMap = this.mapping;

		if (this.useMapMerge == true) {
			usedMap = new MainMemoryMapping();
			pool = Executors.newFixedThreadPool(2);
		} else {
			usedMap = this.mapping;
		}
		
		for (List<Integer> cubeIndex2 : cubes) {
			Hypercube h2 = target.getCube(cubeIndex2);

			if (h2 != null) {
				for (Point a : h1.elements) {
					if (this.pool != null && h2.elements.size() > this.elementsCountForThreading) {
						pool.execute(new HR3PointCompareRunnable(a, h2, this.threshold, usedMap));
					} else {
						for (Point b : h2.elements) {
							d = HR3.computeDistance(a, b);
							if (d <= threshold) {
								usedMap.add(a.label, b.label, d);
							}
						}
					}
				}
			}
		}

		if (this.useMapMerge == true) {
			/*if (this.pool != null) {
				try {
					pool.shutdown();
					pool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}*/

			this.mapping.merge(usedMap);
		}

		// System.out.println("threads running "+HR3DualSourceRunnable.threadCount);
			
		// decreaseThreadCount();
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
