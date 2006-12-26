package org.virion.simulator;

import java.util.logging.Logger;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: Simulator.java,v 1.8 2006/04/19 09:26:30 kdforc0 Exp $
 */
public class Simulator {

    private final int replicateCount;
    private final Simulation simulation;

    public Simulator (
            int replicateCount,
            Simulation simulation) {

        this.replicateCount = replicateCount;

        this.simulation = simulation;
    }

    private double usedMemory() {
        Runtime rt = Runtime.getRuntime();
        return (rt.totalMemory() - rt.freeMemory()) / (1024*1024);
    }

    public void run() {

        Logger.getLogger("org.virion.simulator.memory").fine("Initial memory used: " + usedMemory() + "MB");

        Logger logger = Logger.getLogger("org.virion.simulator");

        long startTime = System.currentTimeMillis();

        Logger.getLogger("org.virion.simulator.memory").finest("Base memory used: " + usedMemory() + "MB");

        for (int replicate = 0; replicate < replicateCount; replicate++) {

            if (replicateCount > 1) {
                logger.info("Replicate " + Integer.toString(replicate + 1));
            }

            simulation.run(replicate, logger);

            Logger.getLogger("org.virion.simulator.memory").finest("Memory used = " + usedMemory() + "MB");

        }

        long time = System.currentTimeMillis() - startTime;
        Logger.getLogger("org.virion.simulator").finest("Time taken: " + time + " ms");
    }
}
