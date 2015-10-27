package template;

import java.util.Comparator;

import logist.simulation.Vehicle;

/**
 * Compares the cost function for A* algorithm between two pairs
 * 
 * @author Marc Schaer and Cynthia Oeschger
 *
 */
class PairComparator implements Comparator<Pair> {
	
	private int costPerKm;
	
	public PairComparator() {
		super();
		costPerKm = 1;
	}
	
	@Override
	public int compare(Pair o1, Pair o2) {
		Double c1 = o1.computeF(costPerKm);
		Double c2 = o2.computeF(costPerKm);
		
		return c1.compareTo(c2);
	}
	
	public void setVehicle(Vehicle v) {
		costPerKm = v.costPerKm();
	}
}
