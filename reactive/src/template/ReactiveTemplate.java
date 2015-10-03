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

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {

		// Reads the discount factor from the agents.xml file.
		// If the property is not present it defaults to 0.95
		Double discount = agent.readProperty("discount-factor", Double.class, 0.95);

		this.random = new Random();
		this.pPickup = discount;

		/***************Matrices r and p**********************/
		// Set matrices r (rewards) and p (probability to have a task from city1 to city2)
		List<City> cities = topology.cities();
		int idC1 = 0;
		int idC2 = 0;
		r = new Integer[cities.size()+1][cities.size()+1];
		p = new Double[cities.size()+1][cities.size()+1];
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
		//printMatrix(r, cities.size(), cities.size());
		/*****************************************************/
		
		
		/*************Transition matrix T(s,a)****************/
		//FIXME : I think this matrix is useless...
		
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
		//printTas(Tsa, cities.size(), cities.size());
		/*****************************************************/
		
		/***********************Matrix Rsa********************/
		Integer R[][] = new Integer[cities.size()][cities.size()];
		
		// When the action is to move without taking the task
		// the reward is 0.
		for (int i = 0; i < cities.size(); i++) {
			R[i][0] = 0;
		}
		
		// Otherwise, we take the reward from matrix r
		for (int i = 0; i < cities.size(); i++) {
			for (int j = 1; j < cities.size(); j++) {
				R[i][j] = r[i][(i+j)%cities.size()];
			}
		}
		printMatrix(R, cities.size(), cities.size());
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
