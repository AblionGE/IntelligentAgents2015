package template;

import logist.task.Task;

/**
 * This class represents a movement that can be to pickup or deliver a task. It
 * also contains the time when the movement must be done and it has a task.
 * 
 * @author Cynthia Oeschger and Marc Schaer
 */
public class Movement {

	private Task task;
	private Action action;
	private int id;

	Movement(Action action, Task task) {
		this.task = task;
		this.action = action;
		if (action == Action.PICKUP) {
			this.id = task.id * 2;
		} else {
			this.id = task.id * 2 + 1;
		}
	}

	protected Action getAction() {
		return action;
	}

	protected Task getTask() {
		return task;
	}

	@Override
	public String toString() {
		return "Movement [task=" + task + ", action=" + action + "]";
	}

	protected int getId() {
		return id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((action == null) ? 0 : action.hashCode());
		result = prime * result + id;
		result = prime * result + ((task == null) ? 0 : task.hashCode());
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
		Movement other = (Movement) obj;
		if (action != other.action)
			return false;
		if (id != other.id)
			return false;
		if (task == null) {
			if (other.task != null)
				return false;
		} else if (!task.equals(other.task))
			return false;
		return true;
	}
}
