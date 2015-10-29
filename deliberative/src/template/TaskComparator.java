package template;

import java.util.Comparator;

import logist.task.Task;
import logist.topology.Topology.City;

class TaskComparator implements Comparator<Task> {
	@Override
	public int compare(Task t1, Task t2) {
		City p1 = t1.pickupCity;
		City p2 = t2.pickupCity;

		if (t1.id < t2.id) {
			return 1;
		} else if (t1.id > t2.id) {
			return -1;
		} else {
			return 0;
		}

		/*if (p1.equals(p2)) {
			return t1.deliveryCity.name.compareToIgnoreCase(t2.deliveryCity.name);
		}
		return p1.name.compareToIgnoreCase(p2.name);*/
	}
}