/*
 * Created on Jul 17, 2006
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package santa.simulator.fitness;

import santa.simulator.genomes.Feature;
import santa.simulator.genomes.SequenceAlphabet;
import santa.simulator.population.Population;

import java.util.Set;

public abstract class AbstractFitnessFactor implements FitnessFactor {

	public AbstractFitnessFactor(Feature feature, Set<Integer> sites) {
		this.feature = feature;
		this.sites = sites;
		SequenceAlphabet alphabet = SequenceAlphabet.NUCLEOTIDES;
		if (feature != null) {
			if (feature.getFeatureType() == Feature.Type.AMINO_ACID) {
				alphabet = SequenceAlphabet.AMINO_ACIDS;
			}
		}
		this.alphabet = alphabet;
	}

    public boolean updateGeneration(int generation, Population population) {
        return false;
    }

	public Feature getFeature() {
		return feature;
	}

	public Set<Integer> getSites() {
		return sites;
	}

	public SequenceAlphabet getAlphabet() {
		return alphabet;
	}

	private final Feature feature;
	private final Set<Integer> sites;
	private final SequenceAlphabet alphabet;
}
