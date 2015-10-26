package template;

/* import table */
import logist.simulation.Vehicle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import logist.agent.Agent;
import logist.behavior.DeliberativeBehavior;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;

/**
 * An optimal planner for one vehicle.
 */
@SuppressWarnings("unused")
public class DeliberativeTemplate implements DeliberativeBehavior {

	enum Algorithm {
		BFS, ASTAR, NAIVE
	}

	/* Environment */
	Topology topology;
	TaskDistribution td;
	List<City> cities;
	Vehicle vehicle;
	State initialState;
	ArrayList<State> goalStates;
	ArrayList<Task> carriedTasks;

	/* the properties of the agent */
	Agent agent;
	int capacity;

	/* the planning class */
	Algorithm algorithm;

	/* other */
	final StateDistanceComparator sComparator = new StateDistanceComparator();

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {
		this.topology = topology;
		this.td = td;
		this.agent = agent;
		this.cities = topology.cities();

		// initialize the planner
		int capacity = agent.vehicles().get(0).capacity();
		String algorithmName = agent.readProperty("algorithm", String.class, "ASTAR");

		vehicle = agent.vehicles().get(0);

		// Throws IllegalArgumentException if algorithm is unknown
		algorithm = Algorithm.valueOf(algorithmName.toUpperCase());

		initialState = null;
		goalStates = new ArrayList<State>();
		carriedTasks = new ArrayList<Task>();
	}

	@Override
	public Plan plan(Vehicle vehicle, TaskSet tasks) {
		Plan plan;

		// initial state:
		initialState = setInitialState(vehicle, tasks);

		// goal states:
		goalStates = setGoalStates(vehicle, tasks);

		switch (algorithm) {
		case ASTAR:
			plan = aStarPlan(vehicle, tasks);
			break;
		case BFS:
			plan = bfsPlan(vehicle, tasks);
			break;
		case NAIVE:
			plan = naivePlan(vehicle, tasks);
			break;
		default:
			throw new AssertionError("Algorithm does not exist.");
		}
		return plan;
	}

	private State setInitialState(Vehicle vehicle, TaskSet tasks) {
		ArrayList<Task> availableTasks = new ArrayList<Task>();
		for (Task t : tasks) {
			if (!carriedTasks.contains(t)) {
				availableTasks.add(t);
			}
		}

		ArrayList<Task> takenTasks = new ArrayList<Task>();
		takenTasks.addAll(vehicle.getCurrentTasks());

		return new State(vehicle.getCurrentCity(), availableTasks, takenTasks, new ArrayList<Task>(), 0, 0, vehicle);
	}

	private ArrayList<State> setGoalStates(Vehicle vehicle, TaskSet tasks) {
		ArrayList<State> goals = new ArrayList<State>();
		ArrayList<Task> deliveryTasks = new ArrayList<Task>();

		// not available tasks
		deliveryTasks.addAll(tasks);
		deliveryTasks.addAll(carriedTasks);

		// possible end cities:
		Set<City> deliveryCities = new HashSet<City>();
		for (Task t : deliveryTasks) {
			deliveryCities.add(t.deliveryCity);
		}

		// goals
		for (City city : deliveryCities) {
			goals.add(new State(city, new ArrayList<Task>(), new ArrayList<Task>(), deliveryTasks, 0, 0, vehicle));
		}
		return goals;
	}

	private Plan aStarPlan(Vehicle vehicle, TaskSet tasks) {
		City current = vehicle.getCurrentCity();
		Plan plan = new Plan(current);

		LinkedList<State> bestPath = astar(initialState, goalStates, vehicle);
		return computePlan(plan, current, bestPath);
	}

	private Plan bfsPlan(Vehicle vehicle, TaskSet tasks) {
		City current = vehicle.getCurrentCity();
		Plan plan = new Plan(current);

		LinkedList<State> bestPath = bfs(initialState, goalStates, vehicle);
		System.out.println("best path size: " + bestPath.size());
		return computePlan(plan, current, bestPath);
	}

	private Plan naivePlan(Vehicle vehicle, TaskSet tasks) {
		City current = vehicle.getCurrentCity();
		Plan plan = new Plan(current);

		for (Task task : tasks) {
			// move: current city => pickup location
			for (City city : current.pathTo(task.pickupCity))
				plan.appendMove(city);

			plan.appendPickup(task);

			// move: pickup location => delivery location
			for (City city : task.path())
				plan.appendMove(city);

			plan.appendDelivery(task);

			// set current city
			current = task.deliveryCity;
		}
		return plan;
	}

	@Override
	public void planCancelled(TaskSet carriedTasks) {
		System.out.println("carriedTask : " + carriedTasks.toString());
		if (!carriedTasks.isEmpty()) {
			this.carriedTasks = new ArrayList<Task>();
			for (Task t : carriedTasks) {
				this.carriedTasks.add(t);
			}
		} else {
			this.carriedTasks = new ArrayList<Task>();
		}
	}

	private Plan computePlan(Plan plan, City current, LinkedList<State> path) {
		State prevState = path.pollFirst();
		State nextState;

		while (!path.isEmpty()) {
			nextState = path.pollFirst();
			// move to next city
			for (City city : current.pathTo(nextState.getAgentPosition()))
				plan.appendMove(city);


			// deliver tasks
			List<Task> diffDeliver = prevState.taskDeliverDifferences(nextState);
			if (diffDeliver.size() > 0) {
				for (Task t : diffDeliver) {
					plan.appendDelivery(t);
				}
			}

			// pickup tasks
			List<Task> diffPickup = prevState.taskPickUpDifferences(nextState);
			if (diffPickup.size() > 0) {
				for (Task t : diffPickup) {
					plan.appendPickup(t);
				}
			}
			prevState = nextState;
			current = prevState.getAgentPosition();
		}
		return plan;
	}

	@SuppressWarnings("unchecked")
	private LinkedList<State> bfs(State init, ArrayList<State> goals, Vehicle vehicle) {
		// initialize queue for bfs
		LinkedList<Pair> queue = new LinkedList<Pair>();
		queue.addLast(new Pair(init, new LinkedList<State>()));

		Pair currentPair;
		State currentState;
		LinkedList<State> currentPath;
		ArrayList<State> visitedStates = new ArrayList<State>();
		List<State> children;
		// bfs loop
		while (!queue.isEmpty()) {
			currentPair = queue.pollFirst();
			currentState = currentPair.getState();
			currentPath = currentPair.getPath();
			sComparator.setState(currentState);

			if (!visitedStates.contains(currentState)) {
				visitedStates.add(currentState);
				currentPath.addLast(currentState);

				if (goals.contains(currentState)) {
					// a goal has been reached
					return currentPath;
				} else {
					// add successors of currentState in the queue sorted by their distance to the current state
					children = new ArrayList<State>(currentState.computeChildren(vehicle));
					Collections.sort(children,sComparator);
					for (State s : children) {
						queue.addLast(new Pair(s, (LinkedList<State>) currentPath.clone()));
					}
				}
			}
		}
		System.err.println("Error in BFS");
		return null;
	}

	@SuppressWarnings("unchecked")
	private LinkedList<State> astar(State init, ArrayList<State> goals, Vehicle vehicle) {
		// initialize queue for A*
		LinkedList<Pair> queue = new LinkedList<Pair>();
		queue.addLast(new Pair(init, new LinkedList<State>()));

		Pair currentPair;
		State currentState;
		LinkedList<State> currentPath;
		ArrayList<State> visitedStates = new ArrayList<State>();
		Set<State> children;
		// bfs loop
		while (!queue.isEmpty()) {
			currentPair = queue.pollFirst();
			currentState = currentPair.getState();
			currentPath = currentPair.getPath();

			// Test if C is already visited or if the cost is lower
			if (!visitedStates.contains(currentState)
					|| hasLowerCost(currentState, visitedStates.get(visitedStates.indexOf((currentState))))) {
				currentPath.addLast(currentState);

				if (goals.contains(currentState)) {
					// a goal has been reached
					return currentPath;
				} else {
					children = currentState.computeChildren(vehicle);
					LinkedList<Pair> tempQueue = new LinkedList<Pair>();
					for (State s : children) {
						tempQueue.addLast(new Pair(s, (LinkedList<State>) currentPath.clone()));
					}

					// Sort tempQueue
					Collections.sort((List<Pair>) tempQueue, new PairComparator());

					// Merge tempQueue into Queue
					int indexQueue = 0;
					if (queue.size() > 0) {
						for (int i = 0; i < tempQueue.size(); i++) {
							while (indexQueue < queue.size()
									&& tempQueue.get(i).getState().getF() > queue.get(indexQueue).getState().getF()) {
								indexQueue++;
							}
							queue.add(indexQueue, tempQueue.get(i));
						}
					} else {
						queue = tempQueue;
					}
				}
			}
		}
		System.err.println("Error in A*");
		return null;

	}

	/**
	 * Return true if the currentState has a lower cost than the one already
	 * visited
	 * 
	 * @param currentState
	 * @param visitedState
	 * @return
	 */
	private boolean hasLowerCost(State currentState, State visitedState) {
		return visitedState.getCost() > currentState.getCost();
	}

	/**
	 * Compute the cost of task for a given vehicle
	 * 
	 * @param vehicle
	 * @param task
	 * @return
	 */
	private double computeCost(Vehicle vehicle, Task task) {
		City cityFrom = task.pickupCity;
		City cityTo = task.deliveryCity;
		return distanceBetween(cities, cityFrom, cityTo) * vehicle.costPerKm();
	}

	/**
	 * Returns the distance between 2 cities
	 * 
	 * @param cityA
	 * @param cityB
	 * @return distance between 2 cities
	 */
	public double distanceBetween(List<City> cities, City cityA, City cityB) {
		return cityA.distanceTo(cityB);
	}
}
