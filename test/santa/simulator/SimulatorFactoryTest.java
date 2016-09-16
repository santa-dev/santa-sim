package santa.simulator;

import static org.junit.Assert.*;
import org.apache.commons.lang3.Range;

import org.junit.Before;
import org.junit.Test;

import java.util.*;

import santa.simulator.genomes.GenomeDescription;

public class SimulatorFactoryTest {
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void parseExamples() {
		List<String[]> arglist = Arrays.asList(
			new String[] {"examples/HIV-1.xml"},
			new String[] {"examples/agedependent.xml"},
			new String[] {"examples/epoch.xml"},
			new String[] {"examples/exposuredependent.xml"},
			new String[] {"examples/fluctuating.xml"},
			new String[] {"-rate=2.5e-5", "-dual=1", "examples/footprint.xml"},
			new String[] {"examples/frequencydependent.xml"},
			new String[] {"-replicates=10", "-population=1000", "-generations=5000", "-samplesize=10", "examples/indel_test.xml"},
			new String[] {"examples/neutral.xml"},
			new String[] {"examples/purifying.xml"},
			new String[] {"examples/purifyingBeta.xml"},
			new String[] {"examples/purifying_recombination.xml"},
			new String[] {"examples/recombinationHotspots.xml"},
			new String[] {"examples/simpleRecombination.xml"},
			new String[] { "-population=1000", "-generations=5000", "-samplesize=10", "examples/small-indel.xml"},
			new String[] {"examples/small.xml"},
			new String[] { "-population=1000", "-generations=5000", "-samplesize=10", "examples/sweep.xml"}
			);
		
		for (String[] args: arglist) {
			System.out.println("Testing Parser on \"" + String.join(" ", Arrays.asList(args)) + "\"");
			GenomeDescription.root = null;
			Simulator sim = SimulatorMain.simulatorFactory(args);
			assertNotNull("Expected simulator object from  \"" + Arrays.toString(args) + "\".", sim);
		}
	}
}
