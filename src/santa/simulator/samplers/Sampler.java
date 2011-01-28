package santa.simulator.samplers;

import santa.simulator.population.Population;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: Sampler.java,v 1.2 2006/04/19 09:26:30 kdforc0 Exp $
 */
public interface Sampler {

    void initialize(int replicate);

    void sample(int generation, Population population);

    void cleanUp();
}
