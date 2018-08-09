/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package santa.simulator.compartments;

import java.util.List;

/**
 *
 * @author Bradley R. Jones
 */
public interface Transfer {
    void genomeTransfer(List<Compartment> compartments, int generation, List<CompartmentEpoch> currentEpochs);
}
