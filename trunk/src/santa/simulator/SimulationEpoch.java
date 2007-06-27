/*
 * Created on Mar 12, 2007
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package santa.simulator;

import santa.simulator.fitness.FitnessFunction;
import santa.simulator.genomes.GenePool;
import santa.simulator.mutators.Mutator;
import santa.simulator.replicators.Replicator;
import santa.simulator.samplers.SamplingSchedule;

import java.util.logging.Logger;

public class SimulationEpoch {
    private String name;
    private int generationCount;
    private FitnessFunction fitnessFunction;
    private Mutator mutator;
    private Replicator replicator;

    public SimulationEpoch(String name, int generationCount,
            FitnessFunction fitnessFunction, Mutator mutator,
            Replicator replicator) {
        this.name = name;
        this.generationCount = generationCount;
        this.fitnessFunction = fitnessFunction;
        this.mutator = mutator;
        this.replicator = replicator;
    }

    public int run(Simulation simulation, Logger logger, int startGeneration) {
        System.err.println("Starting epoch: " + (name != null ? name : "(unnamed)"));

        Population population = simulation.getPopulation();
        GenePool genePool = simulation.getGenePool();
        SamplingSchedule samplingSchedule = simulation.getSamplingSchedule();

        final int endGeneration = startGeneration + generationCount;

        for (int generation = startGeneration; generation < endGeneration; ++generation) {
            EventLogger.setEpoch(generation);

            fitnessFunction.updateGeneration(generation, population);

            if (generation == startGeneration) {
                // adapt to this epoch, and the new generation
                population.updateAllFitnesses(fitnessFunction);
                
                System.err.println("Initial population:  fitness = " + population.getMeanFitness() +
                        ", distance = " + population.getMeanDistance() +
                        ", max freq = " + population.getMaxFrequency() +
                        ", genepool size = " + genePool.getUniqueGenomeCount() +
                        " (" + genePool.getUnusedGenomeCount() + " available)");                
            }

            population.selectNextGeneration(generation, replicator, mutator, fitnessFunction);

            if (generation % 100 == 0) {
                if (population.getPhylogeny() != null)
                    population.getPhylogeny().pruneDeadLineages();

                System.err.print("Generation " + generation + ":  fitness = " + population.getMeanFitness() +
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
                if (false) {
                    logger.finest("Generation " + generation + ":  fitness = " + population.getMeanFitness() +
                            ", distance = " + population.getMeanDistance() +
                            ", max freq = " + population.getMaxFrequency() +
                            ", genepool size= " + genePool.getUniqueGenomeCount() +
                            "(" + genePool.getUnusedGenomeCount() + " available)");
                }
            }

            samplingSchedule.doSampling(generation, population);
        }

        return endGeneration;
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

}
