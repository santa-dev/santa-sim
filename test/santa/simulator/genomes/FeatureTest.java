package santa.simulator.genomes;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class FeatureTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void hashCodeEqual() {
		Feature feature1 = new Feature("testing", Feature.Type.NUCLEOTIDE);
		Feature feature2 = new Feature("testing", Feature.Type.NUCLEOTIDE);
		feature1.addFragment(10, 10);
		feature2.addFragment(10, 10);
		
		assertEquals(feature1.hashCode(), feature2.hashCode());
	}

}
