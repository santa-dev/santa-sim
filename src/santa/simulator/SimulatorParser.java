
package santa.simulator;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Paths.get;
import org.jdom2.Element;

import santa.simulator.fitness.AgeDependentFitnessFactor;
import santa.simulator.fitness.BetaDistributedPurifyingFitnessModel;
import santa.simulator.fitness.ExposureDependentFitnessFactor;
import santa.simulator.fitness.FitnessFactor;
import santa.simulator.fitness.FitnessFunction;
import santa.simulator.fitness.FrequencyDependentFitnessFactor;
import santa.simulator.fitness.PopulationSizeDependentFitnessFactor;
import santa.simulator.fitness.PurifyingFitnessFactor;
import santa.simulator.fitness.PurifyingFitnessModel;
import santa.simulator.fitness.PurifyingFitnessPiecewiseLinearModel;
import santa.simulator.fitness.PurifyingFitnessRank;
import santa.simulator.fitness.PrematureStopException;
import santa.simulator.fitness.PurifyingFitnessValuesModel;
import santa.simulator.genomes.CompactGenePool;
import santa.simulator.genomes.Feature;
import santa.simulator.genomes.GenePool;
import santa.simulator.genomes.GenomeDescription;
import santa.simulator.genomes.Sequence;
import santa.simulator.genomes.SequenceAlphabet;
import santa.simulator.genomes.SimpleGenePool;
import santa.simulator.genomes.SimpleSequence;
import santa.simulator.genomes.RecombinationHotSpot;
import santa.simulator.mutators.Mutator;
import santa.simulator.mutators.NucleotideMutator;
import santa.simulator.replicators.ClonalReplicator;
import santa.simulator.replicators.RecombinantReplicatorWithHotSpots;
import santa.simulator.replicators.RecombinantReplicator;
import santa.simulator.replicators.Replicator;
import santa.simulator.samplers.AlignmentSampler;
import santa.simulator.samplers.AlleleFrequencySampler;
import santa.simulator.samplers.Sampler;
import santa.simulator.samplers.SamplingSchedule;
import santa.simulator.samplers.StatisticsSampler;
import santa.simulator.samplers.TreeSampler;
import santa.simulator.samplers.GenomeDescriptionSampler;
import santa.simulator.IndelModel;

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
	private final static String POPULATION_TYPE = "populationType";
	private final static String STATIC_POPULATION = "staticPopulation";
	private final static String DYNAMIC_POPULATION = "dynamicPopulation";
	
	private final static String RECOMBINATION_HOTSPOTS = "recombinationHotSpots";
	private final static String RECOMBINATION_HOTSPOT = "recombinationHotSpot";
	private final static String BOOST_FACTOR = "boostFactor";
	
	private final static String INOCULUM = "inoculum";
	private final static String INOCULUM_NONE = "none";
	private final static String INOCULUM_CONSENSUS = "consensus";
	private final static String INOCULUM_RANDOM = "random";
	private final static String INOCULUM_ALL = "all";

	
	private final static String GENOME_DESCRIPTION = "genome";
	private final static String GENOME_LENGTH = "length";
	private final static String FEATURE = "feature";
	private final static String TYPE = "type";
	private final static String NUCLEOTIDE = "nucleotide";
	private final static String AMINO_ACID = "aminoAcid";
	private final static String COORDINATES = "coordinates";

	private final static String GENE_POOL = "genePool";
	private final static String SIMPLE_GENE_POOL = "simpleGenePool";
	private final static String COMPACT_GENE_POOL = "complexGenePool";

	private final static String FITNESS_FUNCTION = "fitnessFunction";
	private final static String SITES = "sites";
	private final static String NEUTRAL_MODEL_FITNESS_FUNCTION = "neutralFitness";

	private final static String PURIFYING_FITNESS_FUNCTION = "purifyingFitness";
	private final static String FITNESS = "fitness";
	private final static String VALUES = "values";
	private static final String LOW_FITNESS = "lowFitness";
	private final static String MINIMUM_FITNESS = "minimumFitness";

    private static final String RANK = "rank";

    private final static String ORDER = "order";
	private final static String ORDER_CHEMICAL = "chemical";
	private final static String ORDER_HYDROPATHY = "hydropathy";
	private final static String ORDER_VOLUME = "volume";
    private final static String ORDER_OBSERVED_FREQUENCY = "observed";
    private static final String PROBABLE_SET = "probableSet";

	private final static String SEQUENCES = "sequences";
	private final static String FILENAME = "file";
	private static final String BREAK_TIES = "breakTies";
	private final static String BREAK_TIES_RANDOM = "random";
	private final static String BREAK_TIES_ORDERED = "ordered";
	private final static String FLUCTUATE = "fluctuate";
	private final static String FLUCTUATE_FITNESS_LIMIT = "fitnessLimit";
	private final static String FLUCTUATE_RATE = "rate";

	private final static String FREQUENCY_DEPENDENT_FITNESS_FUNCTION = "frequencyDependentFitness";
	private final static String SHAPE = "shape";
	private final static String AGE_DEPENDENT_FITNESS_FUNCTION = "ageDependentFitness";
	private final static String DECLINE_RATE = "declineRate";
	private final static String EXPOSURE_DEPENDENT_FITNESS_FUNCTION = "exposureDependentFitness";
	private final static String PENALTY = "penalty";
	// yes, 'empirical' is misspelled throughout this source code.  Thankfully it is correct in the config files.
	private final static String EMPIRICAL_FITNESS_FUNCTION = "empiricalFitness";
	private final static String POPULATION_SIZE_DEPENDENT_FITNESS_FUNCTION = "populationSizeDependentFitness";
	private final static String MAX_POP_SIZE = "maxPopulationSize";

	private final static String MUTATOR = "mutator";
	private final static String REPLICATOR = "replicator";

	private final static String SAMPLING_SCHEDULE = "samplingSchedule";
	private final static String SAMPLER = "sampler";
	private final static String FILE_NAME = "fileName";
	private final static String AT_FREQUENCY = "atFrequency";
	private final static String AT_GENERATION = "atGeneration";

	private final static String ALIGNMENT = "alignment";
	private final static String TREE = "tree";
	private final static String GENOMEDESCRIPTION = "genomedescription";
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
	private final static String MODEL = "model";
	private final static String INDEL_MODEL = "indelmodel";
	private final static String INDEL_PROB = "indelprob";
	private final static String INSERT_PROB = "insertprob";
	private final static String DELETE_PROB = "deleteprob";

	private final static String CLONAL_REPLICATOR = "clonalReplicator";
	private final static String RECOMBINANT_REPLICATOR = "recombinantReplicator";
	private final static String RECOMBINANT_REPLICATOR_WITH_HOTSPOTS = "recombinantReplicatorWithHotspots";

	private final static String DUAL_INFECTION_PROBABILITY = "dualInfectionProbability";
	private final static String RECOMBINATION_PROBABILITY = "recombinationProbability";

    private static final String BETA_DISTIBUTED = "betaDistributed";

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

	//Default populationType is dynamicPopulation but in the xml file static population could be selected
	Simulation parseSimulation(Element element) throws ParseException {

		Simulation.InoculumType inoculumType = Simulation.InoculumType.NONE;

		int populationSize = -1;

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
						String v = e1.getTextNormalize();
						if (v.equals(INOCULUM_NONE)) {
							inoculumType = Simulation.InoculumType.NONE;
						} else if (v.equals(INOCULUM_CONSENSUS)) {
							inoculumType = Simulation.InoculumType.CONSENSUS;
						} else if (v.equals(INOCULUM_RANDOM)) {
							inoculumType = Simulation.InoculumType.RANDOM;
						} else if (v.equals(INOCULUM_ALL)) {
							inoculumType = Simulation.InoculumType.ALL;
						} else {
							// do nothing
						}
					} else {
						throw new ParseException("Error parsing <" + SIMULATION + "> element: <" + e.getName() + "> is unrecognized");
					}
				}
			} else if (!e.getName().equals(GENOME_DESCRIPTION) &&
					!e.getName().equals(GENE_POOL) &&
					!e.getName().equals(FITNESS_FUNCTION) &&
					!e.getName().equals(MUTATOR) &&
					!e.getName().equals(REPLICATOR) &&
					!e.getName().equals(SAMPLING_SCHEDULE) &&
					!e.getName().equals(EVENT_LOGGER) &&
					!e.getName().equals(EPOCH) &&
					!e.getName().equals(POPULATION_TYPE) &&
					!e.getName().equals(RECOMBINATION_HOTSPOTS)) {
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

		FitnessFunction defaultFitnessFunction = null;
		Mutator defaultMutator = null;
		Replicator defaultReplicator = null;
		
		String populationType = null;

		List<RecombinationHotSpot> recombinationHotSpots = new ArrayList<RecombinationHotSpot>();		
		
		for (Object o : element.getChildren()) {
			Element e = (Element)o;
			String name = e.getName();
			if (name.equals(GENE_POOL)) {
				genePool = parseGenePool(e);
			} else if (name.equals(SAMPLING_SCHEDULE)) {
				samplingSchedule = parseSamplingSchedule(e);
			} else if (name.equals(EVENT_LOGGER)) {
				parseEventLogger(e);
			} else if (name.equals(FITNESS_FUNCTION)) {
				defaultFitnessFunction = parseFitnessFunction(e);
			} else if (name.equals(MUTATOR)) {
				defaultMutator = parseMutator(e);
			} else if (name.equals(RECOMBINATION_HOTSPOTS)){
				recombinationHotSpots = parseRecombinationHotSpots(e);
				GenomeDescription.setHotSpots(recombinationHotSpots);
			} else if (name.equals(POPULATION_TYPE)){
				populationType = (String) e.getTextNormalize();
			} else if (name.equals(REPLICATOR)) {
				defaultReplicator = parseReplicator(e);
			} 
		}

		if (samplingSchedule == null)
			throw new ParseException("Error parsing <" + SIMULATION + "> element: <" + SAMPLING_SCHEDULE + "> is missing");

		if (genePool == null) {
			genePool = new SimpleGenePool();
		}

		if (defaultFitnessFunction == null)
			throw new ParseException("Error parsing <" + SIMULATION + "> element: <" + FITNESS_FUNCTION + "> is missing");

		if (defaultMutator == null)
			throw new ParseException("Error parsing <" + SIMULATION + "> element: <" + MUTATOR + "> is missing");

		if (defaultReplicator == null)
			throw new ParseException("Error parsing <" + SIMULATION + "> element: <" + REPLICATOR + "> is missing");
		
		if (populationType == null) {
			populationType = STATIC_POPULATION;
        }

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
		
		if (populationType.equals(STATIC_POPULATION))
			return new  Simulation(populationSize, inoculumType, genePool, epochs, samplingSchedule, populationType);
		else if (populationType.equals(DYNAMIC_POPULATION))
			return new Simulation(populationSize, inoculumType, genePool, epochs, samplingSchedule);
		else throw new ParseException("unrecognized populatioin type. should be either staticPopulation or dynamicPopulation.");
		
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

	private void parseGenomeDescription(Element element) throws ParseException {

		int genomeLength = -1;
		List<Feature> features = new ArrayList<Feature>();
		List<Sequence> sequences = null;

		for (Object o : element.getChildren()) {
			Element e = (Element)o;
			if (e.getName().equals(GENOME_LENGTH)) {
				try {
					genomeLength = parseInteger(e, 1, Integer.MAX_VALUE);
				} catch (ParseException pe) {
					throw new ParseException("Error parsing <" + GENOME_DESCRIPTION + "> element: " + pe.getMessage());
				}
			} else if (e.getName().equals(FEATURE)) {
                features.add(parseFeature(e));
			} else if (e.getName().equals(SEQUENCES)) {
				if (e.getAttributeValue(FILENAME) != null) {
					try {
						// quick and dirty one-liner to read in a text file.
						// http://jdevelopment.nl/java-7-oneliner-read-file-string/
						String text = new String(readAllBytes(get(e.getAttributeValue(FILENAME))));
						sequences = parseAlignment(text);
					} catch (IOException eio) {
						throw new ParseException("Error parsing <" + SEQUENCES + "> file attribute: Cannot open file " + eio.getMessage());
					}
				} else {
					sequences = parseAlignment(e.getTextTrim());
				}
			} else  {
				throw new ParseException("Error parsing <" + element.getName() + "> element: <" + e.getName() + "> is unrecognized");
			}
		}

		if (genomeLength == -1) {
			throw new ParseException("Error parsing <" + element.getName() + "> the element: <" + GENOME_LENGTH + "> is missing");
		}

		GenomeDescription.setDescription(genomeLength, features, sequences);
	}

	private Feature parseFeature(Element element) throws ParseException {

		String name = null;
		Feature.Type type = Feature.Type.NUCLEOTIDE;
		String sites = null;
		List<String> parts = new ArrayList<String>();

		for (Object o : element.getChildren()) {
			Element e = (Element) o;

			if (e.getName().equals(NAME)) {
				name = e.getTextNormalize();
			} else if (e.getName().equals(TYPE)) {
				if (e.getTextNormalize().equals(AMINO_ACID)) {
					type = Feature.Type.AMINO_ACID;
				} else if (e.getTextNormalize().equals(NUCLEOTIDE)) {
					type = Feature.Type.NUCLEOTIDE;
				} else {
					String msg = String.format("Error parsing <%s> element: <%s> should be '%s' or '%s'",
											   element.getName(), e.getName(),NUCLEOTIDE, AMINO_ACID);
					throw new ParseException(msg);
				}
			} else if (e.getName().equals(COORDINATES)) {
				sites = e.getTextNormalize();
				parts = Arrays.asList(sites.split(","));
			} else {
				throw new ParseException("Error parsing <" + element.getName()
						+ "> element: <" + e.getName() + "> is unrecognized");
			}
		}

		Feature feature = new Feature(name, type);

		try {
			for (String part: parts) {
				part = part.trim();

				if (part.contains("-")) {
					String[] ranges = part.split("-");

					if (ranges.length != 2) {
						throw new ParseException("Error parsing <" + element.getName()
								+ "> element: \"" + part + "\" is not a proper range.");
					}

					int start = Integer.parseInt(ranges[0]);
					int end = Integer.parseInt(ranges[1]);

					feature.addFragment(start - 1, end-start+1);
				} else {
					int site = Integer.parseInt(part);
					feature.addFragment(site - 1, 1);
				}
			}
		} catch (NumberFormatException e) {
			throw new ParseException("Syntax error parsing <" + element.getName()
					+ "> element: " + e.getMessage());
		}

		return feature;
	}

	
	private RecombinationHotSpot parseRecombinationHotSpot(Element element) throws ParseException{		
		RecombinationHotSpot hotspot = null;
		double factor = 0;
		FeatureAndSites segment = parseFeatureAndSites(element);
		for (Object o : element.getChildren()) {
			Element e = (Element) o;
			if (e.getName().equals(BOOST_FACTOR)) {
				factor =  parseDouble(e, 0, Double.MAX_VALUE);
				if (factor < 0) {
					throw new ParseException("RecombinationHotSpot boost factor must be > 0");
				}
			}
		}
		hotspot = new RecombinationHotSpot(segment.sites,factor);
		return hotspot;
	}
	
	private List<RecombinationHotSpot> parseRecombinationHotSpots(Element element) throws ParseException{
		List<RecombinationHotSpot> recombinationHotSpots = new ArrayList<RecombinationHotSpot>();		
		for (Object o : element.getChildren()) {
			Element e = (Element) o;
			RecombinationHotSpot segment = null;			
			if (e.getName().equals(RECOMBINATION_HOTSPOT)) {
				segment = parseRecombinationHotSpot(e);
				recombinationHotSpots.add(segment);
			}
		}
		return recombinationHotSpots;		
	}
	
	private FitnessFunction parseFitnessFunction(Element element) throws ParseException {
		List<FitnessFactor> components = new ArrayList<FitnessFactor>();

		for (Object o : element.getChildren()) {
			Element e = (Element) o;
			FitnessFactor factor = null;

			if (e.getName().equals(NEUTRAL_MODEL_FITNESS_FUNCTION)) {
				// don't need to add a factor to the product
			} else if (e.getName().equals(PURIFYING_FITNESS_FUNCTION)) {
				factor = parsePurifyingFitnessFunction(e);
			} else if (e.getName().equals(EMPIRICAL_FITNESS_FUNCTION)) {
				factor = parseEmpiricalFitnessFunction(e);
			} else if (e.getName().equals(FREQUENCY_DEPENDENT_FITNESS_FUNCTION)) {
				factor = parseFrequencyDependentFitnessFunction(e);
			} else if (e.getName().equals(AGE_DEPENDENT_FITNESS_FUNCTION)) {
				factor = parseAgeDependentFitnessFunction(e);
			} else if (e.getName().equals(EXPOSURE_DEPENDENT_FITNESS_FUNCTION)) {
				factor = parseExposureDependentFitnessFunction(e);
			} else if (e.getName().equals(POPULATION_SIZE_DEPENDENT_FITNESS_FUNCTION)) {
				factor = parsePopulationSizeDependentFitnessFunction(e);
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

	static private class FeatureAndSites {
		Feature          feature;
		Set<Integer>    sites;

		FeatureAndSites(Feature feature, Set<Integer> sites) {
			this.feature = feature;
			this.sites = sites;
		}
	}

	private FitnessFactor parseExposureDependentFitnessFunction(Element element) throws ParseException {
		FitnessFactor result = getFitnessFactor(element, ExposureDependentFitnessFactor.class.getName());

		if (result != null)
			return result;

		FeatureAndSites factor = parseFeatureAndSites(element);

		double penalty = 0.001;

		for (Object o : element.getChildren()) {
			Element e = (Element)o;

			if (e.getName().equals(PENALTY)) {
				try {
					penalty = parseDouble(e, 0, Double.MAX_VALUE);
				} catch (ParseException pe) {
					throw new ParseException("Error parsing <" + element.getName() + "> element: " + pe.getMessage());
				}
			} else if (!e.getName().equals(FEATURE) && !e.getName().equals(SITES)) {
				throw new ParseException("Error parsing <" + element.getName() + "> element: <" + e.getName() + "> is unrecognized");
			}
		}

		if (penalty < 0) {
			throw new ParseException("Error parsing <" + element.getName() + "> element: expecting <" + PENALTY + ">");
		}

		return new ExposureDependentFitnessFactor(penalty, factor.feature, factor.sites);
	}

	/**
	 * @param element
	 * @return
	 * @throws ParseException
	 */
	private FitnessFactor parseAgeDependentFitnessFunction(Element element) throws ParseException {
		FitnessFactor result = getFitnessFactor(element, AgeDependentFitnessFactor.class.getName());

		if (result != null)
			return result;

		FeatureAndSites factor = parseFeatureAndSites(element);

		double declineRate = -1;

		for (Object o : element.getChildren()) {
			Element e = (Element)o;
			if (e.getName().equals(DECLINE_RATE)) {
				try {
					declineRate = parseDouble(e, 0, Double.MAX_VALUE);
				} catch (ParseException pe) {
					throw new ParseException("Error parsing <" + element.getName() + "> element: " + pe.getMessage());
				}
			} else if (!e.getName().equals(FEATURE) && !e.getName().equals(SITES)) {
				throw new ParseException("Error parsing <" + element.getName() + "> element: <" + e.getName() + "> is unrecognized");
			}
		}

		if (declineRate < 0) {
			throw new ParseException("Error parsing <" + element.getName() + "> element: expecting <" + DECLINE_RATE + ">");
		}

		return new AgeDependentFitnessFactor(declineRate, factor.feature, factor.sites);
	}
	
	private FitnessFactor parsePopulationSizeDependentFitnessFunction(Element element) throws ParseException {
		FitnessFactor result = getFitnessFactor(element, PopulationSizeDependentFitnessFactor.class.getName());

		if (result != null)
			return result;

		FeatureAndSites factor = parseFeatureAndSites(element);

		double declineRate = -1;
		double maxPopSize = -1;
		
		for (Object o : element.getChildren()) {
			Element e = (Element)o;
			if (e.getName().equals(DECLINE_RATE)) {
				try {
					declineRate = parseDouble(e, 0, Double.MAX_VALUE);
				} catch (ParseException pe) {
					throw new ParseException("Error parsing <" + element.getName() + "> element: " + pe.getMessage());
				}
			} else if(e.getName().equals(MAX_POP_SIZE)) {
				try {
					maxPopSize = parseDouble(e, 0, Double.MAX_VALUE);
				} catch (ParseException pe) {
					throw new ParseException("Error parsing <" + element.getName() + "> element: " + pe.getMessage());
				}
			}else if (!e.getName().equals(FEATURE) && !e.getName().equals(SITES)) {
				throw new ParseException("Error parsing <" + element.getName() + "> element: <" + e.getName() + "> is unrecognized");
			}
		}

		if (declineRate < 0 || maxPopSize < 0) {
			throw new ParseException("Error parsing <" + element.getName() + "> element: expecting <" + DECLINE_RATE + ">");
		}

		return new PopulationSizeDependentFitnessFactor((int) maxPopSize, declineRate, factor.feature, factor.sites);
	}

	/**
	 * @param element
	 * @return
	 * @throws ParseException
	 */
	private FitnessFactor parseFrequencyDependentFitnessFunction(Element element) throws ParseException {
		FitnessFactor result = getFitnessFactor(element, FrequencyDependentFitnessFactor.class.getName());

		if (result != null)
			return result;

		FeatureAndSites factor = parseFeatureAndSites(element);

		double shape = -1.0;

		for (Object o : element.getChildren()) {
			Element e = (Element)o;
			if (e.getName().equals(SHAPE)) {
				try {
					shape = parseDouble(e, 0, Double.MAX_VALUE);
				} catch (ParseException pe) {
					throw new ParseException("Error parsing <" + element.getName() + "> element: " + pe.getMessage());
				}
			} else if (!e.getName().equals(FEATURE) && !e.getName().equals(SITES)) {
				throw new ParseException("Error parsing <" + element.getName() + "> element: <" + e.getName() + "> is unrecognized");
			}
		}

		if (shape < 0) {
			throw new ParseException("Error parsing <" + element.getName() + "> element: expecting <" + SHAPE + ">");
		}

		return new FrequencyDependentFitnessFactor(shape, factor.feature, factor.sites);
	}

	private FitnessFactor parsePurifyingFitnessFunction(Element element) throws ParseException {
		// parse <ref> element to reuse a purifying fitness factor that has already been specified.
		FitnessFactor result = getFitnessFactor(element, PurifyingFitnessFactor.class.getName());
		if (result != null)
			return result;

		FeatureAndSites factor = parseFeatureAndSites(element);

		PurifyingFitnessRank rank = null;
		PurifyingFitnessModel valueModel = null;
		double fluctuateRate = 0;
		double fluctuateFitnessLimit = 0;

		for (Object o:element.getChildren()) {
			Element e = (Element) o;
			if (e.getName().equals(RANK)) {
				rank = parsePurifyingFitnessRank(e, factor);
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
			} else if (!e.getName().equals(FEATURE) && !e.getName().equals(SITES)) {
                throw new ParseException("Error parsing <" + element.getName() + "> element: <" + e.getName() + "> is unrecognized");
			}
		}

		if (rank == null)
			throw new ParseException("Error parsing <" + element.getName() + "> element: missing <rank>");

		if (valueModel == null)
			throw new ParseException("Error parsing <" + element.getName() + "> element: missing <fitness>");

		return new PurifyingFitnessFactor(rank, valueModel, fluctuateRate, fluctuateFitnessLimit, factor.feature, factor.sites);
	}

	private FitnessFactor parseEmpiricalFitnessFunction(Element element) throws ParseException {
		// parse <ref> element to reuse a purifying fitness factor that has already been specified.
		FitnessFactor result = getFitnessFactor(element, PurifyingFitnessFactor.class.getName());
		if (result != null)
			return result;

		FeatureAndSites factor = parseFeatureAndSites(element);

		double[] fitnesses = null;

		for (Object o:element.getChildren()) {
			Element e = (Element) o;

			if (e.getName().equals(VALUES)) {
				try {
					fitnesses = parseNumberList(e);
					if (factor.feature != null &&
							factor.feature.getFeatureType() == Feature.Type.AMINO_ACID) {
						if (fitnesses.length != 20) {
							throw new ParseException("expected 20 fitnesses, got " + fitnesses.length);
						}
					} else {
						if (fitnesses.length != 4) {
							throw new ParseException("expected 4 fitnesses, got " + fitnesses.length);
						}
					}
				} catch (ParseException e1) {
					throw new ParseException("Error parsing <" + e.getName() + "> element: " + e1.getMessage());
				}

			} else if (!e.getName().equals(FEATURE) && !e.getName().equals(SITES)) {
				throw new ParseException("Error parsing <" + element.getName() + "> element: <" + e.getName() + "> is unrecognized");
			}
		}

		if (fitnesses == null) {
			throw new ParseException("Error parsing <" + element.getName() + "> element: missing <" + VALUES + "> element");
		}

		return PurifyingFitnessFactor.createEmpiricalFitnessFunction(fitnesses, factor.feature, factor.sites);
	}


	private FitnessFactor getFitnessFactor(Element element, String classType) throws ParseException {
		String ref = element.getAttributeValue(REF);

		if (ref != null) {
			if (!element.getChildren().isEmpty()) {
				throw new ParseException("Error parsing <" + element.getName() + "> element: must be empty when referenced");
			}

			try {
				FitnessFactor referenced = (FitnessFactor) lookupObjectById(ref, FitnessFactor.class);

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
	private FeatureAndSites parseFeatureAndSites(Element element) throws ParseException {
		Feature feature = null;
		Set<Integer> sites = null;

		for (Object o:element.getChildren()) {
			Element e = (Element) o;

			if (e.getName().equals(FEATURE)) {
				String featureName = e.getTextNormalize();
				feature = GenomeDescription.root.getFeature(featureName);
				if (feature == null) {
					throw new ParseException("Error parsing <" + element.getName() + "> element: referenced feature '" + featureName + "' is not defined.");
				}
			} else if (!e.getName().equals(SITES)) {
                // ignore other elements

                //throw new ParseException("Error parsing <" + element.getName() + "> element: <" + e.getName() + "> is unrecognized");
			}
		}

        if (feature == null) {
			// there is always the complete genome feature
			feature = GenomeDescription.root.getFeature("genome");
			assert(feature != null);
		}

        for (Object o:element.getChildren()) {
            Element e = (Element) o;

            if (e.getName().equals(SITES)) {
                sites = parseSites(e, feature);
            }
        }

        if (sites == null || sites.size() == 0) {
            // assume the full length of the feature
            sites = new TreeSet<Integer>();
            for (int i = 0; i < feature.getLength(); i++) {
                sites.add(i);
            }
        }
		return new FeatureAndSites(feature, sites);
	}

	private PurifyingFitnessModel parsePurifyingFitnessModel(Element element, FeatureAndSites factor) throws ParseException {
		double minimumFitness = -1;
		double lowFitness = -1;

		SequenceAlphabet alphabet = SequenceAlphabet.NUCLEOTIDES;
		if (factor.feature != null) {
			if (factor.feature.getFeatureType() == Feature.Type.AMINO_ACID) {
				alphabet = SequenceAlphabet.AMINO_ACIDS;
			}
		}

		for (Object o:element.getChildren()) {
			Element e = (Element) o;

            if (e.getName().equals(BETA_DISTIBUTED)) {

                // Carrasco et al (2007)  J Virology 81:12979-12984
                double a = 1.151;
                double b = 1.709;
                double pLethal = 0.4;

                int alphabetSize = alphabet.getStateCount();
                int sites = factor.sites.size();

                Element aChild = e.getChild("a");
                a = Double.parseDouble(aChild.getText());
                Element bChild = e.getChild("b");
                b = Double.parseDouble(bChild.getText());
                Element pChild = e.getChild("pLethal");
                pLethal = Double.parseDouble(pChild.getText());

                return new BetaDistributedPurifyingFitnessModel(a,b, pLethal, alphabetSize, sites);
            }
            else if (e.getName().equals(VALUES)) {
				double[] fitnesses;
				try {
					fitnesses = parseNumberList(e);

					// confirm that we parsed one fitness value for each possible state.
					if (fitnesses.length != alphabet.getStateCount()) {
						throw new ParseException("expected " + alphabet.getStateCount() +" fitnesses, got " + fitnesses.length);
					}
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

		return new PurifyingFitnessPiecewiseLinearModel(alphabet, minimumFitness, lowFitness);
	}

	public final static String CHEMICAL_CLASSES =
			"AVIL|"+ // Aliphatic
					"F|"+ // Phenylalanine
					"CM|"+ // Sulphur
					"G|"+ //Glycine
					"ST|"+ // Hydroxyl
					"W|"+ // Tryptophan
					"Y|"+ // Tyrosine
					"P|"+   // Proline
					"DE|"+    // Acidic
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

	enum OrderEnum {
		CLASSES, OBSERVED, STATES;
	};

	private PurifyingFitnessRank parsePurifyingFitnessRank(Element element, FeatureAndSites factor) throws ParseException {

		if (element.getAttribute(REF) != null) {
			if (!element.getChildren().isEmpty()) {
				throw new ParseException("Error parsing <" + element.getName() + "> element: must be empty when referenced");
			}
			return (PurifyingFitnessRank) lookupObjectById(element.getAttributeValue(REF), PurifyingFitnessRank.class);
		}

		SequenceAlphabet alphabet = SequenceAlphabet.NUCLEOTIDES;
		if (factor.feature != null) {
			if (factor.feature.getFeatureType() == Feature.Type.AMINO_ACID) {
				alphabet = SequenceAlphabet.AMINO_ACIDS;
			}
		}

		List<Byte> stateOrder = null;
		int probableNumber = -1;
		OrderEnum order = null;
		List<Set<Byte>> orderSetClasses = null;
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

			if (e.getName().equals(ORDER)) {
				String v = e.getTextNormalize();

                if (v.equals(ORDER_OBSERVED_FREQUENCY)) {
                    order = OrderEnum.OBSERVED;
                } else if (v.equals(ORDER_CHEMICAL)) {
                    order = OrderEnum.CLASSES;
                    orderSetClasses = parseProbableSetClasses(alphabet, CHEMICAL_CLASSES);
                } else if (v.equals(ORDER_HYDROPATHY)) {
                    order = OrderEnum.CLASSES;
                    orderSetClasses = parseProbableSetClasses(alphabet, HYDROPATHY_CLASSES);
                } else if (v.equals(ORDER_VOLUME)) {
                    order = OrderEnum.CLASSES;
                    orderSetClasses = parseProbableSetClasses(alphabet, VOLUME_CLASSES);
                } else {
                    if (v.contains("|")) {
                        order = OrderEnum.CLASSES;
                        orderSetClasses = parseProbableSetClasses(alphabet, v);
                    } else {
                        order = OrderEnum.STATES;
                        stateOrder = new ArrayList<Byte>();

                        for (int i = 0; i < v.length(); ++i) {
                            stateOrder.add(alphabet.parse(v.charAt(i)));
                        }
                    }
                }
			} else if (e.getName().equals(PROBABLE_SET)) {
			    probableNumber = parseInteger(e, 1, alphabet.getStateCount());
			} else if (!e.getName().equals(BREAK_TIES)) {
				throw new ParseException("Error parsing <" + element.getName() + "> element: <" + e.getName() + "> is unrecognized");
			}
		}

        if (order == null) {
			throw new ParseException("Error parsing <" + element.getName() + "> element: missing <" + ORDER + ">");
		}

		PurifyingFitnessRank result = null;

		try {
			switch (order) {
			case CLASSES:
				result = new PurifyingFitnessRank(factor.feature, orderSetClasses, breakTiesRandom, probableNumber);
				break;
			case STATES:
				result = new PurifyingFitnessRank(factor.feature, stateOrder, probableNumber, breakTiesRandom);
				break;
			case OBSERVED:
				result = new PurifyingFitnessRank(factor.feature, probableNumber, breakTiesRandom);
				break;
			}
		} catch(PrematureStopException e) {
			String msg = String.format("premature STOP codon in sequence #%d, feature %s, position %d",
									   e.index, factor.feature.getName(), e.position);
			throw new ParseException(String.format("Error parsing <%s> element: %s",  element.getName(), msg));
		}

		if (element.getAttributeValue(ID) != null) {
			storeObjectById(element.getAttributeValue(ID), result);
		}

		return result;
	}

	private List<Set<Byte>> parseProbableSetClasses(SequenceAlphabet alphabet, String str) throws ParseException {
        Set<Byte> completeCheck = new HashSet<Byte>();
		List<Set<Byte>> classes = new ArrayList<Set<Byte>>();

		String[] sets = str.split("\\|");
		for (String set : sets) {
			Set<Byte> stateSet = new HashSet<Byte>();
			for (int i = 0; i < set.length(); i++) {
				byte symbol = alphabet.parse(set.charAt(i));
                stateSet.add(symbol);
                if (completeCheck.contains(symbol)) {
                    throw new ParseException("symbol '" + set.charAt(i) + "' occurs more than once in classes '" + str + "'");
                }
                completeCheck.add(symbol);
			}
			classes.add(stateSet);
		}
        
        if (completeCheck.size() != alphabet.getStateCount()) {
            throw new ParseException("classes are not complete: '" + str + "'");
        }
        
		return classes;
	}

	private List<Sequence> parseAlignment(String text) throws ParseException {
		List<Sequence> result = new ArrayList<Sequence>();
        int firstLength = 0;
		String[] seqStrings;

		if (text.charAt(0) == '>') {
			/* FASTA format */
			seqStrings = text.split("(?m)^\\s*>.*$");
		} else {
			/* newline delimited sequences */
			seqStrings = text.split("\\s+");
		}
		
		int i = 1;
		for (String s: seqStrings) {
			if (s.equals(""))
				continue;
			try { // catch ParseExceptions
				// remove all whitespace characters in the sequences
				s = s.replaceAll("\\s", "");
				
				Sequence seq = parseSequence(s);
				if (firstLength > 0) {
					if (seq.getLength() != firstLength) {
						throw new ParseException("Expected length " + firstLength + ", got " + seq.getLength());
					}
				} else {
					firstLength = seq.getLength();
				}
				result.add(seq);
			} catch (PrematureStopException pe) {
				throw new ParseException("Premature stop codon detected in sequence " + i + ": " + pe.getMessage());
			} catch (ParseException pe) {
				throw new ParseException("Error in sequence " + i + ": " + pe.getMessage());
			}
		}
		return result;
	}

	public Sequence parseSequence(String sequenceString) {
		return new SimpleSequence(sequenceString);
	}

	/**
	 * @param element
	 * @return the number list
	 * @throws ParseException
	 */
	public double[] parseNumberList(Element element) throws ParseException {
		String text = element.getTextNormalize();
		String[] values = text.split("\\s*,\\s*|\\s+");
		double[] numbers = new double[values.length];
		for (int i = 0; i < numbers.length; i++) {
			try {
				numbers[i] = Double.parseDouble(substituteParameter(values[i]));
			} catch (NumberFormatException e1) {
				throw new ParseException("content of <" + element.getName() + "> is not a number");
			}
		}

		return numbers;
	}

	private Set<Integer> parseSites(Element element, Feature feature) throws ParseException {
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

					for (int j = start; j <= end; j++) {
						addSite(result, j, feature);
					}
				} else {
					int site = Integer.parseInt(part);
					addSite(result, site, feature);
				}
			}
		} catch (NumberFormatException e) {
			throw new ParseException("Error parsing <" + element.getName()
					+ "> element: " + e.getMessage());
		}

		return result;
	}

    private void addSite(Set<Integer> result, int j, Feature feature) throws ParseException {
        if (j <= 0 || j > feature.getLength()) {
            throw new ParseException("Error parsing <" + SITES + ">: "
                    + j + " is out the valid range (1 - " + feature.getLength() + ") for feature '"
                    + feature.getName() + "'");
        }

        result.add(j - 1);
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
			IndelModel indelModel = null;
			double insertProb = -1.0; // negative value to indicate unset
			double deleteProb = -1.0; // negative value to indicate unset

			try {
				for (Object o : e.getChildren()) {
					Element e1 = (Element)o;
					try {
						// this should really be a switch statement if we could use a modern java implementation...
						if (e1.getName().equals(MUTATION_RATE)) {
							mutationRate = parseDouble(e1, 0.0, Double.MAX_VALUE);
						} else if (e1.getName().equals(TRANSITION_BIAS)) {
							transitionBias = parseDouble(e1, 0.0, Double.MAX_VALUE);
						} else if (e1.getName().equals(RATE_BIAS)) {
							rateBiases = parseNumberList(e1);
							if (rateBiases.length != 12) {
								throw new ParseException("expected 12 rate biases, got " + rateBiases.length);
							}
						} else if (e1.getName().equals(INSERT_PROB)) {
							// http://abacus.gene.ucl.ac.uk/software/indelible/manual/model.shtml#[indelmodel]
							if (insertProb != -1.0) {
								throw new ParseException("insertion probability is already set - see previous <" + INSERT_PROB + "> or <" + INDEL_PROB + ">.");
							}
							insertProb = parseDouble(e1, 0.0, 1.0);
						} else if (e1.getName().equals(DELETE_PROB)) {
							if (deleteProb != -1.0) {
								throw new ParseException("deletion probability already set - see previous <" + DELETE_PROB + "> or <" + INDEL_PROB + ">.");
							}
							deleteProb = parseDouble(e1, 0.0, 1.0);
						} else if (e1.getName().equals(INDEL_PROB)) {
							if (insertProb != -1.0 || deleteProb != -1.0) {
								throw new ParseException("insertion or deletion probability already set - see previous <" + DELETE_PROB + ">, <" + INSERT_PROB + ">, or <" + INDEL_PROB + ">.");
							}
							insertProb = parseDouble(e1, 0.0, 1.0);
							deleteProb = insertProb;
						} else if (e1.getName().equals(INDEL_MODEL)) {
							indelModel = parseIndelModel(e1);
						} else {
							throw new ParseException("element is unrecognized");
						}
						
					} catch(ParseException pe) {
						throw new ParseException("element: <" + e1.getName() + ">: " + pe.getMessage());
					}
				}

				if (mutationRate < 0.0) {
					throw new ParseException("<" + MUTATION_RATE + "> is missing");
				}

				if (transitionBias != -1 && rateBiases != null) {
					throw new ParseException("specify either <" + TRANSITION_BIAS + "> or <" + RATE_BIAS + ">, but not both.");
				}

				if (transitionBias == -1 && rateBiases == null) {
					throw new ParseException("must specify one of <" + TRANSITION_BIAS + "> or <" + RATE_BIAS + ">.");
				}

				// insert and delete probability default to 0 if left unspecificed.
				if (insertProb < 0) insertProb = 0;
				if (deleteProb < 0) deleteProb = 0;
				  
			
			} catch (ParseException pe) {
				throw new ParseException("Error parsing <" + e.getName() + "> element: " + pe.getMessage());
			}

			return new NucleotideMutator(mutationRate, transitionBias, rateBiases, insertProb, deleteProb, indelModel);
		} else {
			throw new ParseException("Error parsing <" + element.getName() + "> element: <" + e.getName() + "> is unrecognized");
		}
			
	}


	private IndelModel parseIndelModel(Element element) throws ParseException {
		String modelName = null;
		IndelModel m = null;

		modelName = element.getAttributeValue(MODEL);
		if (modelName.equalsIgnoreCase("NB")) {
			// Parse Negative binomial model
			double insertParams[] = parseNumberList(element);
			if (insertParams.length != 2) {
				throw new ParseException("expected 2 insertion process parameters, got " + insertParams.length);
			}
			double q = insertParams[0];
			int r = (int) insertParams[1];
			if (q <= 0 || q > 1.0) {
				throw new ParseException("Negative-binomial distribution, expected first parameter to be between (0, 1.0], got " + q);
			}
			if (r <= 0 || r != insertParams[1]) {
				throw new ParseException("Negative-binomial distribution, expected second parameter to be a positive integer, got " + insertParams[1]);
			}
			m = new NegBinIndelModel(r, q);
		} else if (modelName.equalsIgnoreCase("zipfian")) {
			// m = ZifIndelModel.fromXML(element);
		} else if (modelName.equalsIgnoreCase("lavalette")) {
			// m = LavIndelModel.fromXML(element);
		} else {
			throw new ParseException("Error parsing <" + element.getName() + "> element: Unrecognized indel model specification");
		}
		return(m);
	}


	private Replicator parseReplicator(Element element) throws ParseException {

		if (element.getChildren().size() == 0) {
			throw new ParseException("Error parsing <" + element.getName() + "> element: the element is empty");
		}

		Element e = (Element)element.getChildren().get(0);
		if (e.getName().equals(CLONAL_REPLICATOR)) {
			return new ClonalReplicator();
		} else if (e.getName().equals(RECOMBINANT_REPLICATOR_WITH_HOTSPOTS)) {
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
	        
			return new RecombinantReplicatorWithHotSpots(dualInfectionProbability, recombinationProbability);
		} 
		
		else if (e.getName().equals(RECOMBINANT_REPLICATOR)) {
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
		} 
		
		
			else  {
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

	private SamplingSchedule parseSamplingSchedule(Element element) throws ParseException {
		SamplingSchedule samplingSchedule = new SamplingSchedule();

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
			} else if (e1.getName().equals(GENOMEDESCRIPTION)) {
				sampler = parseGenomeDescriptionSampler(e1, samplingSchedule, fileName);
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

	private Sampler parseGenomeDescriptionSampler(Element element, SamplingSchedule samplingSchedule, String fileName) throws ParseException {
		int sampleSize = -1;
		Map<Integer,Integer> schedule = null;
		String label = null;

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
			} else if (e1.getName().equals(LABEL)) {
				label = e1.getTextNormalize();
			} else {
				throw new ParseException("Error parsing <" + element.getName() + "> element: <" + e1.getName() + "> is unrecognized");
			}

		}

		if (schedule != null && sampleSize != -1) {
			throw new ParseException("Error parsing <" + element.getName() + "> element: specify only one of <" + SAMPLE_SIZE + "> or <" + SCHEDULE + ">.");
		}


		return new GenomeDescriptionSampler(sampleSize, schedule, label, fileName);
	}

	private Sampler parseAlignmentSampler(Element element, SamplingSchedule samplingSchedule, String fileName) throws ParseException {

		int sampleSize = -1;
		Map<Integer,Integer> schedule = null;
		String label = null;
		boolean consensus = false;

		AlignmentSampler.Format format = AlignmentSampler.Format.NEXUS;

        FeatureAndSites f = parseFeatureAndSites(element);

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
					consensus = false;
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

		return new AlignmentSampler(f.feature, f.sites, sampleSize, consensus, schedule, format, label, fileName);
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

        FeatureAndSites f = parseFeatureAndSites(element);

		return new AlleleFrequencySampler(f.feature, f.sites, fileName);
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
