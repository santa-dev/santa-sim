package santa.simulator;

import santa.simulator.fitness.FitnessFunction;
import santa.simulator.genomes.*;
import santa.simulator.mutators.Mutator;
import santa.simulator.replicators.Replicator;
import santa.simulator.samplers.SamplingSchedule;
import santa.simulator.selectors.Selector;

import java.util.List;
import java.util.logging.Logger;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: Simulation.java,v 1.11 2006/07/19 12:53:05 kdforc0 Exp $
 */
public class Simulation {

    private final int populationSize;
    private final List<Sequence> inoculum;
    private final StoppingCriterion stoppingCriterion;
    private final GenePool genePool;
    private final FitnessFunction fitnessFunction;
    private final Mutator mutator;
    private final Replicator replicator;
    private final Selector selector;
    private final SamplingSchedule samplingSchedule;

    private final Population population;

    public Simulation (
            int populationSize,
            List<Sequence> inoculum,
            StoppingCriterion stoppingCriterion,
            GenePool genePool,
            FitnessFunction fitnessFunction,
            Mutator mutator,
            Replicator replicator,
            Selector selector,
            SamplingSchedule samplingSchedule) {

        this.populationSize = populationSize;

        this.inoculum = inoculum;

        this.stoppingCriterion = stoppingCriterion;

        this.fitnessFunction = fitnessFunction;
        this.mutator = mutator;
        this.replicator = replicator;
        this.samplingSchedule = samplingSchedule;

        this.genePool = genePool;

        this.selector = selector;

        // This pre-computes all possible mutation objects as singletons...
        Mutation.initialize();

        population = new Population(populationSize, genePool, selector);
    }

    public void run(int replicate, Logger logger) {

        samplingSchedule.initialize(replicate);

        logger.finer("Initializing population: " + populationSize + " viruses.");

        population.initialize(inoculum, fitnessFunction);

        int generation = 0;

        if (fitnessFunction.updateGeneration(generation, population))
            population.updateFitness(fitnessFunction);

        while (!stoppingCriterion.stop(generation, population)) {

            if (fitnessFunction.updateGeneration(generation, population))
                population.updateFitness(fitnessFunction);

            population.selectNextGeneration(replicator, mutator, fitnessFunction);

            if (generation % 100 == 0) {
                System.err.println("Generation " + generation + ":  fitness = " + population.getMeanFitness() +
                        ", distance = " + population.getMeanDistance() +
                        ", max freq = " + population.getMaxFrequency() +
                        ", genepool size= " + genePool.getUniqueGenomeCount() +
                        "(" + genePool.getUnusedGenomeCount() + " available)");
            } else {
                if (false)
                    logger.finest("Generation " + generation + ":  fitness = " + population.getMeanFitness() +
                            ", distance = " + population.getMeanDistance() +
                            ", max freq = " + population.getMaxFrequency() +
                            ", genepool size= " + genePool.getUniqueGenomeCount() +
                            "(" + genePool.getUnusedGenomeCount() + " available)");
            }

            samplingSchedule.doSampling(generation, population);

            generation++;
        }

        samplingSchedule.cleanUp();
    }
}
