# Introduction and problem description

For this exercise, we were asked to build a centralized agent for the pickup and delivery problem in order to make an efficient delivery plan for a whole compagny and coordinate the actions of its vehicles. The planning is build as a constraint satisfaction problem using the stochastic local search algorithm.

The goal of the compagny is to determine a plan for each of its vehicles such that:

* all tasks assigned to the compagny are delivered
* vehicles can carry more than one task at a time if their capacity allows them to
* the total revenue of the compagny (the sum of the individual revenues obtained by each vehicle) is maximized

# Solution: Encoding as a CSP

## Representation

We have $V=\{ v_1 , v_2, ... , v_{N_v}\}$ the set of available vehicles, $T=\{ t_1 , t_2 , ... , t_{N_T} \}$ the set of tasks to be delivered. We also use a structure called *Movement* which is either to pickup a specific task or to deliver it. We have thus $2N_T$ movements, 2 for each available tasks and $M=\{ m_1 , m_2 , ... , m_{2N_T} \}$ the set of movements.

For solving the constraint satisfaction optimization problem, we use the following variables :

* *nextMovement* : a ```HashMap``` of $2N_T$ variables. It contains one key per existing movement for which their values are the next movement to be performed. The value can be *NULL* and means that the key is the last movement done by a vehicle (and must thus have a *Deliver* action).
* *nextVehicleMovement* : a ```HashMap``` of $N_V$ variables. It contains one key per existing vehicle for which their values are the first movement to be performed by each vehicle (which contains always a *Pickup* action). The value can be *NULL* and means that the vehicle corresponding to the key will carry no task.

* *time* : this is the time when a movement is performed. This is represented in a ```HashMap<Movement, Integer>``` of $2N_T$ variables.
* *plans* : the list of movements ordered in performed time for each vehicle. This is represented as a ```HashMap<Vehicle, LinkedList<Movement>>```.

## Constraints

* $nextMovement(m) \neq m$
* $nextVehicleMovement(v) = m \Rightarrow time(m) = 1$.
* $nextVehicleMovement(v) = m \Rightarrow m.Action == Pickup$.
* $nextVehicleMovement(v) = m \Rightarrow m \in plans.get(v)$
* $nextMovement(m_i) = m_j \Rightarrow time(m_j) = time(m_i) + 1$
* $nextMovement(m_i) = m_j$ and $m_i \in plans.get(v) \Rightarrow m_j \in plans.get(v)$ 
* $nextMovement(m) = NULL \Rightarrow m.Action == Deliver$.
* For a task *t* and its corresponding movements $m_{pickup}$ and $m_{deliver}$: $time(m_{pickup}) < time(m_{deliver})$ : a task must be picked up before being delivered.
* For a task *t* and its corresponding movements $m_{pickup}$ and $m_{deliver}$: $m_{pickup} \in vehicle(v) \Leftrightarrow m_{deliver} \in vehicle(v)$ : the vehicle that picks up a task must deliver it and vice versa.
* All tasks must be delivered.
* The capacity of a vehicle cannot be exceeded at any time.

## Objective

As the reward for each plan will be exactly the same (we always deliver all tasks), the objective is to minimize the cost of delivering the tasks ($distance \times costPerKm$) for each vehicle:

<!--\begin{gather*}
  C = \sum_{i=1}^{2N_T} dist(m_i, nextMovement(m_i)) \times cost(vehicle(m_i)) + \sum_{k=1}^{N_V} dist(v_k, nextMovement(v_k)) \times cost(v_k)
\end{gather*} -->
\begin{gather*}
C = \sum_{i=1}^{N_V} \left( dist(v_i, nextVehicleMovement(v_i)) + \sum_{k=1}^{length(plan.get(v_i))-1} dist(m_{i,k}, m_{i,k+1})\right) \times costPerKm(v_i)
\end{gather*}
where $m_{i,k}$ is the *k*iest movement in the linked list $plan.get(v_i)$. When $a$ is a vehicle and $b$ a movement: $dist(a,b)$ is the distance bewteen the initial city of $a$ and the pickup city of *b.Task*. When $a$ and $b$ are both movements, dist(a,b) is the distance between *a.Task*'s *a.Action* city and *b.Task*'s *b.Action* city.

# Implementation

**Initial Solution:** For the initial solution, the set of tasks is spread over the set of vehicles: $task_i$ goes to $vehicle_{j}$ where $j = i \ mod \ N_{V}$ and the movements are such that a task is directly delivered after being picked up. So, in the initial solution, a vehicle carries only one task at a time. If the case where a task is heavier than the vehicle's capacity happens, we choose another vehicle for this task. If no vehicle is able to carry this task, a message is displayed and the normal execution continues resulting in a failure. 

**Action:** Is represented as an enumeration being either *Pickup* or *Deliver*.

**Movement:** A Movement is a ```Java``` class that contains:

* ```Task``` : the task to be taken or delivered
* ```Action``` : the action associated with that movement

**Solution states:** A solution state is reprensented by a ```Java``` class that contains :

* ```HashMap<Vehicle, Movement>``` : the first movement for each vehicle
* ```HashMap<Movement, Movement>``` : the next movement for each movement
* ```HashMap<Movement, Integer>``` : the time at which each movement is executed
* ```HashMap<Vehicle, LinkedList<Movement>>``` : an ordered plan of movements for each vehicle
* ```double cost``` : the cost for delivering all tasks in the plan

It also contains some useful functions : ```computeTime()``` to compute the times of each movement, ```computeVehiclePlans()``` to compute the plan for all vehicles, ```computeCost()``` to compute the plans cost.

**Constraints:** We have coded a class that contains methods to check the constraints presented above. We can note that our implementation should not create uncorrect states. For performance purposes, the constraints are checked only in the ```localChoice``` method, after having the cost of a solution which is promissing.

**Algorithm:** The *SLS* algorithm is implemented as in the algorithm 1 in the document "*Finding the Optimal Delivery Plan : Model as a Constraint Satisfaction Problem*":

* *SelectInitialSolution()* is implemented as explained in the above section.
* *ChooseNeighbours()* creates the neighbours candidates as follows: 1) moves the first task of one vehicle and gives it to another vehicle. The task is placed in the new vehicle's plan such that *Pickup* and *Deliver* of that task are the first two movements in the vehicle's plan. 2) change the positions of the movements related to a task in the plan of the vehicle carrying it. More specifically, it creates $O(n^2)$ neighbours by considering any possible combinations by moving the *Pickup* movement, or the *Deliver* movement, or both of a task, keeping the consistency such that *Pickup* happens before *Deliver*.
* *LocalChoice()* implemented as explained in the document execpted that we check if the constraints are satisfied before returning the choosen state.

**Termination:** We have 3 termination conditions :

* the maximum number of loops is reached (3000 in our code but should be adapted depending on the number of tasks to deliver).
* the number of loops where the best state doesn't change is reached (50).
* the number of loops where the best cost doesn't change is reached (1000), to avoid looping too much when only some local changes happen on a vehicle where tasks are simply swaped without changing the cost.

# Evaluation

**TODO**

parler du choix de *p*, du nombre de taches, du fait de rester coincer facilement dans des minima locaux...

On pourrait faire un joli graphe avec le cout pour plusieurs execution avec le meme nombre

# Conclusion

We can observe that this algorithm is quite good even if in some cases we can be stucked in a local minimum that can be far away from the optimal solution. Despite this, in general we have some good results not so far from the optimal solution. We know what to have a good solution we need to run this algorithm several times.
