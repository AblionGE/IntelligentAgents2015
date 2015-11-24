package template;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import logist.task.Task;

/**
 * This class is used to have functions verifying constraints
 * 
 * @author Cynthia Oeschger and Marc Schaer
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

		ArrayList<LinkedList<Movement>> plans = state.getPlans();
		for (int vehicle = 0; vehicle < AuctionTemplate.nbVehicles; vehicle++) {

			int timeCounter = 1;
			int currentLoad = 0;
			HashMap<Task, Integer> consistencyVerification = new HashMap<Task, Integer>();

			errors += checkVehicleLoad(state, vehicle);
			errors += checkFirstMovementTime(state, vehicle);

			LinkedList<Movement> movements = plans.get(vehicle);
			for (int i = 0; i < movements.size(); i++) {
				consistencyVerification = computeMovementConsistency(movements.get(i), consistencyVerification);
				
				errors += checkTime(state, timeCounter, movements.get(i));
				int loadResult = checkLoad(movements.get(i), vehicle, currentLoad);
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
	private static int checkLoad(Movement m, int v, int currentLoad) {
		if (m.getAction() == Action.PICKUP) {
			currentLoad += m.getTask().weight;
			if (currentLoad > AuctionTemplate.vehicles.get(v).capacity()) {
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
	public static int checkVehicleLoad(SolutionState state, int v) {
		int currentLoad = 0;
		LinkedList<Movement> movements = state.getPlans().get(v);
		for (int i = 0; i < movements.size(); i++) {
			Movement m = movements.get(i);
			if (m.getAction() == Action.PICKUP) {
				currentLoad += m.getTask().weight;
				if (currentLoad > AuctionTemplate.vehicles.get(v).capacity()) {
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
		ArrayList<LinkedList<Movement>> plans = state.getPlans();

		for (int i = 0; i < plans.size(); i++) {
			LinkedList<Movement> movements = plans.get(i);
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
	private static int checkFirstMovementTime(SolutionState state, int v) {
		Movement[] nextMove = state.getNextMovementsVehicle();
		if (nextMove[v] == null) {
			return 0;
		}
		if (state.getTimedMovements()[nextMove[v].getId()] != 1) {
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
		if (state.getTimedMovements()[m.getId()] != timeCounter) {
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
		Movement[] nextActionVehicle = state.getNextMovementsVehicle();
		for (int i = 0; i < nextActionVehicle.length; i++) {
			if (nextActionVehicle[i] == null) {
				return 0;
			}
			if (nextActionVehicle[i].getAction() != Action.PICKUP) {
				return 1;
			}

		}
		return 0;
	}
}
