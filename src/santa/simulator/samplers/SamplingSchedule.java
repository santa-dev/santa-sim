package santa.simulator.samplers;

import santa.simulator.population.Population;

import java.util.ArrayList;
import java.util.List;

/**
 * @author rambaut
 *         Date: Sep 12, 2005
 *         Time: 11:03:25 PM
 */
public class SamplingSchedule {

	public void addSampler(int generation, Sampler sampler) {
		entries.add(new Entry(generation, false, sampler));
	}

	public void addRecurringSampler(int frequency, Sampler sampler) {
		entries.add(new Entry(frequency, true, sampler));
	}

    public void initialize(int replicate) {
        for (Entry entry : entries) {
            entry.sampler.initialize(replicate);
        }
    }

	public void doSampling(int generation, Population population) {
		for (Entry entry : entries) {
			if ((entry.recurring && (generation % entry.generation == 0)) ||
					generation == entry.generation) {
				entry.sampler.sample(generation, population);
			}
		}
	}

    public void cleanUp() {
        for (Entry entry : entries) {
            entry.sampler.cleanUp();
        }
    }

    public boolean isSamplingTrees() {
        return isSamplingTrees;
    }

    public void setSamplingTrees(boolean samplingTrees) {
        isSamplingTrees = samplingTrees;
    }

    private boolean isSamplingTrees = false;

    private List<Entry> entries = new ArrayList<Entry>();

	private class Entry {

		Entry(int generation, boolean recurring, Sampler sampler) {
			this.generation = generation;
			this.recurring = recurring;
			this.sampler = sampler;
		}

		private int generation;
		private boolean recurring;
		private Sampler sampler;
	};
}
