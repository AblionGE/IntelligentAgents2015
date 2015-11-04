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
 * A very simple action agent that assigns all tasks to its first vehicle and
 * handles them sequentially.
 *
 */
@SuppressWarnings("unused")
public class CentralizedTemplate implements CentralizedBehavior {

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
		HashMap<Vehicle, LinkedList<Action>> vehiclePlans = computeSLS(vehicles, tasks);

		// System.out.println("Agent " + agent.id() + " has tasks " + tasks);
		// Plan planVehicle1 = individualVehiclePlan(vehicles.get(0), tasks);
		for (Vehicle vehicle : vehicles) {
			LinkedList<Action> actions = vehiclePlans.get(vehicle);
			Plan plan = individualVehiclePlan(vehicle, actions);
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
	 * @param vehicle
	 * @param actions an orderd list of action to perform
	 * @return
	 */
	private Plan individualVehiclePlan(Vehicle vehicle, LinkedList<Action> actions) {
		City current = vehicle.getCurrentCity();
		Plan plan = new Plan(current);
		
		// If vehicle has nothing to do
		if (actions == null) {
			return Plan.EMPTY;
		}
		
		for (Action a : actions) {
			// move: current city => pickup location
			if (a.getAction() == ActionsEnum.PICKUP) {
				for (City city : current.pathTo(a.getTask().pickupCity)) {
					plan.appendMove(city);
				}
				plan.appendPickup(a.getTask());
				current = a.getTask().pickupCity;
			}

			// move: pickup location => delivery location
			if (a.getAction() == ActionsEnum.DELIVER) {
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
	private HashMap<Vehicle, LinkedList<Action>> computeSLS(List<Vehicle> vehicles, TaskSet tasks) {
		SolutionState bestState;
		SolutionState oldState;

		int maxLoop = 1000;
		int currentLoop = 0;
		bestState = computeInitState(vehicles, tasks);

		// TODO : // add condition if no improvement
		/*
		 * while (currentLoop < maxLoop) { currentLoop++; oldState = bestState;
		 * ArrayList<SolutionState> neighbours = chooseNeighbours(bestState);
		 * bestState = localChoice(oldState, neighbours); }
		 */

		return computeVehiclePlans(bestState);
	}

	/**
	 * Each vehicle has some tasks and they are picked up and delivered one
	 * after the other.
	 * 
	 * @param vehicles
	 * @param tasks
	 * @return
	 */
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

		HashMap<Action, Action> nextActions = new HashMap<Action, Action>();
		HashMap<Vehicle, Action> nextActionsVehicle = new HashMap<Vehicle, Action>();
		for (Vehicle v : vehicles) {
			ArrayList<Task> vTasks = distributedTasks.get(v);
			if (vTasks != null) {
				int time = 1;
				Action firstAction = new Action(ActionsEnum.PICKUP, vTasks.get(0), time);
				time++;
				Action previousAction = firstAction;
				nextActionsVehicle.put(v, firstAction);
				for (int i = 1; i < vTasks.size(); i++) {
					Action nextDeliverAction = new Action(ActionsEnum.DELIVER, vTasks.get(i - 1), time);
					nextActions.put(previousAction, nextDeliverAction);
					time++;
					Action nextPickupAction = new Action(ActionsEnum.PICKUP, vTasks.get(i), time);
					time++;
					nextActions.put(nextDeliverAction, nextPickupAction);
					previousAction = nextPickupAction;

				}
				Action finalAction = new Action(ActionsEnum.DELIVER, vTasks.get(vTasks.size() - 1), time);
				nextActions.put(previousAction, finalAction);
			}
		}
		SolutionState solution = new SolutionState(nextActions, nextActionsVehicle);
		
		return solution;
	}

	// TODO
	public ArrayList<SolutionState> chooseNeighbours(SolutionState actions) {
		ArrayList<SolutionState> neighbours = null;
		return neighbours;
	}

	// TODO
	public SolutionState localChoice(SolutionState old, ArrayList<SolutionState> neighbours) {
		return old;
	}

	/**
	 * It computes for each vehicle an orderd list of tasks
	 * @param solutionState
	 * @return
	 */
	public static HashMap<Vehicle, LinkedList<Action>> computeVehiclePlans(SolutionState solutionState) {
		HashMap<Vehicle, LinkedList<Action>> plans = new HashMap<Vehicle, LinkedList<Action>>();

		HashMap<Vehicle, Action> vehicleAction = solutionState.getNextActionsVehicle();
		HashMap<Action, Action> actions = solutionState.getNextActions();

		for (Vehicle v : vehicleAction.keySet()) {
			LinkedList<Action> orderedActions = new LinkedList<Action>();
			orderedActions.add(vehicleAction.get(v));

			Action next = vehicleAction.get(v);
			while (next != null) {
				next = actions.get(next);
				if (next != null) {
					orderedActions.add(next);
				}
			}
			plans.put(v, orderedActions);
		}
		return plans;
	}

}
