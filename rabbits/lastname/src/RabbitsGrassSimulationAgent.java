import java.awt.Color;
import java.awt.image.BufferedImage;
import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.space.Object2DGrid;

/**
 * Class that implements the simulation agent for the rabbits grass simulation.
 * 
 * @author Cynthia Oeschger and Marc Schaer
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

	/**
	 * 
	 * @param minEnergy			lower bound for initialization energy
	 * @param maxEnergy			upper bound for initialization energy
	 * @param lossRateEnergy	amount of energy unit lost per time unit
	 * @param birthThreshold	amount of energy needed to give birth
	 * @param lossReproductionEnergy	amount of energy lost after reproduction
	 * @param img				image of the agent
	 */
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

	/**
	 * Displays the image of the agent
	 * @param simG	Object where the image is displayed
	 */
	public void draw(SimGraphics simG) {
		// If the rabbt picture is loaded, use it, else use a blue Rect
		if (img != null) {
			simG.drawImageToFit(img);
		} else {
			simG.drawRect(Color.BLUE);
		}
	}
	
	/**
	 * Updates a random direction
	 */
	public void setVxVy() {
		vX = 0;
		vY = 0;
		while (((vX == 0) && (vY == 0)) || ((vX != 0) && (vY != 0))) {
			vX = (int) Math.floor(Math.random() * 3) - 1;
			vY = (int) Math.floor(Math.random() * 3) - 1;
		}
	}

	/**
	 * Prints informations on this agent
	 */
	public void report() {
		System.out.println(getID() + " at " + x + ", " + y + " has " + getEnergy());
	}

	/**
	 * The agent does the following things:
	 * 	- moves to a neighboring cell is it is possible
	 * 	- eats the grass after moving
	 * 	- loses an amount of energy
	 * 	- reproduces if it has enough energy
	 */
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

	/**
	 * 
	 * @return true if the agent can give birth
	 */
	public boolean isReproducing() {
		return reproductionStatus;
	}

	/**
	 * 
	 * @param newX destination's x coordinate
	 * @param newY destination's y coordinate
	 * @return true if the moving succeed
	 */
	private boolean tryMove(int newX, int newY) {
		return rabbitSpace.moveRabbitAt(x, y, newX, newY);
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
	
	public String getID() {
		return "rabbit number " + ID;
	}

	public int getEnergy() {
		return energy;
	}

	public void setSpace(RabbitsGrassSimulationSpace newSpace) {
		this.rabbitSpace = newSpace;
	}

	public static void setIDNumber(int iDNumber) {
		IDNumber = iDNumber;
	}

}
