package template;

//the list of imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import logist.LogistPlatform;
import logist.LogistSettings;

import logist.Measures;
import logist.behavior.AuctionBehavior;
import logist.behavior.CentralizedBehavior;
import logist.agent.Agent;
import logist.config.Parsers;
import logist.simulation.Vehicle;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;

/**
 * 
 *
 */
@SuppressWarnings("unused")
public class CentralizedTemplate implements CentralizedBehavior {

	private final double SLS_PROBABILITY = 0.5;
	private final int MAX_SLS_LOOPS = 1000;
	private final int MAX_SLS_STATE_REPETITION = 50;

	private Topology topology;
	private TaskDistribution distribution;
	private Agent agent;
	private long timeout_setup;
	private long timeout_plan;

	@Override
	public void setup(Topology topology, TaskDistribution distribution, Agent agent) {

		// this code is used to get the timeouts
		LogistSettings ls = null;
		try {
			ls = LogistPlatform.getSettings();
		} catch (Exception exc) {
			System.out.println("There was a problem loading the configuration file.");
		}

		// the setup method cannot last more than timeout_setup milliseconds
		timeout_setup = ls.get(LogistSettings.TimeoutKey.SETUP);
		// the plan method cannot execute more than timeout_plan milliseconds
		timeout_plan = ls.get(LogistSettings.TimeoutKey.PLAN);

		this.topology = topology;
		this.distribution = distribution;
		this.agent = agent;
	}

	@Override
	public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
		long time_start = System.currentTimeMillis();
		List<Plan> plans = new ArrayList<Plan>();

		// Compute the centralized plan
		HashMap<Vehicle, LinkedList<Movement>> vehiclePlans = computeSLS(vehicles, tasks);

		// System.out.println("Agent " + agent.id() + " has tasks " + tasks);
		// Plan planVehicle1 = individualVehiclePlan(vehicles.get(0), tasks);
		for (Vehicle vehicle : vehicles) {
			LinkedList<Movement> movements = vehiclePlans.get(vehicle);
			Plan plan = individualVehiclePlan(vehicle, movements);
			plans.add(plan);
		}
		/*
		 * while (plans.size() < vehicles.size()) { plans.add(Plan.EMPTY); }
		 */

		long time_end = System.currentTimeMillis();
		long duration = time_end - time_start;
		System.out.println("The plan was generated in " + duration + " milliseconds.");

		return plans;
	}

	/**
	 * Compute the plan for one vehicle
	 * 
	 * @param vehicle
	 * @param movements
	 *            an orderd list of movements to perform
	 * @return
	 */
	private Plan individualVehiclePlan(Vehicle vehicle, LinkedList<Movement> movements) {
		City current = vehicle.getCurrentCity();
		Plan plan = new Plan(current);

		// If vehicle has nothing to do
		if (movements == null) {
			return Plan.EMPTY;
		}

		for (Movement a : movements) {
			// move: current city => pickup location
			if (a.getAction() == Action.PICKUP) {
				for (City city : current.pathTo(a.getTask().pickupCity)) {
					plan.appendMove(city);
				}
				plan.appendPickup(a.getTask());
				current = a.getTask().pickupCity;
			}

			// move: pickup location => delivery location
			if (a.getAction() == Action.DELIVER) {
				for (City city : current.pathTo(a.getTask().deliveryCity)) {
					plan.appendMove(city);
				}
				plan.appendDelivery(a.getTask());
				current = a.getTask().deliveryCity;
			}
		}
		return plan;
	}

	/**
	 * This function computes the plan for all vehicle of one company.
	 * 
	 * @param vehicles
	 * @param tasks
	 */
	private HashMap<Vehicle, LinkedList<Movement>> computeSLS(List<Vehicle> vehicles, TaskSet tasks) {
		SolutionState bestState;
		SolutionState oldState;

		double p = SLS_PROBABILITY;
		int maxLoop = MAX_SLS_LOOPS;
		int maxStateRepetition = MAX_SLS_STATE_REPETITION;
		int currentLoop = 0;
		int stateRepetition = 0;
		bestState = computeInitState(vehicles, tasks);

		while (stateRepetition < maxStateRepetition && currentLoop < maxLoop) {
			currentLoop++;
			System.out.println(currentLoop + ", cost : " + bestState.getCost());
			// System.out.println(bestState.toString());
			oldState = bestState;

			Random random = new Random();
			int r = random.nextInt(100);
			ArrayList<SolutionState> neighbours = null;
			if (r <= p * 100) {
				neighbours = chooseNeighbours(bestState, vehicles);
				if (neighbours == null) {
					currentLoop = maxLoop;
					System.out.println("No more promissing neighbours");
				}
				bestState = localChoice(oldState, neighbours);
			}

			if (bestState.equals(oldState)) {
				stateRepetition++;
			} else {
				stateRepetition = 0;
			}
		}

		System.out.println(" ======================================================== ");
		System.out.println("Number of loops in SLS: " + currentLoop);
		System.out.println("Expected cost: " + bestState.getCost());
		System.out.println("Best " + bestState.toString());
		return bestState.getPlans();
	}

	/**
	 * Each vehicle has some tasks and they are picked up and delivered one
	 * after the other.
	 * 
	 * @param vehicles
	 * @param tasks
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private SolutionState computeInitState(List<Vehicle> vehicles, TaskSet tasks) {
		HashMap<Vehicle, ArrayList<Task>> distributedTasks = new HashMap<Vehicle, ArrayList<Task>>();
		ArrayList<Vehicle> arrayOfVehicles = new ArrayList<Vehicle>(vehicles);
		ArrayList<Task> arrayOfTasks = new ArrayList<Task>(tasks);

		// Each task is assigned to one (different) vehicle (if it is possible)
		// And there will be only one task in a vehicle at a given time.
		for (int i = 0; i < arrayOfTasks.size(); i++) {
			Vehicle v = arrayOfVehicles.get(i % arrayOfVehicles.size());
			ArrayList<Task> oldTasksList = distributedTasks.get(v);
			if (oldTasksList == null) {
				oldTasksList = new ArrayList<Task>();
			}
			// If the task is too big for the vehicle
			boolean found = false;
			if (v.capacity() < arrayOfTasks.get(i).weight) {
				// we need to check if it fits into another vehicle
				for (int j = 0; j < arrayOfVehicles.size(); j++) {
					if (arrayOfTasks.get(i).weight <= arrayOfVehicles.get(j).capacity()) {
						v = arrayOfVehicles.get(j);
						j = arrayOfVehicles.size();
						found = true;
					}
				}
				if (!found) {
					System.err.println("No vehicle can carry task " + arrayOfTasks.get(i).toString());
				}
			}
			oldTasksList.add(arrayOfTasks.get(i));
			distributedTasks.put(v, (ArrayList<Task>) oldTasksList.clone());
		}

		HashMap<Movement, Movement> nextMovements = new HashMap<Movement, Movement>();
		HashMap<Vehicle, Movement> nextMovementsVehicle = new HashMap<Vehicle, Movement>();
		for (Vehicle v : vehicles) {
			ArrayList<Task> vTasks = distributedTasks.get(v);
			if (vTasks != null) {
				Movement firstMovement = new Movement(Action.PICKUP, vTasks.get(0));
				Movement previousMovement = firstMovement;
				nextMovementsVehicle.put(v, firstMovement);
				for (int i = 1; i < vTasks.size(); i++) {
					Movement nextDeliverMovement = new Movement(Action.DELIVER, vTasks.get(i - 1));
					nextMovements.put(previousMovement, nextDeliverMovement);
					Movement nextPickupMovement = new Movement(Action.PICKUP, vTasks.get(i));
					nextMovements.put(nextDeliverMovement, nextPickupMovement);
					previousMovement = nextPickupMovement;

				}
				Movement finalMovement = new Movement(Action.DELIVER, vTasks.get(vTasks.size() - 1));
				nextMovements.put(previousMovement, finalMovement);
			}
		}
		SolutionState solution = new SolutionState(nextMovements, nextMovementsVehicle);

		// Compute the cost of this plan a save it into the SolutionState object
		solution.getCost();
		int contraintsErrors = Constraints.checkSolutionState(solution);
		if (contraintsErrors != 0) {
			System.err.println("The initial solution does not respect " + contraintsErrors + " errors.");
		}

		return solution;
	}

	// algo from paper
	private ArrayList<SolutionState> chooseNeighbours(SolutionState oldState, List<Vehicle> vehicles) {
		ArrayList<SolutionState> neighbours = new ArrayList<SolutionState>();
		HashMap<Vehicle, Movement> nextMovementsVehicle = oldState.getNextMovementsVehicle();
		SolutionState ss;

		// pick a random vehicle
		Random ran = new Random();
		int x = ran.nextInt(vehicles.size());
		Vehicle vehicle = vehicles.get(x);
		while (nextMovementsVehicle.get(vehicle) == null) {
			x = ran.nextInt(vehicles.size());
			vehicle = vehicles.get(x);
		}

		// FIXME
		// works only if it was previous state
		// We need to store visited states somewhere... HashMap ?
		if (!oldState.getNeighbours().containsKey(vehicle)) {

			// apply changing vehicle operator:
			Movement m;
			for (Vehicle v : vehicles) {
				if (!v.equals(vehicle)) {
					m = nextMovementsVehicle.get(vehicle);
					if (m.getTask().weight < v.capacity()) {
						ss = changingVehicle(oldState, vehicle, v);
						if (Constraints.checkSolutionState(ss) == 0) {
							neighbours.add(ss);
						}
					}
				}
			}

			// apply changing task order operator:
			Movement pMov, dMov;
			LinkedList<Movement> plan = oldState.getPlans().get(vehicle);
			int size = plan.size();
			if (size > 2) {
				for (int k = 0; k < size - 1; k++) {
					// select a pickup movement
					pMov = plan.get(k);
					if (pMov.getAction() == Action.PICKUP) {
						// find the corresponding deliver movement
						dMov = plan.get(k + 1);
						int kk = k + 2;
						while (dMov.getTask().id != pMov.getTask().id && kk < size) {
							dMov = plan.get(kk);
							kk++;
						}
						if (dMov.getTask().id != pMov.getTask().id) {
							System.out.println(
									"Deliver not found for task " + pMov.getTask().id + " in changingTaskOrder.");
							dMov = null;
						}

						// all permutations:
						if (dMov != null) {
							for (int i = 0; i < size - 1; i++) {
								for (int j = i + 1; j < size; j++) {
									if (i != k || j != kk - 1) {
										ss = changingTaskOrder(oldState, vehicle, k, kk - 1, i, j);
										if (ss != null) {
											if (Constraints.checkSolutionState(ss) == 0) {
												neighbours.add(ss);
											}
										}
									}
								}
							}
						}
					}
				}
			}
			oldState.getNeighbours().put(vehicle, neighbours);
		} else {
			neighbours = oldState.getNeighbours().get(vehicle);
		}

		return neighbours;
	}

	/**
	 * Choose the next best solution
	 * 
	 * @param old
	 * @param neighbours
	 * @param probability
	 * @return
	 */
	private SolutionState localChoice(SolutionState old, ArrayList<SolutionState> neighbours) {
		SolutionState bestSolution;

		if (neighbours == null || neighbours.size() == 0) {
			System.err.println("Neighbours should not be null !");
			return old;
		}

		double bestCost = old.getCost();
		ArrayList<SolutionState> bestSolutions = new ArrayList<SolutionState>();

		double cost;
		for (SolutionState neighbour : neighbours) {
			cost = neighbour.getCost();
			if (cost <= bestCost) {
				if (cost < bestCost) {
					bestSolutions.clear();
					bestCost = cost;
				}
				bestSolutions.add(neighbour);
			}
		}

		if (bestSolutions.size() == 0) {
			return old;
		}

		if (bestSolutions.size() > 1) {
			// random number to select a best solution
			Random ran = new Random();
			int x = ran.nextInt(bestSolutions.size());
			bestSolution = bestSolutions.get(x);
		} else {
			bestSolution = bestSolutions.get(0);
		}

		return bestSolution;
	}

	/**
	 * Remove a task from the plan of vehicle v1 Append the task at the
	 * beginning of the vehicle v2
	 * 
	 * @param oldState
	 * @param v1
	 * @param v2
	 * @return state following from this exchange
	 */
	private SolutionState changingVehicle(SolutionState oldState, Vehicle v1, Vehicle v2) {
		HashMap<Vehicle, Movement> nextMovementsVehicle = oldState.getNextMovementsVehicle();
		HashMap<Movement, Movement> nextMovements = oldState.getNextMovements();

		Movement m1 = nextMovementsVehicle.get(v1);
		Movement m1Deliver = null;
		Movement m2 = nextMovementsVehicle.get(v2);

		Movement m1Next = nextMovements.get(m1);
		if (m1Next.getAction() == Action.DELIVER) {
			// deliver m1 right after having it picked up
			m1Deliver = m1Next;
			m1Next = nextMovements.get(m1Next);
		} else {
			// deliver m1 after pickup other tasks
			Movement prev = m1Next;
			Movement current = nextMovements.get(m1Next);
			while (current != null && !current.getTask().equals(m1.getTask())) {
				prev = current;
				current = nextMovements.get(current);
			}
			if (current != null) {
				// remove delivery of m1 from v1's plan
				Movement next = nextMovements.get(current);
				nextMovements.put(prev, next);
				m1Deliver = current;
			} else {
				System.out.println("Deliver not found for task " + m1.getTask().id + " in changingVehicle().");
			}
		}

		// remove m1 from the plan for vehicle v1
		// append m1 to the beginning of the plan for vehicle v2
		if (m1Next != null) {
			nextMovementsVehicle.put(v1, m1Next);
		} else {
			nextMovementsVehicle.remove(v1);
		}
		nextMovements.put(m1, m1Deliver);
		nextMovements.put(m1Deliver, m2);
		nextMovementsVehicle.put(v2, m1);

		return new SolutionState(nextMovements, nextMovementsVehicle);
	}

	@SuppressWarnings("unchecked")
	private SolutionState changingTaskOrder(SolutionState oldState, Vehicle vehicle, int pickupIdx, int deliverIdx,
			int pickupNextIdx, int deliverNextIdx) {
		HashMap<Vehicle, Movement> nextVehicleMovement = oldState.getNextMovementsVehicle();
		HashMap<Movement, Movement> nextMovement = oldState.getNextMovements();
		LinkedList<Movement> plan = (LinkedList<Movement>) oldState.getPlans().get(vehicle).clone();

		Movement deliverChanging = plan.get(deliverIdx);
		Movement pickupChanging = plan.get(pickupIdx);

		if (deliverIdx != deliverNextIdx) {
			plan.remove(deliverIdx);
			plan.add(deliverNextIdx, deliverChanging);
		}
		if (pickupIdx != pickupNextIdx) {
			if (deliverNextIdx <= pickupIdx && deliverIdx != deliverNextIdx) {
				plan.remove(pickupIdx + 1);
			} else {
				plan.remove(pickupIdx);
			}
			plan.add(pickupNextIdx, pickupChanging);
		}

		// creates a new state given the new plan
		nextVehicleMovement.put(vehicle, plan.getFirst());
		for (int i = 0; i < plan.size() - 1; i++) {
			nextMovement.put(plan.get(i), plan.get(i + 1));
		}
		nextMovement.put(plan.getLast(), null);

		SolutionState solution = new SolutionState(nextMovement, nextVehicleMovement);

		if (Constraints.checkVehicleLoad(solution, vehicle) != 0) {
			return null;
		}

		return solution;
	}

}
