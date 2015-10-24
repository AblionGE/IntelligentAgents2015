package template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import logist.task.Task;
import logist.topology.Topology.City;

/**
 * A state is composed by the position of an agent and the state of the world (all tasks with their respective positions)
 *
 */
public class State {

	private City agentPosition;
	private List<State> children = null;
	// Tasks are represented in a HashMap with the task and the current city
	private HashMap<Task, City> tasks = new HashMap<Task, City>();

	public State(City agentPosition, HashMap<Task, City> tasks) {
		super();
		this.agentPosition = agentPosition;
		this.tasks = tasks;
	}

	protected City getAgentPosition() {
		return agentPosition;
	}

	protected void setAgentPosition(City agentPosition) {
		this.agentPosition = agentPosition;
	}

	protected HashMap<Task, City> getTasks() {
		return tasks;
	}

	protected void setTasks(HashMap<Task, City> tasks) {
		this.tasks = tasks;
	}
	
	public City getCity(Task task) {
		return tasks.get(task);
	}
	
	/**
	 * Returns the tasks that changed their location
	 * @param tasks
	 * @return
	 */
	public List<Task> taskDifferences(State state) {
		List<Task> differences = new ArrayList<Task>();
		for(Task t: this.tasks.keySet()) {
			if(!this.tasks.get(t).equals(state.getCity(t))) {
				differences.add(t);
			}
		}
		return differences;
	}

	/**
	 * Create all reachable states from the current state
	 * @param knownStates
	 * @param vehicleCapacity
	 * @return
	 */
	public List<State> computeChildren(HashMap<State, Boolean> knownStates, int vehicleCapacity) {
		if (children != null) {
			return children;
		}

		ArrayList<State> returnedChildren = new ArrayList<State>();
		List<City> neighbours = this.agentPosition.neighbors();

		// For each neighbour
		for (City c : neighbours) {
			// move without tasks - simply move the agent
			State newState = new State(c, this.tasks);
			returnedChildren.add(newState);

			// move with tasks
			ArrayList<Task> tasksInCurrentCity = new ArrayList<Task>();

			// Get all the task in the current city
			for (Task t : tasks.keySet()) {
				// If the destination city is not the current one and the task
				// is at the place where the agent is
				if (!t.deliveryCity.equals(agentPosition) && tasks.get(t).equals(agentPosition)) {
					// We add the task to the list
					tasksInCurrentCity.add(t);
				}
			}

			// Compute all combinations with tasks
			List<List<Task>> resultCombination = computeCombinationsOfTasks(tasksInCurrentCity,
					tasksInCurrentCity.size(), vehicleCapacity);

			for (List<Task> lt : resultCombination) {
				// Creation of HashMap for result
				HashMap<Task, City> tempTasks = new HashMap<Task, City>();
				// Add moved tasks
				for (Task t : lt) {
					tempTasks.put(t, c);
				}
				// Add tasks that do not move
				for (Task t : tasks.keySet()) {
					if (!tempTasks.containsKey(t)) {
						tempTasks.put(t, tasks.get(t));
					}
				}

				State tempState = new State(c, tempTasks);

				// FIXME : to verify if really works
				// We remove states that already exist
				if (!knownStates.containsKey(tempState)) {
					returnedChildren.add(tempState);
				}
			}
		}
		children = returnedChildren;
		return returnedChildren;
	}

	/**
	 * Creates all possible combinations of tasks that can be moved
	 * @param tasks
	 * @param sizeOfCombination
	 * @param vehicleCapacity
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<List<Task>> computeCombinationsOfTasks(ArrayList<Task> tasks, int sizeOfCombination,
			int vehicleCapacity) {
		List<List<Task>> result = new ArrayList<List<Task>>();
		ArrayList<ArrayList<Task>> lastIteration = new ArrayList<ArrayList<Task>>();

		// Only consider alone tasks
		for (Task t : tasks) {
			ArrayList<Task> tempList = new ArrayList<Task>();
			// Test if we do not have more weight than possible
			if (t.weight <= vehicleCapacity) {
				tempList.add(t);
				result.add((List<Task>) tempList.clone());
				lastIteration.add((ArrayList<Task>) tempList.clone());
			}
		}

		// And make all possible combinations with 2, 3, ... tasks to be moved
		for (int i = 1; i < sizeOfCombination; i++) {
			lastIteration = composeListsOfTasks(tasks, lastIteration);
			for (ArrayList<Task> tempTasks : lastIteration) {
				// Test if we do not have more weight than possible
				if (computeWeightOfAListOfTasks(tempTasks) <= vehicleCapacity) {
					result.add((List<Task>) tempTasks.clone());
				}
			}
		}
		return result;
	}

	/**
	 * Takes as argument a list of all tasks and the last result of this function.
	 * It simply adds to each element of the previous computation, each task once
	 * (except if the task is already present or of the id is bigger than the one of the first element.
	 * Indeed, it allows us avoid having redundancy in states)
	 * @param tasks
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
				ArrayList<Task> temp = (ArrayList<Task>) tempTask.clone();
				if (!tempTask.contains(t) && t.id > tempTask.get(0).id) {
					temp.add(t);
					result.add(temp);
				}
			}
		}
		return result;
	}
	
	/**
	 * Compute the total weight of a list of tasks
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
	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (other == this)
			return true;
		if (!(other instanceof State))
			return false;
		State s = (State) other;
		if (!this.agentPosition.equals(s.getAgentPosition())) {
			return false;
		}
		if (!this.tasks.equals(s.getTasks())) {
			return false;
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		return agentPosition.hashCode() ^ tasks.hashCode();
	}

	@Override
	public String toString() {
		String result = "Agent in " + agentPosition.name + " with tasks : \n";

		for (Task t : tasks.keySet()) {
			result += "\t " + t.toString() + ", current city : " + tasks.get(t) + "\n";
		}
		result += "\nwith children : \n";
		if (children != null) {
			for (State s : children) {
				result += s.toString();
			}
		} else {
			result += "none\n";
		}
		return result;
	}
}
