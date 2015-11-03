package template;

import java.util.HashMap;

import logist.simulation.Vehicle;

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

}
