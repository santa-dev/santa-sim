/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package santa.simulator.compartments;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import santa.simulator.Random;
import santa.simulator.Virus;
import santa.simulator.genomes.GenePool;
import santa.simulator.genomes.Genome;
import santa.simulator.population.Population;

/**
 *
 * @author Bradley R. Jones
 */
public class Compartments implements Iterable<Compartment> {
    private double[][] transferProbs;
    private List<Compartment> compartments;
    private int numCompartments;
    
    public Compartments(List<Compartment> compartments, double[] transferRates) {
        this.compartments = compartments;
        this.numCompartments = compartments.size();
        this.transferProbs = new double[this.numCompartments][this.numCompartments];
        
        double[] sum = new double[this.numCompartments];
//        for (int i = 0; i < transferRates.length; i++) {
//            sum[i / this.numCompartments] += transferRates[i];
//        }        
        for (int i = 0; i < transferRates.length; i++) {
            this.transferProbs[i / this.numCompartments][i % this.numCompartments] = transferRates[i];
        }
    }
    
    public void genomeTransfer(int generation) {
        if (numCompartments > 1) {
        ArrayList< ArrayList<Genome> > allGenomesToAdd = new ArrayList(numCompartments);
        ArrayList< ArrayList<Virus> > allVirusesToAdd = new ArrayList(numCompartments);
        
        for (int i = 0; i < numCompartments; i++) {
            allGenomesToAdd.add(new ArrayList<>());
            allVirusesToAdd.add(new ArrayList<>());
        }
        
        for (int i = 0; i < numCompartments; i++) {
            Compartment migrator = compartments.get(i);
            List<Virus> viruses = migrator.getPopulation().getCurrentGeneration();
            GenePool genePool = migrator.getGenePool();
            
            for (int j = 0; j < numCompartments; j++) {
                if (i != j) {
                    ArrayList<Genome> genomesToAdd = allGenomesToAdd.get(j);
                    ArrayList<Virus> virusesToAdd = allVirusesToAdd.get(j);
                    
                    for (int k =0; k < viruses.size(); k++) {
                        if (transferProbs[i][j] > 0) {
                            boolean transfer = Random.nextUniform(0, 1) <= transferProbs[i][j];
                            
                            if (transfer) {
                                Virus virus = viruses.get(k);
                                Genome genome = virus.getGenome();
                                Genome migratingGenome = genome.copy();
                                
                                virus.setGenome(migratingGenome);
                                virusesToAdd.add(virus);
                                viruses.remove(k);
                                
                                genePool.killGenome(genome);
                                genomesToAdd.add(migratingGenome);
                            }
                        }
                    }
                }
            }
        }
        
        for (int i = 0; i < numCompartments; i++) {
            GenePool compartmentGenePool = compartments.get(i).getGenePool();
            List<Genome> compartmentGenomes = compartmentGenePool.getGenomes();  
            List<Virus> viruses = compartments.get(i).getPopulation().getCurrentGeneration();
            
            for (Genome genome: allGenomesToAdd.get(i)) {
                Genome newGenome = compartmentGenePool.createGenome(genome.getSequence(), genome.getDescription());
                
                newGenome.setFrequency(genome.getFrequency());
                compartmentGenomes.add(newGenome);
            }
            
            viruses.addAll(allVirusesToAdd.get(i));
        }
        }
    }
    
    public int getNumCompartments() {
        return numCompartments;
    }

    @Override
    public Iterator<Compartment> iterator() {
        return compartments.iterator();
    }
}
