package template;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

import logist.simulation.Vehicle;
import logist.agent.Agent;
import logist.behavior.ReactiveBehavior;
import logist.plan.Action;
import logist.plan.Action.Move;
import logist.plan.Action.Pickup;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.topology.Topology;
import logist.topology.Topology.City;

public class ReactiveTemplate implements ReactiveBehavior {

	private Random random;
	private double pPickup;
	private Double[][] p;
	private Integer[][] r;
	List<City> cities;
	private int numCities;
	private static final double EPSILON = 0.0001;
	private int numStates;
	private int numActions;
	private double gamma = 0.5;
	double[] V;

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {

		// Reads the discount factor from the agents.xml file.
		// If the property is not present it defaults to 0.95
		Double discount = agent.readProperty("discount-factor", Double.class, 0.95);

		this.random = new Random();
		this.pPickup = discount;
		cities = topology.cities();
		numCities = cities.size();
		
		numStates = numCities*(numCities-1);
		numActions = 2;

		/***************Matrices r and p**********************/
		// Set matrices r (rewards) and p (probability to have a task from city1 to city2)
		int idC1 = 0;
		int idC2 = 0;
		r = new Integer[numCities][numCities];
		p = new Double[numCities][numCities];
		for (City c1 : cities) {
			for (City c2 : cities) {
				r[idC1][idC2] = td.reward(c1, c2);
				p[idC1][idC2] = td.probability(c1, c2);
				idC2++;
			}
			idC1++;
			idC2 = 0;
		}
		//FIXME : to remove
		//printMatrix(r, numCities, numCities);
		/*****************************************************/

		/***********************Matrix R(s,a)*****************/
		Double R[][] = new Double[numActions][numStates];

		// When the action is to move without taking the task
		// the reward is -distance.
		for (int i = 0; i < numStates; i++) {
			int sd[] = sourceAndDestinationFromIndex(i, numCities);
			R[0][i] = -distanceBetween(sd[0], sd[1]);
		}

		// Otherwise, we take the reward from matrix r minus the distance
		for (int i = 0; i < numStates; i++) {
			int sd[] = sourceAndDestinationFromIndex(i, numCities);
			R[1][i] = r[sd[0]][sd[1]] - distanceBetween(sd[0], sd[1]);
		}
		//FIXME: to remove
		//printMatrix(R, numCities*(numCities-1), 2);
		/*****************************************************/

		/***********************Matrix T(s,a,s')**************/
		Double T[][][] = new Double[numStates][2][numStates];

		// When the action is to move without taking the task
		for (int i = 0; i < numStates; i++) {
			for (int j = 0; j < numStates; j++) {
				int sdA[] = sourceAndDestinationFromIndex(i, numCities);
				int sdB[] = sourceAndDestinationFromIndex(j, numCities);

				if(sdA[1] == sdB[0] && areClosestNeighbour(sdA[0], sdA[1])) {
					T[i][0][j] = new Double(1);
				} else {
					T[i][0][j] = new Double(0);
				}
			}
		}

		// When the action is to deliver the task
		for (int i = 0; i < numStates; i++) {
			for (int j = 0; j < numStates; j++) {
				int sdA[] = sourceAndDestinationFromIndex(i, numCities);
				int sdB[] = sourceAndDestinationFromIndex(j, numCities);

				if(sdA[1] == sdB[0]) {
					T[i][1][j] = p[sdA[0]][sdA[1]];
				} else {
					T[i][1][j] = new Double(0);
				}
			}
		}
		/*****************************************************/
		
		/********************Compute V(S)*********************/
		V = new double[numStates];
		double[] oldV = new double[numStates];
		
		// Initialize V
		for (int i = 0; i < numStates ; i++) {
			V[i] = 0.0;
			oldV[i] = 1.0;
		}
		
		double[][] Q = new double[numStates][numActions];
		
		while (computeDifference(oldV, V) > EPSILON) {
			oldV = V;
			for (int i = 0; i < numStates; i++) {
				for (int j = 0; j < numActions; j++) {
					//sum over s'
					double TsaV = 0;
					for (int k = 0; k < numStates; k++) {
						TsaV = TsaV + (T[i][j][k]*oldV[k]);
					}
					
					Q[i][j] = R[j][i] + gamma * TsaV;
				}
				V[i] = max(Q[i]);
			}
		}
		/*****************************************************/
	}

	@Override
	public Action act(Vehicle vehicle, Task availableTask) {
		Action action;

		if (availableTask == null || random.nextDouble() > pPickup) {
			City currentCity = vehicle.getCurrentCity();
			action = new Move(currentCity.randomNeighbor(random));
		} else {
			// Here we need to decide if the task should be taken or not.
			action = new Pickup(availableTask);
			System.out.println("pickup : " + availableTask.pickupCity.name);
			System.out.println("delivery : " + availableTask.deliveryCity.name);
		}
		return action;
	}
	
	private double max(double vector[]) {
		double max = -1;
		for (int i = 0; i < vector.length; i++) {
			if (vector[i] > max) {
				max = vector[i];
			}
		}
		return max;
		
	}
	private double computeDifference(double[] oldV, double[] newV) {
		double max = new Double(-1);
		for (int i = 0; i < oldV.length; i++) {
			if (Math.abs(oldV[i] - newV[i]) < max || max == -1) {
				max = Math.abs(oldV[i] - newV[i]);
			}
		}
		return max;
	}

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

	// From an array with entries:
	//  city 0 -> city 1
	//  city 0 -> city 2
	//  ...
	//  city n -> city n-1
	// Returns the source and destination corresponding to the index
	private int[] sourceAndDestinationFromIndex(int index, int size) {
		int source = (int) Math.floor(index/(size-1));
		int destination = (index+source)%size;
		if(destination >= source) {
			destination = (destination+1)%size;
		}
		return new int[]{source, destination};
	}

	private double distanceBetween(int cityA, int cityB) {
		return cities.get(cityA).distanceTo(cities.get(cityB));
	}

	private boolean areClosestNeighbour(int cityA, int cityB) {
		return cities.get(cityA).equals(closestNeighbour(cities.get(cityB)));
	}

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
