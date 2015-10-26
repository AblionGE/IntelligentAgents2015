package template;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import logist.simulation.Vehicle;
import logist.task.Task;
import logist.topology.Topology.City;

/**
 * A state is composed by the position of an agent and the state of the world
 * (all freeTasks with their respective positions)
 *
 */
public class State {

	private City agentPosition;
	// Tasks are represented in a HashMap with the task and the current city
	private ArrayList<Task> freeTasks = new ArrayList<Task>();
	private ArrayList<Task> takenTasks = new ArrayList<Task>();
	private ArrayList<Task> deliveredTasks = new ArrayList<Task>();
	private double cost = 0;
	private double reward = 0;
	private double f = 0;

	public State(City agentPosition, ArrayList<Task> freeTasks, ArrayList<Task> takenTasks,
			ArrayList<Task> deliveredTasks, double cost, double reward, Vehicle vehicle) {
		super();
		this.agentPosition = agentPosition;
		this.freeTasks = freeTasks;
		this.takenTasks = takenTasks;
		this.deliveredTasks = deliveredTasks;
		this.cost = cost;
		this.reward = reward;
		this.f = computeF(vehicle);
	}

	protected City getAgentPosition() {
		return agentPosition;
	}

	protected void setAgentPosition(City agentPosition) {
		this.agentPosition = agentPosition;
	}

	protected ArrayList<Task> getFreeTasks() {
		return freeTasks;
	}

	protected void setFreeTasks(ArrayList<Task> freeTasks) {
		this.freeTasks = freeTasks;
	}

	protected ArrayList<Task> getTakenTasks() {
		return takenTasks;
	}

	protected void setTakenTasks(ArrayList<Task> takenTasks) {
		this.takenTasks = takenTasks;
	}

	protected ArrayList<Task> getDeliveredTasks() {
		return deliveredTasks;
	}

	protected void setDeliveredTasks(ArrayList<Task> deliveredTasks) {
		this.deliveredTasks = deliveredTasks;
	}

	protected double getCost() {
		return cost;
	}

	protected void setCost(double cost) {
		this.cost = cost;
	}

	protected double getReward() {
		return reward;
	}

	protected void setReward(double reward) {
		this.reward = reward;
	}

	protected double getF() {
		return f;
	}

	protected void setF(double f) {
		this.f = f;
	}

	/**
	 * Compute f(n) = g(n) + h(n) where g(n) is the cost from previous actions
	 * and h(n) is an estimation of the future cost
	 * 
	 * @param vehicle
	 * @return
	 */
	private double computeF(Vehicle vehicle) {

		// Total reward
		double totalReward = 0;
		for (Task t : deliveredTasks) {
			totalReward += t.reward;
		}

		double g = -cost + totalReward;

		// Compute h
		double costUndelivered = 0;
		double rewardUndelivered = 0;
		for (Task t : takenTasks) {
			costUndelivered += t.pickupCity.distanceTo(t.deliveryCity) * vehicle.costPerKm();
			rewardUndelivered += t.reward;
		}
		for (Task t : freeTasks) {
			costUndelivered += t.pickupCity.distanceTo(t.deliveryCity) * vehicle.costPerKm();
			rewardUndelivered += t.reward;
		}

		double h = -costUndelivered + rewardUndelivered;

		return g + h;
	}

	/**
	 * Returns the tasks that are picked up
	 * 
	 * @param tasks
	 * @return
	 */
	public List<Task> taskPickUpDifferences(State state) {
		List<Task> differences = new ArrayList<Task>();
		List<Task> currentTasks = this.takenTasks;
		List<Task> stateTasks = state.getTakenTasks();
		for (Task t : stateTasks) {
			if (!currentTasks.contains(t)) {
				differences.add(t);
			}
		}
		return differences;
	}

	/**
	 * Returns the tasks that are delivered
	 * 
	 * @param tasks
	 * @return
	 */
	public List<Task> taskDeliverDifferences(State state) {
		List<Task> differences = new ArrayList<Task>();
		List<Task> currentTasks = this.deliveredTasks;
		List<Task> stateTasks = state.getDeliveredTasks();
		for (Task t : stateTasks) {
			if (!currentTasks.contains(t)) {
				differences.add(t);
			}
		}
		return differences;
	}

	/**
	 * Create all reachable states from the current state
	 * 
	 * @param knownStates
	 * @param vehicleCapacity
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Set<State> computeChildren(Vehicle vehicle) {
		int vehicleCapacity = vehicle.capacity();
		Set<State> returnedChildren = new HashSet<State>();

		/************* Pickup a Task ***************/
		int weightInVehicle = computeWeightOfAListOfTasks(takenTasks);
		for (Task task : freeTasks) {
			ArrayList<Task> childFreeTasks = (ArrayList<Task>) freeTasks.clone();
			ArrayList<Task> childTakenTasks = (ArrayList<Task>) takenTasks.clone();
			ArrayList<Task> childDeliveredTasks = (ArrayList<Task>) deliveredTasks.clone();
			if (task.weight + weightInVehicle <= vehicleCapacity) {
				childFreeTasks.remove(task);
				childTakenTasks.add(task);
				State childState = new State(task.pickupCity, childFreeTasks, childTakenTasks, childDeliveredTasks,
						this.cost + vehicle.costPerKm() * agentPosition.distanceTo(task.pickupCity), this.reward,
						vehicle);
				returnedChildren.add(childState);
			}
		}

		/************ Deliver a Task ***************/
		for (Task task : takenTasks) {
			ArrayList<Task> childFreeTasks = (ArrayList<Task>) freeTasks.clone();
			ArrayList<Task> childTakenTasks = (ArrayList<Task>) takenTasks.clone();
			ArrayList<Task> childDeliveredTasks = (ArrayList<Task>) deliveredTasks.clone();
			childTakenTasks.remove(task);
			childDeliveredTasks.add(task);
			State childState = new State(task.deliveryCity, childFreeTasks, childTakenTasks, childDeliveredTasks,
					this.cost + vehicle.costPerKm() * agentPosition.distanceTo(task.pickupCity),
					this.reward + task.reward, vehicle);
			returnedChildren.add(childState);
		}
		return returnedChildren;
	}

	/**
	 * Compute the total weight of a list of freeTasks
	 * 
	 * @param tempTasks
	 * @return
	 */
	private int computeWeightOfAListOfTasks(ArrayList<Task> tempTasks) {
		int totalWeight = 0;
		for (Task t : tempTasks) {
			totalWeight += t.weight;
		}
		return totalWeight;
	}

	@Override
	public String toString() {
		return "State [agentPosition=" + agentPosition + ", freeTasks=" + freeTasks + ", takenTasks=" + takenTasks
				+ ", deliveredTasks=" + deliveredTasks + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((agentPosition == null) ? 0 : agentPosition.hashCode());
		result = prime * result + ((deliveredTasks == null) ? 0 : deliveredTasks.hashCode());
		result = prime * result + ((freeTasks == null) ? 0 : freeTasks.hashCode());
		result = prime * result + ((takenTasks == null) ? 0 : takenTasks.hashCode());
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
		State other = (State) obj;

		// To uncomment if we want agent position in the state
		/*
		 * if (agentPosition == null) { if (other.agentPosition != null) return
		 * false; } else if (!agentPosition.equals(other.agentPosition)) return
		 * false;
		 */
		if (deliveredTasks == null) {
			if (other.deliveredTasks != null)
				return false;
		} else if (!deliveredTasks.equals(other.deliveredTasks))
			return false;
		if (freeTasks == null) {
			if (other.freeTasks != null)
				return false;
		} else if (!freeTasks.equals(other.freeTasks))
			return false;
		if (takenTasks == null) {
			if (other.takenTasks != null)
				return false;
		} else if (!takenTasks.equals(other.takenTasks))
			return false;
		return true;
	}

}
