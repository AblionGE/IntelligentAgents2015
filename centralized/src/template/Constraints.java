package template;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import logist.simulation.Vehicle;
import logist.task.Task;

/**
 * This class is used to have functions verifying constraints
 *
 */
public final class Constraints {

	/**
	 * This function verifies all constraints
	 * 
	 * @param state
	 * @return the number of errors.
	 */
	public static int checkSolutionState(SolutionState state) {
		int errors = 0;

		errors += checkMovementDoneOnce(state);
		errors += checkFirstMovementTime(state);
		errors += checkTime(state);
		errors += checkMovementConsistency(state);
		errors += checkFirstTaskIsPickup(state);

		return errors;
	}

	/**
	 * Check that each action is done only once
	 * 
	 * @param state
	 * @return
	 */
	private static int checkMovementDoneOnce(SolutionState state) {
		HashMap<Vehicle, LinkedList<Movement>> plans = state.getPlans();

		Collection<LinkedList<Movement>> m = plans.values();

		for (LinkedList<Movement> movements : m) {
			int sizeOfMovements = movements.size();
			Set<Movement> set = new HashSet<Movement>(movements);
			if (set.size() != sizeOfMovements) {
				return 1;
			}
		}
		return 0;
	}

	/**
	 * Check that each first action has time 1
	 * 
	 * @param state
	 * @return
	 */
	private static int checkFirstMovementTime(SolutionState state) {
		HashMap<Vehicle, Movement> nextMove = state.getNextMovementsVehicle();
		Set<Vehicle> vehicles = nextMove.keySet();
		for (Vehicle v : vehicles) {
			if (nextMove.get(v).getTime() != 1) {
				return 1;
			}
		}
		return 0;
	}

	/**
	 * Check that actions are ordered correctly for each vehicle
	 * 
	 * @param state
	 * @return
	 */
	private static int checkTime(SolutionState state) {
		HashMap<Vehicle, LinkedList<Movement>> plans = state.getPlans();
		Set<Vehicle> vehicles = plans.keySet();
		for (Vehicle v : vehicles) {
			LinkedList<Movement> movements = plans.get(v);
			int timeCounter = 1;
			for (int i = 0; i < movements.size(); i++) {
				if (movements.get(i).getTime() != timeCounter) {
					return 1;
				} else {
					timeCounter++;
				}
			}
		}
		return 0;
	}

	/**
	 * Test if pickup of each task is before the delivery and check if the
	 * pickup and delivery for one task is done by the same vehicle.
	 * 
	 * @param state
	 * @return
	 */
	private static int checkMovementConsistency(SolutionState state) {
		HashMap<Vehicle, LinkedList<Movement>> plans = state.getPlans();
		HashMap<Task, Integer> verification = new HashMap<Task, Integer>();
		Set<Vehicle> vehicles = plans.keySet();
		for (Vehicle v : vehicles) {
			LinkedList<Movement> movements = plans.get(v);
			for (Movement m : movements) {
				if (verification.containsKey(m.getTask())) {
					verification.put(m.getTask(), verification.get(m.getTask()));
				} else {
					verification.put(m.getTask(), 1);
				}
			}
			Collection<Integer> nbOfActionsPerTask = verification.values();
			for (Integer i : nbOfActionsPerTask) {
				if (i != 2) {
					return 1;
				}
			}
		}
		return 0;
	}

	/**
	 * Check that the first task for each vehicule is a PICKUP action
	 * 
	 * @param state
	 * @return
	 */
	private static int checkFirstTaskIsPickup(SolutionState state) {
		HashMap<Vehicle, Movement> nextActionVehicle = state.getNextMovementsVehicle();
		Collection<Movement> movements = nextActionVehicle.values();
		for (Movement m : movements) {
			if (m.getAction() != Action.PICKUP) {
				return 1;
			}

		}
		return 0;
	}

}
