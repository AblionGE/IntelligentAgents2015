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

# Results

# Conclusion
