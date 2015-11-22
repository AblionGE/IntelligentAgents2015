package template;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

import logist.simulation.Vehicle;
import logist.topology.Topology.City;

/**
 * This class represent a solution state composed by all actions to do and the
 * action after and a relation for each vehicle and their first action
 * 
 * @author Cynthia Oeschger and Marc Schaer
 */
public class SolutionState {

	private HashMap<Movement, Movement> nextMovements = new HashMap<Movement, Movement>();
	private HashMap<Vehicle, Movement> nextMovementsVehicle = new HashMap<Vehicle, Movement>();
	private HashMap<Movement, Integer> timedMovements = new HashMap<Movement, Integer>();
	private HashMap<Vehicle, LinkedList<Movement>> plans;
	private double cost;

	SolutionState(HashMap<Movement, Movement> nextMovements, HashMap<Vehicle, Movement> nextMovementsVehicle) {
		this.nextMovements = nextMovements;
		this.nextMovementsVehicle = nextMovementsVehicle;
		this.plans = computeVehiclePlans(this);
		this.timedMovements = computeTime(this.plans);
		cost = -1;
	}

	/**
	 * Compute the cost of this solution
	 */
	private void computeCost() {
		double totalCost = 0;
		Set<Vehicle> vehicles = plans.keySet();
		for (Vehicle v : vehicles) {
			double totalVehicleDistance = computeVehicleDistance(v, nextMovementsVehicle.get(v));
			LinkedList<Movement> currentPath = plans.get(v);
			for (int i = 0; i < currentPath.size() - 1; i++) {
				Movement currentMovement = currentPath.get(i);
				totalVehicleDistance += computeMovementsDistance(currentMovement, currentPath.get(i + 1));
			}
			totalCost += (totalVehicleDistance * v.costPerKm());
		}
		this.cost = totalCost;
	}

	/**
	 * Compute the distance from the starting position of a vehicle and its
	 * first task to perform
	 * 
	 * @param v
	 * @param a
	 * @return
	 */
	private double computeVehicleDistance(Vehicle v, Movement a) {
		City start = v.getCurrentCity();
		City destination = a.getTask().pickupCity;
		return start.distanceTo(destination);
	}

	/**
	 * Compute the distance between two movements
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	private double computeMovementsDistance(Movement a, Movement b) {
		City cityTaskA;
		City cityTaskB;

		if (a.getAction() == Action.PICKUP) {
			cityTaskA = a.getTask().pickupCity;
		} else {
			cityTaskA = a.getTask().deliveryCity;
		}

		if (b.getAction() == Action.PICKUP) {
			cityTaskB = b.getTask().pickupCity;
		} else {
			cityTaskB = b.getTask().deliveryCity;
		}

		return cityTaskA.distanceTo(cityTaskB);

	}

	/**
	 * It computes for each vehicle an ordered list of tasks
	 * 
	 * @param solutionState
	 * @return
	 */
	private HashMap<Vehicle, LinkedList<Movement>> computeVehiclePlans(SolutionState solutionState) {
		HashMap<Vehicle, LinkedList<Movement>> plans = new HashMap<Vehicle, LinkedList<Movement>>();

		HashMap<Vehicle, Movement> vehicleMovement = solutionState.getNextMovementsVehicle();
		HashMap<Movement, Movement> movements = solutionState.getNextMovements();

		for (Vehicle v : vehicleMovement.keySet()) {
			LinkedList<Movement> orderedMovements = new LinkedList<Movement>();

			Movement next = vehicleMovement.get(v);
			while (next != null) {
				orderedMovements.add(next);
				next = movements.get(next);
			}
			plans.put(v, orderedMovements);
		}
		return plans;
	}

	/**
	 * Compute a HashMap of time for each action
	 * 
	 * @param plans
	 * @return
	 */
	private HashMap<Movement, Integer> computeTime(HashMap<Vehicle, LinkedList<Movement>> plans) {
		Set<Vehicle> vehicles = plans.keySet();
		HashMap<Movement, Integer> timedMovements = new HashMap<Movement, Integer>();
		for (Vehicle v : vehicles) {
			LinkedList<Movement> movements = plans.get(v);
			int time = 1;
			for (Movement m : movements) {
				timedMovements.put(m, time);
				time++;
			}
		}
		return timedMovements;
	}

	@Override
	public String toString() {
		String s = "State: ";
		int i = 0;
		for (Vehicle v : plans.keySet()) {
			i = 0;
			String plan = "";
			s += "\nVehicle " + v.id() + ": Nb of tasks processed : ";
			for (Movement m : plans.get(v)) {
				plan += m.toString() + ", ";
				i += 1;
			}
			s += i / 2;
			s = s + ", plan : " + plan;
		}
		return s;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(cost);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((nextMovements == null) ? 0 : nextMovements.hashCode());
		result = prime * result + ((nextMovementsVehicle == null) ? 0 : nextMovementsVehicle.hashCode());
		result = prime * result + ((plans == null) ? 0 : plans.hashCode());
		result = prime * result + ((timedMovements == null) ? 0 : timedMovements.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SolutionState other = (SolutionState) obj;
		if (Double.doubleToLongBits(cost) != Double.doubleToLongBits(other.cost))
			return false;
		if (nextMovements == null) {
			if (other.nextMovements != null)
				return false;
		} else if (!nextMovements.equals(other.nextMovements))
			return false;
		if (nextMovementsVehicle == null) {
			if (other.nextMovementsVehicle != null)
				return false;
		} else if (!nextMovementsVehicle.equals(other.nextMovementsVehicle))
			return false;
		if (plans == null) {
			if (other.plans != null)
				return false;
		} else if (!plans.equals(other.plans))
			return false;
		if (timedMovements == null) {
			if (other.timedMovements != null)
				return false;
		} else if (!timedMovements.equals(other.timedMovements))
			return false;
		return true;
	}

	@SuppressWarnings("unchecked")
	protected HashMap<Movement, Movement> getNextMovements() {
		return (HashMap<Movement, Movement>) nextMovements.clone();
	}

	@SuppressWarnings("unchecked")
	protected HashMap<Vehicle, Movement> getNextMovementsVehicle() {
		return (HashMap<Vehicle, Movement>) nextMovementsVehicle.clone();
	}

	@SuppressWarnings("unchecked")
	protected HashMap<Movement, Integer> getTimedMovements() {
		return (HashMap<Movement, Integer>) timedMovements.clone();
	}

	protected double getCost() {
		if (cost < 0) {
			computeCost();
		}
		return cost;
	}

	protected HashMap<Vehicle, LinkedList<Movement>> getPlans() {
		return plans;
	}
}
