package template;

/* import table */
import logist.simulation.Vehicle;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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

	enum Algorithm { BFS, ASTAR, NAIVE }

	/* Environment */
	Topology topology;
	TaskDistribution td;
	List<City> cities;
	State initialState;
	HashMap<State,Boolean> goalStates = new HashMap<State,Boolean>();
	//HashMap<State,Boolean> visitedStates = new HashMap<State,Boolean>();

	/* the properties of the agent */
	Agent agent;
	int capacity;

	/* the planning class */
	Algorithm algorithm;

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {
		this.topology = topology;
		this.td = td;
		this.agent = agent;
		this.cities = topology.cities();

		// initialize the planner
		int capacity = agent.vehicles().get(0).capacity();
		String algorithmName = agent.readProperty("algorithm", String.class, "ASTAR");

		// Throws IllegalArgumentException if algorithm is unknown
		algorithm = Algorithm.valueOf(algorithmName.toUpperCase());

		// ...
	}

	@Override
	public Plan plan(Vehicle vehicle, TaskSet tasks) {
		Plan plan;
		HashMap<Task, City> pickupLocations = new HashMap<Task, City>();
		HashMap<Task, City> deliveryLocations = new HashMap<Task, City>();

		for (Task t : tasks) {
			// FIXME
			// Is it always true, when recomputing the plan to ?
			// Problem is if the task has moved...
			pickupLocations.put(t, t.pickupCity);
			deliveryLocations.put(t, t.deliveryCity);
		}

		// initial state:
		initialState = new State (vehicle.getCurrentCity(), pickupLocations);

		// goal states:
		Set<City> deliveryCities = new HashSet<City>(deliveryLocations.values());
		for (City city : deliveryCities) {
			goalStates.put(new State(city, deliveryLocations), true);
		}

		switch (algorithm) {
		case ASTAR:
			// ...
			plan = aStarPlan(vehicle, tasks);
			break;
		case BFS:
			// ...
			plan = bfsPlan(vehicle, tasks);
			break;
		case NAIVE:			

			HashMap<State,Boolean> visitedStates = new HashMap<State,Boolean>();
			List<State> children = initialState.computeChildren(visitedStates, vehicle.capacity());
			System.out.println(initialState.toString());
			plan = naivePlan(vehicle, tasks);
			break;
		default:
			throw new AssertionError("Algorithm does not exist.");
		}		
		return plan;
	}

	private Plan aStarPlan(Vehicle vehicle, TaskSet tasks) {
		City current = vehicle.getCurrentCity();
		Plan plan = new Plan(current);
		// TODO
		return plan;
	}

	private Plan bfsPlan(Vehicle vehicle, TaskSet tasks) {
		City current = vehicle.getCurrentCity();
		Plan plan = new Plan(current);

		LinkedList<State> bestPath = bfs(initialState, goalStates, vehicle.capacity());

		System.out.println(bestPath.size());
		for(State s: bestPath) {
			System.out.println(s.getAgentPosition());
		}
		
		State prevState = initialState;
		State nextState;
		while(!bestPath.isEmpty()) {
			nextState = bestPath.pollFirst();

			// pickup tasks
			List<Task> diff = prevState.taskDifferences(nextState);
			if(diff.size() > 0) {
				for(Task t: diff) {
					if(pickedup(t, prevState))
						plan.appendPickup(t);
				}
			}
			
			// move to next city
			for (City city : current.pathTo(nextState.getAgentPosition()))
				plan.appendMove(city);

			// deliver tasks
			if(diff.size() > 0) {
				for(Task t: diff) {
					if(delivered(t, nextState))
						plan.appendDelivery(t);
				}
			}
			
			prevState = nextState;
			current = prevState.getAgentPosition();
		}

		return plan;
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

		if (!carriedTasks.isEmpty()) {
			// This cannot happen for this simple agent, but typically
			// you will need to consider the carriedTasks when the next
			// plan is computed.

			//TODO : A regarder si plan est rappele ?
		}
	}



	private boolean pickedup(Task task, State state) {
		return task.pickupCity.equals(state.getCity(task));
	}

	private boolean delivered(Task task, State state) {
		return task.deliveryCity.equals(state.getCity(task));
	}

	private LinkedList<State> bfs(State init, HashMap<State,Boolean> goals, int vehicleCapacity) {
		// initialize queue for bfs
		LinkedList<Pair> queue = new LinkedList<Pair>();
		queue.addLast(new Pair(init, new LinkedList<State>()));

		Pair currentPair;
		State currentState;
		LinkedList<State> currentPath;
		HashMap<State,Boolean> visitedStates;
		List<State> children;
		// bfs loop
		while(!queue.isEmpty()) {
			currentPair = queue.pollFirst();
			currentState = currentPair.getState();
			currentPath = currentPair.getPath();
			currentPath.addLast(currentState);
			
			if(goals.containsKey(currentState)) {
				// a goal has been reached
				return currentPath;
			} else {
				// add children to the queue
				visitedStates = new HashMap<State,Boolean>();
				for(State s: currentPath) {
					visitedStates.put(s, false);
				}
				children = currentState.computeChildren(visitedStates, vehicleCapacity);
				for(State s: children) {
					queue.addLast(new Pair(s, (LinkedList<State>) currentPath.clone()));
				}
			}
		}
		return null;
	}

	/**
	 * Compute the cost of task for a given vehicle
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
