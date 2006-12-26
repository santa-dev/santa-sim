package org.virion.simulator;

import jebl.evolution.sequences.SequenceType;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.virion.simulator.fitness.*;
import org.virion.simulator.genomes.*;
import org.virion.simulator.mutators.Mutator;
import org.virion.simulator.mutators.NucleotideMutator;
import org.virion.simulator.replicators.*;
import org.virion.simulator.samplers.*;
import org.virion.simulator.selectors.*;

import java.util.*;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: SimulatorParser.java,v 1.19 2006/07/19 14:35:32 kdforc0 Exp $
 */
public class SimulatorParser {

	public final static String SIMULATOR = "simulator";

	public final static String REPLICATE_COUNT = "replicates";

	public final static String SIMULATION = "simulation";

	public final static String STOPPING = "stopping";
	public final static String GENERATION_COUNT = "generationCount";

	public final static String POPULATION = "population";
	public final static String POPULATION_SIZE = "populationSize";
	public final static String INOCULUM = "inoculum";
	public final static String SEQUENCE = "sequence";

	public final static String GENOME_DESCRIPTION = "genome";
	public final static String GENOME_LENGTH = "length";

	public final static String GENE_POOL = "genePool";
	public final static String SIMPLE_GENE_POOL = "simpleGenePool";
	public final static String COMPACT_GENE_POOL = "complexGenePool";

	public final static String SELECTOR = "selector";
	public final static String MONTE_CARLO_SELECTOR = "monteCarloSelector";
	public final static String ROULETTE_WHEEL_SELECTOR = "rouletteWheelSelector";

	public final static String FITNESS_FUNCTION = "fitnessFunction";
	public final static String EMPIRICAL_FITNESS_FUNCTION = "empiricalFitness";
	public final static String RANDOM = "random";
	public final static String NUCLEOTIDES = "nucleotides";
	public final static String AMINO_ACIDS = "aminoAcids";
	public final static String NEUTRAL_MODEL_FITNESS_FUNCTION = "neutralFitness";
	public final static String PURIFYING_FITNESS_FUNCTION = "purifyingFitness";
	public final static String FITTEST = "fittest";
	public final static String STRENGTH = "strength";
	public final static String LAMBDA = "lambda";
	public final static String SITES = "sites";
	public final static String FLUCTUATING_FITNESS_FUNCTION = "fluctuatingFitness";
	public final static String FREQUENCY_DEPENDENT_FITNESS_FUNCTION = "frequencyDependentFitness";
	public final static String SHAPE = "shape";
    public final static String AGE_DEPENDENT_FITNESS_FUNCTION = "ageDependentFitness";
    public final static String DECLINE_RATE = "declineRate";
    public final static String EXPOSURE_DEPENDENT_FITNESS_FUNCTION = "exposureDependentFitness";
    public final static String PENALTY = "penalty";

	public final static String MUTATOR = "mutator";
	public final static String REPLICATOR = "replicator";

	public final static String SAMPLING_SCHEDULE = "samplingSchedule";
	public final static String SAMPLER = "sampler";
	public final static String FILE_NAME = "fileName";
	public final static String AT_FREQUENCY = "atFrequency";
	public final static String AT_GENERATION = "atGeneration";

	public final static String ALIGNMENT = "alignment";
	public final static String RECURRING_SAMPLER = "recurringSampler";
	public final static String SAMPLE_SIZE = "sampleSize";
	public final static String SCHEDULE = "schedule";
	public final static String FORMAT = "format";
	public final static String LABEL = "label";
	public final static String CONSENSUS = "consensus";

	public final static String ALLELE_FREQUENCY = "alleleFrequency";
	public final static String SITE = "site";

	public final static String STATISTICS = "statistics";

	public final static String NUCLEOTIDE_MUTATOR = "nucleotideMutator";
	public final static String MUTATION_RATE = "mutationRate";
	public final static String TRANSITION_BIAS = "transitionBias";
	public final static String RATE_BIAS = "rateBias";

	public final static String CLONAL_REPLICATOR = "clonalReplicator";
	public final static String RECOMBINANT_REPLICATOR = "recombinantReplicator";
	public final static String DUAL_INFECTION_PROBABILITY = "dualInfectionProbability";
	public final static String RECOMBINATION_PROBABILITY = "recombinationProbability";


	Simulator parse(Element element) throws ParseException {

		if (!element.getName().equals(SIMULATOR)) {
			throw new ParseException("The root element is not of type <" + SIMULATOR + ">");
		}

		int replicateCount = -1;
		for (Object o : element.getChildren()) {
			Element e = (Element)o;
			if (e.getName().equals(REPLICATE_COUNT)) {
				try {
					replicateCount = parseInteger(e, 1, Integer.MAX_VALUE);
				} catch (ParseException pe) {
					throw new ParseException("Error parsing <" + SIMULATOR + "> element: " + pe.getMessage());
				}
			}
		}

		if (replicateCount == -1) {
			throw new ParseException("Error parsing <" + SIMULATOR + "> element: <" + REPLICATE_COUNT + "> is missing");
		}


		Simulator simulator = null;
		for (Object o : element.getChildren()) {
			Element e = (Element)o;
			if (e.getName().equals(SIMULATION)) {
				simulator = new Simulator(replicateCount, parseSimulation(e));
			} else if (!e.getName().equals(REPLICATE_COUNT)) {
				throw new ParseException("Error parsing <" + SIMULATOR + "> element: <" + e.getName() + "> is unrecognized");
			}
		}

		if (simulator == null) {
			throw new ParseException("Error parsing <" + SIMULATOR + "> element: <" + SIMULATION + "> is missing");
		}

		return simulator;
	}

	Simulation parseSimulation(Element element) throws ParseException {

		StoppingCriterion stoppingCriterion = null;
		int populationSize = -1;
		List<Sequence> inoculum = null;

		boolean genomeDescription = false;
		for (Object o : element.getChildren()) {
			Element e = (Element)o;
			if (e.getName().equals(GENOME_DESCRIPTION)) {
				parseGenomeDescription(e);
				genomeDescription = true;
			}
		}

		if (!genomeDescription) {
			throw new ParseException("Error parsing <" + SIMULATION + "> element: <" + GENOME_DESCRIPTION + "> is missing");
		}

		for (Object o : element.getChildren()) {
			Element e = (Element)o;
			if (e.getName().equals(GENERATION_COUNT)) {
				try {
					stoppingCriterion = StoppingCriterion.generationCount(parseInteger(e, 1, Integer.MAX_VALUE));
				} catch (ParseException pe) {
					throw new ParseException("Error parsing <" + SIMULATION + "> element: " + pe.getMessage());
				}
			} else if (e.getName().equals(POPULATION)) {

				for (Object o1 : e.getChildren()) {
					Element e1 = (Element)o1;
					if (e1.getName().equals(POPULATION_SIZE)) {
						try {
							populationSize = parseInteger(e1, 1, Integer.MAX_VALUE);
						} catch (ParseException pe) {
							throw new ParseException("Error parsing <" + POPULATION + "> element: " + pe.getMessage());
						}
					} else if (e1.getName().equals(INOCULUM)) {
						inoculum = parseInoculum(e1);
					} else {
						throw new ParseException("Error parsing <" + SIMULATION + "> element: <" + e.getName() + "> is unrecognized");
					}
				}
			} else if (!e.getName().equals(GENOME_DESCRIPTION) &&
					!e.getName().equals(GENE_POOL) &&
					!e.getName().equals(SELECTOR) &&
					!e.getName().equals(FITNESS_FUNCTION) &&
					!e.getName().equals(MUTATOR) &&
					!e.getName().equals(REPLICATOR) &&
					!e.getName().equals(SAMPLING_SCHEDULE)) {
				throw new ParseException("Error parsing <" + SIMULATION + "> element: <" + e.getName() + "> is unrecognized");
			}
		}

        if (stoppingCriterion == null) {
            throw new ParseException("Error parsing <" + SIMULATION + "> element: <" + INOCULUM + "> is missing");
        }

		if (stoppingCriterion == null) {
			throw new ParseException("Error parsing <" + SIMULATION + "> element: <" + STOPPING + "> is missing");
		}

		if (populationSize == -1) {
			throw new ParseException("Error parsing <" + SIMULATION + "> element: <" + POPULATION_SIZE + "> is missing");
		}

		if (!GenomeDescription.isSet()) {
			throw new ParseException("Error parsing <" + SIMULATION + "> element: <" + GENOME_DESCRIPTION + "> is missing");
		}

		FitnessFunction fitnessFunction = null;
		Mutator mutator = null;
		Replicator replicator = null;
		SamplingSchedule samplingSchedule = null;
		GenePool genePool = null;
		Selector selector = null;

		for (Object o : element.getChildren()) {
			Element e = (Element)o;
			if (e.getName().equals(GENE_POOL)) {
				genePool = parseGenePool(e);
			} else if (e.getName().equals(FITNESS_FUNCTION)) {
				fitnessFunction = parseFitnessFunction(e);
			} else if (e.getName().equals(MUTATOR)) {
				mutator = parseMutator(e);
			} else if (e.getName().equals(REPLICATOR)) {
				replicator = parseReplicator(e);
			} else if (e.getName().equals(SELECTOR)) {
				selector = parseSelector(e);
			} else if (e.getName().equals(SAMPLING_SCHEDULE)) {
				samplingSchedule = parseSamplingSchedule(e);
			}
		}

		if (fitnessFunction == null) {
			throw new ParseException("Error parsing <" + SIMULATION + "> element: <" + FITNESS_FUNCTION + "> is missing");
		}

		if (mutator == null) {
			throw new ParseException("Error parsing <" + SIMULATION + "> element: <" + MUTATOR + "> is missing");
		}

		if (replicator == null) {
			throw new ParseException("Error parsing <" + SIMULATION + "> element: <" + REPLICATOR + "> is missing");
		}

		if (samplingSchedule == null) {
			throw new ParseException("Error parsing <" + SIMULATION + "> element: <" + SAMPLING_SCHEDULE + "> is missing");
		}

		if (genePool == null) {
			genePool = new SimpleGenePool();
		}

		if (selector == null) {
			selector = new RouletteWheelSelector();
		}

		return new Simulation(populationSize, inoculum, stoppingCriterion, genePool, fitnessFunction, mutator, replicator, selector, samplingSchedule);
	}

	private List<Sequence> parseInoculum(Element element) throws ParseException {
		List<Sequence> inoculum = new ArrayList<Sequence>();

		for (Object o : element.getChildren()) {
			Element e = (Element)o;
			if (e.getName().equals(SEQUENCE)) {
				Sequence sequence = parseSequence(e.getTextNormalize());
				inoculum.add(sequence);
			} else  {
				throw new ParseException("Error parsing <" + element.getName() + "> element: <" + e.getName() + "> is unrecognized");
			}

		}
		return inoculum;
	}

	public Sequence parseSequence(String sequenceString) {
		int genomeLength = GenomeDescription.getGenomeLength();

		if (sequenceString.length() != genomeLength) {
			throw new IllegalArgumentException("The initializing sequence string does not match the expected genome length ("
					+ "got: " + sequenceString.length() + ", expected: " + genomeLength);
		}

		return new SimpleSequence(sequenceString);
	}

	private void parseGenomeDescription(Element element) throws ParseException {

		int genomeLength = -1;

		for (Object o : element.getChildren()) {
			Element e = (Element)o;
			if (e.getName().equals(GENOME_LENGTH)) {
				try {
					genomeLength = parseInteger(e, 1, Integer.MAX_VALUE);
				} catch (ParseException pe) {
					throw new ParseException("Error parsing <" + SIMULATOR + "> element: " + pe.getMessage());
				}
			} else  {
				throw new ParseException("Error parsing <" + element.getName() + "> element: <" + e.getName() + "> is unrecognized");
			}

		}

		if (genomeLength == -1) {
			throw new ParseException("Error parsing <" + element.getName() + "> element: <" + GENOME_LENGTH + "> is missing");
		}

		GenomeDescription.setDescription(genomeLength);
	}

	private FitnessFunction parseFitnessFunction(Element element) throws ParseException {
		if (element.getChildren().size() == 0) {
			throw new ParseException("Error parsing <" + element.getName() + "> element: the element is empty");
		}

		List<FitnessFunctionFactor> components = new ArrayList<FitnessFunctionFactor>();

		for (int c = 0; c < element.getChildren().size(); ++c) {
			Element e = (Element)element.getChildren().get(c);
			if (e.getName().equals(AMINO_ACIDS)) {
				FitnessFunctionFactor f1 = parseFitnessFunction(e, SequenceAlphabet.AMINO_ACIDS);
				if (f1 != null)
					components.add(f1);
			} else if (e.getName().equals(NUCLEOTIDES)) {
				FitnessFunctionFactor f1 = parseFitnessFunction(e, SequenceAlphabet.NUCLEOTIDES);
				if (f1 != null)
					components.add(f1);
			} else {
				throw new ParseException("Error parsing <" + element.getName() + "> element: <" + e.getName() + "> is unrecognized");
			}
		}

		return new FitnessFunction(components);

	}

	private FitnessFunctionFactor parseFitnessFunction(Element element, SequenceAlphabet alphabet) throws ParseException {
		if (element.getChildren().size() == 0) {
			throw new ParseException("Error parsing <" + element.getName() + "> element: the element is empty");
		}

		Set<Integer> sites = null;

		for (int c = 0; c < element.getChildren().size(); ++c) {
			Element e = (Element)element.getChildren().get(c);
			if (e.getName().equals(SITES)) {
				sites = parseSites(e);
			} else if (e.getName().equals(EMPIRICAL_FITNESS_FUNCTION)) {
				boolean random = false;
				if (e.getAttribute(RANDOM) != null) {
					try {
						random = e.getAttribute(RANDOM).getBooleanValue();
					} catch (DataConversionException dce) {
						throw new ParseException("Error parsing <" + e.getName() + "> attribute: " + dce.getMessage());
					}
				}
				double[] fitnesses = parseNumberList(e);

				return new EmpiricalFitnessFunction(fitnesses, random, sites, alphabet);
			} else if (e.getName().equals(NEUTRAL_MODEL_FITNESS_FUNCTION)) {
				return null;
			} else if (e.getName().equals(PURIFYING_FITNESS_FUNCTION)) {
				double strength = 1;
				Sequence fittest = null;

				for (Object o : e.getChildren()) {
					Element e1 = (Element)o;
					if (e1.getName().equals(FITTEST)) {
						fittest = parseSequence(e1.getTextNormalize());
					} else if (e1.getName().equals(STRENGTH)) {
						strength = parseDouble(e1, 0, Double.MAX_VALUE);
					} else {
						throw new ParseException("Error parsing <" + e.getName() + "> element: <" + e1.getName() + "> is unrecognized");
					}
				}

				if (fittest == null) {
					throw new ParseException("Error parsing <" + element.getName() + "> element: <" + FITTEST + "> is missing");
				}

				return new PurifyingFitnessFunction(fittest, strength, sites, alphabet);
			} else if (e.getName().equals(FLUCTUATING_FITNESS_FUNCTION)) {
				double strength = 1;
				double lambda = 0.01;

				for (Object o : e.getChildren()) {
					Element e1 = (Element)o;
					if (e1.getName().equals(LAMBDA)) {
						lambda = parseDouble(e1, 0, Double.MAX_VALUE);
					} else if (e1.getName().equals(STRENGTH)) {
						strength = parseDouble(e1, 0, Double.MAX_VALUE);
					} else {
						throw new ParseException("Error parsing <" + e.getName() + "> element: <" + e1.getName() + "> is unrecognized");
					}
				}

				return new FluctuatingFitnessFunction(strength, lambda, sites, alphabet);
			} else if (e.getName().equals(FREQUENCY_DEPENDENT_FITNESS_FUNCTION)) {
				double shape = 0.5;

				for (Object o : e.getChildren()) {
					Element e1 = (Element)o;
					if (e1.getName().equals(SHAPE)) {
						shape = parseDouble(e1, 0, Double.MAX_VALUE);
					} else {
						throw new ParseException("Error parsing <" + e.getName() + "> element: <" + e1.getName() + "> is unrecognized");
					}
				}

				return new FrequencyDependentFitnessFunction(shape, sites, alphabet);
            } else if (e.getName().equals(AGE_DEPENDENT_FITNESS_FUNCTION)) {
                double declineRate = 0.001;

                for (Object o : e.getChildren()) {
                    Element e1 = (Element)o;
                    if (e1.getName().equals(DECLINE_RATE)) {
                        declineRate = parseDouble(e1, 0, Double.MAX_VALUE);
                    } else {
                        throw new ParseException("Error parsing <" + e.getName() + "> element: <" + e1.getName() + "> is unrecognized");
                    }
                }

                return new AgeDependentFitnessFunction(declineRate, sites, alphabet);
            } else if (e.getName().equals(EXPOSURE_DEPENDENT_FITNESS_FUNCTION)) {
                double penalty = 0.001;

                for (Object o : e.getChildren()) {
                    Element e1 = (Element)o;
                    if (e1.getName().equals(PENALTY)) {
                        penalty = parseDouble(e1, 0, Double.MAX_VALUE);
                    } else {
                        throw new ParseException("Error parsing <" + e.getName() + "> element: <" + e1.getName() + "> is unrecognized");
                    }
                }

                return new ExposureDependentFitnessFunction(penalty, sites, alphabet);
			} else {
				throw new ParseException("Error parsing <" + element.getName() + "> element: <" + e.getName() + "> is unrecognized");
			}
		}

		throw new ParseException("Error parsing <" + element.getName() + "> element: " +
				"expecting one of <" + EMPIRICAL_FITNESS_FUNCTION + ">, " +
				"< " + NEUTRAL_MODEL_FITNESS_FUNCTION + ">, " +
				"< " + PURIFYING_FITNESS_FUNCTION + ">, " +
				"< " + FLUCTUATING_FITNESS_FUNCTION + ">");
	}

	/**
	 * @param e
	 * @return the number list
	 */
	private double[] parseNumberList(Element e) throws NumberFormatException {
		String text = e.getTextNormalize();
		String[] values = text.split("\\s*,\\s*|\\s+");
		double[] fitnesses = new double[values.length];
		for (int i = 0; i < fitnesses.length; i++) {
			fitnesses[i] = Double.parseDouble(values[i]);
		}
		return fitnesses;
	}

	private Set<Integer> parseSites(Element element) throws ParseException {
		Set<Integer> result = new TreeSet<Integer>();

		String sites = element.getTextNormalize();
		String[] parts = sites.split(",");

		try {
			for (int i = 0; i < parts.length; ++i) {
				String part = parts[i].trim();

				if (part.contains("-")) {
					String[] ranges = part.split("-");

					if (ranges.length != 2) {
						throw new ParseException("Error parsing <" + element.getName()
								+ "> element: \"" + part + "\" is not a proper range.");
					}

					int start = Integer.parseInt(ranges[0]);
					int end = Integer.parseInt(ranges[1]);

					for (int j = start; j <= end; ++j) {
						result.add(j);
					}
				} else {
					int site = Integer.parseInt(part);
					result.add(site);
				}
			}
		} catch (NumberFormatException e) {
			throw new ParseException("Error parsing <" + element.getName()
					+ "> element: " + e.getMessage());
		}

		return result;
	}

	private Mutator parseMutator(Element element) throws ParseException {
		if (element.getChildren().size() == 0) {
			throw new ParseException("Error parsing <" + element.getName() + "> element: the element is empty");
		}

		Element e = (Element)element.getChildren().get(0);

		if (e.getName().equals(NUCLEOTIDE_MUTATOR)) {
			double mutationRate = -1.0;
			double transitionBias = -1.0;
			double rateBiases[] = null;

			for (Object o : e.getChildren()) {
				Element e1 = (Element)o;
				if (e1.getName().equals(MUTATION_RATE)) {
					try {
						mutationRate = parseDouble(e1, 0.0, Double.MAX_VALUE);
					} catch (ParseException pe) {
						throw new ParseException("Error parsing <" + e.getName() + "> element: " + pe.getMessage());
					}
				} else if (e1.getName().equals(TRANSITION_BIAS)) {
					try {
						transitionBias = parseDouble(e1, 0.0, Double.MAX_VALUE);
					} catch (ParseException pe) {
						throw new ParseException("Error parsing <" + e.getName() + "> element: " + pe.getMessage());
					}
				} else if (e1.getName().equals(RATE_BIAS)) {
					try {
						rateBiases = parseNumberList(e1);
					} catch (NumberFormatException pe) {
						throw new ParseException("Error parsing <" + e.getName() + "> element: " + pe.getMessage());
					}
				} else {
					throw new ParseException("Error parsing <" + e.getName() + "> element: <" + e1.getName() + "> is unrecognized");
				}

			}

			if (mutationRate < 0.0) {
				throw new ParseException("Error parsing <" + element.getName() + "> element: <" + MUTATION_RATE + "> is missing");
			}

			if (transitionBias != -1 && rateBiases != null) {
				throw new ParseException("Error parsing <" + element.getName() + "> element: " +
						"specify not both of <" + TRANSITION_BIAS + "> and <" + RATE_BIAS + ">.");
			}

			return new NucleotideMutator(mutationRate, transitionBias, rateBiases);
		} else {
			throw new ParseException("Error parsing <" + element.getName() + "> element: <" + e.getName() + "> is unrecognized");
		}

	}

	private Replicator parseReplicator(Element element) throws ParseException {

		if (element.getChildren().size() == 0) {
			throw new ParseException("Error parsing <" + element.getName() + "> element: the element is empty");
		}

		Element e = (Element)element.getChildren().get(0);
		if (e.getName().equals(CLONAL_REPLICATOR)) {
			return new ClonalReplicator();
		} else if (e.getName().equals(RECOMBINANT_REPLICATOR)) {
			double dualInfectionProbability = -1.0;
			double recombinationProbability = -1.0;

			for (Object o : e.getChildren()) {
				Element e1 = (Element)o;
				if (e1.getName().equals(DUAL_INFECTION_PROBABILITY)) {
					try {
						dualInfectionProbability = parseDouble(e1, 0.0, 1.0);
					} catch (ParseException pe) {
						throw new ParseException("Error parsing <" + e.getName() + "> element: " + pe.getMessage());
					}
				} else if (e1.getName().equals(RECOMBINATION_PROBABILITY)) {
					try {
						recombinationProbability = parseDouble(e1, 0.0, 1.0);
					} catch (ParseException pe) {
						throw new ParseException("Error parsing <" + e.getName() + "> element: " + pe.getMessage());
					}
				} else {
					throw new ParseException("Error parsing <" + e.getName() + "> element: <" + e1.getName() + "> is unrecognized");
				}

			}

			if (dualInfectionProbability < 0.0) {
				throw new ParseException("Error parsing <" + element.getName() + "> element: <" + DUAL_INFECTION_PROBABILITY + "> is missing");
			}

			if (recombinationProbability < 0.0) {
				throw new ParseException("Error parsing <" + element.getName() + "> element: <" + RECOMBINATION_PROBABILITY + "> is missing");
			}

			return new RecombinantReplicator(dualInfectionProbability, recombinationProbability);
		} else  {
			throw new ParseException("Error parsing <" + element.getName() + "> element: <" + e.getName() + "> is unrecognized");
		}

	}

	private GenePool parseGenePool(Element element) throws ParseException {

		if (element.getChildren().size() == 0) {
			throw new ParseException("Error parsing <" + element.getName() + "> element: the element is empty");
		}

		Element e = (Element)element.getChildren().get(0);
		if (e.getName().equals(SIMPLE_GENE_POOL)) {
			// SimpleGenome/GenePool uses a simple format where the whole sequence is stored
			return new SimpleGenePool();
		} else if (e.getName().equals(COMPACT_GENE_POOL)) {
			// CompactGenome/GenePool uses a compact format where only changes are stored
			return new CompactGenePool();
		} else {
			throw new ParseException("Error parsing <" + element.getName() + "> element: <" + e.getName() + "> is unrecognized");
		}

	}

	private Selector parseSelector(Element element) throws ParseException {

		if (element.getChildren().size() == 0) {
			throw new ParseException("Error parsing <" + element.getName() + "> element: the element is empty");
		}

		Element e = (Element)element.getChildren().get(0);
		if (e.getName().equals(MONTE_CARLO_SELECTOR)) {
			return new MonteCarloSelector();
		} else if (e.getName().equals(ROULETTE_WHEEL_SELECTOR)) {
			return new RouletteWheelSelector();
		} else {
			throw new ParseException("Error parsing <" + element.getName() + "> element: <" + e.getName() + "> is unrecognized");
		}
	}

	private SamplingSchedule parseSamplingSchedule(Element element) throws ParseException {
		SamplingSchedule samplingSchedule = new SamplingSchedule();

		if (element.getChildren().size() == 0) {
			throw new ParseException("Error parsing <" + element.getName() + "> element: the element is empty");
		}

		for (Object o : element.getChildren()) {
			Element e1 = (Element)o;
			if (e1.getName().equals(SAMPLER)) {
				parseSampler(e1, samplingSchedule);
			} else {
				throw new ParseException("Error parsing <" + element.getName() + "> element: <" + e1.getName() + "> is unrecognized");
			}
		}

		return samplingSchedule;
	}

	private void parseSampler(Element element, SamplingSchedule samplingSchedule) throws ParseException {

		int frequency = -1;
		int generation = -1;
		String fileName = null;


		for (Object o : element.getChildren()) {
			Element e1 = (Element)o;
			if (e1.getName().equals(AT_FREQUENCY)) {
				try {
					frequency = parseInteger(e1, 1, Integer.MAX_VALUE);
				} catch (ParseException pe) {
					throw new ParseException("Error parsing <" + element.getName() + "> element: " + pe.getMessage());
				}
			} else if (e1.getName().equals(AT_GENERATION)) {
				try {
					generation = parseInteger(e1, 1, Integer.MAX_VALUE);
				} catch (ParseException pe) {
					throw new ParseException("Error parsing <" + element.getName() + "> element: " + pe.getMessage());
				}
			} else if (e1.getName().equals(FILE_NAME)) {
				fileName = e1.getTextNormalize();
			} else {
				// skip over it
			}

		}

		if (generation == -1 && frequency == -1) {
			throw new ParseException("Error parsing <" + element.getName() + "> element: <" + AT_FREQUENCY + "> or <" + AT_GENERATION + "> is missing");
		}

		if (generation != -1 && frequency != -1) {
			throw new ParseException("Error parsing <" + element.getName() + "> only one of: <" + AT_FREQUENCY + "> or <" + AT_GENERATION + "> can be specified");
		}

		Sampler sampler = null;

		for (Object o : element.getChildren()) {
			Element e1 = (Element)o;
			if (e1.getName().equals(ALIGNMENT)) {
				sampler = parseAlignmentSampler(e1, samplingSchedule, fileName);
			} else if (e1.getName().equals(ALLELE_FREQUENCY)) {
				sampler = parseAlleleFrequencySampler(e1, samplingSchedule, fileName);
			} else if (e1.getName().equals(STATISTICS)) {
				sampler = parseStatisticsSampler(e1, samplingSchedule, fileName);
			} else if (e1.getName().equals(AT_FREQUENCY) || e1.getName().equals(AT_GENERATION) || e1.getName().equals(FILE_NAME)) {
				// skip over it
			} else {
				throw new ParseException("Error parsing <" + element.getName() + "> element: <" + e1.getName() + "> is unrecognized");
			}

		}

		if (sampler == null) {
			throw new ParseException("Error parsing <" + element.getName() + "> element: type of sampler (e.g., <alignment>) is missing");
		}

		if (generation != -1) {
			samplingSchedule.addSampler(generation, sampler);
		} else {
			samplingSchedule.addRecurringSampler(frequency, sampler);
		}
	}

	private Sampler parseStatisticsSampler(Element e1, SamplingSchedule samplingSchedule, String fileName) {
		return new StatisticsSampler(fileName);
	}

	private Sampler parseAlignmentSampler(Element element, SamplingSchedule samplingSchedule, String fileName) throws ParseException {

		int sampleSize = -1;
		Map<Integer,Integer> schedule = null;
		String label = null;
		boolean consensus = false;

		AlignmentSampler.Format format = AlignmentSampler.Format.NEXUS;

		for (Object o : element.getChildren()) {
			Element e1 = (Element)o;
			if (e1.getName().equals(SAMPLE_SIZE)) {
				try {
					sampleSize = parseInteger(e1, 1, Integer.MAX_VALUE);
				} catch (ParseException pe) {
					throw new ParseException("Error parsing <" + element.getName() + "> element: " + pe.getMessage());
				}
			} else if (e1.getName().equals(SCHEDULE)) {
				String[] values = e1.getTextTrim().split("\\s+");
				schedule = new TreeMap<Integer,Integer>();
				try {
					for (int i = 0; i<values.length/2; ++i) {
						int g = Integer.parseInt(values[i*2]);
						int n = Integer.parseInt(values[i*2 + 1]);

						schedule.put(g, n);
					}
				} catch (NumberFormatException e) {
					throw new ParseException("Error parsing <" + element.getName() + "> element: "
							+ e.getMessage());
				}
			} else if (e1.getName().equals(FORMAT)) {
				String formatText = e1.getTextNormalize();
				if (formatText.equalsIgnoreCase("NEXUS")) {
					format = AlignmentSampler.Format.NEXUS;
				} else if (formatText.equalsIgnoreCase("FASTA")) {
					format = AlignmentSampler.Format.FASTA;
				} else if (formatText.equalsIgnoreCase("XML")) {
					format = AlignmentSampler.Format.XML;
				} else {
					throw new ParseException("Error parsing <" + element.getName() + "> element: <" + FORMAT + "> value of " + formatText + " is unrecognized");
				}
			} else if (e1.getName().equals(CONSENSUS)) {
				String booleanText = e1.getTextNormalize();
				if (booleanText.equalsIgnoreCase("TRUE")) {
					consensus = true;
				} else if (booleanText.equalsIgnoreCase("FALSE")) {
					consensus = true;
				} else {
					throw new ParseException("Error parsing <" + element.getName() + "> element: <" + CONSENSUS + "> value " + booleanText + " is unrecognized");
				}
			} else if (e1.getName().equals(LABEL)) {
				label = e1.getTextNormalize();
			} else {
				throw new ParseException("Error parsing <" + element.getName() + "> element: <" + e1.getName() + "> is unrecognized");
			}

		}

		if (schedule != null && sampleSize != -1) {
			throw new ParseException("Error parsing <" + element.getName() + "> element: specify only one of <" + SAMPLE_SIZE + "> or <" + SCHEDULE + ">.");
		}

		return new AlignmentSampler(sampleSize, consensus, schedule, format, label, fileName);
	}

	private Sampler parseAlleleFrequencySampler(Element element, SamplingSchedule samplingSchedule, String fileName) throws ParseException {

		int site = -1;

		for (Object o : element.getChildren()) {
			Element e1 = (Element)o;
			if (e1.getName().equals(SITE)) {
				try {
					site = parseInteger(e1, 1, Integer.MAX_VALUE);
				} catch (ParseException pe) {
					throw new ParseException("Error parsing <" + element.getName() + "> element: " + pe.getMessage());
				}
			} else {
				throw new ParseException("Error parsing <" + element.getName() + "> element: <" + e1.getName() + "> is unrecognized");
			}

		}

		return new AlleleFrequencySampler(site - 1, SequenceType.CODON, fileName);
	}

	private int parseInteger(Element element, int minValue, int maxValue) throws ParseException {
		int value;
		try {
			value = Integer.parseInt(element.getValue());
		} catch (NumberFormatException nfe) {
			throw new ParseException("content of <" + element.getName() + "> is not an integer");
		}
		if (value < minValue) {
			throw new ParseException("value of <" + element.getName() + "> is less than minimum value, " + minValue);
		}
		if (value > maxValue) {
			throw new ParseException("value of <" + element.getName() + "> is greater than minimum value, " + maxValue);
		}
		return value;
	}

	private double parseDouble(Element element, double minValue, double maxValue) throws ParseException {
		double value;
		try {
			value = Double.parseDouble(element.getValue());
		} catch (NumberFormatException nfe) {
			throw new ParseException("content of <" + element.getName() + "> is not a number");
		}
		if (value < minValue) {
			throw new ParseException("value of <" + element.getName() + "> is less than minimum value, " + minValue);
		}
		if (value > maxValue) {
			throw new ParseException("value of <" + element.getName() + "> is greater than minimum value, " + maxValue);
		}
		return value;
	}

	public static class ParseException extends Exception {
        private static final long serialVersionUID = -9196845799436472129L;

        public ParseException() {
		}

		public ParseException(String string) {
			super(string);
		}

		public ParseException(String string, Throwable throwable) {
			super(string, throwable);
		}

		public ParseException(Throwable throwable) {
			super(throwable);
		}
	};
}