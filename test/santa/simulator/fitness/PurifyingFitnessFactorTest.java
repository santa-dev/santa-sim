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
	public static void IniitializeGenomeDescriptor() throws Exception {
		List<Sequence> sequences = new ArrayList<Sequence>();
		Sequence seq = new SimpleSequence("aaaaCCCCCcCCCCggTTTTTTaa");
		// 								   012345678901234567890123
		sequences.add(seq);
		
		List<Feature> features = new ArrayList<Feature>();
		Feature pol = new Feature("POL", Feature.Type.NUCLEOTIDE);
		pol.addFragment(4, 8);
		pol.addFragment(10, 13);
		Feature gag = new Feature("GAG", Feature.Type.AMINO_ACID);
		gag.addFragment(16, 21);

		features.add(pol);
		features.add(gag);

		GenomeDescription.setDescription(24, features, sequences);
	}

	
	@Before
	public void setUp() throws Exception {
		assertNotNull("Expected GenomeDescription class to be initialized.", GenomeDescription.root);
		
		Feature gag = GenomeDescription.root.getFeature("GAG");
		assertNotNull("Expected GAG feature to be defined.", gag);
		
		// define a fitness factor on the GAG protein, from
		SequenceAlphabet alphabet = SequenceAlphabet.NUCLEOTIDES;
		if (gag.getFeatureType() == Feature.Type.AMINO_ACID) {
			alphabet = SequenceAlphabet.AMINO_ACIDS;
		}

		assertSame("Expected GAG feature to use AMINO_ACID alphabet", SequenceAlphabet.AMINO_ACIDS, alphabet);
		
		List<Set<Byte>> orderSetClasses = parseProbableSetClasses(alphabet, SimulatorParser.CHEMICAL_CLASSES);
		PurifyingFitnessRank rank = new PurifyingFitnessRank(gag, orderSetClasses, true, -1);
		PurifyingFitnessModel valueModel = new PurifyingFitnessPiecewiseLinearModel(alphabet, 0.1, 0.9);
		Set<Integer> sites = new TreeSet<Integer>();
		for (int i = 16; i <= 21; i++) 
			sites.add(i);
		factor = new PurifyingFitnessFactor(rank, valueModel, 0.0, 0.0, gag, sites);
	}

	@Test
	public void testGetLogFitnessChange() {
		StateChange change = new StateChange(0				/* position */,
											 AminoAcid.Y	/*oldState */,
											 AminoAcid.STP	/* newState */);
		assertEquals(factor.getLogFitnessChange(change), Double.NEGATIVE_INFINITY, 0);
	}

}
