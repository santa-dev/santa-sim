
package santa.simulator.compartments;


import java.util.logging.Logger;
import santa.simulator.fitness.FitnessFunction;
import santa.simulator.genomes.GenePool;
import santa.simulator.mutators.Mutator;
import santa.simulator.population.Population;
import santa.simulator.replicators.Replicator;
import santa.simulator.samplers.SamplingSchedule;

/* 
 * @author Andrew Rambaut
 * @author Bradley R. Jones
*/
public class CompartmentEpoch {
    private String name;
    private int generationCount;
    private FitnessFunction fitnessFunction;
    private Mutator mutator;
    private Replicator replicator;

    public CompartmentEpoch(String name, int generationCount,
            FitnessFunction fitnessFunction, Mutator mutator,
            Replicator replicator) {
        this.name = name;
        this.generationCount = generationCount;
        this.fitnessFunction = fitnessFunction;
        this.mutator = mutator;
        this.replicator = replicator;
    }

    public int step(Compartment compartment, Logger logger, int startGeneration, int generation) {
        Population population = compartment.getPopulation();
        GenePool genePool = compartment.getGenePool();
        SamplingSchedule samplingSchedule = compartment.getSamplingSchedule();
        
        if (population.getCurrentGeneration().isEmpty())
            return 0;
        
        fitnessFunction.updateGeneration(generation, population);

        if (generation == startGeneration) {
            // adapt to this epoch, and the new generation
            population.updateAllFitnesses(fitnessFunction);
            
            System.err.println("Initial population:" +
                    " compartment = " + compartment.getName() +
                    ", population = " + population.getCurrentGeneration().size() +
                    ", fitness = " + population.getMeanFitness() +
                ", distance = " + population.getMeanDistance() +
                ", max freq = " + population.getMaxFrequency() +
                ", genepool size = " + genePool.getUniqueGenomeCount() +
                " (" + genePool.getUnusedGenomeCount() + " available)");                
        }

        population.selectNextGeneration(generation, replicator, mutator, fitnessFunction);
        
        if (population.getCurrentGeneration().isEmpty())
            return 0;
        
        if (generation % 100 == 0) {
            if (population.getPhylogeny() != null)
                population.getPhylogeny().pruneDeadLineages();

            System.err.print("Generation " + generation + 
                    ": compartment = " + compartment.getName() +
                    ", population = " + population.getCurrentGeneration().size() +
                    ", fitness = " + population.getMeanFitness() +
                ", distance = " + population.getMeanDistance() +
                ", max freq = " + population.getMaxFrequency() +
                ", genepool size = " + genePool.getUniqueGenomeCount() +
                " (" + genePool.getUnusedGenomeCount() + " available)");
            if (population.getPhylogeny() != null) {
                population.getPhylogeny().pruneDeadLineages();
                System.err.println(", phylogeny size = " + population.getPhylogeny().getSize() +
                    " (used = " + population.getPhylogeny().getLineageCount()+ ")" +
                    ", tmrca = " + population.getPhylogeny().getMRCA().getGeneration() );
            } else
                System.err.println();
        } else {
            logger.finest("Generation " + generation + 
                    ": compartment = " + compartment.getName() +
                    ", population = " + population.getCurrentGeneration().size() +
                    " fitness = " + population.getMeanFitness() +
                ", distance = " + population.getMeanDistance() +
            	", max freq = " + population.getMaxFrequency() +
            	", genepool size= " + genePool.getUniqueGenomeCount() +
            	"(" + genePool.getUnusedGenomeCount() + " available)");                
        }
        
        samplingSchedule.doSampling(generation, population);
        
        return population.getPopulationSize();
    }

    public FitnessFunction getFitnessFunction() {
        return fitnessFunction;
    }

    public Mutator getMutator() {
        return mutator;
    }

    public Replicator getReplicator() {
        return replicator;
    }
    
    public int getGenerationCount() {
        return generationCount;
    }
    
    public String getName() {
        return name;
    }
}
