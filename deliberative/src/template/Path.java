package template;

import java.util.LinkedList;

import logist.task.Task;
import logist.topology.Topology.City;

/**
 * Class that holds a state and the path traveled before arriving at it
 * 
 * @author Marc Schaer and Cynthia Oeschger
 * 
 */
public class Path {

	private final State lastState;
	private final LinkedList<State> statesList;
	private double traveledDistance = 0;
	private double deliveredReward = 0;
	private double undeliveredReward = 0;
	private double expectedFutureDistance = 0;

	public Path(State state, LinkedList<State> statesList, boolean runningAstar) {
		lastState = state;
		this.statesList = statesList;
		if (runningAstar) {
			traveledDistance = travelDistance();
			initCosts();
		}
	}

	@SuppressWarnings("unchecked")
	public Path(State state, Path path, boolean runningAstar) {
		lastState = state;
		statesList = (LinkedList<State>) path.getPath().clone();
		statesList.addLast(path.getState());
		if (runningAstar) {
			traveledDistance = path.getTravelDistance()
					+ distanceBetween(lastState, statesList.peekLast());
			initCosts();
		}
	}

	/**
	 * Initialization of the path costs
	 */
	private void initCosts() {
		deliveredReward = lastState.getDeliveredReward();
		undeliveredReward = lastState.getUndeliveredReward();
		expectedFutureDistance = estimateFutureDistance();
	}

	/**
	 * Computes the distance traveled in the whole path
	 * 
	 * @return distance
	 */
	private double travelDistance() {
		double dist = 0.0;
		LinkedList<City> cityPath = new LinkedList<City>();

		for (State s : statesList) {
			cityPath.addLast(s.getAgentPosition());
		}
		cityPath.addLast(lastState.getAgentPosition());

		City start = cityPath.pollFirst();
		City next;
		while (!cityPath.isEmpty()) {
			next = cityPath.pollFirst();
			dist += start.distanceTo(next);
			start = next;
		}
		return dist;
	}

	/**
	 * Estimates the distance that will be traveled. Takes into account the
	 * distances each free task's pickup and delivery city and the distance
	 * between the vehicle and its closest pickup city
	 * 
	 * @return
	 */
	private double estimateFutureDistance() {
		double estimate = 0;
		City lastCity = lastState.getAgentPosition();

		// estimation to pick and deliver remaining tasks
		Task closestTask = null;
		double min = Double.MAX_VALUE;
		double dist;
		for (Task t : lastState.getFreeTasks()) {
			dist = t.pickupCity.distanceTo(t.deliveryCity);
			estimate += dist;
			if (dist < min) {
				closestTask = t;
			}
		}
		// estimation to go and pick the first remaining task
		if (min < Double.MAX_VALUE) {
			estimate += lastCity.distanceTo(closestTask.pickupCity);
		}

		return estimate;
	}

	/**
	 * Distance between the agent's position between two states
	 * 
	 * @param s1
	 * @param s2
	 * @return
	 */
	private double distanceBetween(State s1, State s2) {
		return s1.getAgentPosition().distanceTo(s2.getAgentPosition());
	}

	/**
	 * Compute f(n) = g(n) + h(n) where g(n) is the cost from previous actions
	 * and h(n) is an estimation of the future cost
	 * 
	 * @param vehicle
	 * @return
	 */
	public double totalReward(int costPerKm) {
		double g = -traveledDistance * costPerKm + deliveredReward;
		double h = -expectedFutureDistance * costPerKm + undeliveredReward;

		return g + h;
	}

	@Override
	public int hashCode() {
		return lastState.hashCode() ^ statesList.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Path))
			return false;
		Path pairo = (Path) o;
		return this.lastState.equals(pairo.getState())
				&& this.statesList.equals(pairo.getPath());
	}

	public State getState() {
		return lastState;
	}

	public LinkedList<State> getPath() {
		return statesList;
	}

	public double getTravelDistance() {
		return traveledDistance;
	}

}