# Introduction

# Encoding the COP

## Representation

For solving this problem, we need several variables :

* *nextMovement* : this is the next action to perform. There exists two different actions :
    * *Pickup* : move to a city and take a task in the vehicle
    * *Deliver* : move to a city and deliver a task that is in the vehicle
* *nextVehicleMovement* : exactly like *nextMovement* but it represents the first action for a vehicle (which is always a *Pickup*)

    Each movement (or vehicle in case of *nextVehicleMovement*) is represented by a key in a ```HashMap``` and each value represents the next action to perform.

* *time* : this is the time when an action is performed. This is represented in a ```HashMap<Movement, Integer>```.
* *vehicle* : the list of vehicles and their assigned actions. This is represented as a ```HashMap<Vehicle, LinkedList<Movement>>``` which contains for each vehicle the ordered actions to perform.

## Constraints

We need to define some constraints to solve the *PDP* problem. The variable $x$ is used to define both actions.

* $nextMovement(x_i) \neq x_i$
* $nextVehicleMovement(v_i) = x_j \Rightarrow time(x_j) = 1$ : $nextVehicleMovement$ is always at the beginning.
* $nextVehicleMovement(x_i) \Rightarrow$ always a *Pickup* action.
* $nextMovement(x_i) = x_j \Rightarrow time(x_j) = time(x_i) + 1$
* $time(Pickup(t_i)) < time(Deliver(t_i))$ : a task must be picked up before delivered.
* $nextVehicleMovement(v_k) = x_i \Rightarrow x_i \in vehicle(v_k)$
* $nextMovement(x_i) = x_j \Rightarrow x_i \in vehicle(v_k), x_j \in vehicle(v_k)$ 
* $Pickup(t_i) \in vehicle(v_k) \Rightarrow Deliver(t_i) \in vehicle(v_k)$ : the vehicle that takes a task must deliver it.
* All tasks must be delivered.
* The capacity of a vehicle cannot be exceeded.

## Objective

As the reward for each plan will be exactly the same (we always deliver all tasks), the objective is simply to minimize the cost of delivering tasks ($distance \times costPerKM$). The formula to compute it is :

\begin{gather*}
  C = \sum_{i=1}^{2N} dist(x_i, nextMovement(x_i)) \times cost(vehicle(x_i)) + \sum_{k=1}^{M} dist(v_k, nextMovement(v_k)) \times cost(v_k)
\end{gather*}
where $dist(a,b)$ is the distance between task in action (or vehicle) $a$ and task in action (or vehicle) $b$. Variable $x$, as above, represents an action (*Pickup* or *Deliver*).

# Implementation

#### Initial Solution

#### Actions 

Actions are represented by *Java* classes and contain simply a task and the time they are executed by the vehicle.

# Conclusion
