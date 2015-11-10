# Introduction

For this exercise, we were asked to build a centralized agent for the pickup and delivery problem in order to make an efficient delivery plan for a whole compagny and coordinate the actions of its vehicles. The planning is build as a constraint satisfaction problem using the stochastic local search algorithm.

# Problem description

The goal of the compagny is to determine a plan for each of its vehicles such that:

* all tasks assigned to the compagny are delivered
* vehicles can carry more than one task at a time if their capacity allows them to
* the total revenue of the compagny (the sum of the individual revenues obtained by each vehicle) is maximized

# Solution: Encoding as a CSP

## Representation

We use a structure called *Movement* which contains:

* a Task object 
* an Action which is either *Pickup* or *Deliver*.

We have $V=\{ v_1 , v_2, ... , v_{N_v}\}$ the set of available vehicles, $T=\{ t_1 , t_2 , ... , t_{N_T} \}$ the set of tasks to be delivered and $M=\{ m_1 , m_2 , ... , m_{2N_T} \}$ the set of movements containg two elements per task.

For solving the constraint satisfaction optimization problem, we use the following variables :

* *nextMovement* : a ```HashMap``` of $2N_T$ variables. It contains one key per existing movement for which their values are the next movement to be performed. The value can be *NULL* and means that the key is the last movement done by a vehicle (and must thus have a *Deliver* action).
* *nextVehicleMovement* : a ```HashMap``` of $N_V$ variables. It contains one key per existing vehicle for which their values are the first movement to be performed by each vehicle (which contains always a *Pickup* action). The value can be *NULL* and means that the vehicle corresponding to the key will carry no task.

* *time* : this is the time when a movement is performed. This is represented in a ```HashMap<Movement, Integer>``` of $2N_T$ variables.
* *plans* : the list of movements ordered in performed time for each vehicle. This is represented as a ```HashMap<Vehicle, LinkedList<Movement>>```.

## Constraints

* $nextMovement(m) \neq m$
* $nextVehicleMovement(v) = m \Rightarrow time(m) = 1$.
* $nextVehicleMovement(v) = m \Rightarrow$ m.Action == *Pickup*.
* $nextVehicleMovement(v) = m \Rightarrow m \in plans.get(v)$
* $nextMovement(m_i) = m_j \Rightarrow time(m_j) = time(m_i) + 1$
* $nextMovement(m_i) = m_j$ and $m_i \in plans.get(v) \Rightarrow m_j \in plans.get(v)$ 
* For a task *t* and its corresponding movements $m_{pickup}$ and $m_{deliver}$: $time(m_{pickup}) < time(m_{deliver})$ : a task must be picked up before being delivered.
* For a task *t* and its corresponding movements $m_{pickup}$ and $m_{deliver}$: $m_{pickup} \in vehicle(v) \Leftrightarrow m_{deliver} \in vehicle(v)$ : the vehicle that picks up a task must deliver it and vice versa.
* All tasks must be delivered.
* The capacity of a vehicle cannot be exceeded at any time.

## Objective

As the reward for each plan will be exactly the same (we always deliver all tasks), the objective is simply to minimize the cost of delivering tasks ($distance \times costPerKM$) of each vehicle:

<!--\begin{gather*}
  C = \sum_{i=1}^{2N_T} dist(m_i, nextMovement(m_i)) \times cost(vehicle(m_i)) + \sum_{k=1}^{N_V} dist(v_k, nextMovement(v_k)) \times cost(v_k)
\end{gather*} -->
\begin{gather*}
C = \sum_{i=1}^{N_V} \left( dist(v_i, nextVehicleMovement(v_i)) + \sum_{k=1}^{length(plan.get(v_i))-1} dist(m_k, m_{k+1})\right) \times cost(v_i)
\end{gather*}
where $dist(a,b)$ is the distance between task in action (or vehicle) $a$ and task in action (or vehicle) $b$. Variable $x$, as above, represents an action (*Pickup* or *Deliver*).

# Implementation

#### Initial Solution

For the initial solution, we distribute each task (*Pickup* and directly after it *Deliver*) to one vehicle ($task_i$ to $vehicle_{i \ mod \ nb_{vehicles}}$) if it can carry it. As each task is direclty delivered after has been picked up, a vehicle cannot carry a task only if the task is bigger than the vehicle capacity. If this case happens, we choose another vehicle. If no vehicle is able to carry this task, a message is displayed and the normal execution continues (it will fail !).

#### Actions 

An action is represented by a class (```Movement```) that contains a task and and which action it is (*Pickup* or *Deliver*).

#### Solution states

A solution state is reprensented by a ```Java``` class that contains :

* ```HashMap<Vehicle, Movement>``` : the first movement for each vehicle
* ```HashMap<Movement, Movement>``` : the next movement for each movement
* ```HashMap<Movement, Integer>``` : the time for each movement
* ```HashMap<Vehicle, LinkedList<Movement>>``` : the ordered plan for each vehicle
* ```double cost``` : the cost for delivering all tasks in the plan

It also contains some useful functions : ```computeTime()``` to compute the times of each movement, ```computeVehiclePlans()``` to compute the plan for all vehicles, ```computeCost()``` to compute the plans cost.

#### Constraints

To verfiy constraints, we have coded a class that contains methods to check constraints presented above. We can note that our implement does not create uncorrect states so we allow to check constraints only in the ```localChoice``` method when the cost of a solution is promissing. We do that for performance problem. Indeed, to verify each created state, it takes some time that makes the algorithm slower.

#### Algorithm

To compute the *SLS* algorithm, we have an approach similar to the paper given in example. Indeed, we keep, as we saw above, what is the next movement for each of them. It helps us to choose neighbours (```chooseNeighbours()```) : we select a vehicle and we create neighbours in giving the first picked up task (we must give both action of a task) to each other vehicles. Then, for the first selected vehicle, we simply consider each *Pickup* action and the related *Deliver* action. Then we create $O(n^2)$ neighbours (all possible combinations to put this two actions keeping the consistency\footnote{A \textit{Pickup} must be before the \textit{Delivery} of a task.}. So, we do not do as in the paper (we do not swap tasks), we simply move actions in the plan. Of course, each of these operations is done such that constraints are respected.

Afterwards, we select the best ```localChoice()``` and with probability *p* we select it (if not, we keep the previous state). We can note that for performance issues, we first decide if we keep the previous state (before computing neighbours) and if note, we compute neighbours and we select one of the best ones randomly.

#### Termination

About the termination, we have 3 conditions : the maximum number of loops is reached (3000 loops in our code, but it strongly depends on which kind of results\footnote{It depends if we want more precise results or not.} we want and on the number of tasks to deliver. The second condition is the number of loop where the best state is the same. This number is fixed to 50 to be sure to be on a local minimum. Finally, the third condition is a number of loops where the best computed cost is the same. Indeed, we observed that sometimes it continues to loop without improving the cost. This phenomenon is due to some local changes on a vehicle where tasks are simply swaped without changing the cost. This leads to a inifinite loop even if we are in a local minimum. We set this value to 1000 to avoid stopping the algorithm too quickly, in case a better solution is present in neighbours.

# Evaluation

**TODO**

parler du choix de *p*, du nombre de taches, du fait de rester coincer facilement dans des minima locaux...

On pourrait faire un joli graphe avec le cout pour plusieurs execution avec le meme nombre

# Conclusion

In conclusion, we can observe that this algorithm is quite good even if in some cases we can be stucked in a local minimum that can be far away of the optimal solution. Despite this, in general we have some good results not so far from the optimal solution. We know what to have a good solution we need to run this algorithm several times.
