package template;

import logist.task.Task;

/**
 * This class represent the action "Deliver" a task
 * and will correspond to move to the delivery city of the task and to deliver it.
 *
 */
public class Deliver {
	
	private Task task;
	private int time;
	
	Deliver (Task task) {
		this.task = task;
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

}
