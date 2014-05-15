package de.uni_leipzig.simba.hr3;

import java.io.File;
import java.io.IOException;

import de.uni_leipzig.simba.hr3.Mapping.DiskMapping;
import de.uni_leipzig.simba.hr3.Mapping.NoOpMapping;
import de.uni_leipzig.simba.hr3.Util.Config;

public class HR3Starter {

	public static void main(String[] args) throws IOException {
		Config config = Util.parseArgs(args);
		long begin, end;

		HR3 hr3;
		if (config.numThreads < 0) {
			if (!config.benchmark)
				System.out.println("Single-threaded execution mode");
			hr3 = new HR3(config.threshold, config.granularity);

		} else {
			if (!config.benchmark) {
				System.out.println("Multi-threaded execution mode");
				System.out.println("Running on " + config.numThreads + " threads");
			}

			hr3 = new HR3Threaded(config.threshold, config.granularity, config.numThreads);
		}

		Mapping m = config.mapping;
		begin = System.currentTimeMillis();

		if (config.target == null) {
			if (!config.benchmark)
				System.out.println("Deduplication\n");
			hr3.deduplicate(config.source, m);
		} else {
			if (!config.benchmark) {
				System.out.println("Deduplication\n");
				System.out.println("Dual source matching\n");
			}
			hr3.run(config.source, config.target, m);
		}

		config.mapping.close();
		end = System.currentTimeMillis();

		if (!config.benchmark) {
			System.out.println("|source cubes|= "+hr3.getSourceCubes());
			if (config.target == null)
				System.out.println("|target cubes|= "+hr3.getTargetCubes());
			System.out.println("HR3 took " + (end - begin) + " ms for " + m.size() + " correspondences");
		} else {
			// csv time stats
			if (config.benchmarkHeader) {
				System.out.println("threshold;granularity;source cubes;target cubes;threads;extreme pool;extreme pool threshold;map merge;duration;correspondences");
			}

			System.out.print(config.threshold+";");
			System.out.print(config.granularity+";");
			System.out.print(hr3.getSourceCubes()+";");

			if (config.target == null)
				System.out.print(hr3.getTargetCubes()+";");
			else 
				System.out.print(";");

			System.out.print(config.numThreads+";");
			System.out.print((config.extremepool ? "1" : "0") + ";");
			System.out.print(""+config.extremepoolthreshold + ";");
			System.out.print(""+config.mapmerge + ";");
			System.out.print(""+(end - begin)+";");
			System.out.print(m.size());
			System.out.println();
		}

		if (config.bruteForce) {
			//run brute force
			begin = System.currentTimeMillis();
			Mapping m2;
			if (m instanceof MainMemoryMapping) {
				m2 = new MainMemoryMapping();
			} else if (m instanceof DiskMapping) {
				String dirName = ((DiskMapping) m).getOutputDirName();
				dirName = dirName.endsWith(File.separator) ? dirName : dirName + File.separator;
				m2 = new DiskMapping(new File(dirName + "brute_force" + (config.writeCSV ? ".csv" : ".txt")), config.writeCSV);
			} else {
				m2 = new NoOpMapping();
			}

			if (config.target == null) {
				hr3.runBruteForce(config.source, m2);
			} else {
				hr3.runBruteForce(config.source, config.target, m2);
			}
			end = System.currentTimeMillis();

			m2.close();

			System.out.println("\nBrute force took " + (end - begin) + " ms for " + m2.size() + " corrs.");

			//nothing should come out of here. Just checking whether HR3 missed something.
			if (m.size() != m2.size()) {
				System.err.println("Number of correspondences differs");
				System.exit(1);
			}

			System.out.println("Correspondence count matches");

			if (m instanceof MainMemoryMapping) {
				for (String s : ((MainMemoryMapping) m2).map.keySet()) {
					for (String t : ((MainMemoryMapping) m2).map.get(s).keySet()) {
						if (!((MainMemoryMapping) m).contains(s, t)) {
							System.err.println("Missing entry (" + s + ", " + t + ")");
							System.exit(1);
						}
					}
				}
				System.out.println("Correspondences match");
			} else {
				System.out.println("Skip comparison of non-memory mappings");
			}
		}
	}
}
