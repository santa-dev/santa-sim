/*
 * Virus.java
 *
 * (c) 2002-2005 BEAST Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package santa.simulator;

import santa.simulator.genomes.Genome;

/**
 * @author rambaut
 *         Date: Apr 22, 2005
 *         Time: 2:23:14 PM
 */
public class Virus {

    public Virus() {
    }

    public Virus(Genome genome, Virus parent) {
        this.genome = genome;
        this.parent = parent;
    }

    public Genome getGenome() {
        return genome;
    }

    public Virus getParent() {
        return parent;
    }

	public double getLogFitness() {
		return genome.getLogFitness();
	}

	public double getFitness() {
		return genome.getFitness();
	}

    public void setGenome(Genome genome) {
        this.genome = genome;
    }

    public void setParent(Virus parent) {
        this.parent = parent;
    }

    public int getOffspringCount() {
        return offspringCount;
    }

    public void setOffspringCount(int offspringCount) {
        this.offspringCount = offspringCount;
    }

    private Genome genome = null;
    private Virus parent = null;
    private int offspringCount = 0;

}
