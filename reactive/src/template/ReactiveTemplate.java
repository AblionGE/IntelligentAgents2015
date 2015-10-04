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

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {

		// Reads the discount factor from the agents.xml file.
		// If the property is not present it defaults to 0.95
		Double discount = agent.readProperty("discount-factor", Double.class, 0.95);

		this.random = new Random();
		this.pPickup = discount;
		cities = topology.cities();
		numCities = cities.size();

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


		/*************Transition matrix T(s,a)****************/
		/*//FIXME : I think this matrix is useless... Me too

		// 						C1	C2	... Cn
		//noDeliver
		//DeliverTo (C+1)%size
		//...
		//DeliverTo (C+n)%size
		City[][] Tsa = new City[cities.size()][cities.size()];
		// First line of the matrix (closer city)
		for (int i = 0; i < cities.size(); i++) {
			Tsa[0][i] = closestNeighbour(cities.get(i));
		}

		// Rest of the matrix
		for (int i = 1; i < cities.size(); i++) {
			for (int j = 0; j < cities.size(); j++) {
				Tsa[i][j] = cities.get((j+i)%cities.size());
			}
		}
		//FIXME : to remove
		//printTas(Tsa, cities.size(), cities.size());*/
		/*****************************************************/

		/***********************Matrix R(s,a)*****************/
		Double R[][] = new Double[numCities*(numCities-1)][2];

		// When the action is to move without taking the task
		// the reward is -distance.
		for (int i = 0; i < R.length; i++) {
			int sd[] = sourceAndDestinationFromIndex(i, numCities);
			R[i][0] = -distanceBetween(sd[0], sd[1]);
		}

		// Otherwise, we take the reward from matrix r minus the distance
		for (int i = 0; i < R.length; i++) {
			int sd[] = sourceAndDestinationFromIndex(i, numCities);
			R[i][1] = r[sd[0]][sd[1]] - distanceBetween(sd[0], sd[1]);
		}
		//FIXME: to remove
		//printMatrix(R, numCities*(numCities-1), 2);
		/*****************************************************/

		/***********************Matrix T(s,a,s')**************/
		Double T[][][] = new Double[numCities*(numCities-1)][2][numCities*(numCities-1)];

		// When the action is to move without taking the task
		for (int i = 0; i < T.length; i++) {
			for (int j = 0; j < T.length; j++) {
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
		for (int i = 0; i < T.length; i++) {
			for (int j = 0; j < T.length; j++) {
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
