/**
 * 
 */
package santa.simulator.genomes;

import static org.junit.Assert.*;
import java.util.List;
import java.util.ArrayList;

import org.junit.BeforeClass;
import org.junit.Test;


/**
 * @author cswarth
 *
 */
public class GenomeDescriptionTest {
	/**
	 * Keep in mind that when we create a GenomeDescription object,
	 * derived from a parent description by applying a mutation, we
	 * are only affecting Feature coordinaes, not actual genomic
	 * sequences.  Particularly in the case of an Insertion mutation,
	 * it looks like we are adding a new sequence, but actually all
	 * that matters the the position and length of the inserted or
	 * deleted fragment.
	 */

	/**
	 * This gets runs once before any tests in this class.  It is responsible for initializing the state expected by the subsequent tests.
	 *
	 * Create a GenomeDescription that has two features, the first of which has two fragements.
	 */
	@BeforeClass
	public static void onceExecutedBeforeAll() throws Exception {
		List<Sequence> sequences = new ArrayList<Sequence>();
		Sequence seq = new SimpleSequence("aaaaCCCCCcCCCCggTTTTTTaa");
		// 								   012345678901234567890123
		sequences.add(seq);
		
		List<Feature> features = new ArrayList<Feature>();
		Feature pol = new Feature("POL", Feature.Type.NUCLEOTIDE);
		pol.addFragment(4, 5);
		pol.addFragment(10, 4);
		Feature gag = new Feature("GAG", Feature.Type.NUCLEOTIDE);
		gag.addFragment(16, 6);

		features.add(pol);
		features.add(gag);

		GenomeDescription.setDescription(24, features, sequences);
	}

	@Test
	public void testRootGenomeDescription() {
		assertNotNull(GenomeDescription.root);
		Feature pol = GenomeDescription.root.getFeature("POL");
		assertNotNull(pol);
		assertEquals(9, pol.getLength());
		Feature gag = GenomeDescription.root.getFeature("GAG");
		assertNotNull(gag);
		assertEquals(6, gag.getLength());
	}



	/**
	 * We make new GenomeDescriptor objects by modifying an existing one.
	 * Verify that various types of modifications do what we expect.
	 */
	@Test
	public void testGenomeDescriptionIndelAtEnd() {
		// aaaaCCCCCcCCCCggTTTTTTaa
		// 012345678901234567890123

		Mutation m;
		GenomeDescription gd;

		// delete at end
		gd = GenomeDescription.applyIndel(GenomeDescription.root, 23, -1);
		assertEquals(23, gd.getGenomeLength());

		// delete more than is available
		gd = GenomeDescription.applyIndel(GenomeDescription.root, 21, -5);
		assertEquals(21, gd.getGenomeLength());

		// inserting at end
		gd = GenomeDescription.applyIndel(GenomeDescription.root, 23, 1);
		assertEquals(25, gd.getGenomeLength());

		// overlapping delete at the start
		gd = GenomeDescription.applyIndel(GenomeDescription.root, 2, -4);
		assertEquals(20, gd.getGenomeLength());

		Feature pol = gd.getFeature("POL");
		assertNotNull(pol);
		assertEquals(2, pol.getFragmentStart(0));
		assertEquals(4, pol.getFragmentFinish(0));
		assertEquals(6, pol.getFragmentStart(1));
		assertEquals(9, pol.getFragmentFinish(1));
	}

	
	/**
	 * Test method for {@link santa.simulator.genomes.GenomeDescription#GenomeDescription(santa.simulator.genomes.GenomeDescription, santa.simulator.genomes.Mutation)}.
	 */
	@Test
	public void testGenomeDescriptionMutationDelete() {
		//     ***
		//  aaaaCCCCCcCCCCggTTTTTTaa
		//  012345678901234567890123
		GenomeDescription gd;
		Feature f;

		// a zero-length indel should not alter the GenomeDescription.
		gd = GenomeDescription.applyIndel(GenomeDescription.root, 3, 0);
		f = gd.getFeature("POL");
		assertEquals(9, f.getLength());
		f = gd.getFeature("GAG");
		assertEquals(6, f.getLength());

		gd = GenomeDescription.applyIndel(GenomeDescription.root, 3, -3);
		f = gd.getFeature("POL");
		assertEquals(7, f.getLength());
		f = gd.getFeature("GAG");
		assertEquals(6, f.getLength());
	}

	@Test
	public void testGenomeDescriptionInsertBefore() {
		// aaa***aCCCCCcCCCCggTTTTTTaa
		// 012345678901234567890123456

		// insert occurs before (to the left of) the features.
		// length is not affected but coordinates are.
		GenomeDescription gd = GenomeDescription.applyIndel(GenomeDescription.root, 3, 3);
		Feature pol = gd.getFeature("POL");
		assertEquals(9, pol.getLength());
		assertEquals(7, pol.getFragmentStart(0));
		assertEquals(11, pol.getFragmentFinish(0));
		assertEquals(13, pol.getFragmentStart(1));
		assertEquals(16, pol.getFragmentFinish(1));

		Feature gag = gd.getFeature("GAG");
		assertEquals(6, gag.getLength());
		assertEquals(19, gag.getFragmentStart(0));
		assertEquals(24, gag.getFragmentFinish(0));
	}

	@Test
	public void testGenomeDescriptionInsertInto() {
		// aaaaC***CCCCcCCCCggTTTTTTaa
		// 012345678901234567890123456

		// insert occurs in the middle of the feature.
		// feature gets wider and finish coordinate moves right.
		// start coordinate stays where it is.
		GenomeDescription gd = GenomeDescription.applyIndel(GenomeDescription.root, 5, 3);
		Feature pol = gd.getFeature("POL");
		assertEquals(12, pol.getLength());
		assertEquals(4, pol.getFragmentStart(0));
		assertEquals(11, pol.getFragmentFinish(0));
		assertEquals(13, pol.getFragmentStart(1));
		assertEquals(16, pol.getFragmentFinish(1));

		Feature gag = gd.getFeature("GAG");
		assertEquals(6, gag.getLength());
		assertEquals(19, gag.getFragmentStart(0));
		assertEquals(24, gag.getFragmentFinish(0));
	}

	@Test
	public void testGenomeDescriptionInsertAfter() {
		// aaaaCCCCCcCCCCg***gTTTTTTaa
		// 012345678901234567890123456

		// insert occurs to the right of the feature.
		// feature is unaffected.
		GenomeDescription gd = GenomeDescription.applyIndel(GenomeDescription.root, 15, 3);
		Feature pol = gd.getFeature("POL");
		assertEquals(9, pol.getLength());
		assertEquals(4, pol.getFragmentStart(0));
		assertEquals(8, pol.getFragmentFinish(0));
		assertEquals(10, pol.getFragmentStart(1));
		assertEquals(13, pol.getFragmentFinish(1));

		Feature gag = gd.getFeature("GAG");
		assertEquals(6, gag.getLength());
		assertEquals(19, gag.getFragmentStart(0));
		assertEquals(24, gag.getFragmentFinish(0));
		
	}


	// test the special case of inserting exactly at the beginning of a feature.
	// Does the whole feature move to the right?  Or does feature stay where it is and just gets wider?
	
	@Test
	public void testGenomeDescriptionInsertAt() {
		// aaaaCCCCCcCCCCgg***TTTTTTaa
		// 012345678901234567890123456

		GenomeDescription gd = GenomeDescription.applyIndel(GenomeDescription.root, 16, 3);
		Feature pol = gd.getFeature("POL");
		assertEquals(9, pol.getLength());
		assertEquals(4, pol.getFragmentStart(0));
		assertEquals(8, pol.getFragmentFinish(0));
		assertEquals(10, pol.getFragmentStart(1));
		assertEquals(13, pol.getFragmentFinish(1));

		// the second feature starts at the same place, but gets longer.
		Feature gag = gd.getFeature("GAG");
		assertEquals(9, gag.getLength());
		assertEquals(16, gag.getFragmentStart(0));
		assertEquals(24, gag.getFragmentFinish(0));
	}

}
