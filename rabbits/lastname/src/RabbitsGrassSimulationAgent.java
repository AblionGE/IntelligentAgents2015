import java.awt.Color;
import java.awt.image.BufferedImage;
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
	private BufferedImage img = null;

	public RabbitsGrassSimulationAgent(int minEnergy, int maxEnergy, int lossRateEnergy, int birthThreshold,
			int lossReproductionEnergy, BufferedImage img) {
		x = -1;
		y = -1;
		energy = (int) ((Math.random() * (maxEnergy - minEnergy)) + minEnergy);
		this.lossRateEnergy = lossRateEnergy;
		this.birthThreshold = birthThreshold;
		this.lossReproductionEnergy = lossReproductionEnergy;
		reproductionStatus = false;
		setVxVy();
		if (IDNumber < Integer.MAX_VALUE) {
			IDNumber++;
		} else {
			IDNumber = 0;
		}
		ID = IDNumber;
		this.img = img;
	}

	public void draw(SimGraphics simG) {
		// If the rabbt picture is loaded, use it, else use a blue Rect
		if (img != null) {
			simG.drawImageToFit(img);
		} else {
			simG.drawRect(Color.BLUE);
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

	// Choose a direction
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
		
		// attempts to move
		int attempts = 5;

		while ((attempts > 0) && !hasMoved) {
			attempts--;
			//Choose a direction
			setVxVy();
			int newX = x + vX;
			int newY = y + vY;

			Object2DGrid grid = rabbitSpace.getRabbitSpace();
			newX = (newX + grid.getSizeX()) % grid.getSizeX();
			newY = (newY + grid.getSizeY()) % grid.getSizeY();

			// Move the rabbit
			if (tryMove(newX, newY)) {
				hasMoved = true;
			}
		}
		
		energy += rabbitSpace.eatGrassAt(x, y);

		// Reproduce the rabbit
		if (energy >= birthThreshold) {
			reproductionStatus = true;
			energy -= lossReproductionEnergy;
		}

		energy -= lossRateEnergy;
		report();
	}

	public boolean isReproducing() {
		return reproductionStatus;
	}

	private boolean tryMove(int newX, int newY) {
		return rabbitSpace.moveRabbitAt(x, y, newX, newY);
	}

	public static void setIDNumber(int iDNumber) {
		IDNumber = iDNumber;
	}

}
