package template;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

import logist.simulation.Vehicle;
import logist.task.Task;
import logist.topology.Topology.City;

/**
 * This class represent a solution state composed by 
 * All actions to do and the action after and a relation for each vehicle
 * and their first action
 *
 */
public class SolutionState {
	
	private HashMap<Action, Action> nextActions = new HashMap<Action, Action>();
	private HashMap<Vehicle, Action> nextActionsVehicle = new HashMap<Vehicle, Action>();
	private double cost;
	
	SolutionState(HashMap<Action, Action> nextActions, HashMap<Vehicle, Action> nextActionsVehicle) {
		this.nextActions = nextActions;
		this.nextActionsVehicle = nextActionsVehicle;
	}

	protected HashMap<Action, Action> getNextActions() {
		return nextActions;
	}

	protected void setNextActions(HashMap<Action, Action> nextActions) {
		this.nextActions = nextActions;
	}

	protected HashMap<Vehicle, Action> getNextActionsVehicle() {
		return nextActionsVehicle;
	}

	protected void setNextActionsVehicle(HashMap<Vehicle, Action> nextActionsVehicle) {
		this.nextActionsVehicle = nextActionsVehicle;
	}
	
	protected double getCost() {
		return cost;
	}

	protected double computeCost() {
		double totalCost = 0;
		HashMap<Vehicle, LinkedList<Action>> paths = CentralizedTemplate.computeVehiclePlans(this);
		Set<Vehicle> vehicles = paths.keySet();
		
		for (Vehicle v : vehicles) {
			totalCost += computeVehicleDistance(v, nextActionsVehicle.get(v));
			LinkedList<Action> currentPath = paths.get(v);
			for (int i = 0; i < currentPath.size() - 1; i++) {
				Action currentAction = currentPath.get(i);
				totalCost += computeActionsDistance(currentAction, currentPath.get(i+1));
			}
			totalCost = totalCost * v.costPerKm();
		}
		this.cost = totalCost;
		return totalCost;
	}
	
	private double computeVehicleDistance(Vehicle v, Action a) {
		City start = v.getCurrentCity();
		City destination = a.getTask().pickupCity;
		return start.distanceTo(destination);
	}
	
	private double computeActionsDistance(Action a, Action b) {
		City cityTaskA;
		City cityTaskB;
	
		if (a.getAction() == ActionsEnum.PICKUP) {
			cityTaskA = a.getTask().pickupCity;
		} else {
			cityTaskA = a.getTask().deliveryCity;
		}
		
		if (b.getAction() == ActionsEnum.PICKUP) {
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
		result = prime * result + ((nextActions == null) ? 0 : nextActions.hashCode());
		result = prime * result + ((nextActionsVehicle == null) ? 0 : nextActionsVehicle.hashCode());
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
		if (nextActions == null) {
			if (other.nextActions != null)
				return false;
		} else if (!nextActions.equals(other.nextActions))
			return false;
		if (nextActionsVehicle == null) {
			if (other.nextActionsVehicle != null)
				return false;
		} else if (!nextActionsVehicle.equals(other.nextActionsVehicle))
			return false;
		return true;
	}
	
	

}
