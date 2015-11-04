package template;

import logist.task.Task;

/**
 * This class represents an action that can be to pickup or deliver a task.
 * It also contains the time when te action must be done and it has a task.
 *
 */
public class Action {

	private Task task;
	private int time;
	private ActionsEnum action;
	
	Action (ActionsEnum action, Task task, int time) {
		this.task = task;
		this.action = action;
		this.time = time;
	}

	protected ActionsEnum getAction() {
		return action;
	}

	protected Task getTask() {
		return task;
	}

	protected int getTime() {
		return time;
	}

	protected void setTime(int time) {
		this.time = time;
	}

	@Override
	public String toString() {
		return "Action [task=" + task + ", time=" + time + ", action=" + action + "]";
	}
	
}
