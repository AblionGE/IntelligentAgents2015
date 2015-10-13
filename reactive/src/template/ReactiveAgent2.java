package template;

import java.util.Iterator;
import java.util.List;

import logist.agent.Agent;
import logist.behavior.ReactiveBehavior;
import logist.plan.Action;
import logist.plan.Action.Move;
import logist.plan.Action.Pickup;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.topology.Topology;
import logist.topology.Topology.City;

// Reactive agent will state containing null tasks
public class ReactiveAgent2 implements ReactiveBehavior {

	private Double[][] p;
	private Integer[][] r;
	static private List<City> cities;
	private int numCities;
	private static final double EPSILON = 0.0001;
	private int numStates;
	private int numActions;
	double[] V;
	private Double generalReward = 0.0;
	private int nbOfActions = 0;
	int[] Best;
	Double discount;
	Double R[][];


	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {

		// Reads the discount factor from the agents.xml file.
		// If the property is not present it defaults to 0.95
		discount = agent.readProperty("discount-factor", Double.class, 0.95);

		cities = topology.cities();
		numCities = cities.size();

		numStates = numCities * numCities;
		numActions = 2;

		/*************** Matrices r and p **********************/
		Double[] pTask = new Double[numCities];
		// Set matrices r (rewards) and p (probability to have a task from city1
		// to city2)
		int idC1 = 0;
		int idC2 = 0;
		r = new Integer[numCities][numCities];
		p = new Double[numCities][numCities];
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

		/*****************************************************/

		/*********************** Matrix R(s,a) *****************/
		R = new Double[numStates][numActions];

		// One agent has only one vehicle
		Vehicle vehicle = agent.vehicles().get(0);

		// When the action is to move without taking the task
		// the reward is -distance*(cost/km).
		for (int i = 0; i < numStates; i++) {
			Integer ct[] = cityAndTaskFromIndex(i, numCities);
			if(ct[1] != null) {
				R[i][0] = -distanceBetween(cities, ct[0], ct[1]) * vehicle.costPerKm();
			} else {
				City A = cities.get(ct[0]);
				City nNeighbour = closestNeighbour(A);
				R[i][0] = -A.distanceTo(nNeighbour) * vehicle.costPerKm();
			}
		}

		// Otherwise, we take the reward from matrix r minus the travel cost
		for (int i = 0; i < numStates; i++) {
			Integer ct[] = cityAndTaskFromIndex(i, numCities);
			if(ct[1] != null) {
				R[i][1] = r[ct[0]][ct[1]] - distanceBetween(cities, ct[0], ct[1]) * vehicle.costPerKm();
			} else {
				// Should not be possible to deliver a task when there is none
				R[i][1] = -Double.MAX_VALUE;
			}
		}
		/*****************************************************/

		/*********************** Matrix T(s,a,s') **************/
		Double T[][][] = new Double[numStates][2][numStates];

		// When the action is to move without taking the task
		for (int i = 0; i < numStates; i++) {
			Integer ctA[] = cityAndTaskFromIndex(i, numCities);
			for (int j = 0; j < numStates; j++) {
				Integer ctB[] = cityAndTaskFromIndex(j, numCities);
				
				// Probability is non zero only when B is the nearest neighbour of A
				if(ctA[0]!=ctB[0] && areClosestNeighbours(ctA[0], ctB[0])) {
					if(ctB[1] == null) {
						// Probability that task at B is null
						T[i][0][j] = (1-pTask[ctB[0]]);
					} else {
						T[i][0][j] = p[ctB[0]][ctB[1]];
					}
				} else {
					T[i][0][j] = new Double(0);
				}
			}
		}

		// When the action is to deliver the task
		for (int i = 0; i < numStates; i++) {
			Integer ctA[] = cityAndTaskFromIndex(i, numCities);
			for (int j = 0; j < numStates; j++) {
				Integer ctB[] = cityAndTaskFromIndex(j, numCities);

				if (ctA[0]!=ctB[0] && ctA[1] == ctB[0]) {
					if(ctB[1] == null) {
						T[i][1][j] = (1-pTask[ctB[0]]);
					} else {
						T[i][1][j] = p[ctB[0]][ctB[1]];
					}
				} else {
					T[i][1][j] = 0.0;
				}
			}
		}
		/*****************************************************/

		/******************** Compute V(S) and Best(S) *********/
		V = new double[numStates];
		Best = new int[numStates];
		double[] oldV = new double[numStates];

		// Initialise V
		for (int i = 0; i < numStates; i++) {
			V[i] = 0.0;
			oldV[i] = 1.0;
		}

		double[][] Q = new double[numStates][numActions];

		int loops = 0;
		while (computeDifference(oldV, V) > EPSILON) {
			oldV = V.clone();
			for (int i = 0; i < numStates; i++) {
				for (int j = 0; j < numActions; j++) {
					// sum over s'
					double TsaV = 0;
					for (int k = 0; k < numStates; k++) {
						TsaV = TsaV + (T[i][j][k] * oldV[k]);
					}

					Q[i][j] = R[i][j] + discount * TsaV;
				}
				double[] best = max(Q[i]);
				V[i] = best[0];
				Best[i] = (int) best[1];
			}
			loops++;
		}

		System.out.println("Number of loops: " + loops);
		System.out.println("Reactive Agent " + agent.id() + " (vehicle " + agent.vehicles().get(0).name()
				+ ") with lambda=" + discount + "\nBest(x) = 0 means move without the task");
		for (int i = 0; i < numStates; i++) {
			if (Best[i] == 0) {
				System.out.println("V[" + i + "] : " + V[i] + ", Best[" + i + "] : " + Best[i]);
			}
		}
		/*****************************************************/
	}

	@Override
	public Action act(Vehicle vehicle, Task availableTask) {
		Action action;
		City currentCity = vehicle.getCurrentCity();
		int indexBest;

		if (availableTask == null) {
			// If the task is null, move to the closest neighbour
			indexBest = indexFromCityAndTask(currentCity.id, closestNeighbour(currentCity).id, numCities);
			System.out.println(
					vehicle.name() + " there is no task from " + currentCity + ". Benefit : " + R[indexBest][0]);
			action = new Move(closestNeighbour(currentCity));
			generalReward += R[indexBest][0];
		} else {
			indexBest = indexFromCityAndTask(currentCity.id, availableTask.deliveryCity.id, numCities);
			if (Best[indexBest] == 0) {
				// If the best solution is to move, move to the closest
				// neighbour
				System.out.println(vehicle.name() + " does not take the task from " + availableTask.pickupCity + " to "
						+ availableTask.deliveryCity + ". Benefit : " + R[indexBest][0]);
				action = new Move(closestNeighbour(currentCity));
				generalReward += R[indexBest][0];
			} else {
				// else pickup the task
				System.out.println(vehicle.name() + " takes the task from " + availableTask.pickupCity + " to "
						+ availableTask.deliveryCity + ". Benefit : " + R[indexBest][1]);
				action = new Pickup(availableTask);
				generalReward += R[indexBest][1];
			}
		}
		nbOfActions++;
		System.out.println("Reactive Agent, vehicle : " + vehicle.name() + ", general reward : " + generalReward);
		System.out.println("Reactive Agent, vehicle : " + vehicle.name() + ", average reward : " + generalReward/nbOfActions);
		return action;
	}

	/**
	 * Gives the maximum value of a vector with its index
	 * 
	 * @param vector
	 * @return max value of a vector with its index
	 */
	private double[] max(double vector[]) {
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
	private double computeDifference(double[] oldV, double[] newV) {
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
	private City closestNeighbour(City city) {
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
	 *            From an array with entries: city 0 -> city 1 city 0 -> city 2
	 *            ... city 1 -> city 0 city 1 -> city 2 ... city n -> city n-1
	 * 
	 * @return the source and destination corresponding to the index
	 */
	public static Integer[] cityAndTaskFromIndex(int index, int size) {
		int source = (int) Math.floor(index / size);
		Integer destination = (index + source) % (size+1);
		if (destination >= source) {
			destination = (destination + 1) % (size+1);
		}
		if (destination==size) {
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
	 * From an array with entries: city 0 -> city 1 city 0 -> city 2 ... city 1
	 * -> city 0 city 1 -> city 2 ... city n -> city n-1
	 * 
	 * @param citySource
	 * @param cityDestination
	 * @param numberOfCities
	 * @return
	 */
	public static int indexFromCityAndTask(int citySource, int cityDestination, int numberOfCities) {
		int startIndexCitySource = citySource * (numberOfCities - 1);
		int returnedIndex = startIndexCitySource + cityDestination;
		if (citySource > cityDestination) {
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
	public static double distanceBetween(List<City> cities, int cityA, int cityB) {
		return cities.get(cityA).distanceTo(cities.get(cityB));
	}

	/**
	 * Returns a boolean that indicates if two cities are closest neighbours
	 * 
	 * @param cityA
	 * @param cityB
	 * @return boolean that indicates if two cities are closest neighbours
	 */
	private boolean areClosestNeighbours(int cityA, int cityB) {
		return cities.get(cityB).equals(closestNeighbour(cities.get(cityA)));
	}

	/****** The following functions are used for debugging purpose *******/

	@SuppressWarnings("unused")
	private void printMatrix(Number[][] matrix, int sizeX, int sizeY) {
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
	private void printTas(City[][] cities, int sizeX, int sizeY) {
		int i = 0;
		int j = 0;
		for (i = 0; i < sizeX; i++) {
			for (j = 0; j < sizeY; j++) {
				System.out.print(cities[i][j].name + " ");
			}
			System.out.println("");
		}
	}
}