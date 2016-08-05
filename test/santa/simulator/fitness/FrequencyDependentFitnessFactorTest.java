package santa.simulator.fitness;

import santa.simulator.SimulatorParser;
import santa.simulator.genomes.GenomeDescription;
import santa.simulator.genomes.Sequence;
import santa.simulator.genomes.SimpleSequence;
import santa.simulator.genomes.Feature;
import santa.simulator.genomes.SequenceAlphabet;
import santa.simulator.genomes.StateChange;
import santa.simulator.genomes.AminoAcid;

import santa.simulator.population.Population;
import santa.simulator.population.StaticPopulation;
import santa.simulator.genomes.GenePool;
import santa.simulator.genomes.SimpleGenePool;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeSet;


import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Before;
import org.junit.Test;

public class FrequencyDependentFitnessFactorTest {

	FrequencyDependentFitnessFactor factor;

	@BeforeClass
	public static void IniitializeGenomeDescriptor() throws Exception {
		List<Sequence> sequences = new ArrayList<Sequence>();
		Sequence seq = new SimpleSequence("aaaaCCCCCcCCCCggTTTTTTaa");
		// 								   012345678901234567890123
		sequences.add(seq);
		
		List<Feature> features = new ArrayList<Feature>();
		Feature pol = new Feature("POL", Feature.Type.NUCLEOTIDE);
		pol.addFragment(4, 5);
		pol.addFragment(10, 4);
		Feature gag = new Feature("GAG", Feature.Type.AMINO_ACID);
		gag.addFragment(16, 6);

		features.add(pol);
		features.add(gag);

		GenomeDescription.setDescription(seq.getLength(), features, sequences);
	}

	@Before
	public void setUp() throws Exception {
		assertNotNull("Expected GenomeDescription class to be initialized.", GenomeDescription.root);
		
		Feature gag = GenomeDescription.root.getFeature("GAG");
		assertNotNull("Expected GAG feature to be defined.", gag);
		assertSame("Expected GAG feature to use AMINO_ACID alphabet", Feature.Type.AMINO_ACID, gag.getFeatureType());

		// At this level, sites are zero-based in units of amino acids
		// (determined by the feature alphabet).  If going through the
		// parser, santa.simulator.SimulatorParser.addSite() would
		// subtract 1 from the value supplied in the config file to
		// give a zero-based value.
		Set<Integer> sites = new TreeSet<Integer>();
		sites.add(0);
		sites.add(1);
		factor = new FrequencyDependentFitnessFactor(0.5, gag, sites);
	}

	@Test
	public void updateFitnessFromFullPool() {
		// edge case of updating generation fitness from an empty genome pool.
		byte[] states = {AminoAcid.F, AminoAcid.F};

		// frequency fitness on an empty genomepool should be zero
		double contrib = factor.computeLogFitness(states);
		assertEquals(0.0, contrib, 0.0);

		// populate the genome pool
		GenePool pool = new SimpleGenePool();
		Population pop = new StaticPopulation(100, pool, null, null);
		Sequence founder = new SimpleSequence("aaaaCCCCCcCCCCggTTTTTTaa");
		List<Sequence> inoculum = new ArrayList<Sequence>();
		inoculum.add(founder);
		pop.initialize(inoculum, 100);

		assertTrue(factor.updateGeneration(1, pop));

		contrib = factor.computeLogFitness(states);
		assertTrue(contrib!= 0);
		// contrib is actually -10 here but the essential point is it has an actual non-zero value,

		states[0] = AminoAcid.G;
		contrib = factor.computeLogFitness(states);
		assertEquals(0.0, contrib, 0.0);

	}

}
