package template;

import java.util.LinkedList;

public class Pair {

	private final State state;
	private final LinkedList<State> path;

	public Pair(State state, LinkedList<State> path) {
		this.state = state;
		this.path = path;
	}

	public State getState() {
		return state;
	}

	public LinkedList<State> getPath() {
		return path;
	}

	@Override
	public int hashCode() {
		return state.hashCode() ^ path.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Pair)) return false;
		Pair pairo = (Pair) o;
		return this.state.equals(pairo.getState()) &&
				this.path.equals(pairo.getPath());
	}

}