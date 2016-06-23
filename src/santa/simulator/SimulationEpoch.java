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
import santa.simulator.population.Population;
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

	private static long usedMemory() {
        Runtime rt = Runtime.getRuntime();
        return (rt.totalMemory() - rt.freeMemory());
    }    

	public static String humanReadableByteCount(long bytes) {
		int unit = 1024;
		if (bytes < unit) return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = ("KMGTPE").charAt(exp-1) + ("i");
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
	
    public int run(Simulation simulation, Logger logger, int startGeneration) {
        System.err.println("Starting epoch: " + (name != null ? name : "(unnamed)"));

        Population population = simulation.getPopulation();
        GenePool genePool = simulation.getGenePool();
        SamplingSchedule samplingSchedule = simulation.getSamplingSchedule();

        final int endGeneration = startGeneration + generationCount;

		System.gc();
		long usedMemoryBefore = usedMemory();
		logger.fine("@start of Epoch Memory used = " + humanReadableByteCount(usedMemoryBefore) + "MB");

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
            if(population.getCurrentGeneration().size() == 0) {
            	return generation;
            }
            
            if (generation % 100 == 0) {
                if (population.getPhylogeny() != null)
                    population.getPhylogeny().pruneDeadLineages();

				System.gc();
				long usedMemoryAfter = usedMemory();
				logger.fine("Generation "+generation+" Memory change: " + humanReadableByteCount(usedMemoryAfter-usedMemoryBefore));

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
            	logger.finest("Generation " + generation + ":  fitness = " + population.getMeanFitness() +
            			", distance = " + population.getMeanDistance() +
            			", max freq = " + population.getMaxFrequency() +
            			", genepool size= " + genePool.getUniqueGenomeCount() +
            			"(" + genePool.getUnusedGenomeCount() + " available)");                
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
