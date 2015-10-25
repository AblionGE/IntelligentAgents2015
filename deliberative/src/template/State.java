package template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import logist.task.Task;
import logist.topology.Topology.City;

/**
 * A state is composed by the position of an agent and the state of the world
 * (all freeTasks with their respective positions)
 *
 */
public class State {

	private City agentPosition;
	private List<State> children = new ArrayList<State>();
	// Tasks are represented in a HashMap with the task and the current city
	private ArrayList<Task> freeTasks = new ArrayList<Task>();
	private ArrayList<Task> takenTasks = new ArrayList<Task>();
	private ArrayList<Task> deliveredTasks = new ArrayList<Task>();

	public State(City agentPosition, ArrayList<Task> freeTasks, ArrayList<Task> takenTasks,
			ArrayList<Task> deliveredTasks) {
		super();
		this.agentPosition = agentPosition;
		this.freeTasks = freeTasks;
		this.takenTasks = takenTasks;
		this.deliveredTasks = deliveredTasks;
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
	public List<State> computeChildren(ArrayList<State> knownStates, int vehicleCapacity) {
		if (!children.isEmpty()) {
			return children;
		}

		ArrayList<State> returnedChildren = new ArrayList<State>();
		List<City> neighbours = this.agentPosition.neighbors();

		// For each neighbour
		for (City c : neighbours) {
			// move without freeTasks - simply move the agent
			State newState = new State(c, this.freeTasks, this.takenTasks, this.deliveredTasks);
			this.children.add(newState);
			returnedChildren.add(newState);

			// move with freeTasks
			ArrayList<Task> tasksInCurrentCity = new ArrayList<Task>();
			// Get all the task in the current city that are free
			for (Task t : freeTasks) {
				// If the destination city is not the current one and the task
				// is at the place where the agent is
				if (!t.deliveryCity.equals(agentPosition) && t.pickupCity.equals(agentPosition)) {
					// We add the task to the list
					tasksInCurrentCity.add(t);
				}
			}

			// Create new takenTasks and delivered task for the child
			ArrayList<Task> childDeliveredTasks = (ArrayList<Task>) this.deliveredTasks.clone();
			ArrayList<Task> childTakenTasksInCurrentState = (ArrayList<Task>) this.takenTasks.clone();
			for (Task t : takenTasks) {
				if (t.deliveryCity.equals(c)) {
					childDeliveredTasks.add(t);
					childTakenTasksInCurrentState.remove(t);
				}
			}

			// Compute all combinations with freeTasks and childTakenTasks
			List<List<Task>> resultCombination = computeCombinationsOfTasks(tasksInCurrentCity,
					tasksInCurrentCity.size(), childTakenTasksInCurrentState, vehicleCapacity);

			for (List<Task> lt : resultCombination) {
				// Creation of HashMap for result
				ArrayList<Task> childTakenTasksInChild = new ArrayList<Task>();
				// Add moved freeTasks
				for (Task t : lt) {
					childTakenTasksInChild.add(t);
				}

				ArrayList<Task> childFreeTasks = new ArrayList<Task>();
				// Add freeTasks that do not move
				for (Task t : this.freeTasks) {
					if (!childTakenTasksInChild.contains(t)) {
						childFreeTasks.add(t);
					}
				}

				State childState = new State(c, childFreeTasks, childTakenTasksInChild, childDeliveredTasks);

				// FIXME : to verify if really works
				// We remove states that already exist
				this.children.add(childState);
				// if (!knownStates.contains(childState)) {
				returnedChildren.add(childState);
				// }
			}
		}
		return returnedChildren;
	}

	/**
	 * Creates all possible combinations of freeTasks that can be moved
	 * 
	 * @param freeTasks
	 * @param sizeOfCombination
	 * @param vehicleCapacity
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<List<Task>> computeCombinationsOfTasks(ArrayList<Task> freeTasks, int sizeOfCombination,
			ArrayList<Task> takenTasks, int vehicleCapacity) {

		List<List<Task>> result = new ArrayList<List<Task>>();
		ArrayList<ArrayList<Task>> lastIteration = new ArrayList<ArrayList<Task>>();

		if (sizeOfCombination == 0) {
			result.add(takenTasks);
		} else {
			// Only consider alone freeTasks
			for (Task t : freeTasks) {
				ArrayList<Task> tempList = (ArrayList<Task>) takenTasks.clone();
				tempList.add(t);
				// Test if we do not have more weight than possible
				if (computeWeightOfAListOfTasks(tempList) <= vehicleCapacity) {
					result.add(tempList);
					lastIteration.add(tempList);
				}
			}

			// And make all possible combinations with 2, 3, ... freeTasks to be
			// moved
			for (int i = 1; i < sizeOfCombination; i++) {
				lastIteration = composeListsOfTasks(freeTasks, lastIteration);
				for (ArrayList<Task> tempTasks : lastIteration) {
					// Test if we do not have more weight than possible
					if (computeWeightOfAListOfTasks(tempTasks) <= vehicleCapacity) {
						result.add((List<Task>) tempTasks.clone());
					}
				}
			}
		}
		return result;
	}

	/**
	 * Takes as argument a list of all freeTasks and the last result of this
	 * function. It simply adds to each element of the previous computation,
	 * each task once (except if the task is already present or of the id is
	 * bigger than the one of the first element. Indeed, it allows us avoid
	 * having redundancy in states)
	 * 
	 * @param freeTasks
	 * @param lastIteration
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private ArrayList<ArrayList<Task>> composeListsOfTasks(ArrayList<Task> tasks,
			ArrayList<ArrayList<Task>> lastIteration) {
		
		ArrayList<ArrayList<Task>> result = new ArrayList<ArrayList<Task>>();
		for (int i = 0; i < lastIteration.size(); i++) {
			ArrayList<Task> tempTask = lastIteration.get(i);
			for (int j = 0; j < tasks.size(); j++) {
				Task t = tasks.get(j);
				if (!tempTask.contains(t) && t.id > tempTask.get(0).id) {
					ArrayList<Task> temp = (ArrayList<Task>) tempTask.clone();
					temp.add(t);
					result.add(temp);
				}
			}
		}
		return result;
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

}
