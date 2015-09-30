# Introduction

In this report we will explain our solution to the first assignment of the Intelligent Agents course at EPFL. We will explain the choices that we have made and how we implemented them.

# Code description

## Default Variables

In addition to the compulsory variables defined in the assignment, here are the variables present in the code. As we are using *descriptors* to let the user change some of the variables, default maximum values were chosen to avoid reloading of the GUI:

* **Max size of Grid** : 101 x 101
* **Maximum number of rabbits** : 1'000
* **Maximum birth threshold** : 20
* **Maximum growth rate for grass** : 200
* **Initial unit of grass** : maximum is 10'000
* **Energy lost per time unit** : maximum is 20
* **Energy loss for reproduction** : maximum is 20
* **Initial energy of a new rabbit** : random between 10 and 20

## Structure

MainRabbit: Main class that lauches the simulation

RabbitsGrassSimulationModel: Class that implements the simulation model

RabbitsGrassSimulationAgent: Class that represents an agent of the simulation, here a rabbit

RabbitsGrassSimulationSpace: Class that represents the space of the simulation

# Strategies

Here are the choices that we have made for our application:

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


# Results

# Conclusion

We observed during this assignment the variations between number of rabbits and amount of grass. The plot helped us to confirm this feeling.
