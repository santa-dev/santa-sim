
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
        if (parent != null)
            this.age = parent.age;
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

    public int getAge() {
        return age;
    }
        
    public void setGenome(Genome genome) {
        this.genome = genome;
    }

    public void setParent(Virus parent) {
        this.parent = parent;
        setAge(parent.age);
    }

    public int getOffspringCount() {
        return offspringCount;
    }

    public void setOffspringCount(int offspringCount) {
        this.offspringCount = offspringCount;
    }
    
    public void setAge(int age) {
        this.age = age;
    }

    private Genome genome = null;
    private Virus parent = null;
    private int offspringCount = 0;
    private int age = 0;

}
