package santa.simulator.compartments;

import java.util.List;
import java.util.TreeSet;
import santa.simulator.Random;
import santa.simulator.Virus;
import santa.simulator.fitness.FitnessFunction;
import santa.simulator.genomes.GenePool;
import santa.simulator.genomes.Genome;
import santa.simulator.genomes.Mutation;

/**
 *
 * @author Bradley R. Jones
 */
public class TimedTransfer implements Transfer {
    private List<TransferEvent> transferEvents;
    private int currentIndex = 0;

    public TimedTransfer(List<TransferEvent> transferEvents) {
        this.transferEvents = transferEvents;
        
        // sort in order of generation
        transferEvents.sort(null);
    }
    
    public void genomeTransfer(List<Compartment> compartments, int generation, List<CompartmentEpoch> currentEpochs) {
        TransferEvent nextEvent = null;
        
        if (currentIndex < transferEvents.size())
            nextEvent = transferEvents.get(currentIndex);
        
        while (nextEvent != null && nextEvent.getGeneration() == generation) {
            Compartment fromCompartment = compartments.get(nextEvent.getFromCompartmentIndex());
            Compartment toCompartment = compartments.get(nextEvent.getToCompartmentIndex());
            List<Virus> fromViruses = fromCompartment.getPopulation().getCurrentGeneration();
            List<Virus> toViruses = toCompartment.getPopulation().getCurrentGeneration();
            GenePool toGenePool = fromCompartment.getGenePool();
            GenePool fromGenePool = toCompartment.getGenePool();
            FitnessFunction fitness = currentEpochs.get(nextEvent.getToCompartmentIndex()).getFitnessFunction();
            
            Object[] virusesToTransfer = Random.nextSample(fromViruses, nextEvent.getAmountToTransfer());
             
            for (Object o: virusesToTransfer) {
                 Virus virus = (Virus)o;
                 Genome genome = virus.getGenome();
                 
                 fromViruses.remove(virus);
                 fromGenePool.killGenome(genome);
                                  
                 virus.setAge(generation);
                 Genome newGenome = toGenePool.createGenome(genome.getSequence(), genome.getDescription());
                 virus.setGenome(newGenome);
                 fromGenePool.duplicateGenome(newGenome, new TreeSet<Mutation>(), fitness);
                 toViruses.add(virus);
            }
            
            if (++currentIndex < transferEvents.size())
                nextEvent = transferEvents.get(currentIndex);
            else
                nextEvent = null;
        }
    }

    public static class TransferEvent implements Comparable<TransferEvent> {
        private String fromCompartment;
        private String toCompartment;
        private int fromCompartmentIndex;
        private int toCompartmentIndex;
        private int generation;
        private int amountToTransfer;
        
        public TransferEvent(String fromCompartment, String toCompartment, int generation, int amountToTransfer) {
            this.fromCompartment = fromCompartment;
            this.toCompartment = toCompartment;
            this.generation = generation;
            this.amountToTransfer = amountToTransfer;
        }
        
        public boolean findCompartmentIndices(List<String> compartmentNames) {
            fromCompartmentIndex = compartmentNames.indexOf(fromCompartment);
            toCompartmentIndex = compartmentNames.indexOf(toCompartment);
            
            return fromCompartmentIndex != -1 && toCompartmentIndex != -1;
        }
        
        public int getFromCompartmentIndex() {
            return fromCompartmentIndex;
        }
        
        public int getToCompartmentIndex() {
            return toCompartmentIndex;
        }
        
        public int getGeneration() {
            return generation;
        }
        
        public int getAmountToTransfer() {
            return amountToTransfer;
        }

        public int compareTo(TransferEvent event) {
            return Integer.compare(this.generation, event.generation);
        }
    }
}
