package santa.simulator;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.io.File;
import java.io.IOException;
import java.util.logging.*;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: SimulatorMain.java,v 1.2 2006/02/17 12:06:55 rambaut Exp $
 */
public class SimulatorMain {

    public static void main(String[] args) {

        Simulator simulator = null;

        if (args.length > 0) {
            File file = new File(args[0]);
            try {
                SAXBuilder builder = new SAXBuilder();
                Document doc = builder.build(file);

                SimulatorParser parser = new SimulatorParser();

                simulator = parser.parse(doc.getRootElement());

            } catch (SimulatorParser.ParseException pe) {
                System.err.println(pe.getMessage());
                System.exit(1);
            } catch (JDOMException jde) {
                System.err.println("Error parsing XML input file: " + jde.getMessage());
                System.exit(1);
            } catch (IOException ioe) {
                System.err.println("Error reading XML input file: " + ioe.getMessage());
                System.exit(1);
            }

        }

        Logger.getLogger("santa.simulator").addHandler(new ConsoleHandler());
        Logger.getLogger("santa.simulator").setLevel(Level.FINEST);

        //Logger.getLogger("santa.simulator.memory").setLevel(Level.FINEST);

        simulator.run();
    }
}
