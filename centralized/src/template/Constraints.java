package template;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import logist.simulation.Vehicle;

/**
 * This class is used to have functions verifying constraints
 *
 */
public final class Constraints {
	
	/**
	 * This function verifies all constraints
	 * @param state
	 * @return the number of errors.
	 */
	public static int checkSolutionState(SolutionState state) {
		int errors = 0;
		
		errors += checkMovementDoneOnce(state);
		errors += checkFirstMovementTime(state);
		errors += checkTime(state);
		errors += checkMovementConsistency(state);
		errors += checkRelationTaskVehicle(state);
		
		return errors;
	}
	/**
	 * Check that each action is done only once
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
	 * @param state
	 * @return
	 */
	private static int checkFirstMovementTime(SolutionState state) {
		return 0;
	}
	
	/**
	 * Check that actions are ordered correctly for each vehicle
	 * @param state
	 * @return
	 */
	private static int checkTime(SolutionState state) {
		return 0;
	}
	
	/**
	 * Test if pickup of each task is before the delivery
	 * and check if the pickup and delivery for one task is done by the same
	 * vehicle.
	 * @param state
	 * @return
	 */
	private static int checkMovementConsistency(SolutionState state) {
		return 0;
	}
	
	/**
	 * Check that following actions are done by the same vehicle
	 * @param state
	 * @return
	 */
	private static int checkRelationTaskVehicle(SolutionState state){
		return 0;
	}

}
