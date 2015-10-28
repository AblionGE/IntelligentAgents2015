# Introduction

In this progamming exercise, we implement the *Pickup and Delivery* problem with deliberative agents. These agents will compute a plan for picking up and delivering tasks using one of the following algorithms: *Breadth First Search* or *A-Star*. They both go through a tree where each node is a state of the entire world known by the agent. The exploration of the tree stops when a goal state is reached.
The agents will also be in competition with each other: whenever they face a change in the world due to another agent, they will recompute an optimal plan and continue.

In the next sections, we will first define the state representation with the goal states and the transitions between states. Then, we will present the main features of our code. Finally we will present some results given by our implementation and we will explain them.

# Representation of states, goals and transitions 

#### State
We represent a state by four parameters: the agent's position, a set of tasks carried by the agent, a set of tasks that are free and a set of tasks that are already delivered :

$$S = \{ (position, takenTasks, freeTasks, deliveredTasks) \}$$

#### Goals
The goal states are all the states where the *deliveredTasks* set is full of tasks, *takenTasks* and *freeTasks* are both empty and the position of the agent is a city where a delivery happend:

$$S_g = \{ (position,\emptyset, \emptyset, deliveredTasks\}$$

#### Transitions
The transitions are simply defined by two actions :

* Move to pickup a task (move to a city and pickup a task in this city)
* Deliver a task (move to a city and deliver a task in this city)

#### Old representations
Before having this representation, we had chosen to represent a state by a tuple $(agentPosition, \{(task, currentCityOfTask)\}$. This representation was more precise than our final one, but it led to a huge tree that takes a lot of time to be computed\footnote{The code to compute states was also more complicated because we had to compute every combinations of tasks position.}.

# Implementation

#### State

A State is represented by an object *State* which contains the agent's position, the three different sets of tasks and the current reward. The sets of tasks are stored in ordered sets to speed up the calculations.

The children of this state are computed with the ```Set<State> computeChildren(Vehicle vehicle)``` method which returns the reachable states by picking a task or delivering one. It also tests if the vehicle can carry the task given its weight. We also need, in the later plan building, two functions that allows us to know if a task was picked up or delivered that are ```taskPickUpDifferences(State state)``` and ```taskDeliverDifferences(State state)```.

#### Path

In order to store a sequence of states in the research tree, we created an object *Path* which stores a state and the path traveling to it. It also contains the profit made during this path and an expectation of the forthcoming profit. In order to save some computations, a boolean value is used to switch between the *A-star* and *BFS* algorithm, the later not needing any profit notion.

The heuristic function for the *A-star* algorithm is computed in ```totalReward(int costPerKm)```. The function ```travelDistance()``` computes the distance made by the vehicle through the associated path and ```estimateFutureDistance()``` estimates the remaining travel to deliver all the tasks.

About the estimation of the remaining travel distance, we sum up the distances between each free task's pickup location and delivery location. We also add the distance from the current agent's position to the closest free task's pickup location.

#### BFS

The BFS is simply coded as given in the exercise's slides. We chose to sort the successors depending on the distance with their parent to speed up the computations. This operation is done using a ```StateDistanceComparator```. It also gives a better solution because the tree is built in a more optimal way than randomly.

We also can note that, as we have only two actions (*pickup* and *deliver*), all branches will have the same depth, so a *DFS* algorithm should be better than a *BFS* in terms of time complexity.

#### A-Star

The A-Star is also coded as given in the exercise's slides. We used a comparator ```PathComparator``` to order ```Path``` objects to sort the successors according to the following heuristic function $f(n)$. The merge operation with the current queue is done using an *insertion sort*.

The $f(n) = g(n) + h(n)$ function is computed as follows :

* $g(n)$ is the current profit ($reward-cost$)
* h(n) is a heuristic function that computes the reward given by all the tasks that are not yet delivered and the cost to deliver them. The cost is computed considering moving to each delivery city and moving to each pickup city (if the task is not yet in the vehicle). In this way we consider the expected profit. Paths with unpromising profits will be put at the end of the queue such that we will have the best solution for this problem in the head of the queue.

# Results

Here are the results obtained by an execution on *Switzerland* map with a seed of $23456$ with 1,2 and 3 agents for three different implementations (*naive*, *BFS*, *A-star*). Each vehicle has a *costPerKm* of 5 and the capacity is set to 30 with task of weight 3. The *history.xml* files can be found in the *histories* folder. There are also some other results in this folder\footnote{Other results are done on map \textit{France} with tasks of weight 10 and a seed equal to 543876. With these other results, we observe the same phenomenon as with the ones with map of \textit{Switzerland}}.

#### Total Profit

The total profits for 1 to 3 agents for each algorithm

\begin{figure} [!h]
  \begin{center}
    \begin{tabular}{|l|c|c|c|}
    \hline
    \textbf{Nb of Agents} & \bf1 & \bf2 & \bf3\\
    \hline
    \textbf{Naive} & 242'357 & 238'957 & 235'257\\
    \hline
    \textbf{BFS} & 252'307 & 249'907 & 246'957\\
    \hline
    \textbf{A*} & 254'657 & 250'707 & 248'257\\
    \hline
    \end{tabular}
  \end{center}
  \caption{Total profit for each algorithm with 1,2 and 3 agents for \textit{Switzerland} map with a seed of $23456$. Agent 1 starts in Lausanne, Agent 2 starts in Zürich and Agent 3 starts in Bern.}
\end{figure}


#### Number of states created and visited

This table gives the number of states created and visited by only one agent. Indeed, when there are several ones, the entire tree is recomputed by some agents and the size of the tree depends strongly on the other agents and the current state of the world.

<!--\begin{figure} [!h]
  \begin{center}
    \begin{tabular}{|l|c|c|}
    \hline
    & \bf Created & \bf Visited \\
    \hline
    \textbf{BFS} & 2479 & 2455 \\
    \hline
    \textbf{A*} & 1657 & 535\\
    \hline
    \end{tabular}
  \end{center}
  \caption{Number of states created and visited for 1 agent for \textit{Switzerland} map with a seed of $23456$}
\end{figure} -->
\begin{figure}[h!]
      \centering \includegraphics[scale=0.5]{img/nodes.png}
      \caption{\it Number of states created (dashed) and visited (solid) for 1 agent depending on the number of task for \textit{Switzerland} map with a seed of $23456$. BFS is blue and AStar is red.}
\end{figure}

## Comments

We can observe with these results that, as expected, the *A-star* algorithm is the best one and the *naive* one is the worst. Indeed, the heuristic function gives us a more optimal exploration of nodes while the *BFS* simply goes through nodes without considering any future profit. We also can note that the *BFS* is quite good because of the ordering of the successors when creating them. If we don't, the *BFS* will give a lower profit but still perform better than the naive algorithm because it considers taking more than one task at a time.

We also observe that the number of created and visited states is clearly lower with the *A-star* algorithm than with *BFS* which leads to a better time complexity for *A-star*. It is an expected result because *A-star* finds a goal state faster due to the heuristic function.

# Conclusion
In this assignment we had to find a good strategy for the states and their successors as well as for the heuristic function in the *A-star* algorithm. Another challenge was to implement the program in an efficient way to save space capacity and time during the optimal plan calculation.

As expected, the *A-star* planning is more efficient than  the *BFS* planning which is still better than the naive one when looking at their profit and traveled distances. The *BFS* strategy takes more calculation time than *A-star*. Nevertheless, luck plays a big role when competing more than one agent.
