package santa.simulator.samplers;

import jebl.evolution.io.*;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.RootedTree;
import santa.simulator.Population;
import santa.simulator.Random;

import java.io.*;
import java.util.*;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: AlignmentSampler.java,v 1.6 2006/07/18 07:37:47 kdforc0 Exp $
 */
public class TreeSampler implements Sampler {
	public enum Format {
		NEXUS,
		NEWICK
	};

	private final int sampleSize;
	private TreeSampler.Format format;
	private String label;
	private String fileName;
	private PrintWriter destination;
	private Map<Integer,Integer> schedule;
	private int replicate;

	private TreeExporter exporter;

	/**
	 * Construct an alignment sampler
	 * @param sampleSize  amount of sequences to sample at regular intervals
	 * @param schedule    amount of sequences to sample at irregular intervals
	 * @param format      format
	 * @param label       label with possible %g, %s and %t variables
	 * @param fileName    name of the file to write the samples
	 */
	public TreeSampler(int sampleSize,
	                   Map<Integer,Integer> schedule, TreeSampler.Format format, String label, String fileName) {
		this.format = format;
		this.fileName = fileName;

		if (label == null) {
			this.label = "virus_%g_%s";
		} else {
			this.label = label;
		}

		this.sampleSize = sampleSize;
		this.schedule = schedule;
	}

	public void initialize(int replicate) {
		this.replicate = replicate;
		String fName = substituteVariables(fileName, 0, 0);

		try {
			destination = new PrintWriter(fName);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Could not open file for writing: " + fName);
		}

		if (format == TreeSampler.Format.NEXUS) {
			exporter = new NexusExporter(destination);
		} else if (format == TreeSampler.Format.NEWICK) {
			exporter = new NewickExporter(destination);
		}
	}

	private String substituteVariables(String name, int generation, int seq) {
		String result = name.replaceAll("%r", String.valueOf(replicate));
		result = result.replaceAll("%g", String.valueOf(generation));
		result = result.replaceAll("%s", String.valueOf(seq));
		return result;
	}

	public void sample(int generation, Population population) {
		int[] sample = getSample(generation, population);

		List<Taxon> taxa = new ArrayList<Taxon>();
		for (int i = 0; i < sample.length; i++) {
			taxa.add(Taxon.getTaxon(substituteVariables(label, generation, i + 1)));
		}
		if (sample != null) {
			RootedTree tree = population.getPhylogeny().reconstructPhylogeny(sample, taxa);
			if (tree != null) {
				try {
					exporter.exportTree(tree);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	protected int[] getSample(int generation, Population population) {
		if (schedule == null) {
			return Random.nextPermutation(population.getPopulationSize(), sampleSize);
		} else {
			if (schedule.containsKey(generation)) {
				int count = schedule.get(generation);
				return Random.nextPermutation(population.getPopulationSize(), count);
			} else
				return null;
		}
	}

	public void cleanUp() {
		destination.close();
		destination = null;
	}
}
