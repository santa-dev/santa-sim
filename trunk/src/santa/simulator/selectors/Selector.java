package santa.simulator.selectors;

import santa.simulator.Virus;

/**
 * This is the interface for selecting viruses from the population. Before
 * a round of selection, the initializeSelector method is called, passing the
 * current generation. Then nextSelection() can be called as many times as
 * required.
 *
 * @author Andrew Rambaut
 * @version $Id: Selector.java,v 1.2 2006/07/18 12:21:33 rambaut Exp $
 */
public interface Selector {

    void initializeSelector(Virus[] currentGeneration);

    Virus nextSelection();
}
