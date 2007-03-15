package santa.simulator;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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

            Map<String, String> parameterValueMap = parseParameters(args);
            
            File file = new File(args[args.length - 1]);
            try {
                SAXBuilder builder = new SAXBuilder();
                Document doc = builder.build(file);

                SimulatorParser parser = new SimulatorParser();
                parser.setParameters(parameterValueMap );
                
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

			// @todo - it would be good to be able to report the RNG seed here and set it

	        Logger.getLogger("santa.simulator").addHandler(new ConsoleHandler());
	        Logger.getLogger("santa.simulator").setLevel(Level.FINEST);

	        //Logger.getLogger("santa.simulator.memory").setLevel(Level.FINEST);

	        simulator.run();
        } else {
	        System.out.println("Usage: santa [-arg=value] <input_file>");
	        System.exit(0);
        }
    }

    private static Map<String, String> parseParameters(String[] args) {
        Map<String, String> parameterValueMap = new HashMap<String, String>();

        for (int i = 0; i < args.length - 1; ++i) {
            String arg = args[i];
            
            if (arg.charAt(0) != '-') {
                System.err.println("Do not understand '" + arg + "': should be -arg=value");
                System.exit(0);
            }
            
            String[] argvalue = arg.substring(1).split("=");
            
            if (argvalue.length != 2) {
                System.err.println("Do not understand '" + arg + "': should be -arg=value");
                System.exit(0);
            }
            
            parameterValueMap.put(argvalue[0], argvalue[1]);
        }

        return parameterValueMap;
    }
}
