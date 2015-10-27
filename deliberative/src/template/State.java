package template;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import logist.simulation.Vehicle;
import logist.task.Task;
import logist.topology.Topology.City;

/**
 * A state is composed by the position of an agent and the state of the world
 * (all freeTasks with their respective positions)
 * 
 * @author Marc Schaer and Cynthia Oeschger
 *
 */
public class State {

	private static final TaskComparator tComparator = new TaskComparator();;

	private City agentPosition;
	// Tasks are represented in ordered sets
	private TreeSet<Task> freeTasks = new TreeSet<Task>(tComparator);
	private TreeSet<Task> takenTasks = new TreeSet<Task>(tComparator);
	private TreeSet<Task> deliveredTasks = new TreeSet<Task>(tComparator);
	private double rewardDelivered = 0;
	private double rewardUndelivered = 0;

	public State(City agentPosition, ArrayList<Task> freeTasks, ArrayList<Task> takenTasks,
			ArrayList<Task> deliveredTasks) {
		super();
		this.agentPosition = agentPosition;
		this.freeTasks.addAll(freeTasks);
		this.takenTasks.addAll(takenTasks);
		this.deliveredTasks.addAll(deliveredTasks);
		this.rewardDelivered = deliveredTasksReward();
		this.rewardUndelivered = undeliveredTasksReward();
	}


	/**
	 * Computes the reward received for the delivered tasks
	 * 
	 * @return
	 */
	private double deliveredTasksReward() {
		double totalReward = 0;
		for (Task t : deliveredTasks) {
			totalReward += t.reward;
		}
		return totalReward;
	}

	/**
	 * Computes the reward that can be received for all not delivered tasks
	 * 
	 * @return
	 */
	private double undeliveredTasksReward() {
		double totalReward = 0;
		for (Task t : takenTasks) {
			totalReward += t.reward;
		}
		for (Task t : freeTasks) {
			totalReward += t.reward;
		}
		return totalReward;
	}

	/**
	 * Compute the total weight of a list of freeTasks
	 * 
	 * @param tempTasks
	 * @return
	 */
	private int computeWeightOfAListOfTasks(TreeSet<Task> tempTasks) {
		int totalWeight = 0;
		for (Task t : tempTasks) {
			totalWeight += t.weight;
		}
		return totalWeight;
	}
	
	/**
	 * Returns the tasks that are picked up when going from this to nextState
	 * 
	 * @param tasks
	 * @return
	 */
	public List<Task> taskPickUpDifferences(State nextState) {
		List<Task> differences = new ArrayList<Task>();
		List<Task> nextTasks = nextState.getTakenTasks();
		for (Task t : nextTasks) {
			if (!takenTasks.contains(t)) {
				differences.add(t);
			}
		}
		return differences;
	}

	/**
	 * Returns the tasks that are delivered when going from this to nextState
	 * 
	 * @param tasks
	 * @return
	 */
	public List<Task> taskDeliverDifferences(State nextState) {
		List<Task> differences = new ArrayList<Task>();
		List<Task> nextTasks = nextState.getDeliveredTasks();
		for (Task t : nextTasks) {
			if (!deliveredTasks.contains(t)) {
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
	public Set<State> computeChildren(Vehicle vehicle) {
		int vehicleCapacity = vehicle.capacity();
		Set<State> returnedChildren = new HashSet<State>();

		ArrayList<Task> childFreeTasks;
		ArrayList<Task> childTakenTasks;
		ArrayList<Task> childDeliveredTasks;

		/************* Pickup a Task ***************/
		int weightInVehicle = computeWeightOfAListOfTasks(takenTasks);
		for (Task task : freeTasks) {
			childFreeTasks = new ArrayList<Task>(freeTasks);
			childTakenTasks = new ArrayList<Task>(takenTasks);
			childDeliveredTasks = new ArrayList<Task>(deliveredTasks);
			if (task.weight + weightInVehicle <= vehicleCapacity) {
				childFreeTasks.remove(task);
				childTakenTasks.add(task);
				State childState = new State(task.pickupCity, childFreeTasks, childTakenTasks, childDeliveredTasks);
				returnedChildren.add(childState);
			}
		}

		/************ Deliver a Task ***************/
		for (Task task : takenTasks) {
			childFreeTasks = new ArrayList<Task>(freeTasks);
			childTakenTasks = new ArrayList<Task>(takenTasks);
			childDeliveredTasks = new ArrayList<Task>(deliveredTasks);
			childTakenTasks.remove(task);
			childDeliveredTasks.add(task);
			State childState = new State(task.deliveryCity, childFreeTasks, childTakenTasks, childDeliveredTasks);
			returnedChildren.add(childState);
		}
		return returnedChildren;
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

		if (agentPosition == null) {
			if (other.agentPosition != null)
				return false;
		} else if (!agentPosition.equals(other.agentPosition))
			return false;
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

	protected City getAgentPosition() {
		return agentPosition;
	}

	protected ArrayList<Task> getFreeTasks() {
		return new ArrayList<Task>(freeTasks);
	}

	protected ArrayList<Task> getTakenTasks() {
		return new ArrayList<Task>(takenTasks);
	}

	protected ArrayList<Task> getDeliveredTasks() {
		return new ArrayList<Task>(deliveredTasks);
	}


	protected double getDeliveredReward() {
		return rewardDelivered;
	}


	protected double getUndeliveredReward() {
		return rewardUndelivered;
	}


}
