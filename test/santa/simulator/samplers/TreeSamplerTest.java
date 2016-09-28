package santa.simulator.samplers;

import santa.simulator.genomes.GenomeDescription;
import santa.simulator.genomes.Sequence;
import santa.simulator.genomes.SimpleSequence;
import santa.simulator.genomes.Feature;
import santa.simulator.phylogeny.Phylogeny;
import santa.simulator.fitness.FrequencyDependentFitnessFactor;

import santa.simulator.population.Population;
import santa.simulator.population.StaticPopulation;
import santa.simulator.genomes.GenePool;
import santa.simulator.genomes.SimpleGenePool;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import java.util.*;


public class TreeSamplerTest {
	static GenePool pool;
	static Phylogeny phy;
	static Population pop;

	@BeforeClass
	public static void IniitializeGenomeDescriptor() throws Exception {
		List<Sequence> sequences = new ArrayList<Sequence>();
		Sequence seq = new SimpleSequence("aaaaCCCCCcCCCCggTTTTTTaa");
		// 								   012345678901234567890123
		sequences.add(seq);
		
		List<Feature> features = new ArrayList<Feature>();
		GenomeDescription.setDescription(seq.getLength(), features, sequences);

		// Initialize a population to sample from
		int populationSize = 100;
		pool = new SimpleGenePool();
		phy = new Phylogeny(populationSize);
		pop = new StaticPopulation(populationSize, pool, null, phy);
		Sequence founder = new SimpleSequence("aaaaCCCCCcCCCCggTTTTTTaa");
		List<Sequence> inoculum = new ArrayList<Sequence>();
		inoculum.add(founder);
		pop.initialize(inoculum, populationSize);
	}

	@Before
	public void setUp() throws Exception {
		assertNotNull("Expected GenomeDescription class to be initialized.", GenomeDescription.root);
	}

	@Rule
	public final ExpectedException thrown = ExpectedException.none();

	@Test
	public void TestBadNexusExport() {
		TreeSampler ts = new TreeSampler(10, null, TreeSampler.Format.NEXUS, "virus_%g_%s", "treesample.nex");
		ts.initialize(1);
		ts.sample(0, pop);
		ts.sample(1, pop);

		thrown.expect(RuntimeException.class);
		thrown.expectMessage("Cannot use variable tip labels");
		ts.cleanUp();
	}
}
