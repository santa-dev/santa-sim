package santa.simulator.compartments;

import java.util.ArrayList;
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
public class MatrixTransfer implements Transfer {

    private TransferProb[][] transferProbs;
    private int numCompartments;

    public MatrixTransfer(List<TransferProb> transferRates, int numCompartments) {
        this.numCompartments = numCompartments;
        this.transferProbs = new TransferProb[this.numCompartments][this.numCompartments];

        for (int i = 0; i < transferRates.size(); i++) {
            this.transferProbs[i / this.numCompartments][i % this.numCompartments] = transferRates.get(i);
        }
    }

    @Override
    public void genomeTransfer(List<Compartment> compartments, int generation, List<CompartmentEpoch> currentEpochs) {
        if (numCompartments > 1) {
            ArrayList< ArrayList<Virus>> allVirusesToAdd = new ArrayList<>(numCompartments);

            for (int i = 0; i < numCompartments; i++) {
                allVirusesToAdd.add(new ArrayList<Virus>());
            }

            for (int compartmentFromID = 0; compartmentFromID < numCompartments; compartmentFromID++) {
                Compartment migrator = compartments.get(compartmentFromID);
                List<Virus> viruses = migrator.getPopulation().getCurrentGeneration();
                GenePool genePool = migrator.getGenePool();

                for (int virusID = 0; virusID < viruses.size(); virusID++) {
                    double transfer = Random.nextUniform(0, 1);
                    Virus virus = viruses.get(virusID);

                    for (int compartmentToID = 0; compartmentToID < numCompartments; compartmentToID++) {
                        double prob = transferProbs[compartmentFromID][compartmentToID].getProb(virus, generation);
                        
                        if (transfer < prob) {
                            if (compartmentFromID != compartmentToID) {
                                ArrayList<Virus> virusesToAdd = allVirusesToAdd.get(compartmentToID);
                                Genome genome = virus.getGenome();

                                // record compartment entry
                                virus.setAge(generation);
                                virus.setLastCompartment(migrator);

                                virusesToAdd.add(virus);
                                viruses.remove(virusID);
                                genePool.killGenome(genome);

                                virusID--;
                            }

                            break;
                        } else {
                            transfer -= prob;
                        }
                    }
                }
            }

            for (int compartmentToID = 0; compartmentToID < numCompartments; compartmentToID++) {
                List<Virus> virusesToAdd = allVirusesToAdd.get(compartmentToID);

                if (!virusesToAdd.isEmpty()) {
                    FitnessFunction fitnessFunction = currentEpochs.get(compartmentToID).getFitnessFunction();
                    GenePool compartmentGenePool = compartments.get(compartmentToID).getGenePool();
                    List<Virus> viruses = compartments.get(compartmentToID).getPopulation().getCurrentGeneration();

                    // fix later to remove create less genomes
                    for (Virus v : virusesToAdd) {
                        Genome genome = v.getGenome();
                        Genome newGenome = compartmentGenePool.createGenome(genome.getSequence(), genome.getDescription());

                        v.setGenome(newGenome);
                        compartmentGenePool.duplicateGenome(newGenome, new TreeSet<Mutation>(), fitnessFunction);
                    }

                    viruses.addAll(virusesToAdd);
                }
            }
        }
    }
}
