package santa.simulator;

import java.io.PrintWriter;
import java.io.Writer;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id$
 */
public class EventLogger {

    private EventLogger() {
        writer = null;
    }

    private void _log(String event) {
        if (writer != null) {
            writer.println(replicate + "\t" + epoch + "\t" + generation + "\t" + event);
            writer.flush();
        }
    }

    private void _setWriter(Writer writer) {
        this.writer = new PrintWriter(writer);
    }

    private void _setReplicate(int replicate) {
        this.replicate = replicate;
    }

    private void _setEpoch(int epoch) {
        this.epoch = epoch;
    }

    private void _setGeneration(int generation) {
        this.generation = generation;
    }

    private int replicate = 0;
    private int epoch = 0;
    private int generation = 0;
    private PrintWriter writer;

    private static final EventLogger INSTANCE = new EventLogger();

    public static void setWriter(Writer writer) { INSTANCE._setWriter(writer); }
    public static void setReplicate(int replicate) { INSTANCE._setReplicate(replicate); }
    public static void setEpoch(int epoch) { INSTANCE._setEpoch(epoch); }
    public static void setGeneration(int generation) { INSTANCE._setGeneration(generation); }
    public static void log(String event) { INSTANCE._log(event); }
}

