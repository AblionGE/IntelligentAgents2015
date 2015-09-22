import uchicago.src.sim.space.Object2DGrid;

/**
 * Class that implements the simulation space of the rabbits grass simulation.
 * 
 * @author
 */

public class RabbitsGrassSimulationSpace {

	private Object2DGrid agentSpace;
	private Object2DGrid grassSpace;

	public RabbitsGrassSimulationSpace(int xSize, int ySize) {
		agentSpace = new Object2DGrid(xSize, ySize);
		grassSpace = new Object2DGrid(xSize, ySize);

		// TODO: Put grass in space

	}

	public void addRabbit(RabbitsGrassSimulationAgent rabbit) {

	}

}
