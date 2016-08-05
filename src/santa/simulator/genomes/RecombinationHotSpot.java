package	 santa.simulator.genomes;

import java.util.Collections;
import java.util.Set;

/**
 * @author abbas
 * @version $Id$
 */
public class RecombinationHotSpot {
	public RecombinationHotSpot(Set<Integer> segment, double factor){
		if (segment.size() != 2) {
			throw new RuntimeException("RecombinationHotSpot size is not equal to two.");
		}
		for (Integer i : segment) {
			if (i > GenomeDescription.root.getGenomeLength())
				throw new RuntimeException("Hotspot boundaries out of genome length.");
		}		
		if (factor < 0) {
			// can this be caught at parse time?
			throw new RuntimeException("RecombinationHotSpot factor must be greater than 0");
		}
		this.probBoost = factor;
		this.hotSegment = segment;
		this.startPosition  = Collections.min(hotSegment);
		this.endPosition = Collections.max(hotSegment);		
	}
	
	public static RecombinationHotSpot createHotSpot(Set<Integer> segment, double factor){
		return new RecombinationHotSpot(segment,factor);	
	}
	public final Set<Integer> hotSegment;
	public final double probBoost;
	public final Integer startPosition;
	public final Integer endPosition;
}
