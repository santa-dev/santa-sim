package santa.simulator.genomes;

import static org.junit.Assert.*;
import org.apache.commons.lang3.Range;

import org.junit.Before;
import org.junit.Test;

import java.util.*;

public class RecombinationTest {
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void recombine() {
		Feature f = new Feature("testing", Feature.Type.NUCLEOTIDE) ;
		f.addFragment(0, 10);
		
		List<Feature> features = new ArrayList<Feature>();
		features.add(f);
		GenomeDescription.setDescription(20, features);
		System.out.println(GenomeDescription.root);

		GenomeDescription gd2 = GenomeDescription.applyIndel(GenomeDescription.root, 5, 5);
		System.out.println(gd2);

		GenomeDescription[] parents = { GenomeDescription.root, gd2 };
		SortedSet<Integer> breaks = new TreeSet<Integer>();
		breaks.add(12);
		GenomeDescription rgd = GenomeDescription.recombine(parents, breaks);
		System.out.format("recombotest\t\t%s\n", rgd);
	}
}
