package template;

import logist.task.Task;

/**
 * This class represents an movement that can be to pickup or deliver a task.
 * It also contains the time when the movement must be done and it has a task.
 *
 */
public class Movement {

	private Task task;
	private Action action;
	
	Movement (Action action, Task task) {
		this.task = task;
		this.action = action;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((action == null) ? 0 : action.hashCode());
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
		if (task == null) {
			if (other.task != null)
				return false;
		} else if (!task.equals(other.task))
			return false;
		return true;
	}
	
}
