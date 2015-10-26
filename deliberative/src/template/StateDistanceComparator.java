package template;

import java.util.Comparator;

import logist.task.Task;
import logist.topology.Topology.City;

public class StateDistanceComparator implements Comparator<State> {

	private City city;
	
	public StateDistanceComparator() {
		city = null;
	}
	
	@Override
    public int compare(State s1, State s2) {
        Double d1 = s1.getAgentPosition().distanceTo(city);
        Double d2 = s2.getAgentPosition().distanceTo(city);
        return d1.compareTo(d2);
    }
	
	public void setState(State s) {
		city = s.getAgentPosition();
	}
}
