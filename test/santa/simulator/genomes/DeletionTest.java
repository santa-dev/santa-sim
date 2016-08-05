package santa.simulator.genomes;

import static org.junit.Assert.*;
import org.apache.commons.lang3.Range;

import org.junit.Before;
import org.junit.Test;

public class DeletionTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testApplyRangeOfInteger() {
		Deletion del = new Deletion(2735, 1);
		Range<Integer> r = Range.between(0, 2735);

		Range<Integer> result = del.apply(r);

		assertEquals(new Integer(2734), result.getMaximum());
	}

}
