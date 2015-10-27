# Introduction

In this progamming exercise, we implement the *Pickup and Delivery* problem with deliberative agents. These agents will compute a plan for picking up and delivering tasks using one of the following algorithms: *Breadth First Search* or *A-Star*. They both go through a tree where each node is a state of the entire world known by the agent. The exploration of the tree stops when a goal state is reached.
The agents will also be in competition with each other: whenever they face a change in the world due to another agent, they will recompute an optimal plan and continue.

In the next sections, we will first define the state representation with the goal states and the transitions between states. Then, we will present the main features of our code. Finally we will present some results given by our implementation and we will explain them.

# Representation of states, goals and transitions 

## State
We represent a state by four parameters: the agent's position, a set of tasks carried by the agent, a set of tasks that are free and a set of tasks that are already delivered :

$$S = \{ (position, takenTasks, freeTasks, deliveredTasks) \}$$

## Goals
The goal states are all the states where the *deliveredTasks* set is full of tasks, *takenTasks* and *freeTasks* are both empty and the position of the agent is a city where a delivery happend:

$$S_g = \{ (position,\emptyset, \emptyset, deliveredTasks\}$$

## Transitions
The transition are simply defined by two actions :

* Move to pickup a task (move to a city and pickup a task in this city)
* Deliver a task (move to a city and deliver a task in this city)

## Old representations
Before having this representation, we had chosen to represent a state by a tuple $(agentPosition, \{(task, currentCityOfTask)\}$. This representation was more precise than our final one, but it led to a huge tree that takes a lot of time to be computed\footnote{The code to compute states was also more complicated because we had to compute every combinations of tasks position.i}.

After implementing that representation, we have chosen a representation where the state is composed exactly like our final one plus the agent position. We still had performance problems because of the number of created states\footnote{In our code, it is simple to come back to this representation. Indeed, we simply need to add the comparison between agent positions in the \textit{equals()} method of the state (the agent position is saved in the state even if it is not used).}.

# Implementation

## State

A State is represented by an object *State* which contains the agent's position, the three different sets of tasks and the current reward. The sets of tasks are stored in ordered sets to speed up the calculations.

The children of this state are computed with the ```Set<State> computeChildren(Vehicle vehicle)``` method which returns the reachable states by picking a task or delivering one. It also tests if the vehicle can carry the task given its weight. We also need, in the later plan building, two functions that allow us to know if a task was picked up or delivered that are ```taskPickUpDifferences(State state)``` and ```taskDeliverDifferences(State state)```.

## Path

In order to store a sequence of states in the research tree, we created an object *Path* which stores a state and the path traveling to it. It also contains the profit made during this path and an expectation of the forthcoming profit. In order to save some computations, a boolean value is used to switch between the *A-star* and *BFS* algorithm, the later not needing any profit notion.

The heuristic function for the *A-star* algorithm is computed in ```totalReward(int costPerKm)```. The function ```travelDistance()``` computes the distance made by the vehicle through the associated path and ```estimateFutureDistance()``` estimates the remaining travel to deliver all the tasks.

## BFS

The BFS is simply coded as given in the exercise's slides. We can note that the ```visitedStates``` are stored in an ```ArrayList<State>``` and that we store the whole state path to a given node in it. We choosed to sort the successors depending on their distance with their parent to speed up the computations. This operation is done using a ```StateDistanceComparator```.

## A-Star

The A-Star is also coded as given in the exercise's slides. We used a comparator ```PathComparator``` to order ```Path``` objects to sort the sucessors according to the following heuristic function $f(n)$. The merge operation with the current queue is done using an *insertion sort*.

The $f(n) = g(n) + h(n)$ function is computed as follows :

* $g(n)$ is the current profit ($reward-cost$)
* h(n) is a heuristic function that simply computes the reward given by all the tasks that are not yet delivered and the cost to deliver them (we consider that we do not have to move to pickup a task). Thus, we assume that everything goes well. This heuristic is quite good because it will "eliminate" TODO : find a better one ?

# Results

# Conclusion
In this assignment we had to find a good strategy for the states and their successors as well as for the heuristic function in the *A-star* algorithm. Another challenge was to implement the programm in an efficient way to save space capacity and time during the optimal plan calculation.

As expected, the *A-star* planning is more efficient that the *BFS* planning which is still better than the naive one when looking at their profit and traveled distances. The *BFS* strategy takes more calculation time than *A-star*. Nevertheless, luck plays a big role when competing more than one agent.
