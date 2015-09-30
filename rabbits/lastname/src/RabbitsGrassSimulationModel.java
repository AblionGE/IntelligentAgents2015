import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import uchicago.src.reflector.RangePropertyDescriptor;
import uchicago.src.sim.analysis.DataSource;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.Sequence;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.event.SliderListener;
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
 * @author Cynthia Oeschger and Marc Schaer
 */

public class RabbitsGrassSimulationModel extends SimModelImpl {

	private static final String NAME = "Rabbits and Grass Simulation";
	private static final int X_SIZE = 20;
	private static final int Y_SIZE = 20;
	private static final int MAX_X_SIZE = 101;
	private static final int MAX_Y_SIZE = 101;
	private static final int NUM_RABBITS = 1;
	private static final int MAX_NUM_RABBITS = MAX_X_SIZE * MAX_Y_SIZE / 10;
	private static final int MIN_INIT_ENERGY = 10;
	private static final int MAX_INIT_ENERGY = 20;
	private static final int BIRTH_THRESHOLD = 20;
	private static final int MAX_BIRTH_THRESHOLD = 50;
	private static final int INIT_GRASS = 500;
	private static final int MAX_INIT_GRASS = MAX_X_SIZE * MAX_Y_SIZE;
	private static final int GROWTH_RATE_GRASS = 50; // unit per run
	private static final int MAX_GROWTH_RATE_GRASS = 200;
	private static final int LOSS_RATE_ENERGY = 1; // unit of energy lost per
													// run
	private static final int MAX_LOSS_RATE_ENERGY = 20;
	private static final int LOSS_REPRODUCTION_ENERGY = 5; // unit of energy
															// lost per
															// reproduction
	private static final int MAX_LOSS_REPRODUCTION_ENERGY = 20;
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
	private OpenSequenceGraph rabbitsAndGrassInSpace;

	public static void main(String[] args) {
		SimInit init = new SimInit();
		RabbitsGrassSimulationModel model = new RabbitsGrassSimulationModel();
		init.loadModel(model, "", false);
	}

	class RabbitsInSpace implements DataSource, Sequence {

		public Object execute() {
			return new Double(getSValue());
		}

		public double getSValue() {
			return (double) rabbitList.size();
		}
	}

	class GrassInSpace implements DataSource, Sequence {

		public Object execute() {
			return new Double(getSValue());
		}

		public double getSValue() {
			return (double) rabbitSpace.getTotalGrass();
		}
	}

	public void begin() {
		buildModel();
		builSchedule();
		buildDisplay();

		displaySurf.display();
		rabbitsAndGrassInSpace.display();
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

		if (rabbitsAndGrassInSpace != null) {
			rabbitsAndGrassInSpace.dispose();
		}
		rabbitsAndGrassInSpace = null;

		// At each setup, start IDs from 0
		RabbitsGrassSimulationAgent.setIDNumber(0);

		// Load image of rabbit
		try {
			img = ImageIO.read(RabbitsGrassSimulationModel.class.getResource("/resources/rabbit.jpg"));
		} catch (IOException e) {
		}

		// Create basic elements
		displaySurf = new DisplaySurface(this, NAME_DISPLAY);
		rabbitsAndGrassInSpace = new OpenSequenceGraph("Number of Rabbits and Amount of Grass", this);

		// Register these elements
		registerDisplaySurface(NAME_DISPLAY, displaySurf);
		this.registerMediaProducer("Plot", rabbitsAndGrassInSpace);

		modelManipulator.init();

		// Set descriptors and sliders
		setDescriptors();
		setSliders();
	}

	private void updatePanel() {
		ProbeUtilities.updateModelProbePanel();
	}

	private void setSliders() {

		SliderListener birthThresholdSlider = new SliderListener() {
			public void execute() {
				if (isAdjusting) {
					setBirthThreshold(value);

				}
			}
		};

		SliderListener grassGrowthRateSlider = new SliderListener() {
			public void execute() {
				if (isAdjusting) {
					setGrowthRateGrass(value);

				}
			}
		};

		// Apparently, setFirstVal() doesn't work
		birthThresholdSlider.setFirstVal(getBirthThreshold());
		grassGrowthRateSlider.setFirstVal(getGrowthRateGrass());

		modelManipulator.addSlider("Birth Threshold", 0, MAX_BIRTH_THRESHOLD, 10, birthThresholdSlider);
		modelManipulator.addSlider("Grass Growth Rate", 0, MAX_GROWTH_RATE_GRASS, 25, grassGrowthRateSlider);
	}

	@SuppressWarnings("unchecked")
	private void setDescriptors() {
		descriptors.clear();

		RangePropertyDescriptor numRabbits = new RangePropertyDescriptor("NumRabbits", 0, MAX_NUM_RABBITS,
				MAX_NUM_RABBITS / 5);
		descriptors.put("NumRabbits", numRabbits);

		RangePropertyDescriptor xSize = new RangePropertyDescriptor("XSize", 1, MAX_X_SIZE, MAX_X_SIZE / 5);
		descriptors.put("XSize", xSize);

		RangePropertyDescriptor ySize = new RangePropertyDescriptor("YSize", 1, MAX_Y_SIZE, MAX_Y_SIZE / 5);
		descriptors.put("YSize", ySize);

		RangePropertyDescriptor growthRateGrass = new RangePropertyDescriptor("GrowthRateGrass", 0,
				MAX_GROWTH_RATE_GRASS, MAX_GROWTH_RATE_GRASS / 5);
		descriptors.put("GrowthRateGrass", growthRateGrass);

		RangePropertyDescriptor birthThreshold = new RangePropertyDescriptor("BirthThreshold", 0, MAX_BIRTH_THRESHOLD,
				MAX_BIRTH_THRESHOLD / 5);
		descriptors.put("BirthThreshold", birthThreshold);

		RangePropertyDescriptor initGrass = new RangePropertyDescriptor("InitGrass", 0, MAX_INIT_GRASS,
				MAX_INIT_GRASS / 4);
		descriptors.put("InitGrass", initGrass);

		RangePropertyDescriptor lossRateEnergy = new RangePropertyDescriptor("LossRateEnergy", 0, MAX_LOSS_RATE_ENERGY,
				MAX_LOSS_RATE_ENERGY / 5);
		descriptors.put("LossRateEnergy", lossRateEnergy);

		RangePropertyDescriptor lossReproductionEnergy = new RangePropertyDescriptor("LossReproductionEnergy", 0,
				MAX_LOSS_REPRODUCTION_ENERGY, MAX_LOSS_REPRODUCTION_ENERGY / 5);
		descriptors.put("LossReproductionEnergy", lossReproductionEnergy);
	}

	private void buildDisplay() {
		ColorMap map = new ColorMap();

		for (int i = 1; i < 16; i++) {
			map.mapColor(i, new Color(0, (int) (i * 8 + 127), 0));
		}

		// When noting, it is white
		map.mapColor(0, Color.WHITE);

		Value2DDisplay displayGrass = new Value2DDisplay(rabbitSpace.getGrassSpace(), map);

		Object2DDisplay displayRabbits = new Object2DDisplay(rabbitSpace.getRabbitSpace());
		displayRabbits.setObjectList(rabbitList);

		displaySurf.addDisplayable(displayGrass, "Grass");
		displaySurf.addDisplayable(displayRabbits, "Rabbits");

		rabbitsAndGrassInSpace.addSequence("Number of Rabbits in Space", new RabbitsInSpace());
		rabbitsAndGrassInSpace.addSequence("Amount of Grass", new GrassInSpace());
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

		class UpdateNumberOfRabbitsInSpace extends BasicAction {
			public void execute() {
				rabbitsAndGrassInSpace.step();
			}
		}

		schedule.scheduleActionAtInterval(1, new UpdateNumberOfRabbitsInSpace());

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
		String[] init_param = { "XSize", "YSize", "NumRabbits", "BirthThreshold", "InitGrass", "GrowthRateGrass",
				"LossRateEnergy", "LossReproductionEnergy"};//, "MinInitEnergy", "MaxInitEnergy" };
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

	public int getBirthThreshold() {
		return birthThreshold;
	}

	public void setBirthThreshold(int birthThreshold) {
		if (birthThreshold < 0) {
			System.out.println("birthThreshold should be positive! It is set to " + BIRTH_THRESHOLD + ".");
			birthThreshold = BIRTH_THRESHOLD;
		}
		this.birthThreshold = birthThreshold;
		this.setLossReproductionEnergy(this.getLossReproductionEnergy());
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
		} else if (lossReproductionEnergy > this.getBirthThreshold()) {
			System.out.println("lossReproductionEnergy should be smaller or equal to the birth threshold ("
					+ this.getBirthThreshold() + "). It is set to " + this.getBirthThreshold() + ".");
			lossReproductionEnergy = this.getBirthThreshold();
		}
		this.lossReproductionEnergy = lossReproductionEnergy;
		updatePanel();
	}
}
