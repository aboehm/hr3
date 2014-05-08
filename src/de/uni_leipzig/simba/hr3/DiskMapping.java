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


public class DiskMapping extends Mapping
{
	CSVWriter csvWriter;
	private String[] outputArray= new String[3];
	private boolean closed= false;
	private File outputFile;
	
	public DiskMapping(File file, boolean csv) throws IOException
	{
		this.outputFile= file;
		if(csv)
			csvWriter= new CSVWriter(new FileWriter(file), CSVParser.DEFAULT_SEPARATOR, CSVParser.DEFAULT_QUOTE_CHARACTER, CSVParser.DEFAULT_ESCAPE_CHARACTER);
		else
			csvWriter= new CSVWriter(new FileWriter(file), '\t', CSVParser.NULL_CHARACTER, CSVParser.NULL_CHARACTER);
	}
	
	public void add(String source, String target, double similarity)
	{
		outputArray[0]= source;
		outputArray[1]= target;
		outputArray[2]= String.valueOf(similarity);
		
		csvWriter.writeNext(outputArray);
	}
	
	public void close() throws IOException
	{
		csvWriter.flush();
		csvWriter.close();
		closed= true;
	}
	
	public int size() throws IOException
	{
		if(!closed)
			throw new RuntimeException("Output file not closed");
		
		int cnt= 0;
		BufferedReader buf= new BufferedReader(new FileReader(outputFile));
		String line;
		while((line=buf.readLine())!=null && !line.isEmpty())
			cnt++;
		
		return cnt;
	}

	@Override
	public void merge(Mapping map) {
	}

	@Override
	public Map getMap() {
		return null;
	}
}
