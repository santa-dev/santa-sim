package santa.simulator.genomes;

/**
 * @author rambaut
 *         Date: Apr 26, 2005
 *         Time: 10:28:35 AM
 */
public class StateChange {

    public StateChange(int position, byte oldState, byte newState) {
        this.position = position;
        this.oldState = oldState;
	    this.newState = newState;
    }

    public final int position;
    public final byte oldState, newState;
}
