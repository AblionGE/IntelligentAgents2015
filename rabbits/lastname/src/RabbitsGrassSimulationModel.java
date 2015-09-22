import java.util.ArrayList;

import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.engine.SimModelImpl;
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
	private static final int NUM_RABBITS = 20;
	private static final int X_SIZE = 20;
	private static final int Y_SIZE = 20;
	private static final int TOTAL_GRASS = 1000;
	private static final String NAME_DISPLAY = "Rabbits and Grass Simulation Window";

	private DisplaySurface displaySurf;

	private Schedule schedule;

	private int numRabbits = NUM_RABBITS;

	private ArrayList rabbitList;

	private RabbitsGrassSimulationSpace space;

	private int xSize = X_SIZE;
	private int ySize = Y_SIZE;

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

	private void buildDisplay() {
		// TODO Auto-generated method stub

	}

	private void builSchedule() {
		// TODO Auto-generated method stub

	}

	private void buildModel() {
		space = new RabbitsGrassSimulationSpace(xSize, ySize);

		// TODO : spread grass
		
		// TODO : add new rabbits
		
		
		// TODO : Spread rabbits
	}

	public String[] getInitParam() {
		String[] init_param = { "xSize", "ySize", "NumberOfRabbits", "birthThreshold", "grassGrowthRate" };
		return init_param;
	}

	public String getName() {
		return NAME;
	}

	public Schedule getSchedule() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setup() {
		space = null;
		rabbitList = new ArrayList();
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
}
