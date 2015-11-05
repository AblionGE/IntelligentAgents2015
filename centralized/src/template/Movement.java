package template;

import logist.task.Task;

/**
 * This class represents an movement that can be to pickup or deliver a task.
 * It also contains the time when the movement must be done and it has a task.
 *
 */
public class Movement {

	private Task task;
	private int time;
	private Action action;
	
	Movement (Action action, Task task, int time) {
		this.task = task;
		this.action = action;
		this.time = time;
	}

	protected Action getAction() {
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
		return "Movement [task=" + task + ", time=" + time + ", action=" + action + "]";
	}
	
}
