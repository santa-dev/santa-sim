package	 santa.simulator.genomes;

import java.util.Collections;
import java.util.Set;
import santa.simulator.NotImplementedException;

/**
 * @author abbas
 * @version $Id$
 */
public class RecombinationHotSpot {
	public RecombinationHotSpot(Set<Integer> segment, double factor){
		if (segment.size() != 2 ){
			
			throw new RuntimeException("RecombinationHotSpot size is not equal to two.");
		}
		// commenting out for now.  since making genomedescription a nomal class with instances,
		// it is not clear what to do here....
		if (true)
			throw new NotImplementedException();
		// for (Integer i : segment){
		// 	if (i > GenomeDescription.getGenomeLength())
		// 		throw new RuntimeException("Hotspot boundaries out of genome length.");
		// }		
		if (factor < 0){	
			throw new RuntimeException();
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
