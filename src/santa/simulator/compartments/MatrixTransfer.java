/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
    private double[][] transferProbs;
    private int numCompartments;
    
    public MatrixTransfer(double [] transferRates, int numCompartments) {
        this.numCompartments = numCompartments;
        this.transferProbs = new double[this.numCompartments][this.numCompartments];
        
        for (int i = 0; i < transferRates.length; i++) {
            this.transferProbs[i / this.numCompartments][i % this.numCompartments] = transferRates[i];
        }
    }
    
    public void genomeTransfer(List<Compartment> compartments, int generation, List<CompartmentEpoch> currentEpochs) {
        if (numCompartments > 1) {
        ArrayList< ArrayList<Virus> > allVirusesToAdd = new ArrayList<>(numCompartments);
        
        for (int i = 0; i < numCompartments; i++) {
            allVirusesToAdd.add(new ArrayList<Virus>());
        }
        
        for (int i = 0; i < numCompartments; i++) {
            Compartment migrator = compartments.get(i);
            List<Virus> viruses = migrator.getPopulation().getCurrentGeneration();
            GenePool genePool = migrator.getGenePool();
            
            for (int j = 0; j < numCompartments; j++) {
                if (i != j) {
                    ArrayList<Virus> virusesToAdd = allVirusesToAdd.get(j);
                    
                    for (int k = 0; k < viruses.size();) {
                        if (transferProbs[i][j] > 0) {
                            boolean transfer = Random.nextUniform(0, 1) <= transferProbs[i][j];
                            
                            if (transfer) {
                                Virus virus = viruses.get(k);
                                Genome genome = virus.getGenome();
                                
                                virusesToAdd.add(virus);
                                viruses.remove(k);
                                
                                genePool.killGenome(genome);
                            } else {
                                k++;
                            }
                        } else {
                            k++;
                        }
                    }
                }
            }
        }
        
        for (int i = 0; i < numCompartments; i++) {
            List<Virus> virusesToAdd = allVirusesToAdd.get(i);
            
            if (!virusesToAdd.isEmpty()) {
                FitnessFunction fitnessFunction = currentEpochs.get(i).getFitnessFunction();
                GenePool compartmentGenePool = compartments.get(i).getGenePool();
                List<Virus> viruses = compartments.get(i).getPopulation().getCurrentGeneration();
            
                // fix later to remove create less genomes
                for (Virus v: virusesToAdd) {
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
