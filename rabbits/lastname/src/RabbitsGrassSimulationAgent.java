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
	private int birthThreshold;
	private static int IDNumber = 0;
	private int ID;
	private RabbitsGrassSimulationSpace rabbitSpace;

	public RabbitsGrassSimulationAgent(int minEnergy, int maxEnergy, int birthThreshold) {
		x = -1;
		y = -1;
		energy = (int) ((Math.random() * (maxEnergy - minEnergy)) + minEnergy);
		this.birthThreshold = birthThreshold;
		setVxVy();
		IDNumber++;
		ID = IDNumber;
	}

	public void draw(SimGraphics simG) {
		// TODO : We could add images instead of simple colored circles
		if (energy > birthThreshold) {
			simG.drawCircle(Color.GREEN);
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
		// TODO : code a "step" -> move, eat grass, make a child
		int newX = x + vX;
		int newY = y + vY;

		Object2DGrid grid = rabbitSpace.getRabbitSpace();
		newX = (newX + grid.getSizeX()) % grid.getSizeX();
		newY = (newY + grid.getSizeY()) % grid.getSizeY();

		if(tryMove(newX, newY)){
			energy += rabbitSpace.eatGrassAt(x, y);
		}
		setVxVy();
		
		// TODO : Add make a child, lower energy?
	}

	private boolean tryMove(int newX, int newY){
		return rabbitSpace.moveRabbitAt(x, y, newX, newY);
	}

}
