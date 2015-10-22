package template;

import java.util.HashMap;
import java.util.List;

import logist.task.Task;
import logist.topology.Topology.City;

public class State {
	
	private boolean goal;
	private City agentPosition;
	private HashMap<Task,City> tasks = new HashMap<Task,City>();
	private List<State> children = null;
	
	public State(boolean goal, City agentPosition, HashMap<Task, City> tasks) {
		super();
		this.goal = goal;
		this.agentPosition = agentPosition;
		this.tasks = tasks;
	}
	protected boolean isGoal() {
		return goal;
	}
	protected void setGoal(boolean goal) {
		this.goal = goal;
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
	
	//TODO
	//succ(n)
	public List<State> computeChildren(HashMap<State,Boolean> knownStates) {
		if (children != null) {
			return children;
		}
		
		//chercher noeuds deja existants
		//Checker poids vehicule
		return null;
	}

}
