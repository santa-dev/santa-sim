/*
 * Created on Mar 12, 2007
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package santa.simulator.fitness;

import santa.simulator.Population;

public interface PurifyingFitnessRank {
    /**
     * @param site 0-based index into the sequence
     * @return the permutation of the states at the given site,
     *         ordered from largest to smallest fitness.
     */
    byte[] getStates(int site);

    boolean updateGeneration(int generation, Population population);
}
