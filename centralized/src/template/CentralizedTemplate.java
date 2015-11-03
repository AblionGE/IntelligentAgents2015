package template;

//the list of imports
import java.util.ArrayList;
import java.util.HashMap;
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
 * A very simple auction agent that assigns all tasks to its first vehicle and
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

	private Plan individualVehiclePlan(Vehicle vehicle, LinkedList<Action> actions) {
		City current = vehicle.getCurrentCity();
		Plan plan = new Plan(current);

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
	 * @param vehicles
	 * @param tasks
	 */
	private HashMap<Vehicle, LinkedList<Action>> computeSLS(List<Vehicle> vehicles, TaskSet tasks) {
		HashMap<Action, Action> nextActions = new HashMap<Action, Action>();
		HashMap<Action, Action> oldNextActions = new HashMap<Action, Action>();
		
		int maxLoop = 1000;
		int currentLoop = 0;
		nextActions = computeInitState();
		
		// TODO : // add condition if no improvement
		while (currentLoop < maxLoop) {
			currentLoop++;
			oldNextActions = nextActions;
			ArrayList<HashMap<Action, Action>> neighbours = chooseNeighbours(nextActions);
			nextActions = localChoice(oldNextActions, neighbours);
		}

		return computeVehiclePlans(nextActions);
	}

	// TODO
	public HashMap<Action, Action> computeInitState() {
		return new HashMap<Action, Action>();
	}
	
	// TODO
	public ArrayList<HashMap<Action, Action>> chooseNeighbours(HashMap<Action, Action> actions) {
		ArrayList<HashMap<Action, Action>> neighbours = new ArrayList<HashMap<Action, Action>>();
		return neighbours;
	}
	
	// TODO
	public boolean IsSatsifyingConstraints() {
		return false;
	}
	
	// TODO
	public HashMap<Action, Action> localChoice(HashMap<Action, Action> old, ArrayList<HashMap<Action, Action>> neighbours) {
		return old;
	}
	
	// TODO
	public HashMap<Vehicle, LinkedList<Action>> computeVehiclePlans(HashMap<Action, Action> actions) {
		HashMap<Vehicle, LinkedList<Action>> plans = new HashMap<Vehicle, LinkedList<Action>>();
		
		return plans;
	}
}
