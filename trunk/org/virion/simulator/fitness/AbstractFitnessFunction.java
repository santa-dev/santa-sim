/*
 * Created on Jul 17, 2006
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.virion.simulator.fitness;

import org.virion.simulator.Population;

public abstract class AbstractFitnessFunction implements FitnessFunctionFactor {
    
    public boolean updateGeneration(int generation, Population population) {
        return false;
    }
}
