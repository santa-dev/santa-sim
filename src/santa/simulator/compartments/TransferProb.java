package santa.simulator.compartments;

import santa.simulator.Virus;

/**
 *
 * @author Bradley R. Jones
 */
public interface TransferProb {
    public double getProb(Virus virus, int generation);
}