import java.awt.Color;

import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.space.Object2DGrid;

/**
 * Class that implements the simulation agent for the rabbits grass simulation.
 * 
 * @author
 */

public class RabbitsGrassSimulationAgent implements Drawable {

	private int x;
	private int y;
	private int vX;
	private int vY;
	private int energy;
	private int lossRateEnergy;
	private int birthThreshold;
	private int lossReproductionEnergy;
	private boolean reproductionStatus;
	private static int IDNumber = 0;
	private int ID;
	private RabbitsGrassSimulationSpace rabbitSpace;

	public RabbitsGrassSimulationAgent(int minEnergy, int maxEnergy, int lossRateEnergy, int birthThreshold,
			int lossReproductionEnergy) {
		x = -1;
		y = -1;
		energy = (int) ((Math.random() * (maxEnergy - minEnergy)) + minEnergy);
		this.lossRateEnergy = lossRateEnergy;
		this.birthThreshold = birthThreshold;
		this.lossReproductionEnergy = lossReproductionEnergy;
		reproductionStatus = false;
		setVxVy();
		IDNumber++;
		ID = IDNumber;
	}

	public void draw(SimGraphics simG) {
		// TODO : We could add images instead of simple colored circles
		if (energy >= birthThreshold) {
			simG.drawCircle(Color.BLUE);
		} else {
			simG.drawCircle(Color.RED);
		}
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public void setXY(int newX, int newY) {
		x = newX;
		y = newY;
	}

	public void setVxVy() {
		vX = 0;
		vY = 0;
		while (((vX == 0) && (vY == 0)) || ((vX != 0) && (vY != 0))) {
			vX = (int) Math.floor(Math.random() * 3) - 1;
			vY = (int) Math.floor(Math.random() * 3) - 1;
		}
	}

	public String getID() {
		return "rabbit number " + ID;
	}

	public int getEnergy() {
		return energy;
	}

	public void setSpace(RabbitsGrassSimulationSpace newSpace) {
		this.rabbitSpace = newSpace;
	}

	public void report() {
		System.out.println(getID() + " at " + x + ", " + y + " has " + getEnergy());
	}

	public void step() {
		reproductionStatus = false;
		boolean hasMoved = false;
		int attempts = 5;

		while ((attempts > 0) && !hasMoved) {
			attempts--;
			setVxVy();
			int newX = x + vX;
			int newY = y + vY;

			Object2DGrid grid = rabbitSpace.getRabbitSpace();
			newX = (newX + grid.getSizeX()) % grid.getSizeX();
			newY = (newY + grid.getSizeY()) % grid.getSizeY();

			// Move the rabbit
			if (tryMove(newX, newY)) {
				energy += rabbitSpace.eatGrassAt(x, y);
				hasMoved = true;
			}
		}

		// Reproduce the rabbit
		if (energy >= birthThreshold) {
			reproductionStatus = true;
			energy -= lossReproductionEnergy;
		}

		energy -= lossRateEnergy;
	}

	public boolean isReproducing() {
		return reproductionStatus;
	}

	private boolean tryMove(int newX, int newY) {
		return rabbitSpace.moveRabbitAt(x, y, newX, newY);
	}

}
