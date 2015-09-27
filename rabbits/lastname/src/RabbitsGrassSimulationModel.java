import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import uchicago.src.reflector.RangePropertyDescriptor;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.gui.Value2DDisplay;
import uchicago.src.sim.util.ProbeUtilities;
import uchicago.src.sim.util.SimUtilities;

/**
 * Class that implements the simulation model for the rabbits grass simulation.
 * This is the first class which needs to be setup in order to run Repast
 * simulation. It manages the entire RePast environment and the simulation.
 *
 * @author
 */

public class RabbitsGrassSimulationModel extends SimModelImpl {

	private static final String NAME = "Rabbits and Grass Simulation";
	private static final int X_SIZE = 20;
	private static final int Y_SIZE = 20;
	private static final int MAX_X_SIZE = 100;
	private static final int MAX_Y_SIZE = 100;
	private static final int NUM_RABBITS = 1;
	private static final int MAX_NUM_RABBITS = MAX_X_SIZE * MAX_Y_SIZE / 10;
	private static final int MIN_INIT_ENERGY = 10;
	private static final int MAX_INIT_ENERGY = 20;
	private static final int BIRTH_THRESHOLD = 20;
	private static final int MAX_BIRTH_THRESHOLD = 100;
	private static final int INIT_GRASS = 500;
	private static final int GROWTH_RATE_GRASS = 50; // unit per run
	private static final int MAX_GROWTH_RATE_GRASS = X_SIZE * Y_SIZE;
	private static final int LOSS_RATE_ENERGY = 1; // unit of energy lost per
													// run
	private static final int LOSS_REPRODUCTION_ENERGY = 5; // unit of energy
															// lost per
															// reproduction
	private static final String NAME_DISPLAY = "Rabbits and Grass Simulation Window";

	private DisplaySurface displaySurf;
	private Schedule schedule;
	private int numRabbits = NUM_RABBITS;
	private ArrayList<RabbitsGrassSimulationAgent> rabbitList;
	private RabbitsGrassSimulationSpace rabbitSpace;
	private int xSize = X_SIZE;
	private int ySize = Y_SIZE;
	private int minInitEnergy = MIN_INIT_ENERGY;
	private int maxInitEnergy = MAX_INIT_ENERGY;
	private int birthThreshold = BIRTH_THRESHOLD;
	private int initGrass = INIT_GRASS;
	private int growthRateGrass = GROWTH_RATE_GRASS;
	private int lossRateEnergy = LOSS_RATE_ENERGY;
	private int lossReproductionEnergy = LOSS_REPRODUCTION_ENERGY;
	private BufferedImage img = null;

	public static void main(String[] args) {

		SimInit init = new SimInit();
		RabbitsGrassSimulationModel model = new RabbitsGrassSimulationModel();
		init.loadModel(model, "", false);
		model.updatePanel();
	}

	public void begin() {
		buildModel();
		builSchedule();
		buildDisplay();

		updatePanel();

		displaySurf.display();
	}

	@Override
	public void setup() {
		rabbitSpace = null;
		rabbitList = new ArrayList<RabbitsGrassSimulationAgent>();
		schedule = new Schedule(1);

		// If used, free variables
		if (displaySurf != null) {
			displaySurf.dispose();
		}
		displaySurf = null;

		RabbitsGrassSimulationAgent.setIDNumber(0);

		// Load image of rabbit
		try {
			img = ImageIO.read(new File("./img/rabbit.jpg"));
		} catch (IOException e) {
		}

		// Create basics elements
		displaySurf = new DisplaySurface(this, NAME_DISPLAY);

		// Register these elements
		registerDisplaySurface(NAME_DISPLAY, displaySurf);
		setDescriptors();

	}

	private void updatePanel() {
		setDescriptors();
		ProbeUtilities.updateModelProbePanel();
	}

	@SuppressWarnings("unchecked")
	private void setDescriptors() {
		descriptors.clear();

		RangePropertyDescriptor numRabbits = new RangePropertyDescriptor("NumRabbits", 0, MAX_NUM_RABBITS,
				MAX_NUM_RABBITS / 5);
		descriptors.put("NumRabbits", numRabbits);

		RangePropertyDescriptor xSize = new RangePropertyDescriptor("XSize", 0, MAX_X_SIZE, MAX_X_SIZE / 5);
		descriptors.put("XSize", xSize);

		RangePropertyDescriptor ySize = new RangePropertyDescriptor("YSize", 0, MAX_Y_SIZE, MAX_Y_SIZE / 5);
		descriptors.put("YSize", ySize);

		RangePropertyDescriptor growthRateGrass = new RangePropertyDescriptor("GrowthRateGrass", 0,
				MAX_GROWTH_RATE_GRASS, MAX_GROWTH_RATE_GRASS / 5);
		descriptors.put("GrowthRateGrass", growthRateGrass);

		RangePropertyDescriptor birthThreshold = new RangePropertyDescriptor("BirthThreshold", 0, MAX_BIRTH_THRESHOLD,
				MAX_BIRTH_THRESHOLD / 5);
		descriptors.put("BirthThreshold", birthThreshold);
	}

	private void buildDisplay() {
		ColorMap map = new ColorMap();

		for (int i = 1; i < 16; i++) {
			map.mapColor(i, new Color(0, (int) (i * 8 + 127), 0));
		}
		map.mapColor(0, Color.WHITE);

		Value2DDisplay displayGrass = new Value2DDisplay(rabbitSpace.getGrassSpace(), map);

		Object2DDisplay displayRabbits = new Object2DDisplay(rabbitSpace.getRabbitSpace());
		displayRabbits.setObjectList(rabbitList);

		displaySurf.addDisplayable(displayGrass, "Grass");
		displaySurf.addDisplayable(displayRabbits, "Rabbits");
	}

	private void builSchedule() {
		class RabbitsGrassSimulationStep extends BasicAction {
			public void execute() {
				SimUtilities.shuffle(rabbitList);
				for (int i = 0; i < rabbitList.size(); i++) {
					RabbitsGrassSimulationAgent rabbit = rabbitList.get(i);
					rabbit.step();
				}

				reapDeadRabbits();
				giveBirthToRabbits();

				rabbitSpace.spreadGrass(growthRateGrass);

				displaySurf.updateDisplay();
			}
		}

		schedule.scheduleActionBeginning(0, new RabbitsGrassSimulationStep());

		// TODO : Sliders and optional population count plot

	}

	private void buildModel() {
		rabbitSpace = new RabbitsGrassSimulationSpace(xSize, ySize);

		rabbitSpace.spreadGrass(initGrass);

		for (int i = 0; i < numRabbits && i < xSize * ySize; i++) {
			addNewRabbit();
		}

		for (int i = 0; i < rabbitList.size(); i++) {
			RabbitsGrassSimulationAgent rabbit = rabbitList.get(i);
			rabbit.report();
		}
	}

	private void addNewRabbit() {
		RabbitsGrassSimulationAgent rabbit = new RabbitsGrassSimulationAgent(minInitEnergy, maxInitEnergy,
				lossRateEnergy, birthThreshold, lossReproductionEnergy, img);
		boolean isAdded = rabbitSpace.addRabbit(rabbit);
		if (isAdded) {
			rabbitList.add(rabbit);
		}
	}

	private void reapDeadRabbits() {
		for (int i = (rabbitList.size() - 1); i >= 0; i--) {
			RabbitsGrassSimulationAgent rabbit = rabbitList.get(i);
			if (rabbit.getEnergy() < 1) {
				rabbitSpace.removeRabbitAt(rabbit.getX(), rabbit.getY());
				rabbitList.remove(i);
			}
		}
	}

	private void giveBirthToRabbits() {
		for (int i = (rabbitList.size() - 1); i >= 0; i--) {
			RabbitsGrassSimulationAgent rabbit = rabbitList.get(i);
			if (rabbit.isReproducing()) {
				addNewRabbit();
			}
		}
	}

	public String[] getInitParam() {
		String[] init_param = { "xSize", "ySize", "NumRabbits", "BirthThreshold", "InitGrass", "GrowthRateGrass",
				"LossRateEnergy", "LossReproductionEnergy", "MinInitEnergy", "MaxInitEnergy" };
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
		if (numRabbits < 0) {
			System.out.println("numRabbits should be positive ! It is set to 1.");
			numRabbits = 1;
		} else if (numRabbits > xSize * ySize) {
			System.out.println(
					"numRabbits should less or equal than the size of the grid! It is set to " + xSize * ySize + ".");
			numRabbits = xSize * ySize;
		}
		this.numRabbits = numRabbits;
		updatePanel();
	}

	public int getXSize() {
		return xSize;
	}

	public void setXSize(int xSize) {
		if (xSize < 1) {
			System.out.println("xSize should be positive! It is set to " + X_SIZE + ".");
			xSize = X_SIZE;
		}
		this.xSize = xSize;
		
		// To update the panel with new values if necessary
		this.setNumRabbits(this.getNumRabbits());

		updatePanel();
	}

	public int getYSize() {
		return ySize;
	}

	public void setYSize(int ySize) {
		if (ySize < 0) {
			System.out.println("ySize should be positive! It is set to " + Y_SIZE + ".");
			ySize = Y_SIZE;
		}
		this.ySize = ySize;

		// To update the panel with new values if necessary
		this.setNumRabbits(this.getNumRabbits());

		updatePanel();
	}

	public int getMinInitEnergy() {
		return minInitEnergy;
	}

	public void setMinInitEnergy(int minEnergy) {
		if (minEnergy < 0) {
			System.out.println("minInitEnergy should be positive! It is set to " + MIN_INIT_ENERGY + ".");
			minEnergy = MIN_INIT_ENERGY;
		} else if (minEnergy > this.getMaxInitEnergy()) {
			System.out.println("minInitEnergy should be smaller than maxInitEnergy. It is set to "
					+ this.getMaxInitEnergy() + ".");
			minEnergy = this.getMaxInitEnergy();
		}
		this.minInitEnergy = minEnergy;
		updatePanel();
	}

	public int getMaxInitEnergy() {
		return maxInitEnergy;
	}

	public void setMaxInitEnergy(int maxEnergy) {
		if (maxEnergy < 0) {
			System.out.println("maxInitEnergy should be positive! It is set to " + MAX_INIT_ENERGY + ".");
			maxEnergy = MAX_INIT_ENERGY;
		} else if (maxEnergy < this.getMinInitEnergy()) {
			System.out.println(
					"maxInitEnergy should be bigger than minInitEnergy. It is set to " + this.getMinInitEnergy() + ".");
			maxEnergy = this.getMinInitEnergy();
		}
		this.maxInitEnergy = maxEnergy;
		updatePanel();
	}

	public int getBirthThreshold() {
		return birthThreshold;
	}

	public void setBirthThreshold(int birthThreshold) {
		if (birthThreshold < 0) {
			System.out.println("birthThreshold should be positive! It is set to " + BIRTH_THRESHOLD + ".");
			birthThreshold = BIRTH_THRESHOLD;
		}
		this.birthThreshold = birthThreshold;
		updatePanel();
	}

	public int getInitGrass() {
		return initGrass;
	}

	public void setInitGrass(int initGrass) {
		if (growthRateGrass < 0) {
			System.out.println("initGrass should be positive! It is set to " + INIT_GRASS + ".");
			initGrass = INIT_GRASS;
		}
		this.initGrass = initGrass;
		updatePanel();
	}

	public int getGrowthRateGrass() {
		return growthRateGrass;
	}

	public void setGrowthRateGrass(int growthRateGrass) {
		if (growthRateGrass < 0) {
			System.out.println("growthRateGrass should be positive! It is set to " + GROWTH_RATE_GRASS + ".");
			growthRateGrass = GROWTH_RATE_GRASS;
		}
		this.growthRateGrass = growthRateGrass;
		updatePanel();
	}

	public int getLossRateEnergy() {
		return lossRateEnergy;
	}

	public void setLossRateEnergy(int lossRateEnergy) {
		if (lossRateEnergy < 0) {
			System.out.println("lossRateEnergy should be positive! It is set to " + LOSS_RATE_ENERGY + ".");
			lossRateEnergy = LOSS_RATE_ENERGY;
		}
		this.lossRateEnergy = lossRateEnergy;
		updatePanel();
	}

	public int getLossReproductionEnergy() {
		return lossReproductionEnergy;
	}

	public void setLossReproductionEnergy(int lossReproductionEnergy) {
		if (lossReproductionEnergy < 0) {
			System.out.println(
					"lossReproductionEnergy should be positive! It is set to " + LOSS_REPRODUCTION_ENERGY + ".");
			lossReproductionEnergy = LOSS_REPRODUCTION_ENERGY;
		}
		this.lossReproductionEnergy = lossReproductionEnergy;
		updatePanel();
	}
}
