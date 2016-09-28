package santa.simulator.samplers;

import jebl.evolution.io.*;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.Tree;
import santa.simulator.Random;
import santa.simulator.population.Population;

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
	private List<Tree> trees = new ArrayList<Tree>();

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
		String result = name.replaceAll("%r", String.valueOf(replicate+1));
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
				trees.add(tree);
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
		if (trees.size() > 0) {
			try {
				exporter.exportTrees(trees);
			} catch (IllegalArgumentException e) {
				// Catch an exception thrown by the JEBL library
				// when NEXUS tip labels differ across trees.
				if (format == TreeSampler.Format.NEXUS) {
					String msg = "Error: Cannot output NEXUS formatted trees.\n"
						+ "NEXUS-format trees must share a single set of tip labels across all trees.\n"
						+ "It is best to avoid using '%g' or '%r'in the the NEXUS tip labels of yourt SANTA config file.\n";
					System.err.print(msg);
					throw new RuntimeException("Cannot use variable tip labels");
				} else {
					throw e;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		destination.close();
		destination = null;
	}
}
