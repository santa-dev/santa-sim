package santa.simulator;

import java.util.logging.Logger;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: Simulator.java,v 1.8 2006/04/19 09:26:30 kdforc0 Exp $
 */
public class Simulator {

    private final int replicateCount;
    private final Simulation simulation;
    final static Logger logger = Logger.getLogger("santa.simulator");
    final static Logger memlogger = Logger.getLogger("santa.simulator.memory");

    public Simulator (
            int replicateCount,
            Simulation simulation) {

        this.replicateCount = replicateCount;

        this.simulation = simulation;
    }

	public static long usedMemory() {
        Runtime rt = Runtime.getRuntime();
        return (rt.totalMemory() - rt.freeMemory());
    }    


	public static String readableByteCount(long bytes) {
		int unit = 1024;
		if (bytes < unit) return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = ("KMGTPE").charAt(exp-1) + ("i");
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
	

    public void run() {

        long startTime = System.currentTimeMillis();
		
		System.gc();
		long usedMemoryBefore = usedMemory();
		memlogger.fine("Initial memory used: " + readableByteCount(usedMemoryBefore));

        for (int replicate = 0; replicate < replicateCount; replicate++) {

            if (replicateCount > 1) {
                logger.info("Replicate " + Integer.toString(replicate + 1));
            }

            simulation.run(replicate, logger);

			System.gc();
			long usedMemoryAfter = usedMemory();
            memlogger.fine("Memory used = " + readableByteCount(usedMemoryAfter));
		    memlogger.fine("Memory change: " + readableByteCount(usedMemoryAfter-usedMemoryBefore));

        }   

        long time = System.currentTimeMillis() - startTime;
		logger.fine("Time taken: " + time + " ms");
    }


	public Simulation getSimulation() {
		return simulation;
	}
}
