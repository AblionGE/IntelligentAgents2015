package template;

//the list of imports
import java.util.ArrayList;
import java.util.HashMap;
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
	private final int MAX_SLS_LOOPS = 10000;
	private final int MAX_SLS_STATE_REPETITION = 10;

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
				for (City city : a.getTask().path()) {
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
			oldState = bestState;
			ArrayList<SolutionState> neighbours = chooseNeighbours(bestState, vehicles);
			bestState = localChoice(oldState, neighbours, p);
			if (bestState.equals(oldState)) {
				stateRepetition++;
			} else {
				stateRepetition = 0;
			}
		}

		System.out.println("Number of loops in SLS: " + currentLoop);
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
	public SolutionState computeInitState(List<Vehicle> vehicles, TaskSet tasks) {
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
		solution.computeCost();
		int contraintsErrors = Constraints.checkSolutionState(solution);
		if (contraintsErrors != 0) {
			System.err.println("The initial solution does not respect " + contraintsErrors + " errors.");
		}

		return solution;
	}

	// TODO
	// algo from paper
	public ArrayList<SolutionState> chooseNeighbours(SolutionState oldState, List<Vehicle> vehicles) {
		ArrayList<SolutionState> neighbours = new ArrayList<SolutionState>();
		HashMap<Vehicle, Movement> nextMovementsVehicle = oldState.getNextMovementsVehicle();

		// pick a random vehicle
		Random ran = new Random(); // FIXME change seed
		int x = ran.nextInt(vehicles.size());
		Vehicle vehicle = vehicles.get(x);
		while (nextMovementsVehicle.get(vehicle) == null) {
			x = ran.nextInt(vehicles.size());
			vehicle = vehicles.get(x);
		}

		// apply changing vehicle operator:
		for (Vehicle v : vehicles) {
			if (!v.equals(vehicle)) {
				Movement m = nextMovementsVehicle.get(vehicle);
				if (m.getTask().weight < v.capacity()) {
					SolutionState ss = changingVehicle(oldState, vehicle, v);
					if (true) {// TODO check constaints
						neighbours.add(ss);
					}
				}
			}
		}

		// apply changing task order operator:
		// FIXME : NO ! We should loop over Movements, not tasks !
		int nbTasks = oldState.taskNumber(vehicle);
		if (nbTasks >= 2) {
			for (int i = 1; i < nbTasks - 1; i++) {
				for (int j = i + 1; j < i + nbTasks; j++) {
					//SolutionState ss = changingTaskOrder(oldState, vehicle, i, j);
					if (true) {// TODO check constraints
						//neighbours.add(ss);
					}
				}
			}
		}
		return neighbours;
	}

	public SolutionState localChoice(SolutionState old, ArrayList<SolutionState> neighbours, double probability) {
		SolutionState bestSolution;

		if (neighbours == null || neighbours.size() == 0) {
			return old;
		}

		double bestCost = old.getCost();
		ArrayList<SolutionState> bestSolutions = new ArrayList<SolutionState>();

		for (SolutionState neighbour : neighbours) {
			double cost = neighbour.computeCost();
			if (cost < bestCost) {
				bestSolutions = new ArrayList<SolutionState>();
				bestSolutions.add(neighbour);
				bestCost = cost;
			} else if (cost == bestCost) {
				bestSolutions.add(neighbour);
			}
		}

		if (bestSolutions.size() > 1) {
			// random number to select a best solution
			Random ran = new Random();
			int x = ran.nextInt(bestSolutions.size());
			bestSolution = bestSolutions.get(x);
		} else {
			bestSolution = bestSolutions.get(0);
		}

		Random ran = new Random();
		int x = ran.nextInt(100);

		if (x > probability * 100) {
			bestSolution = old;
		}

		return bestSolution;
	}

	// TODO comme dans le paper
	private SolutionState changingVehicle(SolutionState oldState, Vehicle v1, Vehicle v2) {
		return new SolutionState(null, null);
	}

	// TODO comme dans le paper
	private SolutionState changingTaskOrder(SolutionState oldState, Vehicle vehicle, Movement move1, Movement move2) {
		return new SolutionState(null, null);
	}

}
