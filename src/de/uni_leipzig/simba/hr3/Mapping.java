/**
 * @author Lars Kolb
 * @since 26.11.2012
 */
package de.uni_leipzig.simba.hr3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVWriter;

public abstract class Mapping {

    public abstract void add(String source, String target, double similarity);

    public abstract int size() throws IOException;

    public void close() throws IOException {
    }

    public final void addSingleSource(String label1, String label2, double sim) {
//		if(label1.compareTo(label2) <= 0)
        add(label1, label2, sim);
//		else
//			add(label2, label1, sim);
    }

    public abstract void merge(Mapping m);

    public abstract Map getMap();

    public static final class DiskMapping extends Mapping {

        private CSVWriter csvWriter;
        private String[] outputArray = new String[3];
        private boolean closed = false;
        private File outputFile;
        private int size = -1;

        public DiskMapping(File file, boolean csv) throws IOException {
            this.outputFile = file;
            if (csv) {
                csvWriter = new CSVWriter(new FileWriter(file), CSVParser.DEFAULT_SEPARATOR, CSVParser.DEFAULT_QUOTE_CHARACTER, CSVParser.DEFAULT_ESCAPE_CHARACTER);
            } else {
                csvWriter = new CSVWriter(new FileWriter(file), '\t', CSVParser.NULL_CHARACTER, CSVParser.NULL_CHARACTER);
            }
        }

        @Override
        public synchronized void add(String source, String target, double similarity) {
            outputArray[0] = source;
            outputArray[1] = target;
            outputArray[2] = String.valueOf(similarity);

            csvWriter.writeNext(outputArray);
        }

	@Override
	public void merge(Mapping m) {
	}

	@Override
	public Map getMap() {
		return null;
	}

        @Override
        public void close() throws IOException {
            csvWriter.flush();
            csvWriter.close();
            closed = true;
        }

        @Override
        public int size() throws IOException {
            if (!closed) {
                throw new RuntimeException("Output file not closed");
            }

            if (size >= 0) {
                return size;
            }

            int size = 0;
            BufferedReader buf = new BufferedReader(new FileReader(outputFile));
            String line;
            while ((line = buf.readLine()) != null && !line.isEmpty()) {
                size++;
            }

            return size;
        }

        String getOutputDirName() {
            return outputFile.getParentFile().getPath();
        }
    }

    public static final class NoOpMapping extends Mapping {

        private int size = 0;

        @Override
        public void add(String source, String target, double similarity) {
            size++;
        }

        @Override
        public int size() throws IOException {
            return size;
        }

	@Override
	public void merge(Mapping m) {
	}

	@Override
	public Map getMap() {
		return null;
	}

    }
}
