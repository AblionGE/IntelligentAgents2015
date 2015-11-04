package template;

/**
 * This class is used to have functions verifying constraints
 *
 */
public final class Constraints {
	
	/**
	 * Check that each action is done only once
	 * @param state
	 * @return
	 */
	public static boolean checkActionDoneOnce(SolutionState state) {
		return false;
	}
	
	/**
	 * Check that each first action has time 1
	 * @param state
	 * @return
	 */
	public static boolean checkFirstActionTime(SolutionState state) {
		return false;
	}
	
	/**
	 * Check that actions are ordered correctly for each vehicle
	 * @param state
	 * @return
	 */
	public static boolean checkTime(SolutionState state) {
		return false;
	}
	
	/**
	 * Test if pickup of each task is before the delivery
	 * and check if the pickup and delivery for one task is done by the same
	 * vehicle.
	 * @param state
	 * @return
	 */
	public static boolean checkActionConsistency(SolutionState state) {
		return false;
	}
	
	/**
	 * Check that following actions are done by the same vehicle
	 * @param state
	 * @return
	 */
	public static boolean checkRelationTaskVehicle(SolutionState state){
		return false;
	}

}
