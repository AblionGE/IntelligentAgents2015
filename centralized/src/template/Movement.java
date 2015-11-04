package template;

import logist.task.Task;

/**
 * This class represents an movement that can be to pickup or deliver a task.
 * It also contains the time when te movement must be done and it has a task.
 *
 */
public class Movement {

	private Task task;
	private int time;
	private MovementsEnumeration movement;
	
	Movement (MovementsEnumeration movement, Task task, int time) {
		this.task = task;
		this.movement = movement;
		this.time = time;
	}

	protected MovementsEnumeration getMovement() {
		return movement;
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
		return "Movement [task=" + task + ", time=" + time + ", movement=" + movement + "]";
	}
	
}
