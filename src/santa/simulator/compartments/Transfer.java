package santa.simulator.compartments;

import java.util.List;

/**
 *
 * @author Bradley R. Jones
 */
public interface Transfer {
    void genomeTransfer(List<Compartment> compartments, int generation, List<CompartmentEpoch> currentEpochs);
}
