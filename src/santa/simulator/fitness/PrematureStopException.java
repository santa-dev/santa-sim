package santa.simulator.fitness;

public class PrematureStopException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	
	public PrematureStopException(int index, int position){
		this.index = index;
		this.position = position;
	}

	public int index;
	public int position;
}
