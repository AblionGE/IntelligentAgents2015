import uchicago.src.sim.space.Object2DGrid;

/**
 * Class that implements the simulation space of the rabbits grass simulation.
 * 
 * @author Cynthia Oeschger and Marc Schaer
 */

public class RabbitsGrassSimulationSpace {

	private Object2DGrid rabbitSpace;
	private Object2DGrid grassSpace;

	/**
	 * 
	 * @param xSize	horizontal size of the grid
	 * @param ySize	vertical size of the grid
	 */
	public RabbitsGrassSimulationSpace(int xSize, int ySize) {
		rabbitSpace = new Object2DGrid(xSize, ySize);
		grassSpace = new Object2DGrid(xSize, ySize);

		// put no grass everywhere
		for (int i = 0; i < xSize; i++) {
			for (int j = 0; j < ySize; j++) {
				grassSpace.putObjectAt(i, j, new Integer(0));
			}
		}
	}

	/**
	 * Distributes randomly an amount of grass over the grid
	 * @param grass	amount of grass to distribute
	 */
	public void spreadGrass(int grass) {
		for (int i = 0; i < grass; i++) {
			int attempts = 5;
			boolean found = false;

			// We try to add grass to an element of the grid 5 times
			// if it is not added, we simply skip this piece of grass
			while (attempts > 0 && !found) {
				int x = (int) (Math.random() * (grassSpace.getSizeX()));
				int y = (int) (Math.random() * (grassSpace.getSizeY()));
				int currentGrass = getGrassAt(x, y);
				// We have 127 levels of Color for each element of the grid
				if (currentGrass < 127) {
					found = true;
					grassSpace.putObjectAt(x, y, new Integer(currentGrass + 1));
				}
				attempts--;
			}
		}
	}

	/**
	 * 
	 * @param 	x	horizontal position of the cell
	 * @param 	y	vertical position of the cell
	 * @return	the amount of grass in the cell
	 */
	public int getGrassAt(int x, int y) {
		int grass = 0;
		if (grassSpace.getObjectAt(x, y) != null) {
			grass = (Integer) grassSpace.getObjectAt(x, y);
		}
		return grass;
	}

	/**
	 * 
	 * @param 	x	horizontal position of the cell
	 * @param 	y	vertical position of the cell
	 * @return	agent contained in the cell
	 */
	public RabbitsGrassSimulationAgent getRabbitAt(int x, int y) {
		RabbitsGrassSimulationAgent rabbit = null;
		if (rabbitSpace.getObjectAt(x, y) != null) {
			rabbit = (RabbitsGrassSimulationAgent) rabbitSpace.getObjectAt(x, y);
		}
		return rabbit;
	}

	/**
	 * 
	 * @param 	x	horizontal position of the cell
	 * @param 	y	vertical position of the cell
	 * @return	true if an agent occupies the cell
	 */
	public boolean isCellOccupied(int x, int y) {
		if (rabbitSpace.getObjectAt(x, y) == null) {
			return false;
		}
		return true;
	}

	/**
	 * Add an agent in a random free cell in the grid
	 * @param rabbit	agent to add in the grid
	 * @return			true if succeed to add the agent
	 */
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

	/**
	 * 
	 * @param 	x	horizontal position of the initial cell
	 * @param 	y	vertical position of the initial cell
	 * @param 	newX	horizontal position of the destination cell
	 * @param 	newY	vertical position of the destination cell
	 * @return	true if succeed to move
	 */
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

	/**
	 * Removes the agent contained in the cell
	 * @param 	x	horizontal position of the cell
	 * @param 	y	vertical position of the cell
	 */
	public void removeRabbitAt(int x, int y) {
		rabbitSpace.putObjectAt(x, y, null);
	}

	/**
	 * Removes the whole grass contained in the cell
	 * @param 	x	horizontal position of the cell
	 * @param 	y	vertical position of the cell
	 * @return	the amount of grass that was in the cell
	 */
	public int eatGrassAt(int x, int y) {
		int grass = getGrassAt(x, y);
		grassSpace.putObjectAt(x, y, new Integer(0));
		return grass;
	}

	/**
	 * 
	 * @return the total amount of grass contained in the grid
	 */
	public int getTotalGrass() {
		int total = 0;
		for (int i = 0; i < grassSpace.getSizeX(); i++) {
			for (int j = 0; j < grassSpace.getSizeY(); j++) {
				if (grassSpace.getObjectAt(i, j) != null) {
					total = total + (Integer) grassSpace.getObjectAt(i, j);
				}
			}
		}
		return total;
	}

	public Object2DGrid getRabbitSpace() {
		return rabbitSpace;
	}

	public Object2DGrid getGrassSpace() {
		return grassSpace;
	}
}
