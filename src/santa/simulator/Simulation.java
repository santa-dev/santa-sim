package santa.simulator;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import santa.simulator.genomes.GenePool;
import santa.simulator.genomes.GenomeDescription;
import santa.simulator.genomes.Sequence;
import santa.simulator.phylogeny.Phylogeny;
import santa.simulator.population.DynamicPopulation;
import santa.simulator.population.Population;
import santa.simulator.population.StaticPopulation;
import santa.simulator.samplers.SamplingSchedule;
import santa.simulator.selectors.DynamicSelector;
import santa.simulator.selectors.Selector;
import santa.simulator.selectors.SimpleRouletteWheelSelector;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: Simulation.java,v 1.11 2006/07/19 12:53:05 kdforc0 Exp $
 */
public class Simulation {

    private final int populationSize;
    private final InoculumType inoculumType;
    private final GenePool genePool;
    private final List<SimulationEpoch> epochs;
    private final Selector selector;
    private final SamplingSchedule samplingSchedule;

    private final Population population;

	public enum InoculumType {
		NONE,
		CONSENSUS,
		RANDOM,
		ALL
	};
	
	//Default constructor (dynamic)
    public Simulation (
            int populationSize,
            InoculumType inoculumType,
            GenePool genePool,
            List<SimulationEpoch> epochs,
            SamplingSchedule samplingSchedule) {

        this.populationSize = populationSize;
        this.inoculumType = inoculumType;
        this.epochs = epochs;
        this.samplingSchedule = samplingSchedule;
        this.genePool = genePool;
        this.selector = new DynamicSelector();

        population = new DynamicPopulation(genePool, selector, samplingSchedule.isSamplingTrees() ? new Phylogeny(populationSize) : null);
    }
 
    //Constructor for static population
    public Simulation (
    		int populationSize,
    		InoculumType inoculumType,
            GenePool genePool,
            List<SimulationEpoch> epochs,
            SamplingSchedule samplingSchedule,
            String populationType){
	
    	this.populationSize = populationSize;
        this.inoculumType = inoculumType;
        this.epochs = epochs;
        this.samplingSchedule = samplingSchedule;
        this.genePool = genePool;
        this.selector = new SimpleRouletteWheelSelector();

        population = new StaticPopulation(populationSize, genePool, selector, samplingSchedule.isSamplingTrees() ? new Phylogeny(populationSize) : null);

    }
    
    public void run(int replicate, Logger logger) {

        samplingSchedule.initialize(replicate);

        EventLogger.setReplicate(replicate);

        logger.finer("Initializing population: " + populationSize + " viruses.");

	    List<Sequence> inoculum = new ArrayList<Sequence>();
	    if (inoculumType == InoculumType.CONSENSUS) {
		    inoculum.add(GenomeDescription.getConsensus());
	    } else if (inoculumType == InoculumType.ALL) {
		    inoculum.addAll(GenomeDescription.getSequences());
	    } else if (inoculumType == InoculumType.RANDOM) {
		    List<Sequence> sequences = GenomeDescription.getSequences();
		    if (sequences.size() == 1) {
			    inoculum.add(sequences.get(0));
		    } else {
		        inoculum.add(sequences.get(Random.nextInt(0, sequences.size() - 1)));
		    }
	    } else { // NONE
		    // do nothing
	    }
        population.initialize(inoculum, populationSize);

        int generation = 1;

        int epochCount = 0;

        for (SimulationEpoch epoch:epochs) {
            EventLogger.setEpoch(epochCount);

            generation = epoch.run(this, logger, generation);
            if(population.getCurrentGeneration().size() == 0) {
            	System.err.println("Population crashed after "+generation+" generations.");
            	return;
            }
            epochCount++;
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
