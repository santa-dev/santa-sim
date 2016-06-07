
package santa.simulator;

import org.jdom2.Element;
import santa.simulator.SimulatorParser.ParseException;

public abstract class IndelModel {
	private final static String MODEL = "model";

	/**
	 * Parse an indel model from XML.   Returns null if indel model could not be determined.
	 * The configuration format specifically mentioned here in case you want to have other routines to parse from JSON or some 
	 * other format.   
	 * Hmm, it would probably be better to have a centralized parser in that case rather than spreading parsing out among the 
	 * individual classes...
	 */
	public static IndelModel fromXML(Element element) throws ParseException {
		String modelName = null;
		IndelModel m = null;

		modelName = element.getAttributeValue(MODEL);
		if (modelName.equalsIgnoreCase("NB")) {
			m = NegBinIndelModel.fromXML(element);
		} else if (modelName.equalsIgnoreCase("zipfian")) {
			// m = ZifIndelModel.fromXML(element);
		} else if (modelName.equalsIgnoreCase("lavalette")) {
			// m = LavIndelModel.fromXML(element);
		} else {
			throw new ParseException("Error parsing <" + element.getName() + "> element: Unrecognized indel model specification");
		}
		return(m);
	}

	public abstract int nextLength();
}
