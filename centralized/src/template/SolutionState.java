package template;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

import logist.simulation.Vehicle;
import logist.topology.Topology.City;

/**
 * This class represent a solution state composed by 
 * All actions to do and the action after and a relation for each vehicle
 * and their first action
 *
 */
public class SolutionState {
	
	private HashMap<Movement, Movement> nextMovements = new HashMap<Movement, Movement>();
	private HashMap<Vehicle, Movement> nextMovementsVehicle = new HashMap<Vehicle, Movement>();
	private double cost;
	
	SolutionState(HashMap<Movement, Movement> nextMovements, HashMap<Vehicle, Movement> nextMovementsVehicle) {
		this.nextMovements = nextMovements;
		this.nextMovementsVehicle = nextMovementsVehicle;
	}

	protected HashMap<Movement, Movement> getNextMovements() {
		return nextMovements;
	}

	protected void setNextMovements(HashMap<Movement, Movement> nextMovements) {
		this.nextMovements = nextMovements;
	}

	protected HashMap<Vehicle, Movement> getNextMovementsVehicle() {
		return nextMovementsVehicle;
	}

	protected void setNextMovementsVehicle(HashMap<Vehicle, Movement> nextMovementsVehicle) {
		this.nextMovementsVehicle = nextMovementsVehicle;
	}
	
	protected double getCost() {
		return cost;
	}

	protected double computeCost() {
		double totalCost = 0;
		HashMap<Vehicle, LinkedList<Movement>> paths = CentralizedTemplate.computeVehiclePlans(this);
		Set<Vehicle> vehicles = paths.keySet();
		
		for (Vehicle v : vehicles) {
			totalCost += computeVehicleDistance(v, nextMovementsVehicle.get(v));
			LinkedList<Movement> currentPath = paths.get(v);
			for (int i = 0; i < currentPath.size() - 1; i++) {
				Movement currentMovement = currentPath.get(i);
				totalCost += computeMovementsDistance(currentMovement, currentPath.get(i+1));
			}
			totalCost = totalCost * v.costPerKm();
		}
		this.cost = totalCost;
		return totalCost;
	}
	
	private double computeVehicleDistance(Vehicle v, Movement a) {
		City start = v.getCurrentCity();
		City destination = a.getTask().pickupCity;
		return start.distanceTo(destination);
	}
	
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nextMovements == null) ? 0 : nextMovements.hashCode());
		result = prime * result + ((nextMovementsVehicle == null) ? 0 : nextMovementsVehicle.hashCode());
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
		return true;
	}
	
	

}
