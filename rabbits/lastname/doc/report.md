# Introduction

In this report we will explain our solution to the first assignment of the Intelligent Agents course at EPFL.

We will explain in this report choices we made and how we implemented them. It is structured as follows : first, we list default variables we chose. Then, we will explain our choices about variables that can be set by the user. Finally,...

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

