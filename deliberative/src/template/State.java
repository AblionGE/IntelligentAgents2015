package template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import logist.task.Task;
import logist.topology.Topology.City;

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


	// Create all the reachable states from the current state
	public List<State> computeChildren(HashMap<State, Boolean> knownStates) {
		if (children != null) {
			return children;
		}
		List<State> returnedChildren = new ArrayList<State>();
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
				if (t.deliveryCity.equals(agentPosition) && tasks.get(t).equals(agentPosition)) {
					// We add the task to the list
					tasksInCurrentCity.add(t);
				}
			}
			
			// Compute all combinations with tasks
			List<List<Task>> resultCombination = computeCombinationsOfTasks(tasksInCurrentCity, tasksInCurrentCity.size());
			
			//Remove combinations where there are too much tasks for the vehicle
			// TODO
			
			for (List<Task> lt : resultCombination) {
				//Creation of HashMap for result
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
				returnedChildren.add(tempState);
			}
		}

		// chercher noeuds deja existants
		// Checker poids vehicule
		return returnedChildren;
	}

	// for each element of this list, create a state
	@SuppressWarnings("unchecked")
	private List<List<Task>> computeCombinationsOfTasks(ArrayList<Task> tasks, int sizeOfCombination) {
		List<List<Task>> result = new ArrayList<List<Task>>();
		ArrayList<ArrayList<Task>> lastIteration = new ArrayList<ArrayList<Task>>();
		
		for (Task t : tasks) {
			ArrayList<Task> tempList = new ArrayList<Task>();
			tempList.add(t);
			result.add((List<Task>) tempList.clone());
			lastIteration.add((ArrayList<Task>) tempList.clone());
		}
		
		for (int i = 1; i < sizeOfCombination; i++) {
			 lastIteration = composeListsOfTasks(tasks, lastIteration);
			 for (ArrayList<Task> tempTasks : lastIteration) {
				 result.add((List<Task>) tempTasks.clone());
			 }
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private ArrayList<ArrayList<Task>> composeListsOfTasks(ArrayList<Task> tasks, ArrayList<ArrayList<Task>> lastIteration) {
		ArrayList<ArrayList<Task>> result = new ArrayList<ArrayList<Task>>();
		for (ArrayList<Task> tempTask : lastIteration) {
			 for (Task t : tasks) {
				 if (!tempTask.contains(t)) {
					ArrayList<Task> temp = (ArrayList<Task>) tempTask.clone();
					temp.add(t);
					result.add(temp);					 
				 }
			 }
		 }
		return result;
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

}
