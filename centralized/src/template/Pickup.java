package template;

import logist.task.Task;

/**
 * This class represent the action "Pickup" a task
 * and will correspond to move to the pickup city of the task and take it into the vehicle
 *
 */
public class Pickup {
	
	private Task task;
	private int time;
	
	Pickup (Task task) {
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
