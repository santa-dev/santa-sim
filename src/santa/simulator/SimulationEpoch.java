package santa.simulator;

import java.util.ArrayList;
import java.util.logging.Logger;
import static santa.simulator.Simulator.readableByteCount;
import static santa.simulator.Simulator.usedMemory;
import santa.simulator.compartments.Compartment;
import santa.simulator.compartments.CompartmentEpoch;
import santa.simulator.compartments.Compartments;

/**
 *
 * @author Andrew Rambaut
 * @author Bradley R. Jones
 */
public class SimulationEpoch {
    private String name;
    private int generationCount;
    private static Logger memlogger = Simulator.memlogger;
   
    public SimulationEpoch(String name, int generationCount) {
        this.name = name;
        this.generationCount = generationCount;
    }
    
    public int run(Simulation simulation, Logger logger, int startGeneration) {
        System.err.println("Starting epoch: " + (name != null ? name : "(unnamed)"));

        Compartments compartments = simulation.getCompartments();
        ArrayList<CompartmentEpoch> compartmentEpochs = new ArrayList<>();
        
        for (Compartment compartment: compartments) {
            compartmentEpochs.add(compartment.getCurrentEpoch(name));
        }

        final int endGeneration = startGeneration + generationCount;

        memlogger.fine("@start of Epoch Memory used = " + readableByteCount(usedMemory()));

        for (int generation = startGeneration; generation < endGeneration; ++generation) {
            EventLogger.setEpoch(generation);
            
            int i = 0;
            int totalPopulation = 0;
            for (Compartment compartment: compartments) {
                totalPopulation += compartmentEpochs.get(i++).step(compartment, logger, startGeneration, generation);
            }
            
            if (totalPopulation == 0) {
            	return generation;
            }
            
            if (compartments.getNumCompartments() > 1)
                compartments.genomeTransfer(generation);
                
            if (generation % 100 == 0) {
                memlogger.finest("Generation "+ generation + " used memory: " + readableByteCount(usedMemory()));
            }
        }

        return endGeneration;
    }
    
    public String getName() {
        return name;
    }
}
