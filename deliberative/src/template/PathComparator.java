package template;

import java.util.Comparator;

import logist.simulation.Vehicle;

/**
 * Compares the cost function for A* algorithm between two pairs
 * 
 * @author Marc Schaer and Cynthia Oeschger
 *
 */
class PathComparator implements Comparator<Path> {

	private int costPerKm;

	public PathComparator() {
		super();
		costPerKm = -1;
	}

	@Override
	public int compare(Path o1, Path o2) {
		Double c1 = o1.totalReward(costPerKm);
		Double c2 = o2.totalReward(costPerKm);

		return -(c1.compareTo(c2));
	}

	public void setVehicle(Vehicle v) {
		costPerKm = v.costPerKm();
	}
}
