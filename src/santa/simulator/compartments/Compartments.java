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
        ArrayList< ArrayList<Genome> > allGenomesToAdd = new ArrayList(numCompartments);
        ArrayList< ArrayList<Genome> > allGenomes = new ArrayList<>(numCompartments);
        
        for (int i = 0; i < numCompartments; i++) {
            allGenomesToAdd.add(new ArrayList<>());
        }
        
        for (int i = 0; i < numCompartments; i++) {
            Compartment migrator = compartments.get(i);
            Population population = migrator.getPopulation();
            GenePool migratorGenePool = migrator.getGenePool();
            ArrayList<Genome> genomes = new ArrayList<>();
            allGenomes.set(i, genomes);
            for (Genome genome: migratorGenePool.getGenomes()) {
                genomes.add(genome.copy());
            }
            
            for (int j = 0; j < numCompartments; j++) {
                ArrayList<Genome> genomesToAdd = allGenomesToAdd.get(j);
                
                if (i != j) {
                    for (Genome genome: genomes) {
                        if (transferProbs[i][j] > 0) {
                            int n = genome.getFrequency();
                            int numMigrating = Random.nextBinomial(n, transferProbs[i][j]);
                            Genome migratingGenome = genome.copy();
                        
                            genome.setFrequency(n - numMigrating);
                            migratingGenome.setFrequency(numMigrating);
                        
                            genomesToAdd.add(migratingGenome);
                        }
                    }
                }
            }
        }
        
        for (int i = 0; i < numCompartments; i++) {
            ArrayList<Genome> genomes = allGenomes.get(i);
            GenePool compartmentGenePool = compartments.get(i).getGenePool();
            List<Genome> compartmentGenomes = compartmentGenePool.getGenomes();            
            for (int j = 0; j < genomes.size(); j++) {
                Genome compartmentGenome = compartmentGenomes.get(j);
                
                compartmentGenome.setFrequency(genomes.get(j).getFrequency());
                
                if  (compartmentGenome.getFrequency() <= 0) {
                    compartmentGenome.setFrequency(1);
                    compartmentGenePool.killGenome(compartmentGenome);
                }
            }
            
            for (Genome genome: allGenomesToAdd.get(i)) {
                Genome newGenome = compartmentGenePool.createGenome(genome.getSequence(), genome.getDescription());
                
                newGenome.setFrequency(genome.getFrequency());
                compartmentGenomes.add(newGenome);
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
