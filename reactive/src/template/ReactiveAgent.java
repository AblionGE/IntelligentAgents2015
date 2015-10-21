package template;

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

public class ReactiveAgent extends ReactiveAbstractAgent implements ReactiveBehavior {

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {

		super.setup(topology, td, agent);

		T = computeT();

		computeVAndBest();
		
		System.out.println("Reactive Agent " + agent.id() + " (vehicle " + agent.vehicles().get(0).name()
				+ ") with lambda=" + discount + "\nBest(x) = 0 means move without the task");
		for (int i = 0; i < numStates; i++) {
			if (Best[i] == 0) {
				System.out.println("V[" + i + "] : " + V[i] + ", Best[" + i + "] : " + Best[i]);
			}
		}
	}

	@Override
	public Action act(Vehicle vehicle, Task availableTask) {
		Action action;
		City currentCity = vehicle.getCurrentCity();
		int indexBest;

		if (availableTask == null) {
			// If the task is null, move to the closest neighbour
			indexBest = indexFromCityAndTask(currentCity.id, null, numCities);
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
		//System.out.println("Reactive Agent, vehicle : " + vehicle.name() + ", general reward : " + generalReward);
		System.out.println(
				"Reactive Agent, vehicle : " + vehicle.name() + ", average reward : " + generalReward / nbOfActions);
		System.out.println("nbOfActions Reactive : " + nbOfActions);
		return action;
	}
	
	/**
	 * Compute the matrix T(s,a,s')
	 * @return T(s,a,s')
	 */
	private Double[][][] computeT() {
		Double T[][][] = new Double[numStates][2][numStates];

		// When the action is to move without taking the task
		for (int i = 0; i < numStates; i++) {
			Integer ctA[] = cityAndTaskFromIndex(i, numCities);
			for (int j = 0; j < numStates; j++) {
				Integer ctB[] = cityAndTaskFromIndex(j, numCities);

				// Probability is non zero only when B is the nearest neighbour
				// of A
				if (ctA[0] != ctB[0] && areClosestNeighbours(ctA[0], ctB[0])) {
					if (ctB[1] == null) {
						// Probability that task at B is null
						T[i][0][j] = (1 - pTask[ctB[0]]);
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

				// Probability is non zero only when task from A goes for B
				if (ctA[0] != ctB[0] && ctA[1] == ctB[0]) {
					if (ctB[1] == null) {
						T[i][1][j] = (1 - pTask[ctB[0]]);
					} else {
						T[i][1][j] = p[ctB[0]][ctB[1]];
					}
				} else {
					T[i][1][j] = 0.0;
				}
			}
		}

		return T;
	}

	/**
	 * Compute V(S) and Best(S)
	 */
	private void computeVAndBest() {
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
	}
}