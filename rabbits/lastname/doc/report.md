# Introduction

In this report we will explain our solution to the first assignment of the Intelligent Agents course at EPFL.

We will explain in this report choices we made and how we implemented them. It is structured as follows : first, we list default variables we chose. Then, we will explain our choices about variables that can be set by the user and choices about implementation. Finally, we will make a small conclusion.

# Default Variables
We chose these default variables for our program :

* **Grid** : 20 x 20
* **Max size of Grid** : 100 x 100
    * This is just to limit to a decent size the grid.
* **Number of rabbits** (initially) : 1
* **Maximum number of rabbits** : 100 x 100 / 10
    * This is just to limit the number of rabbit to a decent number
* **Initial Energy** : between 10 and 20
* **Birth threshold** : 20
* **Maximum birth threshold** : 100
* **Initial grass** : 500
* **Growth rate for grass** : 50
* **Maximum growth rate for grass** : 100 x 100 (size of the grid)
* **Energy loss rate** : 1
* **Energy loss when reproduction** : 5

These variables are set to (especially the ones that limit the maximum values) avoid weird behaviors and to have a nice GUI. 

# Choices

We chose to avoid the following things to have a working application :

* As we are using *descriptors* for chosing variables, a default maximum value was chosen to avoid reloading of GUI (which we were unable to do with *descriptors*).
* The initial number of rabbits should be smaller or equal to the size of the grid. If you try to choose more rabbits, the GUI will update itself to the maximum available size.
* All values the user can set should be positive or equal to 0 (except for the grid size). If it is not the case, the default value is set by the program and a message appears in the console. The default value can, in some cases, be the maximum one (for example, when the size of the grid is reduced, the initial number of rabbit is also reduced.
* To set the initial energy of each rabbit, we chose to take an arbitrary number between minimum and maximum initial energy variable (that can be set by the user).
* At each tick, each rabbit select a direction and move if it is possible. If there is already another rabbit, it try to move again (until 5 times). If it cannot move, it stays where it is.
* At each tick, if the rabbit has enough energy, it give birth to a new rabbit.
* The energy lost during a reproduction cannot be bigger than the birth threshold.
* A new born rabbit is initialized in the same way as the rabbits are at the beginning of a run.
* If the rabbit cannot move, it does not eat.
* The IDs given to each rabbit start from 0 each time the ```setup()``` function is called.
* The user can set how many energy is lost by a rabbit at each tick.

# Conclusion

We observed during this assignment the variations between number of rabbits and amount of grass. The plot helped us to confirm this feeling.
