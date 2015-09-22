import java.util.ArrayList;

import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.DisplaySurface;

/**
 * Class that implements the simulation model for the rabbits grass simulation.
 * This is the first class which needs to be setup in order to run Repast
 * simulation. It manages the entire RePast environment and the simulation.
 *
 * @author
 */

public class RabbitsGrassSimulationModel extends SimModelImpl {

	private static final String NAME = "Rabbits and Grass Simulation";
	private static final int NUM_RABBITS = 100;
	private static final int X_SIZE = 20;
	private static final int Y_SIZE = 20;
	private static final int MIN_ENERGY = 10;
	private static final int MAX_ENERGY = 20;
	private static final int BIRTH_THRESHOLD = 15;
	private static final int INIT_GRASS = 1000;
	private static final int GROWTH_RATE_GRASS = 1; // unit per run
	private static final String NAME_DISPLAY = "Rabbits and Grass Simulation Window";

	private DisplaySurface displaySurf;
	private Schedule schedule;
	private int numRabbits = NUM_RABBITS;
	private ArrayList<RabbitsGrassSimulationAgent> rabbitList;
	private RabbitsGrassSimulationSpace space;
	private int xSize = X_SIZE;
	private int ySize = Y_SIZE;
	private int minEnergy = MIN_ENERGY;
	private int maxEnergy = MAX_ENERGY;
	private int birthThreshold = BIRTH_THRESHOLD;
	private int initGrass = INIT_GRASS;
	private int growthRateGrass = GROWTH_RATE_GRASS;

	public static void main(String[] args) {

		SimInit init = new SimInit();
		RabbitsGrassSimulationModel model = new RabbitsGrassSimulationModel();
		init.loadModel(model, "", false);

	}

	public void begin() {
		buildModel();
		builSchedule();
		buildDisplay();

		displaySurf.display();
	}
	
	public void setup() {
		space = null;
		rabbitList = new ArrayList<RabbitsGrassSimulationAgent>();
		schedule = new Schedule(1);

		// If used, free variables
		if (displaySurf != null) {
			displaySurf.dispose();
		}
		displaySurf = null;

		// Create basics elements
		displaySurf = new DisplaySurface(this, NAME_DISPLAY);

		// Register these elements
		registerDisplaySurface(NAME_DISPLAY, displaySurf);
	}

	private void buildDisplay() {
		ColorMap map = new ColorMap();
		
		// TODO : To complete...

	}

	private void builSchedule() {
		// TODO : To complete...

	}

	private void buildModel() {
		space = new RabbitsGrassSimulationSpace(xSize, ySize);

		// TODO : spread grass

		for (int i = 0; i < numRabbits; i++) {
			addNewRabbit();
		}

		for (int i = 0; i < rabbitList.size(); i++) {
			RabbitsGrassSimulationAgent rabbit = (RabbitsGrassSimulationAgent) rabbitList.get(i);
			rabbit.report();
		}
	}

	private void addNewRabbit() {
		RabbitsGrassSimulationAgent rabbit = new RabbitsGrassSimulationAgent(minEnergy, maxEnergy, birthThreshold);
		rabbitList.add(rabbit);
		space.addRabbit(rabbit);
	}

	public String[] getInitParam() {
		String[] init_param = { "xSize", "ySize", "numRabbits", "birthThreshold", "initGrass", "growthRateGrass",
				"MinEnergy", "MaxEnergy" };
		return init_param;
	}

	public String getName() {
		return NAME;
	}

	public Schedule getSchedule() {
		return schedule;
	}

	public int getNumRabbits() {
		return numRabbits;
	}

	public void setNumRabbits(int numRabbits) {
		this.numRabbits = numRabbits;
	}

	public int getxSize() {
		return xSize;
	}

	public void setxSize(int xSize) {
		this.xSize = xSize;
	}

	public int getySize() {
		return ySize;
	}

	public void setySize(int ySize) {
		this.ySize = ySize;
	}

	public int getMinEnergy() {
		return minEnergy;
	}

	public void setMinEnergy(int minEnergy) {
		this.minEnergy = minEnergy;
	}

	public int getMaxEnergy() {
		return maxEnergy;
	}

	public void setMaxEnergy(int maxEnergy) {
		this.maxEnergy = maxEnergy;
	}

	public int getBirthThreshold() {
		return birthThreshold;
	}

	public void setBirthThreshold(int birthThreshold) {
		this.birthThreshold = birthThreshold;
	}

	public int getInitGrass() {
		return initGrass;
	}

	public void setInitGrass(int initGrass) {
		this.initGrass = initGrass;
	}

	public int getGrowthRateGrass() {
		return growthRateGrass;
	}

	public void setGrowthRateGrass(int growthRateGrass) {
		this.growthRateGrass = growthRateGrass;
	}
}
