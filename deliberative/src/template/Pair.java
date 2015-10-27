package template;

import java.util.LinkedList;

import logist.task.Task;
import logist.topology.Topology.City;

public class Pair {

	private final State state;
	private final LinkedList<State> path;
	private double distance;
	private double reward;

	public Pair(State state, LinkedList<State> path) {
		this.state = state;
		this.path = path;
		distance = pathDistance();
		reward = state.getDeliveredReward();
	}

	public State getState() {
		return state;
	}

	public LinkedList<State> getPath() {
		return path;
	}

	@Override
	public int hashCode() {
		return state.hashCode() ^ path.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Pair)) return false;
		Pair pairo = (Pair) o;
		return this.state.equals(pairo.getState()) &&
				this.path.equals(pairo.getPath());
	}
	
	/**
	 * Compute f(n) = g(n) + h(n) where g(n) is the cost from previous actions
	 * and h(n) is an estimation of the future cost
	 * 
	 * @param vehicle
	 * @return
	 */
	public double computeF(int costPerKm) { // FIXME check h
		
		// Total reward
		double g = -distance*costPerKm + reward;

		// Compute h
		double costUndelivered = 0;
		double rewardUndelivered = state.getUndeliveredReward();
		for (Task t : state.getTakenTasks()) {
			costUndelivered += t.pickupCity.distanceTo(t.deliveryCity) * costPerKm;
		}
		for (Task t : state.getFreeTasks()) {
			costUndelivered += t.pickupCity.distanceTo(t.deliveryCity) * costPerKm;
		}

		double h = -costUndelivered + rewardUndelivered;

		return g + h;
	}
	
	/**
	 * Computes the distance traveled in path
	 * @return distance
	 */
	private double pathDistance() {
		double dist = 0.0;
		LinkedList<City> cityPath = new LinkedList<City>();
		
		for(State s: path) {
			cityPath.addLast(s.getAgentPosition());
		}
		cityPath.addLast(state.getAgentPosition());
		
		City start = cityPath.pollFirst();
		City next;
		while(!cityPath.isEmpty()) {
			next = cityPath.pollFirst();
			dist += start.distanceTo(next);
			start = next;
		}
		return dist;
	}

}