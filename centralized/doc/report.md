## Introduction and problem description

For this exercise, we were asked to build a centralized agent for the pickup and delivery problem in order to make an efficient delivery plan for a whole company and coordinate the actions of its vehicles. The planning is built as a constraint satisfaction problem using the stochastic local search algorithm.

The goal of the company is to determine a plan for each of its vehicles such that:

* all tasks assigned to the company are delivered;
* vehicles can carry more than one task at a time if their capacity allows them to;
* the total revenue of the company (the sum of the individual revenues obtained by each vehicle) is maximized;

## Solution: Encoding as a CSP

#### Representation

We have $V=\{ v_1 , v_2, ... , v_{N_v}\}$ the set of available vehicles, $T=\{ t_1 , t_2 , ... , t_{N_T} \}$ the set of tasks to be delivered. We also use a structure called *Movement* which is either to pickup a specific task or to deliver it. We have thus $2N_T$ movements, 2 for each available tasks and $M=\{ m_1 , m_2 , ... , m_{2N_T} \}$ the set of movements.

For solving the constraint satisfaction optimization problem, we use the following variables :

* *nextMovement* : a ```HashMap``` of $2N_T$ variables. It contains one key per existing movement for which their values are the next movement to be performed. The value can be *NULL* and means that the key is the last movement done by a vehicle (and must thus have a *Deliver* action).
* *nextVehicleMovement* : a ```HashMap``` of $N_V$ variables. It contains one key per existing vehicle for which their values are the first movement to be performed by each vehicle (which contains always a *Pickup* action). The value can be *NULL* and means that the vehicle corresponding to the key will carry no task.

* *time* : this is the time when a movement is performed. This is represented in a ```HashMap<Movement, Integer>``` of $2N_T$ variables.
* *plans* : the list of movements ordered in performed time for each vehicle. This is represented as a ```HashMap<Vehicle, LinkedList<Movement>>```.

#### Constraints

* $nextMovement(m) \neq m$
* $nextVehicleMovement(v) = m \Rightarrow time(m) = 1$.
* $nextVehicleMovement(v) = m \Rightarrow m.Action == Pickup$.
* $nextVehicleMovement(v) = m \Rightarrow m \in plans.get(v)$
* $nextMovement(m_i) = m_j \Rightarrow time(m_j) = time(m_i) + 1$
* $nextMovement(m_i) = m_j$ and $m_i \in plans.get(v_k) \Rightarrow m_j \in plans.get(v_k)$ 
* $nextMovement(m) = NULL \Rightarrow m.Action == Deliver$.
* For a task *t* and its corresponding movements $m_{pickup}$ and $m_{deliver}$: $time(m_{pickup}) < time(m_{deliver})$ : a task must be picked up before being delivered.
* For a task *t* and its corresponding movements $m_{pickup}$ and $m_{deliver}$: $m_{pickup} \in plans.get(v_k) \Leftrightarrow m_{deliver} \in plans.get(v_k)$ : the vehicle that picks up a task must deliver it and vice versa.
* All tasks must be delivered.
* The capacity of a vehicle cannot be exceeded at any time.

#### Objective

As the reward for each plan will be exactly the same (we always deliver all tasks), the objective is to minimize the cost of delivering the tasks ($distance \times costPerKm$) for each vehicle:

<!--\begin{gather*}
  C = \sum_{i=1}^{2N_T} dist(m_i, nextMovement(m_i)) \times cost(vehicle(m_i)) + \sum_{k=1}^{N_V} dist(v_k, nextMovement(v_k)) \times cost(v_k)
\end{gather*} -->

\begin{gather*}
C = \sum_{i=1}^{N_V} \left( dist(v_i, nextVehicleMovement(v_i)) + \sum_{k=1}^{length(plan.get(v_i))-1} dist(m_{i,k}, m_{i,k+1})\right) \times costPerKm(v_i)
\end{gather*}
where $m_{i,k}$ is the *k*-iest movement in the linked list $plan.get(v_i)$. When $a$ is a vehicle and $b$ a movement: $dist(a,b)$ is the distance bewteen the initial city of $a$ and the pickup city of *b.Task*. When $a$ and $b$ are both movements, dist(a,b) is the distance between *a.Task*'s *a.Action* city and *b.Task*'s *b.Action* city.

## Implementation

**Initial Solution:** For the initial solution, we give each task to one vehicle with a certain probability depending on its *costPerKM*. A vehicle with a lower cost will receive more tasks to deliver. Indeed, as described below, the initial state helps a lot the algorithm to have a better solution quickly. We can also note that a vehicle carries only one task at a time. If the case where a task is heavier than the vehicle's capacity happens, we choose another vehicle for this task. If no vehicle is able to carry this task, a message is displayed and the normal execution continues resulting in a failure. 

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

**Algorithm:** The *SLS* algorithm is implemented as in the algorithm 1 in the document "*Finding the Optimal Delivery Plan : Model as a Constraint Satisfaction Problem*" with some modifications. Nevertheless, it stays stochastic and local:

* *SelectInitialSolution()* is implemented as explained in the above section.
* *ChooseNeighbours()* creates the neighbours candidates as follows: 1) moves the first task of one vehicle and gives it to another vehicle. The task is placed in the new vehicle's plan such that *Pickup* is before *Deliver*\footnote{We consider all possible positions for both actions.}.  2) change the positions of the movements related to a task in the plan of the vehicle carrying it. More specifically, it creates $O(n^2)$ neighbours for each task by considering any possible combinations by moving the *Pickup* movement, or the *Deliver* movement, or both of a task, keeping the consistency such that *Pickup* happens before *Deliver*.
* *LocalChoice()* implemented as explained in the document excepted that we check if the constraints are satisfied before returning the choosen state and that we choose between the old solution and the new one outside of the function for performance purposes.

**Termination:** We have 3 termination conditions :

* the maximum number of loops is reached (5000 in our code but should be adapted depending on the number of tasks to deliver);
* the maximum time of execution is reached;
* the number of loops where the best cost doesn't change is reached (100), to avoid looping too much when only some local changes happen on a vehicle where tasks are simply swaped without changing the cost. This count is incremented only when the algorithm computes a new neighbouring state.

## Evaluation

We can observe that the simulation does not always result in an optimal plan. This happens because of the presence of local minima and that there are some random factors in the choices of neighbours, resulting in different possible plans for the same settings. The probability *p* to choose the old solution in the algorithm makes the algorithm converge faster but it can more easily result in a local minimum and not give an optimal plan. As proposed in the paper, we tested with several probabilities and the one that gives the best results is $p=0.4$.

By running several times the simulation, we observe that the total cost for the company depends on the number of tasks because, in general, with more tasks, more distance has to be travelled and the cost increases. The cost does not always depend on the number of vehicles: depending on the repartition of the tasks and the original location of the vehicles, an optimal plan can require that one or more vehicles carry no task, resulting in that case in a constant cost if there is an augmentation of the number of vehicles.

An optimal plan is not necessarily fair as it can be observed from Figure \ref{fig:tasks}. As there are random factors in the algorithm, these values can be different for any execution, especially, vehicle 4 can carry tasks.

The most expensive step of the algorithm in terms of running time is the function ```chooseNeighbours()``` which is $O(N_V + \left( \frac{N_t}{N_V}\right) ^3)$ if the tasks are well spread over the vehicles but at worst $O(N_V + N_t^3)$. The dependence of the number of tasks for the algorithm's complexity is then much more significative than the number of vehicles in the worst case and the increase of the number of vehicles is more likely to reduce the running time. It is illustrated in Figure \ref{fig:complexity}.

In Figure \ref{fig:costs}, we observe that we do not obtain always the best cost and that we can reach a local minimum. Nevertheless, we are always not so far from an optimal solution. We also see that 2 and 3 vehicles for these executions are better than 4. Figure \ref{fig:tasks} confirms this impression.

We also can note that this *SLS* algorithm has some bad sides. Indeed, if we have vehicles with really different *costPerKM* values, it seems normal that vehicles with lower cost per km will deliver more tasks. Nevertheless, it strongly depends on the initial state because, if the tasks are equally distributed, we will quickly fall in a local minimum without having any possibility to get out of it. The possible solution to this problem is to not select the best neighbour in ```localChoice()```, but to select each neighbour with a certain probability (maybe based on the cost of each solution). The algorithm will find a better solution but it needs more time to find it. The other solution, as we did, is to distribute initially the tasks depending on the *costPerKM* of each vehicle\footnote{It can also lead to worst performances because the \textit{chooseNeighbours()} method will have a lot of neighbours to compute for vehicles that have more tasks to manage.}.

## Conclusion

We can observe that this algorithm is better than having deliberative agents even if in some cases we can be stucked in a local minimum that can be far away from the optimal solution. We know that to have a good solution we need to run this algorithm several times and have several vehicles.

## Graphs

All of these graphs were done with a seed equal to 12345, for England map with vehicle 1 starting from Newcastle, vehicle 2 from Cardiff, vehicle 3 from Plymouth and vehicle 4 from Norwich. Each vehicle has a capacity of 30 kg and a cost per km of 5. Each task has a weight of 3 kg. Probability *p* is 0.4.

\begin{figure} [!h]
\minipage{0.49\textwidth}
  \begin{center}
    \begin{tabular}{|l|c|c|c|}
    \hline
    \textbf{Total nb of Tasks} & \bf10 & \bf20 & \bf30\\
    \hline
    \textbf{Vehicle 1} & 6, 7, 0 & 0, 4, 5 & 23, 0, 0\\
    \hline
    \textbf{Vehicle 2} & 0, 3, 5 & 11, 0, 0 & 0, 25, 13\\
    \hline
    \textbf{Vehicle 3} & 0, 0, 0 & 4, 4, 15  & 0, 0, 0\\
    \hline
    \textbf{Vehicle 4} & 4, 0, 5 & 5, 12, 0 & 7, 5, 17\\
    \hline
    \end{tabular}
  \end{center}
  \label{table}
  \caption{\it Number of tasks carried by each vehicle for 3 executions.}
  \label{fig:tasks}
\endminipage\hfill
\minipage{0.49\textwidth}
  \centering \includegraphics[scale=0.2]{img/complexity.png}
  \caption{\it Running time of the simulation for 1 execution. These values can change a lot depending on random factors from the algorithm. 300s is the maximum time of execution.}
  \label{fig:complexity}
\endminipage
\end{figure}

\begin{figure}[h!]
  \begin{center}
    \begin{tabular}{|l|c|c|c|}
      \hline
      \textbf{Costs} & \textbf{Minimal Cost} & \textbf{Maximal Cost} & \bf Average \\
      \hline
      \textbf{2 vehicles} & 13'661.5 & 17'420.5 & 14'836\\
      \hline
      \textbf{3 vehicles} & 11'732.5 & 16'932.5& 13'679\\
      \hline
      \textbf{4 vehicles} & 14'223.5 & 20'548 & 17'500 \\
      \hline
    \end{tabular}
    \caption{\it Minimal, maximal and average costs on 5 executions for 2, 3 and 4 vehicles and 30 tasks. The minimal cost is a suboptimal solution. The maximal one, is clearly not the worst possible plan, but a suboptimal solution, too.}
    \label{fig:costs}
  \end{center}
\end{figure}

