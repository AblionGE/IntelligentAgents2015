package template;

import java.util.HashMap;

import logist.simulation.Vehicle;

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
	
	// TODO
	protected void computeCost() {
		
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
