package santa.simulator;

import santa.simulator.genomes.*;
import santa.simulator.samplers.SamplingSchedule;
import santa.simulator.selectors.Selector;
import santa.simulator.phylogeny.Phylogeny;

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
    private final GenePool genePool;
    private final List<SimulationEpoch> epochs;
    private final Selector selector;
    private final SamplingSchedule samplingSchedule;

    private final Population population;

    public Simulation (
            int populationSize,
            List<Sequence> inoculum,
            GenePool genePool,
            List<SimulationEpoch> epochs,
            Selector selector,
            SamplingSchedule samplingSchedule) {

        this.populationSize = populationSize;
        this.inoculum = inoculum;
        this.epochs = epochs;
        this.samplingSchedule = samplingSchedule;
        this.genePool = genePool;
        this.selector = selector;

        // This pre-computes all possible mutation objects as singletons...
        Mutation.initialize();

        population = new Population(populationSize, genePool, selector, samplingSchedule.isSamplingTrees() ? new Phylogeny(populationSize) : null);
    }

    public void run(int replicate, Logger logger) {

        samplingSchedule.initialize(replicate);

        logger.finer("Initializing population: " + populationSize + " viruses.");

        population.initialize(inoculum);

        int generation = 0;

        for (SimulationEpoch epoch:epochs) {
            generation = epoch.run(this, logger, generation);
        }
        samplingSchedule.cleanUp();
    }

    public GenePool getGenePool() {
        return genePool;
    }

    public Population getPopulation() {
        return population;
    }

    public int getPopulationSize() {
        return populationSize;
    }

    public SamplingSchedule getSamplingSchedule() {
        return samplingSchedule;
    }
}
