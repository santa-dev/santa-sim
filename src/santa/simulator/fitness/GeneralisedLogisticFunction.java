package santa.simulator.fitness;

/**
 * @author gbehey0
 *
 */
public class GeneralisedLogisticFunction {

	private double k; //upper bound
	private double a; //lower bound
	private double b; //growth
	private double c; //center
	private double v; 
	private double q; 
	
	public GeneralisedLogisticFunction(double lowerBound, double upperBound, double growthFactor, double center) {
		this.k = upperBound;
		this.a = lowerBound;
		this.b = growthFactor;
		this.c = center;
		this.v = 1;
		this.q = 1;
	}
	
	public double getFunctionValue(double t) {
		return a+((k-a)/Math.pow((1+q*Math.exp(-b*(t-c))),(1/v)));
	}
}
