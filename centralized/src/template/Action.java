package template;

import logist.task.Task;

public class Action {

	private Task task;
	private int time;
	private ActionsEnum action;
	
	Action (ActionsEnum action, Task task) {
		this.task = task;
		this.action = action;
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
}
