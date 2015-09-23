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
			for (int j = 0; j < ySize; j++) {
				grassSpace.putObjectAt(i, j, new Integer(0));
			}
		}

	}

	public void spreadGrass(int grass) {
		for (int i = 0; i < grass; i++) {
			int x = (int) (Math.random() * (grassSpace.getSizeX()));
			int y = (int) (Math.random() * (grassSpace.getSizeY()));

			int currentGrass = getGrassAt(x, y);

			grassSpace.putObjectAt(x, y, new Integer(currentGrass + 1));
		}

	}

	public int getGrassAt(int x, int y) {
		int grass = 0;
		if (grassSpace.getObjectAt(x, y) != null) {
			grass = (Integer) grassSpace.getObjectAt(x, y);
		}
		return grass;
	}

	public RabbitsGrassSimulationAgent getRabbitAt(int x, int y) {
		RabbitsGrassSimulationAgent rabbit = null;
		if (rabbitSpace.getObjectAt(x, y) != null) {
			rabbit = (RabbitsGrassSimulationAgent) rabbitSpace.getObjectAt(x, y);
		}
		return rabbit;
	}

	public Object2DGrid getRabbitSpace() {
		return rabbitSpace;
	}

	public Object2DGrid getGrassSpace() {
		return grassSpace;
	}

	public boolean isCellOccupied(int x, int y) {
		if (rabbitSpace.getObjectAt(x, y) == null) {
			return false;
		}
		return true;
	}

	public boolean addRabbit(RabbitsGrassSimulationAgent rabbit) {
		boolean returnValue = false;
		int count = 0;
		int countLimit = 10 * rabbitSpace.getSizeX() * rabbitSpace.getSizeY();

		while (!returnValue && count < countLimit) {
			count++;
			int x = (int) (Math.random() * (rabbitSpace.getSizeX()));
			int y = (int) (Math.random() * (rabbitSpace.getSizeY()));

			if (!isCellOccupied(x, y)) {
				rabbitSpace.putObjectAt(x, y, rabbit);
				rabbit.setXY(x, y);
				rabbit.setSpace(this);
				returnValue = true;
			}
		}
		return returnValue;
	}

	public boolean moveRabbitAt(int x, int y, int newX, int newY) {
		boolean retVal = false;
		if (!isCellOccupied(newX, newY)) {
			RabbitsGrassSimulationAgent rabbit = (RabbitsGrassSimulationAgent) rabbitSpace.getObjectAt(x, y);
			removeRabbitAt(x, y);
			rabbit.setXY(newX, newY);
			rabbitSpace.putObjectAt(newX, newY, rabbit);
			retVal = true;
		}
		return retVal;
	}

	public int eatGrassAt(int x, int y) {
		int grass = getGrassAt(x, y);
		grassSpace.putObjectAt(x, y, new Integer(0));
		return grass;
	}

	public int getTotalGrass() {
		// TODO
		return 0;
	}

	public void removeRabbitAt(int x, int y) {
		rabbitSpace.putObjectAt(x, y, null);
	}
}
