package santa.simulator;

import org.jdom.Element;
import santa.simulator.fitness.*;
import santa.simulator.genomes.*;
import santa.simulator.mutators.Mutator;
import santa.simulator.mutators.NucleotideMutator;
import santa.simulator.replicators.*;
import santa.simulator.samplers.*;
import santa.simulator.selectors.*;

import java.util.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: SimulatorParser.java,v 1.19 2006/07/19 14:35:32 kdforc0 Exp $
 */
public class SimulatorParser {

	private final static String SIMULATOR = "santa";

	private final static String REPLICATE_COUNT = "replicates";

	private final static String SIMULATION = "simulation";

	private final static String EPOCH = "epoch";
	private final static String NAME = "name";
	private final static String GENERATION_COUNT = "generationCount";

	private final static String POPULATION = "population";
	private final static String POPULATION_SIZE = "populationSize";
	private final static String INOCULUM = "inoculum";
	private final static String SEQUENCE = "sequence";

	private final static String GENOME_DESCRIPTION = "genome";
	private final static String GENOME_LENGTH = "length";

	private final static String GENE_POOL = "genePool";
	private final static String SIMPLE_GENE_POOL = "simpleGenePool";
	private final static String COMPACT_GENE_POOL = "complexGenePool";

	private final static String SELECTOR = "selector";
	private final static String MONTE_CARLO_SELECTOR = "monteCarloSelector";
	private final static String ROULETTE_WHEEL_SELECTOR = "rouletteWheelSelector";

	private final static String FITNESS_FUNCTION = "fitnessFunction";
	private final static String NUCLEOTIDES = "nucleotides";
	private final static String AMINO_ACIDS = "aminoAcids";
	private final static String NEUTRAL_MODEL_FITNESS_FUNCTION = "neutralFitness";

	private final static String PURIFYING_FITNESS_FUNCTION = "purifyingFitness";
	private final static String FITNESS = "fitness";
	private final static String VALUES = "values";
	private static final String LOW_FITNESS = "lowFitness";
	private final static String MINIMUM_FITNESS = "minimumFitness";
	private static final String PROBABLE_SET = "probableSet";
	private final static String PROBABLE_SET_CHEMICAL = "chemical";
	private final static String PROBABLE_SET_HYDROPATHY = "hydropathy";
	private final static String PROBABLE_SET_VOLUME = "volume";
	private final static String PROBABLE_SET_ESTIMATED = "estimated";

	private static final String RANK = "rank";
	private final static String SEQUENCES = "sequences";
	private static final String BREAK_TIES = "breakTies";
	private final static String BREAK_TIES_RANDOM = "random";
	private final static String BREAK_TIES_ORDERED = "ordered";
	private final static String ORDER = "order";
	private final static String FLUCTUATE = "fluctuate";
	private final static String FLUCTUATE_FITNESS_LIMIT = "fitnessLimit";
	private final static String FLUCTUATE_RATE = "rate";

	private final static String FREQUENCY_DEPENDENT_FITNESS_FUNCTION = "frequencyDependentFitness";
	private final static String SHAPE = "shape";
	private final static String AGE_DEPENDENT_FITNESS_FUNCTION = "ageDependentFitness";
	private final static String DECLINE_RATE = "declineRate";
	private final static String EXPOSURE_DEPENDENT_FITNESS_FUNCTION = "exposureDependentFitness";
	private final static String PENALTY = "penalty";
	private static final Object EMPERICAL_FITNESS_FUNCTION = "empiricalFitness";

	private final static String MUTATOR = "mutator";
	private final static String REPLICATOR = "replicator";

	private final static String SAMPLING_SCHEDULE = "samplingSchedule";
	private final static String SAMPLER = "sampler";
	private final static String FILE_NAME = "fileName";
	private final static String AT_FREQUENCY = "atFrequency";
	private final static String AT_GENERATION = "atGeneration";

	private final static String ALIGNMENT = "alignment";
	private final static String TREE = "tree";
	private final static String SAMPLE_SIZE = "sampleSize";
	private final static String SCHEDULE = "schedule";
	private final static String FORMAT = "format";
	private final static String LABEL = "label";
	private final static String CONSENSUS = "consensus";

	private final static String ALLELE_FREQUENCY = "alleleFrequency";

	private final static String STATISTICS = "statistics";

	private final static String EVENT_LOGGER = "eventLogger";

	private final static String NUCLEOTIDE_MUTATOR = "nucleotideMutator";
	private final static String MUTATION_RATE = "mutationRate";
	private final static String TRANSITION_BIAS = "transitionBias";
	private final static String RATE_BIAS = "rateBias";

	private final static String CLONAL_REPLICATOR = "clonalReplicator";
	private final static String RECOMBINANT_REPLICATOR = "recombinantReplicator";
	private final static String DUAL_INFECTION_PROBABILITY = "dualInfectionProbability";
	private final static String RECOMBINATION_PROBABILITY = "recombinationProbability";

	private static final String ID = "id";
	private static final String REF = "ref";

	/*
		 * Object Cache methods
		 */

	private Map<String, Object> objectIdMap = new HashMap<String, Object>();

	private Object lookupObjectById(String id, Class<? extends Object> expectedType) throws ParseException {
		Object o = objectIdMap.get(id);

		if (o == null) {
			throw new ParseException("Referenced object '" + id + "' was not defined.");
		}

		if (expectedType != null) {
			try {
				expectedType.cast(o);
			} catch (ClassCastException ce) {
				throw new ParseException("Referenced object '" + id + "' is of the wrong type.");
			}
		}

		return o;
	}

	private void storeObjectById(String id, Object o) {
		objectIdMap.put(id, o);
	}

	/*
		 * Parameters
		 */

	Map<String, String> parameters = null;

	public void setParameters(Map<String, String> parameterValueMap) {
		parameters = parameterValueMap;
	};

	private String substituteParameter(String value) throws ParseException {
		if (parameters == null)
			return value;

		if (value.length() != 0) {
			if (value.charAt(0) == '$') {
				String parameter = value.substring(1);
				String parameterValue = parameters.get(parameter);

				if (parameterValue == null) {
					throw new ParseException("Parameter '" + parameter + "' referenced but not defined.");
				}

				return parameterValue;
			} else
				return value;
		} else
			return value;
	}

	/*
		 * Parser methods
		 */
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
			if (e.getName().equals(POPULATION)) {

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
					!e.getName().equals(SAMPLING_SCHEDULE) &&
					!e.getName().equals(EVENT_LOGGER) &&
					!e.getName().equals(EPOCH)) {
				throw new ParseException("Error parsing <" + SIMULATION + "> element: <" + e.getName() + "> is unrecognized");
			}
		}

		if (populationSize == -1) {
			throw new ParseException("Error parsing <" + SIMULATION + "> element: <" + POPULATION_SIZE + "> is missing");
		}

		if (!GenomeDescription.isSet()) {
			throw new ParseException("Error parsing <" + SIMULATION + "> element: <" + GENOME_DESCRIPTION + "> is missing");
		}

		SamplingSchedule samplingSchedule = null;
		GenePool genePool = null;
		Selector selector = null;

		FitnessFunction defaultFitnessFunction = null;
		Mutator defaultMutator = null;
		Replicator defaultReplicator = null;

		for (Object o : element.getChildren()) {
			Element e = (Element)o;
			if (e.getName().equals(GENE_POOL)) {
				genePool = parseGenePool(e);
			} else if (e.getName().equals(SELECTOR)) {
				selector = parseSelector(e);
			} else if (e.getName().equals(SAMPLING_SCHEDULE)) {
				samplingSchedule = parseSamplingSchedule(e);
			} else if (e.getName().equals(EVENT_LOGGER)) {
				parseEventLogger(e);
			} else if (e.getName().equals(FITNESS_FUNCTION)) {
				defaultFitnessFunction = parseFitnessFunction(e);
			} else if (e.getName().equals(MUTATOR)) {
				defaultMutator = parseMutator(e);
			} else if (e.getName().equals(REPLICATOR)) {
				defaultReplicator = parseReplicator(e);
			}
		}

		if (samplingSchedule == null)
			throw new ParseException("Error parsing <" + SIMULATION + "> element: <" + SAMPLING_SCHEDULE + "> is missing");

		if (genePool == null) {
			genePool = new SimpleGenePool();
		}

		if (selector == null) {
			selector = new RouletteWheelSelector();
		}

		if (defaultFitnessFunction == null)
			throw new ParseException("Error parsing <" + SIMULATION + "> element: <" + FITNESS_FUNCTION + "> is missing");

		if (defaultMutator == null)
			throw new ParseException("Error parsing <" + SIMULATION + "> element: <" + MUTATOR + "> is missing");

		if (defaultReplicator == null)
			throw new ParseException("Error parsing <" + SIMULATION + "> element: <" + REPLICATOR + "> is missing");

		List<SimulationEpoch> epochs = new ArrayList<SimulationEpoch>();

		for (Object o : element.getChildren()) {
			Element e = (Element)o;
			if (e.getName().equals(EPOCH)) {
				SimulationEpoch epoch = parseSimulationEpoch(e,
						defaultFitnessFunction, defaultMutator, defaultReplicator);

				epochs.add(epoch);
			}
		}

		if (epochs.isEmpty())
			throw new ParseException("Error parsing <" + SIMULATION + "> element: <" + EPOCH + "> is missing");

		return new Simulation(populationSize, inoculum, genePool, epochs, selector, samplingSchedule);
	}

	SimulationEpoch parseSimulationEpoch(Element element,
	                                     FitnessFunction fitnessFunction, Mutator mutator,
	                                     Replicator replicator) throws ParseException {

		String name = null;
		int generationCount = -1;

		for (Object o : element.getChildren()) {
			Element e = (Element)o;
			if (e.getName().equals(GENERATION_COUNT)) {
				try {
					generationCount = parseInteger(e, 0, Integer.MAX_VALUE);
				} catch (ParseException pe) {
					throw new ParseException("Error parsing <" + EPOCH + "> element: " + pe.getMessage());
				}
			} else if (e.getName().equals(NAME)) {
				name = e.getTextNormalize();
			} else if (!e.getName().equals(FITNESS_FUNCTION) &&
					!e.getName().equals(MUTATOR) &&
					!e.getName().equals(REPLICATOR)) {
				throw new ParseException("Error parsing <" + EPOCH + "> element: <" + e.getName() + "> is unrecognized");
			}
		}

		if (generationCount == -1) {
			throw new ParseException("Error parsing <" + EPOCH + "> element: <" + POPULATION_SIZE + "> is missing");
		}

		for (Object o : element.getChildren()) {
			Element e = (Element)o;
			if (e.getName().equals(FITNESS_FUNCTION)) {
				fitnessFunction = parseFitnessFunction(e);
			} else if (e.getName().equals(MUTATOR)) {
				mutator = parseMutator(e);
			} else if (e.getName().equals(REPLICATOR)) {
				replicator = parseReplicator(e);
			}
		}

		return new SimulationEpoch(name, generationCount, fitnessFunction, mutator, replicator);
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
		List<FitnessFunctionFactor> components = new ArrayList<FitnessFunctionFactor>();

		for (Object o : element.getChildren()) {
			Element e = (Element) o;
			FitnessFunctionFactor factor = null;

			if (e.getName().equals(NEUTRAL_MODEL_FITNESS_FUNCTION)) {
				// don't need to add a factor to the product
			} else if (e.getName().equals(PURIFYING_FITNESS_FUNCTION)) {
				factor = parsePurifyingFitnessFunction(e);
			} else if (e.getName().equals(EMPERICAL_FITNESS_FUNCTION)) {
				factor = parseEmpericalFitnessFunction(e);
			} else if (e.getName().equals(FREQUENCY_DEPENDENT_FITNESS_FUNCTION)) {
				factor = parseFrequencyDependentFitnessFunction(e);
			} else if (e.getName().equals(AGE_DEPENDENT_FITNESS_FUNCTION)) {
				factor = parseAgeDependentFitnessFunction(e);
			} else if (e.getName().equals(EXPOSURE_DEPENDENT_FITNESS_FUNCTION)) {
				factor = parseExposureDependentFitnessFunction(e);
			} else {
				throw new ParseException("Error parsing <" + element.getName()
						+ "> element: <" + e.getName() + "> is unrecognized");
			}

			if (factor != null) {
				components.add(factor);

				if (e.getAttributeValue(ID) != null)
					storeObjectById(e.getAttributeValue(ID), factor);
			}
		}

		return new FitnessFunction(components);

	}

	static private class FitnessFactorCommon {
		SequenceAlphabet      alphabet;
		Set<Integer>          sites;

		FitnessFactorCommon(Set<Integer> sites, SequenceAlphabet alphabet) {
			this.alphabet = alphabet;
			this.sites = sites;
		}
	}

	private FitnessFunctionFactor parseExposureDependentFitnessFunction(Element element) throws ParseException {
		FitnessFunctionFactor result = getFitnessFactor(element, ExposureDependentFitnessFunction.class.getName());

		if (result != null)
			return result;

		FitnessFactorCommon factor = parseFitnessFactor(element);

		double penalty = 0.001;

		for (Object o : element.getChildren()) {
			Element e = (Element)o;

			if (e.getName().equals(PENALTY)) {
				try {
					penalty = parseDouble(e, 0, Double.MAX_VALUE);
				} catch (ParseException pe) {
					throw new ParseException("Error parsing <" + element.getName() + "> element: " + pe.getMessage());
				}
			} else if (!e.getName().equals(NUCLEOTIDES) && !e.getName().equals(AMINO_ACIDS)) {
				throw new ParseException("Error parsing <" + element.getName() + "> element: <" + e.getName() + "> is unrecognized");
			}
		}

		if (penalty < 0) {
			throw new ParseException("Error parsing <" + element.getName() + "> element: expecting <" + PENALTY + ">");
		}

		return new ExposureDependentFitnessFunction(penalty, factor.sites, factor.alphabet);
	}

	/**
	 * @param element
	 * @return
	 * @throws ParseException
	 */
	private FitnessFunctionFactor parseAgeDependentFitnessFunction(Element element) throws ParseException {
		FitnessFunctionFactor result = getFitnessFactor(element, AgeDependentFitnessFunction.class.getName());

		if (result != null)
			return result;

		FitnessFactorCommon factor = parseFitnessFactor(element);

		double declineRate = -1;

		for (Object o : element.getChildren()) {
			Element e = (Element)o;
			if (e.getName().equals(DECLINE_RATE)) {
				try {
					declineRate = parseDouble(e, 0, Double.MAX_VALUE);
				} catch (ParseException pe) {
					throw new ParseException("Error parsing <" + element.getName() + "> element: " + pe.getMessage());
				}
			} else if (!e.getName().equals(NUCLEOTIDES) && !e.getName().equals(AMINO_ACIDS)) {
				throw new ParseException("Error parsing <" + element.getName() + "> element: <" + e.getName() + "> is unrecognized");
			}
		}

		if (declineRate < 0) {
			throw new ParseException("Error parsing <" + element.getName() + "> element: expecting <" + DECLINE_RATE + ">");
		}

		return new AgeDependentFitnessFunction(declineRate, factor.sites, factor.alphabet);
	}

	/**
	 * @param element
	 * @return
	 * @throws ParseException
	 */
	private FitnessFunctionFactor parseFrequencyDependentFitnessFunction(Element element) throws ParseException {
		FitnessFunctionFactor result = getFitnessFactor(element, FrequencyDependentFitnessFunction.class.getName());

		if (result != null)
			return result;

		FitnessFactorCommon factor = parseFitnessFactor(element);

		double shape = -1.0;

		for (Object o : element.getChildren()) {
			Element e = (Element)o;
			if (e.getName().equals(SHAPE)) {
				try {
					shape = parseDouble(e, 0, Double.MAX_VALUE);
				} catch (ParseException pe) {
					throw new ParseException("Error parsing <" + element.getName() + "> element: " + pe.getMessage());
				}
			} else if (!e.getName().equals(NUCLEOTIDES) && !e.getName().equals(AMINO_ACIDS)) {
				throw new ParseException("Error parsing <" + element.getName() + "> element: <" + e.getName() + "> is unrecognized");
			}
		}

		if (shape < 0) {
			throw new ParseException("Error parsing <" + element.getName() + "> element: expecting <" + SHAPE + ">");
		}

		return new FrequencyDependentFitnessFunction(shape, factor.sites, factor.alphabet);
	}

	private FitnessFunctionFactor parsePurifyingFitnessFunction(Element element) throws ParseException {
		FitnessFunctionFactor result = getFitnessFactor(element, PurifyingFitnessFunction.class.getName());
		if (result != null)
			return result;

		FitnessFactorCommon factor = parseFitnessFactor(element);

		PurifyingFitnessRank rank = null;
		PurifyingFitnessModel valueModel = null;
		double fluctuateRate = 0;
		double fluctuateFitnessLimit = 0;

		for (Object o:element.getChildren()) {
			Element e = (Element) o;
			if (e.getName().equals(RANK)) {
				rank = parsePuryfingFitnessRank(e, factor);
			} else if (e.getName().equals(FITNESS)) {
				valueModel = parsePurifyingFitnessModel(e, factor);
			} else if (e.getName().equals(FLUCTUATE)) {
				for (Object o2:e.getChildren()) {
					Element e2 = (Element) o2;
					if (e2.getName().equals(FLUCTUATE_FITNESS_LIMIT)) {
						fluctuateFitnessLimit = parseDouble(e2, 0, 1);
					} else if (e2.getName().equals(FLUCTUATE_RATE)) {
						fluctuateRate = parseDouble(e2, 0, 1);
					} else {
						throw new ParseException("Error parsing <" + e.getName() + "> element: <" + e2.getName() + "> is unrecognized");
					}
				}
			} else if (!e.getName().equals(NUCLEOTIDES) && !e.getName().equals(AMINO_ACIDS)) {
				throw new ParseException("Error parsing <" + element.getName() + "> element: <" + e.getName() + "> is unrecognized");
			}
		}

		if (rank == null)
			throw new ParseException("Error parsing <" + element.getName() + "> element: missing <rank>");

		if (valueModel == null)
			throw new ParseException("Error parsing <" + element.getName() + "> element: missing <fitness>");

		return new PurifyingFitnessFunction(rank, valueModel, fluctuateRate, fluctuateFitnessLimit, factor.sites, factor.alphabet);
	}

	private FitnessFunctionFactor parseEmpericalFitnessFunction(Element element) throws ParseException {
		FitnessFunctionFactor result = getFitnessFactor(element, PurifyingFitnessFunction.class.getName());
		if (result != null)
			return result;

		FitnessFactorCommon factor = parseFitnessFactor(element);

		double[] fitnesses = null;

		for (Object o:element.getChildren()) {
			Element e = (Element) o;

			if (e.getName().equals(VALUES)) {
				try {
					fitnesses = parseNumberList(e, factor.alphabet.getStateCount());
				} catch (ParseException e1) {
					throw new ParseException("Error parsing <" + e.getName() + "> element: " + e1.getMessage());
				}

			} else if (!e.getName().equals(NUCLEOTIDES) && !e.getName().equals(AMINO_ACIDS)) {
				throw new ParseException("Error parsing <" + element.getName() + "> element: <" + e.getName() + "> is unrecognized");
			}
		}

		if (fitnesses == null) {
			throw new ParseException("Error parsing <" + element.getName() + "> element: missing <" + VALUES + "> element");
		}

		return PurifyingFitnessFunction.createEmpiricalFitnessFunction(fitnesses, factor.sites, factor.alphabet);
	}


	private FitnessFunctionFactor getFitnessFactor(Element element, String classType) throws ParseException {
		String ref = element.getAttributeValue(REF);

		if (ref != null) {
			if (!element.getChildren().isEmpty()) {
				throw new ParseException("Error parsing <" + element.getName() + "> element: must be empty when referenced");
			}

			try {
				FitnessFunctionFactor referenced = (FitnessFunctionFactor) lookupObjectById(ref, FitnessFunctionFactor.class);

				if (!referenced.getClass().getName().equals(classType)) {
					throw new ParseException("Error parsing <" + element.getName() + "> element: referenced id '" + ref + "' is not a fitness function of the same type.");
				}

				return referenced;
			} catch (ClassCastException e) {
				throw new ParseException("Error parsing <" + element.getName() + "> element: referenced id '" + ref + "' is not a fitness function.");
			}

		} else
			return null;
	}

	/**
	 * @param element
	 * @throws ParseException
	 */
	private FitnessFactorCommon parseFitnessFactor(Element element) throws ParseException {
		for (Object o:element.getChildren()) {
			Element e = (Element) o;

			if (e.getName().equals(AMINO_ACIDS)) {
				return new FitnessFactorCommon(parseSites(e), SequenceAlphabet.AMINO_ACIDS);
			} else if (e.getName().equals(NUCLEOTIDES)) {
				return new FitnessFactorCommon(parseSites(e), SequenceAlphabet.NUCLEOTIDES);
			}
		}

		throw new ParseException("Error parsing <" + element.getName() + "> element: expecting one of <"
				+ AMINO_ACIDS + "> or <" + NUCLEOTIDES + ">");
	}

	private PurifyingFitnessModel parsePurifyingFitnessModel(Element element, FitnessFactorCommon factor) throws ParseException {
		double minimumFitness = -1;
		double lowFitness = -1;

		for (Object o:element.getChildren()) {
			Element e = (Element) o;

			if (e.getName().equals(VALUES)) {
				double[] fitnesses;
				try {
					fitnesses = parseNumberList(e, factor.alphabet.getStateCount());
				} catch (ParseException e1) {
					throw new ParseException("Error parsing <" + e.getName() + "> element: " + e1.getMessage());
				}

				return new PurifyingFitnessValuesModel(fitnesses);
			} else if (e.getName().equals(MINIMUM_FITNESS)) {
				try {
					minimumFitness = parseDouble(e, 0, 1);
				} catch (ParseException pe) {
					throw new ParseException("Error parsing <" + e.getName() + "> element: " + pe.getMessage());
				}
			} else if (e.getName().equals(LOW_FITNESS)) {
				try {
					lowFitness = parseDouble(e, 0, 1);
				} catch (ParseException pe) {
					throw new ParseException("Error parsing <" + e.getName() + "> element: " + pe.getMessage());
				}
			} else {
				throw new ParseException("Error parsing <" + e.getName() + "> element: <" + e.getName() + "> is unrecognized");
			}
		}
		if (minimumFitness == -1 || lowFitness == -1) {
			throw new ParseException("Error parsing <" + element.getName() + "> element: expecting either a <" + VALUES
					+ "> element, or <" + MINIMUM_FITNESS + ">, <" + LOW_FITNESS + "> elements");
		}


		if (lowFitness == -1) {
			throw new ParseException("Error parsing <" + element.getName() + "> element: missing <" + LOW_FITNESS + ">");
		}
		if (minimumFitness == -1) {
			throw new ParseException("Error parsing <" + element.getName() + "> element: missing <" + MINIMUM_FITNESS + ">");
		}

		return new PurifyingFitnessPiecewiseLinearModel(factor.alphabet, minimumFitness, lowFitness);
	}

	public final static String CHEMICAL_CLASSES =
			"VIL|"+ // Aliphatic
					"F|"+ // Phenylalanine
					"CM|"+ // Sulphur
					"G|"+ //Glycine
					"ST|"+ // Hydroxyl
					"W|"+ // Tryptophan
					"Y|"+ // Tyrosine
					"P|"+   // Proline
					"D|"+    // Acidic
					"NQ|"+    // Amide
					"HKR";    // Basic

	public final static String HYDROPATHY_CLASSES =
			"IVLFCMAW|"+ // Hydropathic
					"GTSYPH|"+ // Neutral
					"DEKNQR"; // Hydrophilic

	public final static String VOLUME_CLASSES =
			"GAS|"+ // 60-90
					"CDPNT|"+ // 108-117
					"EVQH|"+ // 138-154
					"MILKR|"+ // 162-174
					"FYW"; // 189-228

	enum ProbableSetEnum {
		CLASSES, OBSERVED, NUMBER;
	};

	private PurifyingFitnessRank parsePuryfingFitnessRank(Element element, FitnessFactorCommon factor) throws ParseException {

		if (element.getAttribute(REF) != null) {
			if (!element.getChildren().isEmpty()) {
				throw new ParseException("Error parsing <" + element.getName() + "> element: must be empty when referenced");
			}
			return (PurifyingFitnessRank) lookupObjectById(element.getAttributeValue(REF), PurifyingFitnessRank.class);
		}

		List<Sequence> sequences = null;
		List<Byte> stateOrder = null;
		int probableNumber = -1;
		ProbableSetEnum probableSet = null;
		List<Set<Byte>> probableSetClasses = null;
		boolean breakTiesRandom;

		Element breakTiesElement = element.getChild(BREAK_TIES);
		if (breakTiesElement == null)
			throw new ParseException("Error parsing <" + element.getName() + "> element: missing <" + BREAK_TIES + ">");
		String breakTies = breakTiesElement.getTextNormalize();
		if (breakTies.equals(BREAK_TIES_RANDOM))
			breakTiesRandom = true;
		else if (breakTies.equals(BREAK_TIES_ORDERED))
			breakTiesRandom = false;
		else
			throw new ParseException("Error parsing <" + BREAK_TIES + "> element: value must be one of '"
					+ BREAK_TIES_RANDOM + "' or '" + BREAK_TIES_ORDERED + "'");

		for (Object o:element.getChildren()) {
			Element e = (Element) o;

			if (e.getName().equals(SEQUENCES)) {
				sequences = parseAlignment(e.getTextTrim());
			} else if (e.getName().equals(ORDER)) {
				String orderString = e.getTextNormalize();

				stateOrder = new ArrayList<Byte>();

				for (int i = 0; i < orderString.length(); ++i) {
					stateOrder.add(factor.alphabet.parse(orderString.charAt(i)));
				}
			} else if (e.getName().equals(PROBABLE_SET)) {
				String v = e.getTextNormalize();
				if (v.equals(PROBABLE_SET_ESTIMATED)) {
					probableSet = ProbableSetEnum.OBSERVED;
				} else if (v.equals(PROBABLE_SET_CHEMICAL)) {
					probableSet = ProbableSetEnum.CLASSES;
					probableSetClasses = parseProbableSetClasses(factor.alphabet, CHEMICAL_CLASSES);
				} else if (v.equals(PROBABLE_SET_HYDROPATHY)) {
					probableSet = ProbableSetEnum.CLASSES;
                    probableSetClasses = parseProbableSetClasses(factor.alphabet, HYDROPATHY_CLASSES);
				} else if (v.equals(PROBABLE_SET_VOLUME)) {
					probableSet = ProbableSetEnum.CLASSES;
                    probableSetClasses = parseProbableSetClasses(factor.alphabet, VOLUME_CLASSES);
				} else {
					try {
						probableNumber = parseInteger(e, 1, factor.alphabet.getStateCount());
						probableSet = ProbableSetEnum.NUMBER;
					} catch (ParseException pe) {
						probableSet = ProbableSetEnum.CLASSES;
                        probableSetClasses = parseProbableSetClasses(factor.alphabet, v);
					}
				}
			} else if (!e.getName().equals(BREAK_TIES)) {
				throw new ParseException("Error parsing <" + element.getName() + "> element: <" + e.getName() + "> is unrecognized");
			}
		}
		if (probableNumber == -1 || probableSet == null) {
			throw new ParseException("Error parsing <" + element.getName() + "> element: missing <" + PROBABLE_SET + ">");
		}

		if (sequences == null && stateOrder == null) {
			throw new ParseException("Error parsing <" + element.getName() + "> element: missing <" + SEQUENCES + "> or <" + ORDER + ">");
		}

		PurifyingFitnessRank result = new PurifyingFitnessRank(factor.alphabet, sequences, stateOrder, breakTiesRandom);

		if (element.getAttributeValue(ID) != null) {
			storeObjectById(element.getAttributeValue(ID), result);
		}

		return result;
	}

    private List<Set<Byte>> parseProbableSetClasses(SequenceAlphabet alphabet, String str) {
        List<Set<Byte>> classes = new ArrayList<Set<Byte>>();

        String[] sets = str.split("|");
        for (String set : sets) {
            Set<Byte> stateSet = new HashSet<Byte>();
            for (int i = 0; i < set.length(); i++) {
                stateSet.add(alphabet.parse(set.charAt(i)));
            }
            classes.add(stateSet);
        }
        return classes;
    }

    private List<Sequence> parseAlignment(String text) {
		List<Sequence> result = new ArrayList<Sequence>();

		if (text.charAt(0) == '>') {
			/* FASTA format */
			String[] seqStrings = text.split("(?m)^\\s*>.*$");

			for (int i = 1; i < seqStrings.length; i++) {
				seqStrings[i] = seqStrings[i].replaceAll("\\s", "");
				result.add(parseSequence(seqStrings[i]));
			}
		} else {
			/* newline delimited sequences */
			String[] seqStrings = text.split("\\s+");
			for (String seqString:seqStrings) {
				result.add(parseSequence(seqString));
			}
		}
		return result;
	}

	/**
	 * @param element
	 * @return the number list
	 * @throws ParseException
	 */
	private double[] parseNumberList(Element element, int count) throws ParseException {
		String text = element.getTextNormalize();
		String[] values = text.split("\\s*,\\s*|\\s+");
		double[] numbers = new double[values.length];
		for (int i = 0; i < numbers.length; i++) {
			try {
				numbers[i] = Double.parseDouble(values[i]);
			} catch (NumberFormatException e1) {
				throw new ParseException("content of <" + element.getName() + "> is not a number");
			}
		}

		if (numbers.length != count) {
			throw new ParseException("expected " + count + " numbers, got " + numbers.length);
		}

		return numbers;
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
						rateBiases = parseNumberList(e1, 12);
					} catch (ParseException pe) {
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

//		if (element.getChildren().size() == 0) {
//			throw new ParseException("Error parsing <" + element.getName() + "> element: the element is empty");
//		}

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
			} else if (e1.getName().equals(TREE)) {
				sampler = parseTreeSampler(e1, samplingSchedule, fileName);
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

	private Sampler parseTreeSampler(Element element, SamplingSchedule samplingSchedule, String fileName) throws ParseException {

		int sampleSize = -1;
		Map<Integer,Integer> schedule = null;
		String label = null;

		TreeSampler.Format format = TreeSampler.Format.NEXUS;

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
					format = TreeSampler.Format.NEXUS;
				} else if (formatText.equalsIgnoreCase("NEWICK")) {
					format = TreeSampler.Format.NEWICK;
				} else {
					throw new ParseException("Error parsing <" + element.getName() + "> element: <" + FORMAT + "> value of " + formatText + " is unrecognized");
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

		samplingSchedule.setSamplingTrees(true);

		return new TreeSampler(sampleSize, schedule, format, label, fileName);
	}

	private Sampler parseAlleleFrequencySampler(Element element, SamplingSchedule samplingSchedule, String fileName) throws ParseException {

		Set<Integer> sites = null;
		SequenceAlphabet alphabet = null;

		for (Object o : element.getChildren()) {
			Element e1 = (Element)o;
			if (e1.getName().equals(AMINO_ACIDS)) {
				sites = parseSites(e1);
				alphabet = SequenceAlphabet.AMINO_ACIDS;
			} else if (e1.getName().equals(NUCLEOTIDES)) {
				sites = parseSites(e1);
				alphabet = SequenceAlphabet.NUCLEOTIDES;
			} else {
				throw new ParseException("Error parsing <" + element.getName() + "> element: <" + e1.getName()
						+ "> is unrecognized");
			}

		}

		return new AlleleFrequencySampler(sites, alphabet, fileName);
	}

	private void parseEventLogger(Element element) throws ParseException {

		String fileName = null;

		for (Object o : element.getChildren()) {
			Element e1 = (Element)o;
			if (e1.getName().equals(FILE_NAME)) {
				fileName = e1.getTextNormalize();
			} else {
				throw new ParseException("Error parsing <" + element.getName() + "> element: <" + e1.getName()
						+ "> is unrecognized");
			}

		}
		if (fileName != null) {
			try {
				EventLogger.setWriter(new FileWriter(fileName));
			} catch (IOException e) {
				throw new ParseException("Error parsing <" + element.getName() + "> element: Could not open file, " + fileName + ", for writing");
			}
		} else {
			EventLogger.setWriter(new PrintWriter(System.out));
		}
	}

	private int parseInteger(Element element, int minValue, int maxValue) throws ParseException {
		String text = substituteParameter(element.getValue());

		int value;
		try {
			value = Integer.parseInt(text);
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
		String text = substituteParameter(element.getValue());

		double value;
		try {
			value = Double.parseDouble(text);
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
	}
}