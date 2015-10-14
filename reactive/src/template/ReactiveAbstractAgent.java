package template;

import java.util.Iterator;
import java.util.List;

import logist.agent.Agent;
import logist.behavior.ReactiveBehavior;
import logist.simulation.Vehicle;
import logist.task.TaskDistribution;
import logist.topology.Topology;
import logist.topology.Topology.City;

public abstract class ReactiveAbstractAgent implements ReactiveBehavior {
	protected Double[][] p;
	protected Integer[][] r;
	protected List<City> cities;
	protected int numCities;
	protected static final double EPSILON = 0.0001;
	protected int numStates;
	protected int numActions;
	protected double[] V;
	protected Double generalReward = 0.0;
	protected int nbOfActions = 0;
	protected int[] Best;
	protected Double discount;
	protected Double R[][];
	protected Double[] pTask;
	protected Double[][][] T;

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {
		// Reads the discount factor from the agents.xml file.
		// If the property is not present it defaults to 0.95
		discount = agent.readProperty("discount-factor", Double.class, 0.95);

		cities = topology.cities();
		numCities = cities.size();

		numStates = numCities * numCities;
		numActions = 2;

		pTask = new Double[this.getNumCities()];

		R = new Double[numStates][numActions];

		createRAndP(td);
		computeR(agent);

	}

	protected void computeR(Agent agent) {
		// One agent has only one vehicle
		Vehicle vehicle = agent.vehicles().get(0);

		// When the action is to move without taking the task
		// the reward is -distance*(cost/km).
		for (int i = 0; i < numStates; i++) {
			Integer ct[] = cityAndTaskFromIndex(i, this.getNumCities());
			if (ct[1] != null) {
				R[i][0] = -distanceBetween(cities, ct[0], ct[1]) * vehicle.costPerKm();
			} else {
				City A = cities.get(ct[0]);
				City nNeighbour = closestNeighbour(A);
				R[i][0] = -A.distanceTo(nNeighbour) * vehicle.costPerKm();
			}
		}

		// Otherwise, we take the reward from matrix r minus the travel cost
		for (int i = 0; i < numStates; i++) {
			Integer ct[] = cityAndTaskFromIndex(i, this.getNumCities());
			if (ct[1] != null) {
				R[i][1] = r[ct[0]][ct[1]] - distanceBetween(cities, ct[0], ct[1]) * vehicle.costPerKm();
			} else {
				// Should not be possible to deliver a task when there is none
				R[i][1] = -Double.MAX_VALUE;
			}
		}
	}

	protected void createRAndP(TaskDistribution td) {
		// Set matrices r (rewards) and p (probability to have a task from city1
		// to city2)
		int idC1 = 0;
		int idC2 = 0;
		this.setSmallR(new Integer[this.getNumCities()][this.getNumCities()]);
		this.setP(new Double[this.getNumCities()][this.getNumCities()]);
		for (City c1 : cities) {
			pTask[idC1] = 0.0;
			for (City c2 : cities) {
				r[idC1][idC2] = td.reward(c1, c2);
				p[idC1][idC2] = td.probability(c1, c2);
				pTask[idC1] += p[idC1][idC2];
				idC2++;
			}
			idC1++;
			idC2 = 0;
		}
	}

	/**
	 * Gives the maximum value of a vector with its index
	 * 
	 * @param vector
	 * @return max value of a vector with its index
	 */
	protected double[] max(double vector[]) {
		double max = Double.MIN_VALUE;
		int index = -1;
		double[] retVal = new double[2];
		for (int i = 0; i < vector.length; i++) {
			if (vector[i] > max || max == Double.MIN_VALUE) {
				max = vector[i];
				index = i;
			}
		}
		retVal[0] = max;
		retVal[1] = index;
		return retVal;

	}

	/**
	 * Computes the maximum difference between two elements of two vectors
	 * 
	 * @param oldV
	 * @param newV
	 * @return the maximum difference between two elements of two vectors
	 */
	protected double computeDifference(double[] oldV, double[] newV) {
		double max = new Double(-1);
		double difference;
		for (int i = 0; i < oldV.length; i++) {
			difference = Math.abs(oldV[i] - newV[i]);
			if (difference > max || max == -1) {
				max = difference;
			}
		}
		return max;
	}

	/**
	 * Gives the closest neighbour of a city
	 * 
	 * @param city
	 * @return the closest neighbour of a city
	 */
	public City closestNeighbour(City city) {
		City closestNeighbour = null;
		double minDistance = -1;
		List<City> neighbours = city.neighbors();
		Iterator<City> it = neighbours.iterator();
		while (it.hasNext()) {
			City neighbour = (City) it.next();
			if (city.distanceTo(neighbour) < minDistance || minDistance < 0) {
				minDistance = city.distanceTo(neighbour);
				closestNeighbour = neighbour;
			}
		}
		return closestNeighbour;
	}

	/**
	 * Gives the source and the destination from an index from our matrix
	 * construction
	 * 
	 * @param index
	 * @param size
	 * 
	 *            Construction :
	 * 
	 *            From an array with entries: city 0 -> city, 1 city 0 -> city 2,
	 *            ... city 0 -> null, city 1 -> city 0, city 1 -> city 2, ... city n -> city n-1
	 * 
	 * @return the source and destination corresponding to the index
	 */
	public Integer[] cityAndTaskFromIndex(int index, int size) {
		int source = (int) Math.floor(index / size);
		Integer destination = (index + source) % (size + 1);
		if (destination >= source) {
			destination = (destination + 1) % (size + 1);
		}
		if (destination == size) {
			destination = null;
		}
		return new Integer[] { source, destination };
	}

	/**
	 * Gives the index from a source and a destination in our matrix
	 * construction
	 * 
	 * Construction :
	 * 
	 * From an array with entries: city 0 -> city, 1 city 0 -> city 2, ... city 0 -> null, 
	 * city 1 -> city, 0 city 1 -> city 2, ... city n -> city n-1
	 * 
	 * @param citySource
	 * @param taskDestination
	 * @param numberOfCities
	 * @return
	 */
	public int indexFromCityAndTask(int citySource, int taskDestination, int numberOfCities) {
		int startIndexCitySource = citySource * (numberOfCities);
		int returnedIndex = startIndexCitySource + taskDestination;
		if (citySource > taskDestination) {
			// If the source id is bigger than the destination id,
			// we must remove 1 because the element city i -> city i doesn't
			// exist
			returnedIndex--;
		}
		return returnedIndex;
	}

	/**
	 * Returns the distance between 2 cities
	 * 
	 * @param cityA
	 * @param cityB
	 * @return distance between 2 cities
	 */
	public double distanceBetween(List<City> cities, int cityA, int cityB) {
		return cities.get(cityA).distanceTo(cities.get(cityB));
	}

	/**
	 * Returns a boolean that indicates if two cities are closest neighbours
	 * 
	 * @param cityA
	 * @param cityB
	 * @return boolean that indicates if two cities are closest neighbours
	 */
	protected boolean areClosestNeighbours(int cityA, int cityB) {
		return cities.get(cityB).equals(closestNeighbour(cities.get(cityA)));
	}

	/****** The following functions are used for debugging purpose *******/

	@SuppressWarnings("unused")
	protected void printMatrix(Number[][] matrix, int sizeX, int sizeY) {
		int i = 0;
		int j = 0;
		for (i = 0; i < sizeX; i++) {
			for (j = 0; j < sizeY; j++) {
				System.out.print(matrix[i][j] + " ");
			}
			System.out.println("");
		}
	}

	@SuppressWarnings("unused")
	protected void printTas(City[][] cities, int sizeX, int sizeY) {
		int i = 0;
		int j = 0;
		for (i = 0; i < sizeX; i++) {
			for (j = 0; j < sizeY; j++) {
				System.out.print(cities[i][j].name + " ");
			}
			System.out.println("");
		}
	}
	
	protected Double[][] getP() {
		return p;
	}

	protected void setP(Double[][] p) {
		this.p = p;
	}

	protected Integer[][] getSmallR() {
		return r;
	}

	protected void setSmallR(Integer[][] r) {
		this.r = r;
	}

	protected List<City> getCities() {
		return cities;
	}

	protected void setCities(List<City> cities) {
		this.cities = cities;
	}

	protected int getNumCities() {
		return numCities;
	}

	protected void setNumCities(int numCities) {
		this.numCities = numCities;
	}

	protected int getNumStates() {
		return numStates;
	}

	protected void setNumStates(int numStates) {
		this.numStates = numStates;
	}

	protected int getNumActions() {
		return numActions;
	}

	protected void setNumActions(int numActions) {
		this.numActions = numActions;
	}

	protected double[] getV() {
		return V;
	}

	protected void setV(double[] v) {
		V = v;
	}

	protected Double getGeneralReward() {
		return generalReward;
	}

	protected void setGeneralReward(Double generalReward) {
		this.generalReward = generalReward;
	}

	protected int getNbOfActions() {
		return nbOfActions;
	}

	protected void setNbOfActions(int nbOfActions) {
		this.nbOfActions = nbOfActions;
	}

	protected int[] getBest() {
		return Best;
	}

	protected void setBest(int[] best) {
		Best = best;
	}

	protected Double getDiscount() {
		return discount;
	}

	protected void setDiscount(Double discount) {
		this.discount = discount;
	}

	protected Double[][] getR() {
		return R;
	}

	protected void setR(Double[][] r) {
		R = r;
	}

	protected static double getEpsilon() {
		return EPSILON;
	}

}
