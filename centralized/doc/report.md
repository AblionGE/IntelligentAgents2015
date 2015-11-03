# Introduction

# Encoding the COP

## Representation

For solving this problem, we need several variables :

* *nextAction* : this is the next action to perform. There exists two different actions :
    * *Pickup* : move to a city and take a task in the vehicle
    * *Deliver* : move to a city and deliver a task that is in the vehicle

    Each action is represented by a cell in an array and each cell contains the next action to perform. This array contains also cells for vehicle. The content of those cells is simply the first action to perform for each vehicle.

    The array is built as follows with $v$ for vehicle (there are $M$ vehicles), $t$ for task (there are $N$ tasks) :
    \begin{gather*}
    nextAction = [nextAction(Pickup(t_1)), ..., nextAction(Pickup(t_N)),\\
    nextAction(Deliver(t_1)), ..., nextAction(Deliver(t_n), nextAction(v_1),... NextAction(v_M)]
    \end{gather*}
* *time* : this is the time when an action is performed.
* *vehicle* : the list of vehicles and their assigned actions.

## Constraints

We need to define some constraints to solve the *PDP* problem. The variable $x$ is used to define both actions.

* $nextAction(x_i) \neq x_i$
* $nextAction(v_i) = x_j \Rightarrow time(x_j) = 1$ : $nextAction$ of a vehicle is always at the beginning.
* $nextAction(x_i) = x_j \Rightarrow time(x_j) = time(x_i) + 1$
* $time(Pickup(t_i)) < time(Deliver(t_i))$ : a task must be picked up before delivered.
* $nextAction(v_k) = x_i \Rightarrow vehicle(x_i) = v_k$
* $nextAction(x_i) = x_j \Rightarrow vehicle(x_i) = vehicle(x_j)$
* $vehicle(Pickup(t_i)) = vehicle(Deliver(t_i))$ : the vehicle that takes a task must deliver it.
* All tasks must be delivered.
* The capacity of a vehicle cannot be exceeded.

## Objective

As the reward for each plan will be exactly the same (we always deliver all tasks), the objective is simply to minimize the cost of delivering tasks ($distance \times costPerKM$). The formula to compute it is :

\begin{gather*}
  C = \sum_{i=1}^{N} dist(x_i, nextAction(x_i)) \times cost(vehicle(x_i)) + \sum_{k=1}^{M} dist(v_k, nextAction(v_k)) \times cost(v_k)
\end{gather*}
where $dist(a,b)$ is the distance between task in action (or vehicle) $a$ and task in action (or vehicle) $b$. Variable $x$, as above, represents an action (*Pickup* or *Deliver*).

# Implementation

#### Initial Solution

#### Actions 

Actions are represented by *Java* classes and contain simply a task and the time they are executed by the vehicle.

# Conclusion
