package template;

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

public class RandomAgent extends ReactiveAbstractAgent implements ReactiveBehavior {

	private Random random;
	private double pPickup;

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {

		super.setup(topology, td, agent);

		this.random = new Random();
		this.pPickup = discount;
		System.out.println("Random Agent " + agent.id() + " (vehicle " + agent.vehicles().get(0).name() + ")");
	}

	@Override
	public Action act(Vehicle vehicle, Task availableTask) {
		Action action;
		City currentCity = vehicle.getCurrentCity();
		int indexBest;

		if (availableTask == null || random.nextDouble() > pPickup) {
			City next = currentCity.randomNeighbor(random);
			indexBest = indexFromCityAndTask(currentCity.id, next.id, numCities);
			System.out.println(
					vehicle.name() + " there is no task from " + currentCity + ". Benefit : " + R[indexBest][0]);
			action = new Move(next);
			generalReward += R[indexBest][0];
		} else {
			indexBest = indexFromCityAndTask(currentCity.id, availableTask.deliveryCity.id, numCities);
			System.out.println(vehicle.name() + " takes the task from " + availableTask.pickupCity + " to "
					+ availableTask.deliveryCity + ". Benefit : " + R[indexBest][1]);
			action = new Pickup(availableTask);
			generalReward += R[indexBest][1];
		}
		nbOfActions++;
		//System.out.println("Random Agent, vehicle : " + vehicle.name() + ", general reward : " + generalReward);
		System.out.println(
				"Random Agent, vehicle : " + vehicle.name() + ", average reward : " + generalReward / nbOfActions);
		System.out.println("nbOfActions Random : " + nbOfActions);
		return action;
	}
}
