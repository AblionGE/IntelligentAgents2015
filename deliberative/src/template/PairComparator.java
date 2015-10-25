package template;

import java.util.Comparator;

class PairComparator implements Comparator<Pair> {
	@Override
	public int compare(Pair o1, Pair o2) {
		if (o1.getState().getF() < o2.getState().getF()) return -1;
		if (o1.getState().getF() > o2.getState().getF()) return 1;
		return 0;
	}
}
