package template;

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

	private Double[][] p;
	private Integer[][] r;
	List<City> cities;
	private int numCities;
	private int numStates;
	private int numActions;
	private Random random;
	private double pPickup;
	Double R[][];
	Double generalReward = 0.0;

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {

		// Reads the discount factor from the agents.xml file.
		// If the property is not present it defaults to 0.95
		Double discount = agent.readProperty("discount-factor", Double.class, 0.95);

		this.random = new Random();
		this.pPickup = discount;

		cities = topology.cities();
		numCities = cities.size();

		numStates = numCities * (numCities - 1);
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
			int sd[] = ReactiveAgent.sourceAndDestinationFromIndex(i, numCities);
			R[i][0] = -ReactiveAgent.distanceBetween(cities, sd[0], sd[1]) * vehicle.costPerKm();
		}

		// Otherwise, we take the reward from matrix r minus the travel cost
		for (int i = 0; i < numStates; i++) {
			int sd[] = ReactiveAgent.sourceAndDestinationFromIndex(i, numCities);
			R[i][1] = r[sd[0]][sd[1]] - ReactiveAgent.distanceBetween(cities, sd[0], sd[1]) * vehicle.costPerKm();
		}
		/*****************************************************/

		System.out.println("Random Agent " + agent.id() + " (vehicle " + agent.vehicles().get(0).name() + ") "
				+ " with lambda=" + discount);
	}

	@Override
	public Action act(Vehicle vehicle, Task availableTask) {
		Action action;
		City currentCity = vehicle.getCurrentCity();
		int indexBest;

		if (availableTask == null || random.nextDouble() > pPickup) {
			City next = currentCity.randomNeighbor(random);
			indexBest = ReactiveAgent.indexFromSourceAndDestination(currentCity.id, next.id, numCities);
			System.out.println(
					vehicle.name() + " there is no task from " + currentCity + ". Benefit : " + R[indexBest][0]);
			action = new Move(next);
			generalReward += R[indexBest][0];
		} else {
			indexBest = ReactiveAgent.indexFromSourceAndDestination(currentCity.id, availableTask.deliveryCity.id,
					numCities);
			System.out.println(vehicle.name() + " takes the task from " + availableTask.pickupCity + " to "
					+ availableTask.deliveryCity + ". Benefit : " + R[indexBest][1]);
			action = new Pickup(availableTask);
			generalReward += R[indexBest][1];
		}
		System.out.println("Random Agent, vehicle : " + vehicle.name() + ", general reward : " + generalReward);
		return action;
	}
}
