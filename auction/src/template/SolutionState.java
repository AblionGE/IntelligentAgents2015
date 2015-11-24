package template;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import logist.simulation.Vehicle;
import logist.topology.Topology.City;

/**
 * This class represent a solution state composed by all actions to do and the
 * action after and a relation for each vehicle and their first action
 * 
 * @author Cynthia Oeschger and Marc Schaer
 */
public class SolutionState {

	private Movement[] nextMovements;
	private Movement[] nextMovementsVehicle;
	private int[] timedMovements;
	private ArrayList<LinkedList<Movement>> plans;
	private double cost;

	SolutionState(Movement[] nextMovements, Movement[] nextMovementsVehicle) {
		this.nextMovements = nextMovements.clone();
		this.nextMovementsVehicle = nextMovementsVehicle.clone();
		this.plans = computeVehiclePlans(this);
		this.timedMovements = computeTime(this.plans);
		cost = -1;
	}

	/**
	 * Compute the cost of this solution
	 */
	private void computeCost() {
		double totalCost = 0;
		for (int vehicle = 0; vehicle < AuctionTemplate.nbVehicles; vehicle++) {
			double totalVehicleDistance = computeVehicleDistance(AuctionTemplate.vehicles.get(vehicle),
					nextMovementsVehicle[vehicle]);
			LinkedList<Movement> currentPath = plans.get(vehicle);
			for (int i = 0; i < currentPath.size() - 1; i++) {
				Movement currentMovement = currentPath.get(i);
				totalVehicleDistance += computeMovementsDistance(currentMovement, currentPath.get(i + 1));
			}
			totalCost += (totalVehicleDistance * AuctionTemplate.vehicles.get(vehicle).costPerKm());
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
		if (a == null) {
			return 0;
		}
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
	private ArrayList<LinkedList<Movement>> computeVehiclePlans(SolutionState solutionState) {
		ArrayList<LinkedList<Movement>> plans = new ArrayList<LinkedList<Movement>>();

		Movement[] vehicleMovement = solutionState.getNextMovementsVehicle();
		Movement[] movements = solutionState.getNextMovements();

		for (int vehicle = 0; vehicle < AuctionTemplate.nbVehicles; vehicle++) {
			LinkedList<Movement> orderedMovements = new LinkedList<Movement>();

			Movement next = vehicleMovement[vehicle];
			while (next != null) {
				orderedMovements.add(next);
				next = movements[next.getId()];
			}
			plans.add(orderedMovements);
			
		}
		return plans;
	}

	/**
	 * Compute a HashMap of time for each action
	 * 
	 * @param plans
	 * @return
	 */
	private int[] computeTime(ArrayList<LinkedList<Movement>> plans) {
		int[] timedMovements = new int[AuctionTemplate.nbTasks * 2];
		for (int vehicle = 0; vehicle < AuctionTemplate.nbVehicles; vehicle++) {
			LinkedList<Movement> movements = plans.get(vehicle);
			int time = 1;
			for (Movement m : movements) {
				timedMovements[m.getId()] = time;
				time++;
			}
		}
		return timedMovements;
	}

	@Override
	public String toString() {
		return "SolutionState [nextMovements=" + Arrays.toString(nextMovements) + ", nextMovementsVehicle="
				+ Arrays.toString(nextMovementsVehicle) + ", timedMovements=" + Arrays.toString(timedMovements)
				+ ", plans=" + plans + ", cost=" + cost + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(cost);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + Arrays.hashCode(nextMovements);
		result = prime * result + Arrays.hashCode(nextMovementsVehicle);
		result = prime * result + ((plans == null) ? 0 : plans.hashCode());
		result = prime * result + Arrays.hashCode(timedMovements);
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
		if (!Arrays.equals(nextMovements, other.nextMovements))
			return false;
		if (!Arrays.equals(nextMovementsVehicle, other.nextMovementsVehicle))
			return false;
		if (plans == null) {
			if (other.plans != null)
				return false;
		} else if (!plans.equals(other.plans))
			return false;
		if (!Arrays.equals(timedMovements, other.timedMovements))
			return false;
		return true;
	}

	protected Movement[] getNextMovements() {
		return nextMovements.clone();
	}

	protected Movement[] getNextMovementsVehicle() {
		return nextMovementsVehicle.clone();
	}

	protected int[] getTimedMovements() {
		return timedMovements.clone();
	}

	protected double getCost() {
		if (cost < 0) {
			computeCost();
		}
		return cost;
	}

	protected ArrayList<LinkedList<Movement>> getPlans() {
		return plans;
	}
}
