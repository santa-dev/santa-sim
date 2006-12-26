package org.virion.simulator;

import org.jdom.JDOMException;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.ConsoleHandler;
import java.io.File;
import java.io.IOException;

import jebl.evolution.sequences.SequenceType;

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

        Logger.getLogger("org.virion.simulator").addHandler(new ConsoleHandler());
        Logger.getLogger("org.virion.simulator").setLevel(Level.FINEST);

        //Logger.getLogger("org.virion.simulator.memory").setLevel(Level.FINEST);

        simulator.run();
    }
}
