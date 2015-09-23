import uchicago.src.sim.space.Object2DGrid;

/**
 * Class that implements the simulation space of the rabbits grass simulation.
 * 
 * @author
 */

public class RabbitsGrassSimulationSpace {

	private Object2DGrid rabbitSpace;
	private Object2DGrid grassSpace;

	public RabbitsGrassSimulationSpace(int xSize, int ySize) {
		rabbitSpace = new Object2DGrid(xSize, ySize);
		grassSpace = new Object2DGrid(xSize, ySize);

		for (int i = 0; i < xSize; i++) {
			for (int j = 0; i < ySize; j++) {
				grassSpace.putValueAt(i, j, new Integer(0));
			}
		}

	}
	
	public void spreadGrass(int grass) {
		// spread grass
	}
	
	public int getGrassAt(int x, int y) {
		
		return 0;
	}

	public int getRabbitAt(int x, int y) {
		
		return 0;
	}
	
	public Object2DGrid getRabbitSpace() {
		return rabbitSpace;
	}
	
	public Object2DGrid getGrassSpace() {
		return grassSpace;
	}
	
	public boolean isCellOccupied(int x, int y) {
		//TODO
		return false;
	}
	
	public void addRabbit(RabbitsGrassSimulationAgent rabbit) {
		//TODO
	}
	
	public boolean moveRabbitAt(int x, int y, int newX, int newY) {
		//TODO
		return false;
	}
	
	public int getTotalGrass() {
		//TODO
		return 0;
	}
	
	public void removeRabbitAt(int x, int y) {
		//TODO
	}
}
