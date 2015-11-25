package template;

//the list of imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import epfl.lia.logist.task.TasksetDescriptor;
import logist.LogistPlatform;
import logist.LogistSettings;
import logist.Measures;
import logist.behavior.AuctionBehavior;
import logist.agent.Agent;
import logist.simulation.Vehicle;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;

/**
 * A very simple auction agent that assigns all tasks to its first vehicle and
 * handles them sequentially.
 * 
 */
@SuppressWarnings("unused")
public class AuctionDummyAgent implements AuctionBehavior {

	private Topology topology;
	private TaskDistribution distribution;
	private Agent agent;
	private Random random;
	private City currentCity;
	private List<Task> tasksList = new ArrayList<Task>();
	private Vehicle vehicle;

	private long timeout_setup;
	private long timeout_plan;
	private long timeout_bid;

	// This dummy agent will keep the initial solution
	private final double SLS_PROBABILITY = 0;
	private final int MAX_SLS_LOOPS = 10000;
	private final int MAX_SLS_COST_REPETITION = 750;
	private SolutionState currentBestState;
	private int nbTasks;
	private int nbVehicles;
	private List<Vehicle> vehicles = new ArrayList<Vehicle>();

	@Override
	public void setup(Topology topology, TaskDistribution distribution, Agent agent) {

		this.topology = topology;
		this.distribution = distribution;
		this.agent = agent;
		vehicles = agent.vehicles();
		nbVehicles = agent.vehicles().size();
		this.vehicle = agent.vehicles().get(0);
		this.currentCity = vehicle.homeCity();

		long seed = -9019554669489983951L * agent.id();
		this.random = new Random(seed);

		// this code is used to get the timeouts
		LogistSettings ls = null;
		try {
			ls = LogistPlatform.getSettings();
		} catch (Exception exc) {
			System.out.println("There is a problem loading the configuration file.");
		}

		// the setup method cannot last more than timeout_setup milliseconds
		timeout_setup = ls.get(LogistSettings.TimeoutKey.SETUP);
		// the plan method cannot execute more than timeout_plan milliseconds
		timeout_plan = ls.get(LogistSettings.TimeoutKey.PLAN);
		timeout_bid = ls.get(LogistSettings.TimeoutKey.BID);

		this.topology = topology;
		this.distribution = distribution;
		this.agent = agent;
	}

	/**
	 * This function is called when an auction is finished We can observe the
	 * bid from others and adapt our strategy
	 */
	// TODO
	@Override
	public void auctionResult(Task previous, int winner, Long[] bids) {
		if (winner == agent.id()) {
			currentCity = previous.deliveryCity;
			tasksList.add(previous);
			nbTasks++;
			currentBestState = computeSLS(vehicles, tasksList, timeout_bid);
		}

	}

	/**
	 * This function is called when we must bid for a task
	 */
	@Override
	public Long askPrice(Task task) {

		//////////////////////////////////////////////////////////////
		// Simple agent

		long distanceTask = task.pickupCity.distanceUnitsTo(task.deliveryCity);
		long distanceSum = distanceTask + currentCity.distanceUnitsTo(task.pickupCity);
		double marginalCost = Measures.unitsToKM(distanceSum * vehicle.costPerKm());

		double ratio = 1.0 + (random.nextDouble() * 0.05 * task.id);
		double bid = ratio * marginalCost;

		return (long) Math.round(bid);

		//////////////////////////////////////////////////////////////
	}

	////////////////////////////////////////////////////////////////////////////
	// Everything below is taken from centralized agent
	////////////////////////////////////////////////////////////////////////////

	@Override
	public List<Plan> plan(List<Vehicle> allVehicles, TaskSet tasks) {
		long time_start = System.currentTimeMillis();
		List<Plan> plans = new ArrayList<Plan>();
		tasksList = new ArrayList<Task>(tasks);

		ArrayList<LinkedList<Movement>> vehiclePlans;

		nbTasks = tasks.size();
		nbVehicles = allVehicles.size();
		vehicles = allVehicles;

		// Compute the centralized plan
		// ArrayList<LinkedList<Movement>> vehiclePlans
		SolutionState vehicleState = computeSLS(allVehicles, new ArrayList<Task>(tasks), timeout_plan);

		if (vehicleState.getCost() < currentBestState.getCost()) {
			currentBestState = vehicleState;
		}

		vehiclePlans = currentBestState.getPlans();

		if (!vehiclePlans.isEmpty()) {
			for (Vehicle vehicle : allVehicles) {
				// LinkedList<Movement> movements =
				// currentBestState.get(vehicle);
				LinkedList<Movement> movements = vehiclePlans.get(vehicle.id());
				Plan plan = individualVehiclePlan(vehicle, movements);
				plans.add(plan);
			}
		} else {
			for (Vehicle vehicle : allVehicles) {
				plans.add(Plan.EMPTY);
			}
		}

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
	private SolutionState computeSLS(List<Vehicle> vehicles, List<Task> tasks, long timeout) {
		SolutionState bestState;
		SolutionState oldState;
		SolutionState overallBestState;

		long time_start = System.currentTimeMillis();

		double p = SLS_PROBABILITY;
		int currentLoop = 0;
		int stateRepetition = 0;
		int costRepetition = 0;
		bestState = computeInitState(vehicles, new ArrayList<Task>(tasks));
		overallBestState = bestState;
		double bestCost = bestState.getCost();
		double newCost;
		double overallBestCost = bestState.getCost();

		// if p is 0, we will keep the initial state
		if (p == 0.0) {
			bestCost = 0;
		}

		double maxIterationTime = 3000;
		if (p > 0.0 && tasks.size() > 1) {
			while (currentLoop < MAX_SLS_LOOPS && costRepetition < MAX_SLS_COST_REPETITION
					&& (timeout - maxIterationTime) > System.currentTimeMillis() - time_start) {
				double start_iteration = System.currentTimeMillis();
				currentLoop++;
				oldState = bestState;

				Random random = new Random();
				int r = random.nextInt(100);
				ArrayList<SolutionState> neighbours = null;
				// Decide if we keep the old state or not
				// If not, compute neighbours and choose one
				if (r < p * 100) {
					neighbours = chooseNeighbours(bestState, vehicles);
					if (neighbours == null || neighbours.isEmpty()) {
						currentLoop = MAX_SLS_LOOPS;
						System.out.println("No neighbours"); // should never
																// happen
					}
					bestState = localChoice(neighbours, bestState.getCost());
					if (bestState.getCost() < overallBestCost) {
						overallBestCost = bestState.getCost();
						overallBestState = bestState;
					}
					if (bestState == null) {
						bestState = oldState;
					}
					newCost = bestState.getCost();
					if (bestCost == newCost) {
						costRepetition++;
					} else {
						costRepetition = 0;
					}
					bestCost = newCost;
				}
				double end_iteration = System.currentTimeMillis();
				if (end_iteration - start_iteration > maxIterationTime) {
					maxIterationTime = end_iteration - start_iteration + 1000;
				}
			}
		}

		System.out.println(" ======================================================== ");
		System.out.println("INTELLIGENT AGENT");
		System.out.println("Number of loops in SLS: " + currentLoop);
		System.out.println("Expected cost: " + overallBestState.getCost());
		System.out.println("Best " + overallBestState.toString());
		return overallBestState;
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
	private SolutionState computeInitState(List<Vehicle> vehicles, List<Task> tasks) {
		HashMap<Vehicle, ArrayList<Task>> distributedTasks = new HashMap<Vehicle, ArrayList<Task>>();
		Vehicle[] arrayOfVehicles = new Vehicle[vehicles.size()];
		vehicles.toArray(arrayOfVehicles);

		Task[] arrayOfTasks = new Task[tasks.size()];
		tasks.toArray(arrayOfTasks);

		Vehicle bestVehicle = null;

		Random ran = new Random();

		// Each task is assigned to one (possibly different) vehicle
		// And there will be only one task in a vehicle at a given time.
		for (int i = 0; i < arrayOfTasks.length; i++) {

			Vehicle v = null;

			double shortestDistance = Double.MAX_VALUE;
			for (int k = 0; k < vehicles.size(); k++) {
				ArrayList<Task> tasksOfV = distributedTasks.get(vehicles.get(k));
				if (tasksOfV == null || tasksOfV.isEmpty()) {
					if (arrayOfTasks[i].pickupCity.equals(tasksOfV.get(tasksOfV.size() - 1).deliveryCity)) {
						v = vehicles.get(k);
						shortestDistance = 0;
						continue;
					} else if (arrayOfTasks[i].pickupCity.distanceTo(vehicles.get(k).homeCity()) < shortestDistance) {
						shortestDistance = arrayOfTasks[i].pickupCity.distanceTo(vehicles.get(k).homeCity());
						v = vehicles.get(k);

					}
				} else {
					if (arrayOfTasks[i].pickupCity
							.distanceTo(tasksOfV.get(tasksOfV.size() - 1).deliveryCity) < shortestDistance) {
						shortestDistance = arrayOfTasks[i].pickupCity
								.distanceTo(tasksOfV.get(tasksOfV.size() - 1).deliveryCity);
						v = vehicles.get(k);
					}
				}
			}

			ArrayList<Task> oldTasksList = distributedTasks.get(v);
			if (oldTasksList == null) {
				oldTasksList = new ArrayList<Task>();
			}
			// If the task is too big for the vehicle
			boolean found = false;
			if (v.capacity() < arrayOfTasks[i].weight) {
				// we need to check if it fits into another vehicle
				for (int j = 0; j < arrayOfVehicles.length; j++) {
					if (arrayOfTasks[i].weight <= arrayOfVehicles[j].capacity()) {
						v = arrayOfVehicles[j];
						j = arrayOfVehicles.length;
						found = true;
					}
				}
				if (!found) {
					System.err.println("No vehicle can carry task " + arrayOfTasks[i].toString());
				}
			}
			oldTasksList.add(arrayOfTasks[i]);
			distributedTasks.put(v, (ArrayList<Task>) oldTasksList.clone());
		}

		// We construct nextMovements and nextMovementsVehicle
		Movement[] nextMovements = new Movement[tasks.size() * 2];
		Movement[] nextMovementsVehicle = new Movement[vehicles.size()];
		for (Vehicle v : vehicles) {
			ArrayList<Task> vTasks = distributedTasks.get(v);
			if (vTasks != null) {
				Movement firstMovement = new Movement(Action.PICKUP, vTasks.get(0));
				Movement previousMovement = firstMovement;
				nextMovementsVehicle[v.id()] = firstMovement;
				for (int i = 1; i < vTasks.size(); i++) {
					Movement nextDeliverMovement = new Movement(Action.DELIVER, vTasks.get(i - 1));
					nextMovements[previousMovement.getId()] = nextDeliverMovement;
					Movement nextPickupMovement = new Movement(Action.PICKUP, vTasks.get(i));
					nextMovements[nextDeliverMovement.getId()] = nextPickupMovement;
					previousMovement = nextPickupMovement;

				}
				Movement finalMovement = new Movement(Action.DELIVER, vTasks.get(vTasks.size() - 1));
				nextMovements[previousMovement.getId()] = finalMovement;
			} else {
				nextMovementsVehicle[v.id()] = null;
			}
		}
		SolutionState solution = new SolutionState(nextMovements, nextMovementsVehicle, nbVehicles, vehicles, nbTasks);

		// Compute the cost of this plan a save it into the SolutionState object
		solution.getCost();
		int contraintsErrors = Constraints.checkSolutionState(solution, nbVehicles, vehicles);
		if (contraintsErrors != 0) {
			System.err.println("The initial solution does not respect " + contraintsErrors + " errors.");
		}

		return solution;
	}

	// We create neighbours of a state following the idea of the paper
	private ArrayList<SolutionState> chooseNeighbours(SolutionState oldState, List<Vehicle> vehicles) {
		ArrayList<SolutionState> neighbours = new ArrayList<SolutionState>();
		Movement[] nextMovementsVehicle = oldState.getNextMovementsVehicle();
		SolutionState ss;

		// pick a random vehicle
		Random ran = new Random();
		int x = ran.nextInt(vehicles.size());
		Vehicle vehicle = vehicles.get(x);
		while (nextMovementsVehicle[vehicle.id()] == null) {
			x = ran.nextInt(vehicles.size());
			vehicle = vehicles.get(x);
		}

		// apply changing vehicle operator:
		Movement m;
		for (Vehicle v : vehicles) {
			if (!v.equals(vehicle)) {
				m = nextMovementsVehicle[vehicle.id()];
				if (m != null) {
					if (m.getTask().weight < v.capacity()) {
						ss = changingVehicle(oldState, vehicle, v);
						neighbours.add(ss);

						// All possible positions for the moved task in its new
						// vehicle
						SolutionState ss2;
						LinkedList<Movement> plan = ss.getPlans().get(v.id());
						int size = plan.size();
						for (int i = 2; i < size - 1; i++) {
							for (int j = i + 1; j < size; j++) {
								ss2 = changingTaskOrder(ss, v, 0, 1, i, j);
								if (ss2 != null) {
									neighbours.add(ss2);
								}
							}
						}
					}
				}
			}
		}

		// apply changing task order operator:
		Movement pMov, dMov;
		LinkedList<Movement> plan = oldState.getPlans().get(vehicle.id());
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
						System.out
								.println("Deliver not found for task " + pMov.getTask().id + " in changingTaskOrder.");
						dMov = null;
					}

					// all permutations:
					if (dMov != null) {
						for (int i = 0; i < size - 1; i++) {
							for (int j = i + 1; j < size; j++) {
								if (i != k || j != kk - 1) {
									ss = changingTaskOrder(oldState, vehicle, k, kk - 1, i, j);
									if (ss != null) {
										neighbours.add(ss);
									}
								}
							}
						}
					}
				}
			}
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
	private SolutionState localChoice(ArrayList<SolutionState> neighbours, double oldBestCost) {
		SolutionState bestSolution = null;

		if (neighbours == null || neighbours.isEmpty()) {
			System.err.println("Neighbours should not be null !");
			return null;
		}

		double bestCost = Double.MAX_VALUE;
		ArrayList<SolutionState> bestSolutions = new ArrayList<SolutionState>();

		double cost;
		for (SolutionState neighbour : neighbours) {
			cost = neighbour.getCost();
			if (cost <= bestCost) {
				if (Constraints.checkSolutionState(neighbour, nbVehicles, vehicles) != 0) {
					continue;
				}
				if (cost < bestCost) {
					bestSolutions.clear();
					bestCost = cost;
				}
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
		Movement[] nextMovementsVehicle = oldState.getNextMovementsVehicle();
		Movement[] nextMovements = oldState.getNextMovements();

		Movement m1 = nextMovementsVehicle[v1.id()];
		Movement m1Deliver = null;
		Movement m2 = nextMovementsVehicle[v2.id()];

		Movement m1Next = nextMovements[m1.getId()];
		if (m1Next.getAction() == Action.DELIVER) {
			// deliver m1 right after having it picked up
			m1Deliver = m1Next;
			m1Next = nextMovements[m1Next.getId()];
		} else {
			// deliver m1 after pickup other tasks
			Movement prev = m1Next;
			Movement current = nextMovements[m1Next.getId()];
			while (current != null && !current.getTask().equals(m1.getTask())) {
				prev = current;
				current = nextMovements[current.getId()];
			}
			if (current != null) {
				// remove delivery of m1 from v1's plan
				Movement next = nextMovements[current.getId()];
				nextMovements[prev.getId()] = next;
				m1Deliver = current;
			} else {
				System.out.println("Deliver not found for task " + m1.getTask().id + " in changingVehicle().");
			}
		}

		// remove m1 from the plan for vehicle v1
		// append m1 to the beginning of the plan for vehicle v2
		if (m1Next != null) {
			nextMovementsVehicle[v1.id()] = m1Next;
		} else {
			nextMovementsVehicle[v1.id()] = null;
		}
		nextMovements[m1.getId()] = m1Deliver;
		nextMovements[m1Deliver.getId()] = m2;
		nextMovementsVehicle[v2.id()] = m1;

		return new SolutionState(nextMovements, nextMovementsVehicle, nbVehicles, vehicles, nbTasks);
	}

	/**
	 * In oldState, for vehicle, move pickup action at position pikcupIdx to
	 * position pickupNextIdx and deliver action at position deliverIdx to
	 * position deliverNextIdx
	 * 
	 * @param oldState
	 * @param vehicle
	 * @param pickupIdx
	 * @param deliverIdx
	 * @param pickupNextIdx
	 * @param deliverNextIdx
	 * @return the computed SolutionState
	 */
	@SuppressWarnings("unchecked")
	private SolutionState changingTaskOrder(SolutionState oldState, Vehicle vehicle, int pickupIdx, int deliverIdx,
			int pickupNextIdx, int deliverNextIdx) {
		Movement[] nextVehicleMovement = oldState.getNextMovementsVehicle();
		Movement[] nextMovement = oldState.getNextMovements();
		LinkedList<Movement> plan = (LinkedList<Movement>) oldState.getPlans().get(vehicle.id()).clone();

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
		nextVehicleMovement[vehicle.id()] = plan.getFirst();
		for (int i = 0; i < plan.size() - 1; i++) {
			Movement mov = plan.get(i);
			nextMovement[mov.getId()] = plan.get(i + 1);
		}
		nextMovement[plan.getLast().getId()] = null;

		SolutionState solution = new SolutionState(nextMovement, nextVehicleMovement, nbVehicles, vehicles, nbTasks);

		if (Constraints.checkVehicleLoad(solution, vehicle.id(), vehicles) != 0) {
			return null;
		}

		return solution;
	}
}
