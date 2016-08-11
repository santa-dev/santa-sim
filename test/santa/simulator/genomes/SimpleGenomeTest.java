// Compile
// $ javac -cp dist/santa.jar:lib/junit-4.12.jar test/santa/simulator/genomes/SimpleGenomeTest.java 


// To run the test with the test runner used in main(), type:
// $ java -cp test:lib/junit-4.12.jar:lib/java-hamcrest-2.0.0.0.jar:dist/santa.jar santa.simulator.genomes.SimpleGenomeTest main


// To run the test from the console, type:
// $ java -cp test:lib/junit-4.12.jar:lib/java-hamcrest-2.0.0.0.jar:dist/santa.jar org.junit.runner.JUnitCore santa.simulator.genomes.SimpleGenomeTest

package santa.simulator.genomes;

import static org.junit.Assert.*;

import org.junit.Test;

public class SimpleGenomeTest {

	public static void main(String args[]) {
		org.junit.runner.JUnitCore.main("santa.simulator.genomes.SimpleGenomeTest");
    }

	@Test
	public void testSetSequence() {
		SimpleGenome sg;
		Sequence seq;

		String nucleotides = "ACGTACGTACGT";
		sg = new SimpleGenome();
		seq = new SimpleSequence(nucleotides);
		sg.setSequence(seq);
		assertTrue(sg.getLength() == nucleotides.length());

		seq = new SimpleSequence("");
		sg = new SimpleGenome();
		sg.setSequence(seq);
		assertTrue(sg.getLength() == 0);

	}

	@Test
	public void testDeleteStartCodon() {
		String nucleotides = "ACGTACGTACGT";
		SimpleGenome sg = new SimpleGenome();
		Sequence seq = new SimpleSequence(nucleotides);
		sg.setSequence(seq);
		sg.delete(0, 3);
		assertTrue(sg.getLength() == 9);

		SimpleSequence ss = (SimpleSequence) sg.getSequence();
		assertEquals(ss.getNucleotides(),"TACGTACGT");
	}

	@Test
	public void testDeleteMiddleCodon() {
		String nucleotides = "ACGTACGTACGT";
		SimpleGenome sg = new SimpleGenome();
		Sequence seq = new SimpleSequence(nucleotides);
		sg.setSequence(seq);
		sg.delete(4, 3);
		SimpleSequence ss = (SimpleSequence) sg.getSequence();
		assertEquals(ss.getNucleotides(),"ACGTTACGT");
	}

	@Test
	public void testDeleteEndCodon() {
		String nucleotides = "ACGT";
		SimpleGenome sg = new SimpleGenome();
		Sequence seq = new SimpleSequence(nucleotides);
		sg.setSequence(seq);

		// should succeed b/c span is integral codon length
		sg.delete(1, 3);
		
		SimpleSequence ss = (SimpleSequence) sg.getSequence();
		assertEquals("A",ss.getNucleotides());
	}


	/**
	 * test whether the class support deleting non-codon length fragments.
	 */
	@Test
	public void testDeleteNonCodon() {
		String nucleotides = "ACGT";
		SimpleGenome sg = new SimpleGenome();
		Sequence seq = new SimpleSequence(nucleotides);
		sg.setSequence(seq);

		// deletion ignored b/c span not integral codon length.
		assertTrue(sg.delete(3, 1));
		
		SimpleSequence ss = (SimpleSequence) sg.getSequence();
		assertEquals("ACG", ss.getNucleotides());
	}

	/**
	 * test whether deleting beyond the end of the sequence is well-defined.
	 */
	@Test
	public void testDeleteTooMuch() {
		String nucleotides = "ACGT";
		SimpleGenome sg = new SimpleGenome();
		Sequence seq = new SimpleSequence(nucleotides);
		sg.setSequence(seq);

		assertFalse(sg.delete(3, 9));
		
		SimpleSequence ss = (SimpleSequence) sg.getSequence();
		assertEquals("ACGT", ss.getNucleotides());
	}

	@Test
	public void testDeleteCodonTooMuch() {
		String nucleotides = "ACGT";
		SimpleGenome sg = new SimpleGenome();
		Sequence seq = new SimpleSequence(nucleotides);
		sg.setSequence(seq);

		// fails b/c not enough available length
		assertFalse(sg.delete(1, 9));
		
		SimpleSequence ss = (SimpleSequence) sg.getSequence();
		assertEquals("ACGT", ss.getNucleotides());
	}

	@Test
	public void testInsertAtStart() {
		String nucleotides = "ACGTACGTACGT";
		Sequence seq = new SimpleSequence(nucleotides);
		SimpleGenome sg = new SimpleGenome();
		sg.setSequence(seq);

		SimpleSequence ss = new SimpleSequence("TTT");
		sg.insert(0, ss);
		ss = (SimpleSequence) sg.getSequence();
		assertEquals(ss.getNucleotides(),"TTTACGTACGTACGT");
	}

}
