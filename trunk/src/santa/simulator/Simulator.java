package santa.simulator;

import java.util.logging.Logger;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: Simulator.java,v 1.8 2006/04/19 09:26:30 kdforc0 Exp $
 */
public class  bnhkg,mnjhkbSimulator {

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

        Logger.getLogger("santa.simulator.memory").fine("Initial memory used: " + usedMemory() + "MB");

        Logger logger = Logger.getLogger("santa.simulator");

        long startTime = System.currentTimeMillis();

        Logger.getLogger("santa.simulator.memory").finest("Base memory used: " + usedMemory() + "MB");

        for (int replicate = 0; replicate < replicateCount; replicate++) {

            if (replicateCount > 1) {
                logger.info("Replicate " + Integer.toString(replicate + 1));
            }

            simulation.run(replicate, logger);

            Logger.getLogger("santa.simulator.memory").finest("Memory used = " + usedMemory() + "MB");

        }

        long time = System.currentTimeMillis() - startTime;
        Logger.getLogger("santa.simulator").finest("Time taken: " + time + " ms");
    }
}
