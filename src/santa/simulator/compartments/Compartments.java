package santa.simulator.compartments;

import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Bradley R. Jones
 */
public class Compartments implements Iterable<Compartment> {
    private Transfer transfer;
    private List<Compartment> compartments;
    private int numCompartments;
    
    public Compartments(List<Compartment> compartments, Transfer transfer) {
        this.compartments = compartments;
        this.numCompartments = compartments.size();
        this.transfer = transfer;
    }
    
    public void genomeTransfer(int generation, List<CompartmentEpoch> currentEpochs) {
        transfer.genomeTransfer(compartments, generation, currentEpochs);
    }
    
    public int getNumCompartments() {
        return numCompartments;
    }

    @Override
    public Iterator<Compartment> iterator() {
        return compartments.iterator();
    }
}
