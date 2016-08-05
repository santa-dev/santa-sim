package santa.simulator.fitness;

import santa.simulator.SimulatorParser;
import santa.simulator.genomes.GenomeDescription;
import santa.simulator.genomes.Sequence;
import santa.simulator.genomes.SimpleSequence;
import santa.simulator.genomes.Feature;
import santa.simulator.genomes.SequenceAlphabet;
import santa.simulator.genomes.StateChange;
import santa.simulator.genomes.AminoAcid;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeSet;


import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Before;
import org.junit.Test;

public class PurifyingFitnessFactorTest {

	PurifyingFitnessFactor factor;

	private List<Set<Byte>> parseProbableSetClasses(SequenceAlphabet alphabet, String str) {
        Set<Byte> completeCheck = new HashSet<Byte>();
		List<Set<Byte>> classes = new ArrayList<Set<Byte>>();

		String[] sets = str.split("\\|");
		for (String set : sets) {
			Set<Byte> stateSet = new HashSet<Byte>();
			for (int i = 0; i < set.length(); i++) {
				byte symbol = alphabet.parse(set.charAt(i));
                stateSet.add(symbol);
                completeCheck.add(symbol);
			}
			classes.add(stateSet);
		}
        
		return classes;
	}

	@BeforeClass
	public static void mockGenomeDescriptor() throws Exception {
		List<Sequence> sequences = new ArrayList<Sequence>();
		Sequence seq = new SimpleSequence("aaaaCCCCCcCCCCggTTTTTTaa");
		// 								   012345678901234567890123
		// Capital letters show areas covered by POL and GAG features.
		// GAG is contiguous, POL is not.
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

		List<Set<Byte>> orderSetClasses = parseProbableSetClasses(SequenceAlphabet.AMINO_ACIDS, SimulatorParser.CHEMICAL_CLASSES);
		PurifyingFitnessRank rank = new PurifyingFitnessRank(gag, orderSetClasses, true, -1);
		PurifyingFitnessModel valueModel = new PurifyingFitnessPiecewiseLinearModel(SequenceAlphabet.AMINO_ACIDS, 0.1, 0.9);

		// At this level, sites are zero-based in units of amino acids
		// (determined by the feature alphabet).  If going through the
		// parser, santa.simulator.SimulatorParser.addSite() would
		// subtract 1 from the value supplied in the config file to
		// give a zero-based value.
		Set<Integer> sites = new TreeSet<Integer>();
		sites.add(0);
		sites.add(1);
		factor = new PurifyingFitnessFactor(rank, valueModel, 0.0, 0.0, gag, sites);
	}


	// assert that a STOP codon within purifying fitness feature is lethal.
	@Test
	public void stopShouldBeLethal() {
		StateChange change = new StateChange(0				/* position */,
											 AminoAcid.Y	/* oldState */,
											 AminoAcid.STP	/* newState */);
		assertEquals(factor.getLogFitnessChange(change), Double.NEGATIVE_INFINITY, 0);
	}

}
