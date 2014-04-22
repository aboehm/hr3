/**
 * @author Lars Kolb
 * @since 16.11.2012
 */
package de.uni_leipzig.simba.hr3;

import java.awt.Color;
import java.awt.Polygon;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.LegendItemSource;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.block.BlockContainer;
import org.jfree.chart.block.CenterArrangement;
import org.jfree.chart.block.GridArrangement;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.CompositeTitle;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import de.uni_leipzig.simba.hr3.Mapping.DiskMapping;
import de.uni_leipzig.simba.hr3.Mapping.NoOpMapping;
import java.util.*;

public class Util {

    /**
     * Generates a set of random points
     *
     * @param min Minimal coordinate for a point
     * @param max Maximal coordinate for a point
     * @param dim Number of dimensions of the poblem
     * @param size Number of points to generate
     * @return Set of points
     */
    public static Set<Point> generateData(float min, float max, int dim, int size) {
        Set<Point> result = new LinkedHashSet<Point>();
        for (int i = 0; i < size; i++) {
            List<Float> index = new ArrayList<Float>();
            for (int j = 0; j < dim; j++) {
                index.add((float) (min + Math.random() * (max - min)));
            }
            Point p = new Point("p_" + i, index);
            result.add(p);
        }
        return result;
    }
    public static float width = 5f;

    public static Set<de.uni_leipzig.simba.hr3.hausdorff.Polygon> generatePolygons(float min, float max, int dim, int size, int minPoints, int maxPoints) {
        Set<de.uni_leipzig.simba.hr3.hausdorff.Polygon> result = new HashSet<de.uni_leipzig.simba.hr3.hausdorff.Polygon>();
        for (int i = 0; i < size; i++) {
            int nrPoints = (int) (minPoints + (maxPoints - minPoints) * Math.random());
            float center = min + (float) Math.random() * (max - min);

            List<Point> points = new ArrayList<Point>(generateData(center, center + width, dim, nrPoints));
            de.uni_leipzig.simba.hr3.hausdorff.Polygon X = new de.uni_leipzig.simba.hr3.hausdorff.Polygon("Polygon_" + i, points);
            result.add(X);
        }
        return result;
    }

    public static void getStats(Set<de.uni_leipzig.simba.hr3.hausdorff.Polygon> input) {
        Map<Integer, Integer> counts = new HashMap<Integer, Integer>();
        for (de.uni_leipzig.simba.hr3.hausdorff.Polygon p : input) {
            int size = p.points.size();
            if (!counts.containsKey(size)) {
                counts.put(size, 0);
            }
            counts.put(size, counts.get(size) + 1);
        }
        for (int count : counts.keySet()) {
            System.out.println(count + "\t" + counts.get(count));
        }
    }

    public static Set<de.uni_leipzig.simba.hr3.hausdorff.Polygon> getSubset(Set<de.uni_leipzig.simba.hr3.hausdorff.Polygon> polygons, int offset, int size) {
        if (polygons.size() <= size) {
            return polygons;
        }
        if (polygons.size() <= size + offset) {
            offset = polygons.size() - size;
        }
//        System.out.println("Offset " + offset);
        Set<de.uni_leipzig.simba.hr3.hausdorff.Polygon> result = new HashSet<de.uni_leipzig.simba.hr3.hausdorff.Polygon>();
        int i = 0;
        for (de.uni_leipzig.simba.hr3.hausdorff.Polygon p : polygons) {
            if (i >= offset) {
                result.add(p);
            }
            i++;
            if (i == size + offset) {
                break;
            }
        }
        return result;
    }

    private static Set<Point> generateSkewed2DData(float min, float max, int[] sizes) {
        Set<Point> result = new LinkedHashSet<Point>();
        float half = (min + max) / 2.0f;
        int id = 0;

        //First Quadrant
        for (int i = 0; i < sizes[0]; i++) {
            List<Float> index = new ArrayList<Float>();
            index.add((float) (min + Math.random() * (half - min)));
            index.add((float) (half + Math.random() * (max - half)));
            Point p = new Point("p_" + id++, index);
            result.add(p);
        }
        //Second Quadrant
        for (int i = 0; i < sizes[1]; i++) {
            List<Float> index = new ArrayList<Float>();
            index.add((float) (half + Math.random() * (max - half)));
            index.add((float) (half + Math.random() * (max - half)));
            Point p = new Point("p_" + id++, index);
            result.add(p);
        }
        //Third Quadrant
        for (int i = 0; i < sizes[2]; i++) {
            List<Float> index = new ArrayList<Float>();
            index.add((float) (half + Math.random() * (max - half)));
            index.add((float) (min + Math.random() * (half - min)));
            Point p = new Point("p_" + id++, index);
            result.add(p);
        }
        //Fourth Quadrant
        for (int i = 0; i < sizes[3]; i++) {
            List<Float> index = new ArrayList<Float>();
            index.add((float) (min + Math.random() * (half - min)));
            index.add((float) (min + Math.random() * (half - min)));
            Point p = new Point("p_" + id++, index);
            result.add(p);
        }
        return result;
    }

    private static void writeSourceToFile(Set<Point> pointsOfSource, File file, boolean csv) {
        try {
            BufferedWriter buf = new BufferedWriter(new FileWriter(file));

            if (!csv) {
                for (Point p : pointsOfSource) {
                    StringBuffer line = new StringBuffer(p.label);
                    for (float dimValue : p.coordinates) {
                        line.append("\t" + dimValue);
                    }
                    buf.write(line.toString());
                    buf.newLine();
                }

                buf.flush();
                buf.close();
            } else {
                CSVWriter csvWriter = new CSVWriter(buf, CSVParser.DEFAULT_SEPARATOR, CSVParser.DEFAULT_QUOTE_CHARACTER, CSVParser.DEFAULT_ESCAPE_CHARACTER);
                String[] tmp = new String[pointsOfSource.iterator().next().coordinates.size() + 1];
                for (Point p : pointsOfSource) {
                    tmp[0] = p.label;
                    int i = 0;
                    for (float dimValue : p.coordinates) {
                        tmp[++i] = String.valueOf(dimValue);
                    }
                    csvWriter.writeNext(tmp);
                }
                csvWriter.flush();
                csvWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("serial")
    private static void plot2D(Set<Point> sourcePoints, Set<Point> targetPoints, Float threshold, Integer granularity, boolean hideGrid) {
        XYSeriesCollection dataset = new XYSeriesCollection();

        float minX = Float.MAX_VALUE, maxX = Float.MIN_VALUE, minY = Float.MAX_VALUE, maxY = Float.MIN_VALUE;

        XYSeries series = new XYSeries("Source");
        for (Point p : sourcePoints) {
            float x = p.coordinates.get(0);
            if (x < minX) {
                minX = x;
            }
            if (x > maxX) {
                maxX = x;
            }

            float y = p.coordinates.get(1);
            if (y < minY) {
                minY = y;
            }
            if (y > maxY) {
                maxY = y;
            }

            series.add(x, y);
        }
        dataset.addSeries(series);

        XYSeries series2 = new XYSeries("Target");
        for (Point p : targetPoints) {
            float x = p.coordinates.get(0);
            if (x < minX) {
                minX = x;
            }
            if (x > maxX) {
                maxX = x;
            }

            float y = p.coordinates.get(1);
            if (y < minY) {
                minY = y;
            }
            if (y > maxY) {
                maxY = y;
            }
            series2.add(x, y);
        }
        dataset.addSeries(series2);

        JFreeChart chart = ChartFactory.createScatterPlot(null, "x", "y", dataset, PlotOrientation.VERTICAL, false, true, false);

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);

        XYItemRenderer renderer = plot.getRenderer();
        renderer.setSeriesPaint(0, ChartColor.red);
        renderer.setSeriesShape(0, new Polygon() {

            {
                addPoint(-1, -1);
                addPoint(1, -1);
                addPoint(1, 1);
                addPoint(-1, 1);
            }
        });

        renderer.setSeriesPaint(1, ChartColor.blue);
        renderer.setSeriesShape(1, new Polygon() {

            {
                addPoint(1, 1);
                addPoint(-1, 1);
                addPoint(0, -1);
            }
        });


        Marker origin = new ValueMarker(0);
        origin.setPaint(Color.black);
        plot.addRangeMarker(origin);
        plot.addDomainMarker(origin);

        if (!hideGrid) {
            float delta = threshold / granularity;

            int minXIndex = (int) Math.floor(minX / delta);
            int maxXIndex = (int) Math.floor(maxX / delta) + 1;
            int minYIndex = (int) Math.floor(minY / delta);
            int maxYIndex = (int) Math.floor(maxY / delta) + 1;

            for (int i = minXIndex; i <= maxXIndex; i++) {
                Marker gridMarker = new ValueMarker(i * delta);
                gridMarker.setPaint(ChartColor.GRAY);
                plot.addDomainMarker(gridMarker);
            }

            for (int i = minYIndex; i <= maxYIndex; i++) {
                Marker gridMarker = new ValueMarker(i * delta);
                gridMarker.setPaint(ChartColor.GRAY);
                plot.addRangeMarker(gridMarker);
            }
        }

        final LegendItemCollection legendItemsNew = new LegendItemCollection();
        LegendItem l0 = plot.getLegendItems().get(0);
        l0.setShape(DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE[0]);
        l0.setLabelFont(plot.getDomainAxis().getLabelFont());
        legendItemsNew.add(l0);

        LegendItem l1 = plot.getLegendItems().get(1);
        l1.setShape(DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE[2]);
        l1.setLabelFont(plot.getDomainAxis().getLabelFont());
        legendItemsNew.add(l1);

        LegendTitle legendTitle = new LegendTitle(new LegendItemSource() {

            @Override
            public LegendItemCollection getLegendItems() {
                return legendItemsNew;
            }
        }, new GridArrangement(1, 2), new CenterArrangement());
        legendTitle.setPosition(RectangleEdge.TOP);
        legendTitle.setBorder(new BlockBorder());
        legendTitle.setBorder(new BlockBorder());

        BlockContainer blockcontainer = new BlockContainer(new CenterArrangement());
        blockcontainer.add(legendTitle, RectangleEdge.TOP);

        CompositeTitle compositetitle = new CompositeTitle(blockcontainer);
        compositetitle.setPosition(RectangleEdge.BOTTOM);
        chart.addSubtitle(compositetitle);

        TextTitle title = new TextTitle("\u03B8=" + threshold + ", \u03B1=" + granularity);
        title.setFont(plot.getDomainAxis().getLabelFont());
        chart.setTitle(title);

        ChartFrame frame = new ChartFrame("Data Distribution", chart);
        frame.pack();
        frame.setVisible(true);
    }

    private static Set<Point> readPoints(String fileName, boolean csv) {
        Set<Point> result = new HashSet<Point>();
        try {
            String line;
            BufferedReader buf = new BufferedReader(new FileReader(fileName));
            if (!csv) {
                while ((line = buf.readLine()) != null) {
                    if (!line.isEmpty()) {
                        String[] fields = line.split("\t");
                        List<Float> coordinates = new ArrayList<Float>(fields.length - 1);
                        for (int i = 1; i < fields.length; i++) {
                            coordinates.add(new Float(fields[i]));
                        }
                        result.add(new Point(fields[0], coordinates));
                    }

                }
            } else {
                String[] tmp;
                CSVReader csvReader = new CSVReader(buf, CSVParser.DEFAULT_SEPARATOR, CSVParser.DEFAULT_QUOTE_CHARACTER, CSVParser.DEFAULT_ESCAPE_CHARACTER);
                while ((tmp = csvReader.readNext()) != null) {
                    List<Float> coordinates = new ArrayList<Float>(tmp.length - 1);
                    for (int i = 1; i < tmp.length; i++) {
                        coordinates.add(new Float(tmp[i]));
                    }
                    result.add(new Point(tmp[0], coordinates));
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Can not read points from file " + fileName);
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        return result;
    }

    @SuppressWarnings({"static-access", "serial"})
    static Config parseArgs(String[] args) throws IOException {
        Options options = new Options() {

            {

                addOption(OptionBuilder.withLongOpt("help").withDescription("print this message").create('h'));

                //input
                addOptionGroup(new OptionGroup() {

                    {

                        addOption(OptionBuilder.withLongOpt("random").withDescription("run HR3 with a non-skewed dataset").create());

                        addOption(OptionBuilder.withLongOpt("skewed").withDescription("run HR3 with a skewed dataset").create());

                        addOption(OptionBuilder.withLongOpt("dataset").withDescription("run HR3 for a dataset specified by csv files").hasArgs(2).withArgName("source [target]").create());
                    }
                });


                //plot input
                addOption(OptionBuilder.withLongOpt("chart").withDescription("plot the data distribution").create());

                addOption(OptionBuilder.withLongOpt("hide-grid").withDescription("hide the grid from the plotted data distribution").create());


                //commas or tabs
                addOption(OptionBuilder.withLongOpt("read-csv").withDescription("read from CSV (instead of tab separated) file(s)").create());

                addOption(OptionBuilder.withLongOpt("write-csv").withDescription("write to CSV (instead of tab separated) files").create());


                //output
                addOptionGroup(new OptionGroup() {

                    {

                        addOption(OptionBuilder.withLongOpt("output").withDescription("output the generated dataset and the the computed mapping to disk").hasArg().withArgName("directory").create());

                        addOption(OptionBuilder.withLongOpt("memory").withDescription("hold the computed mapping in memory").create());

                        addOption(OptionBuilder.withLongOpt("drop").withDescription("drop correspondences (only count their number)").create());
                    }
                });


                //validation
                addOption(OptionBuilder.withLongOpt("brute-force").withDescription("compare result with brute-force").create());


                //hr3 params
                addOption(OptionBuilder.withLongOpt("threshold").withDescription("set maximum distance between two points (\u03B8)").hasArg().withArgName("value").create());

                addOption(OptionBuilder.withLongOpt("granularity").withDescription("set degree the space tiling (\u03B1)").hasArg().withArgName("value").create());


                //threaded
                addOption(OptionBuilder.withLongOpt("threads").withDescription("run HR3 using multiple threads").hasArg().withArgName("num threads").create());
            }
        };

        CommandLine line = null;
        HelpFormatter formatter = new HelpFormatter();

        try {
            line = new PosixParser().parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp(HR3.class.getSimpleName() + " [OPTIONS]", options);
            System.exit(1);
        }

        if (line.hasOption("help")) {
            formatter.printHelp(HR3.class.getSimpleName() + " [OPTIONS]", options);
            System.exit(0);
        }

        Config config = new Config();

        //commas or tabs
        config.readCSV = line.hasOption("read-csv");
        config.writeCSV = line.hasOption("write-csv");


        //input
        if (line.hasOption("random") || !line.hasOption("skewed") && !line.hasOption("dataset")) {
            config.source = generateData(-180, 180, 2, 1000000);
            config.target = generateData(-180, 180, 2, 1000000);
        } else if (line.hasOption("skewed")) {
            config.source = generateSkewed2DData(-180, 180, new int[]{6000, 1000, 2000, 1000});
            config.target = generateSkewed2DData(-180, 180, new int[]{6000, 1000, 2000, 1000});
        } else {
            String[] files = line.getOptionValues("dataset");
            if (files.length < 1 || files.length > 2) {
                System.err.println("dataset requires one or two input files");
                formatter.printHelp(HR3.class.getSimpleName() + " [OPTIONS]", options);
                System.exit(1);
            }
            if (files.length == 1) {
                config.source = readPoints(files[0], config.readCSV);
            }
            if (files.length == 2) {
                config.source = readPoints(files[0], config.readCSV);
                config.target = readPoints(files[1], config.readCSV);
            }
        }


        //plot input
        if (line.hasOption("chart")) {
            plot2D(config.source, config.target, config.threshold, config.granularity, line.hasOption("hide-grid"));
        }


        //output
        if (line.hasOption("memory")) {
            config.mapping = new MainMemoryMapping();
            System.out.println("==============\nCollecting correspondences in memory");
        } else if (line.hasOption("output")) {
            String dirName = line.getOptionValue("output");
            File f = new File(dirName);
            if (!f.exists() || !f.isDirectory()) {
                System.err.println("Directory " + dirName + " does not exist");
                formatter.printHelp(HR3.class.getSimpleName() + " [OPTIONS]", options);
                System.exit(1);
            }

            if (config.source != null) {
                writeSourceToFile(config.source, new File(dirName, "HR3_source" + (config.writeCSV ? ".csv" : ".txt")), config.writeCSV);
            }
            if (config.target != null) {
                writeSourceToFile(config.target, new File(dirName, "HR3_target" + (config.writeCSV ? ".csv" : ".txt")), config.writeCSV);
            }

            config.mapping = new DiskMapping(new File(dirName, "HR3_mapping" + (config.writeCSV ? ".csv" : ".txt")), config.writeCSV);
            System.out.println("==============\nWrite mapping to file");
        } else {
            config.mapping = new NoOpMapping();
            System.out.println("==============\nDrop correspondences");
        }


        //validation
        config.bruteForce = line.hasOption("brute-force");


        //hr3 params
        if (line.hasOption("threshold")) {
            try {
                config.threshold = Float.parseFloat(line.getOptionValue("threshold"));
                if (config.threshold < 0f) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                System.err.println("threshold \u03B8 must be a float\u22650");
                formatter.printHelp(HR3.class.getSimpleName() + " [OPTIONS]", options);
                System.exit(1);
            }
        } else {
            config.threshold = HR3.DEFAULT_THRESHOLD;
        }

        if (line.hasOption("granularity")) {
            try {
                config.granularity = Integer.parseInt(line.getOptionValue("granularity"));
                if (config.granularity < 1) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                System.err.println("granularity \u03B1 must be a int\u22651");
                formatter.printHelp(HR3.class.getSimpleName() + " [OPTIONS]", options);
                System.exit(1);
            }
        } else {
            config.granularity = HR3.DEFAULT_GRANULARITY;
        }

        int dimensions = config.source.iterator().next().coordinates.size();
        if (dimensions > 1) {
            double sqrtDim = Math.sqrt(dimensions);
            int minGranularity = (int) Math.ceil(sqrtDim / (sqrtDim - 1));
            if (config.granularity < minGranularity) {
                System.err.println("n" + "\u00B7(\u03B1-1)\u00B2 \u2271 \u03B1\u00B2 (minimum granularity \u03B1 for " + dimensions + " dimensions: " + minGranularity + ")");
                formatter.printHelp(HR3.class.getSimpleName() + " [OPTIONS]", options);
                System.exit(1);
            }
        }


        //threads
        if (line.hasOption("threads")) {
            try {
                config.numThreads = Integer.parseInt(line.getOptionValue("threads"));
                if (config.numThreads < 1) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                System.err.println("number of threads must be a int\u22651");
                formatter.printHelp(HR3.class.getSimpleName() + " [OPTIONS]", options);
                System.exit(1);
            }
        }



        //no further args
        if (line.getArgs() != null && line.getArgs().length > 0) {
            System.err.println("Non-recognized arguments " + Arrays.toString(line.getArgs()));
            formatter.printHelp(HR3.class.getSimpleName() + " [OPTIONS]", options);
            System.exit(1);
        }

        return config;
    }

    static class Config {

        boolean readCSV = false;
        boolean writeCSV = false;
        Set<Point> source;
        Set<Point> target;
        Mapping mapping;
        boolean bruteForce;
        float threshold;
        int granularity;
        int numThreads = -1;
    }
}