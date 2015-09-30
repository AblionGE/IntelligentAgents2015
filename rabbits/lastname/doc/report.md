# Introduction

In this report we will explain our solution to the first assignment of the Intelligent Agents course at EPFL. We will explain the choices that we have made and how we implemented them.

# Code description

## Default Variables

In addition to the compulsory variables defined in the assignment, here are the variables present in the code. As we are using *descriptors* to let the user change some of the variables, default maximum values were chosen to avoid reloading of the GUI:

* **Max size of Grid** : 101 x 101
* **Maximum number of rabbits** : 1'000
* **Maximum birth threshold** : 20
* **Maximum growth rate for grass** : 200
* **Maximum grass per element of the grid** : 128
* **Initial unit of grass** : maximum is 10'000
* **Energy lost per time unit** : maximum is 20
* **Energy loss for reproduction** : maximum is 20
* **Initial energy of a new rabbit** : random between 10 and 20

## Structure

We present here the most important methods of our code. You can find further informations on others directly in the source code.

```MainRabbit```: Main class that launches the simulation

```RabbitsGrassSimulationModel```: Class that implements the RePast simulation model. It contains the required methods for a RePast simulation as well as the settings for the sliders and the population plot.

```RabbitsGrassSimulationAgent```: Class that represents an agent of the simulation which is a rabbit. The main functions are:

* ```setVxVy()```: function that chooses randomly a direction between north, south, west and east
* ```step()```: function called at each time run. The rabbit moves to a neighbouring cell if possible, eats the grass after moving, loses an amount of energy and reproduces if it has enough energy.
* ```isReproducing()```: returns true if the rabbit has enough energy to give birth

```RabbitsGrassSimulationSpace```: Class that represents the environment of the simulation. The main functions are:

* ```spreadGrass(int grass)```: distributes randomly the amount of grass specified in the function's parameter over the grid. One cell can contain at most 127 unit of grass so it tries to spread a unit of grass at most 5 times and then it simply skips it.
* ```addRabbit(RabbitsGrassSimulationAgent rabbit)```: adds a rabbit randomly in a free cell of the grid
* ```moveRabbitAt(int x, int y, int newX, int newY)```: if the cell specified by ```newX``` and ```newY``` is free, moves the rabbit located in ```x``` and ```y``` to ```newX``` and ```newY``` and returns true. Returns false if the destination cell is already occupied.
* ```eatGrassAt(int x, int y)```: returns the amount of grass contained in the cell specified by ```x``` and ```y``` and removes all the grass from this cells

# Strategies

Here are the choices that we have made for our application to avoid unexpected behaviours:

## GUI:

* Additional sliders were added to control the simulation:
	* Energy loss rate
	* Energy loss for reproduction
* The energy lost during a reproduction cannot be bigger than the birth threshold.
* If the user wants to set a value outside of the available range, the GUI will update itself to the nearest available value and a message appears in the console.
* The initial number of rabbits must fit in the grid space. If the user sets more rabbits than the available space, the number of initial rabbits will be the number of cells in the grid.
* The user can set the amount of energy lost by a rabbit at each tick.
* During a run, the user can change the birth threshold and the grass growth rate to see the impact on the simulation. On the contrary, it is not possible to change the initial number of rabbits and the size of the grid during a run. These two variables are taken into account at each ```setup()``` of the simulation only.
* The initial value of sliders cannot be set, so it is always the minimum possible value.

## Simulation:

* At the creation of a rabbit, its energy is selected randomly and uniformly between 10 and 20.
* The IDs given to each rabbits start from 0 each time the ```setup()``` function is called.
* When a rabbit tries to move at each time unit:
	* It selects a direction randomly.
	* If the corresponding cell is available, the rabbit moves. Otherwise, it tries those two steps again.
	* After 5 failures to move, the rabbit gives up and stays in his current cell.
* If a rabbit cannot move, it does not eat.
* At each time unit, if the rabbit has reached the *birth threshold* amount of energy, it gives birth to a new rabbit and loses *loss energy reproduction* amount of energy.
* A new born rabbit is initialized in the same way as the rabbits are at the beginning of a run.
* A cell can contain at most 127 unit of grass to be able to have a green screen (and not a black one when there is more grass).

# Conclusion

We observed during this assignment the variations between number of rabbits and amount of grass. The plot helped us to confirm this feeling.  
We can observe that the number of rabbits as well as the number of grass units varies in a kind of sinusoidal fashion. Indeed, as the number of rabbits grows, they eat more grass. Then, the amount of grass decreases which implies that the rabbits don't have enough food any more and die one after another. As there is less rabbits, the amount of grass increases and the circuit starts again.



