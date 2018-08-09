/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package santa.simulator.compartments;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import santa.simulator.Random;
import santa.simulator.Virus;
import santa.simulator.fitness.FitnessFunction;
import santa.simulator.genomes.GenePool;
import santa.simulator.genomes.Genome;

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
