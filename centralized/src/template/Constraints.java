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

		if (state == null) {
			return 1;
		}

		HashMap<Vehicle, LinkedList<Movement>> plans = state.getPlans();
		Set<Vehicle> vehicles = plans.keySet();
		for (Vehicle v : vehicles) {

			int timeCounter = 1;
			int currentLoad = 0;
			HashMap<Task, Integer> consistencyVerification = new HashMap<Task, Integer>();

			errors += checkVehicleLoad(state, v);
			errors += checkFirstMovementTime(state, v);

			LinkedList<Movement> movements = plans.get(v);
			for (int i = 0; i < movements.size(); i++) {
				consistencyVerification = computeMovementConsistency(movements.get(i), consistencyVerification);
				errors += checkTime(state, timeCounter, movements.get(i));
				int loadResult = checkLoad(movements.get(i), v, currentLoad);
				if (loadResult < 0) {
					errors += 1;
					return errors;
				}
				currentLoad = loadResult;
				timeCounter++;
			}
			errors += verificationMovementConsistency(consistencyVerification);
			if (currentLoad != 0) {
				errors += 1;
			}
		}

		errors += checkMovementDoneOnce(state);
		errors += checkFirstTaskIsPickedUp(state);

		return errors;
	}

	/**
	 * Check if for a movement, a Vehicle and the current load in this vehicle,
	 * we can execute the movement
	 * 
	 * @param m
	 * @param v
	 * @param currentLoad
	 * @return
	 */
	private static int checkLoad(Movement m, Vehicle v, int currentLoad) {
		if (m.getAction() == Action.PICKUP) {
			currentLoad += m.getTask().weight;
			if (currentLoad > v.capacity()) {
				return -1;
			}
		} else {
			currentLoad -= m.getTask().weight;
			if (currentLoad < 0) {
				return -1;
			}
		}
		return currentLoad;
	}

	/**
	 * For a SolutionState and a vehicule, check if the capacitz of the vehicle
	 * is respected in the plan.
	 * 
	 * @param state
	 * @param v
	 * @return
	 */
	public static int checkVehicleLoad(SolutionState state, Vehicle v) {
		int currentLoad = 0;
		LinkedList<Movement> movements = state.getPlans().get(v);
		for (int i = 0; i < movements.size(); i++) {
			Movement m = movements.get(i);
			if (m.getAction() == Action.PICKUP) {
				currentLoad += m.getTask().weight;
				if (currentLoad > v.capacity()) {
					return 1;
				}
			} else {
				currentLoad -= m.getTask().weight;
				if (currentLoad < 0) {
					return 1;
				}
			}
		}
		if (currentLoad != 0) {
			return 1;
		}
		return 0;
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
	private static int checkFirstMovementTime(SolutionState state, Vehicle v) {
		HashMap<Vehicle, Movement> nextMove = state.getNextMovementsVehicle();
		if (state.getTimedMovements().get(nextMove.get(v)) != 1) {
			return 1;
		}
		return 0;
	}

	/**
	 * Check that actions are ordered correctly for each vehicle
	 * 
	 * @param state
	 * @return
	 */
	private static int checkTime(SolutionState state, int timeCounter, Movement m) {
		if (state.getTimedMovements().get(m) != timeCounter) {
			return 1;
		}
		return 0;
	}

	/**
	 * Compute number of pickup and delivery for each task for a specific
	 * movement in a specific vehicle
	 * 
	 * @param state
	 * @return
	 */
	private static HashMap<Task, Integer> computeMovementConsistency(Movement m, HashMap<Task, Integer> verification) {
		if (verification.containsKey(m.getTask()) && m.getAction() == Action.DELIVER) {
			verification.put(m.getTask(), verification.get(m.getTask()) + 1);
		} else if (m.getAction() == Action.PICKUP) {
			verification.put(m.getTask(), 1);
		}
		return verification;
	}

	/**
	 * Verify that there is only one pickup and one deliver for each vehicle
	 * 
	 * @param verification
	 * @return
	 */
	private static int verificationMovementConsistency(HashMap<Task, Integer> verification) {
		Collection<Integer> nbOfActionsPerTask = verification.values();
		for (Integer i : nbOfActionsPerTask) {
			if (i != 2) {
				return 1;
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
	private static int checkFirstTaskIsPickedUp(SolutionState state) {
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
