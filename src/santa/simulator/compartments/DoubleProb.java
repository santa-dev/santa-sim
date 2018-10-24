package santa.simulator.compartments;

import santa.simulator.Virus;

/**
 *
 * @author Bradley R. Jones
 */
public class DoubleProb implements TransferProb {
    private double transferRate;
    
    public DoubleProb(double transferRate) {
        this.transferRate = transferRate;
    }
    
    public double getProb(Virus virus, int generation) {
        return transferRate;
    }
}
