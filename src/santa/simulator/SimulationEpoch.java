package santa.simulator;

import java.util.ArrayList;
import java.util.logging.Logger;
import static santa.simulator.Simulator.readableByteCount;
import static santa.simulator.Simulator.usedMemory;
import santa.simulator.compartments.Compartment;
import santa.simulator.compartments.CompartmentEpoch;
import santa.simulator.compartments.Compartments;
import java.util.List;

/**
 *
 * @author Andrew Rambaut
 */
public class SimulationEpoch {
    private int generationCount;
    private List<CompartmentEpoch> compartmentEpochs;
    private String name;
    private static Logger memlogger = Simulator.memlogger;
   
    public SimulationEpoch(List<CompartmentEpoch> epochs, int generationCount) {
        this.compartmentEpochs = epochs;
        this.generationCount = generationCount;
        
        this.name = null;
        
        for (CompartmentEpoch epoch: compartmentEpochs) {
            if (name == null) {
                epoch.getName();
            } else {
                this.name += ", " + epoch.getName();
            }
            
        }
    }
    
    public int run(Simulation simulation, Logger logger, int startGeneration) {
        Compartments compartments = simulation.getCompartments();

        final int endGeneration = startGeneration + generationCount;

        System.err.println("Starting epochs: " + name);
                
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
                compartments.genomeTransfer(generation, compartmentEpochs);
                
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
