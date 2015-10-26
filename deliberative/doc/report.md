# Introduction

In this progamming exercise, we implement the *Pickup and Delivery* problem with deliberative agents. These agents will compute a plan for picking up and delivering tasks using different algorithms like *Breadth First Search* or *A-Star* to go through a tree where each node is a state of the entire world known by the agent. The exploration of the tree stops when a goal state is reached.

In the next sections, we will first define the state representation with the goal states en the transitions between states. Then, we will present the main features of our code. Finally we will present some results given by our implementation and we will explain them.

# Representation of states, goals and transitions 

## State
We represent a state by a set of task carried by the agent, a set of task that are free and a set of tasks that are already delivered :

$$S = \{ (takenTasks, freeTasks, deliveredTasks) \}$$

## Goals
The goal states are the states where the *deliveredTasks* set is full of tasks and the *takenTasks* and *freeTasks* are empty.

$$S_g = \{ (\emptyset, \emptyset, deliveredTasks\}$$

## Transitions
The transition are simply defined by two actions :

* Move to pickup a task (move the a city a pickup a task in this city)
* Deliver a task (move to a city and deliver a task in this city)

## Old representations
Before having this representation, we had chosen to represent a state by a tuple $(agentPosition, \{(task, currentCityOfTask)\}$. This representation was more precise than our final one, but it led to a huge tree that takes a lot of time to be computed\footnote{The code to compute states was also more complicated because we had to compute every combinations of tasks position.i}.

After implementing that representation, we have chosen a representation where the state is composed exactly like our final one plus the agent position. We still had performance problems because of the number of created states\footnote{In our code, it is simple to come back to this representation. Indeed, we simply need to add the comparison between agent positions in the \textit{equals()} method of the state (the agent position is saved in the state even if it is not used).}.

# Implementation

## BFS
The BFS is simply coded as given in the exercise's slides. We can note that the ```visitedStates``` are stored in an ```ArrayList<Task>```.

## A-Star

The A-Star is also coded as given in the exercise's slides. We used a comparator ```PairComparator``` to order ```Pair``` objects (see below) for the children of a state accord to $f(n)$ function. The merge operation with the current queue is done using a *insertion sort*.

The $f(n) = g(n) + h(n)$ function is computed as follows :

* $g(n)$ is the current profit ($reward-cost$)
* h(n) is a heuristic function that simply computes the reward given by all the tasks that are not yet delivered and the cost to deliver them (we consider that we do not have to move to pickup a task). Thus, we assume that everything goes well. This heuristic is quite good because it will "eliminate" TODO : find a better one ?

## State

A State is represented by an object *State* which contains the agent's position, the three different sets of tasks, the current cost in this state, the current reward and the *f(n)* value for the *A-Star* algorithm.

The children of this state are computed with the ```Set<State> computeChildren(Vehicle vehicle)``` method and this method returns the reachable states (pickup\footnote{It also tests if the vehicle can carry the task.} a task or deliver one).

The $f(n)$ function is computed in ```computeF(Vehicle vehicle)``` method. We also need for creating the plan two functions that allow us to know if a task was picked up of delivered (```taskPickUpDifferences(State state)``` and ```taskDeliverDifferences(State state)```).

## Pair

TODO

# Results

# Conclusion
