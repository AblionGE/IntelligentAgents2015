# Introduction

In this programming exercise, we implement the Pickup and Delivery problem with reactive agents. These agents will decide to deliver or not a task in a city depending on an offline computation of a *Markov Decision Process*. This reinforcement learning permits the agents to know at each state what to do next so to maximise the benefit, which is a trade off between the reward given by the task and the cost of travelling.

In the next sections, we will define the representation of the states and actions, how we have build the different matrices that are necessary to have a reactive agent and make some remarks about our code. Finally, we will present different results given by our program.

# Representation of states and actions

## States

The world perception of an agent is represented by a tuple *S(i,$t_i$)* where *i* is the current city of the agent and $t_i$ is a task to city from city *i*. We can note that $t_i$ can be *null*.

In our matrices, we need to represent the task $t_i$ by $n-1$ columns (or rows) because we need to express the destinations of the task. Here is the schema of our matrices :

----------------------------------------------------------------------------------------
City0$\rightarrow$City1 City0$\rightarrow$City2 ... City0$\rightarrow$CityN City1$\rightarrow$ City0 City1$\rightarrow$City2 ... Cityn$\rightarrow$CityN-1
-------------- ------------ --- ------------ ------------- ------------ --- ------------

## Actions
The two possible actions are :

- $m_{ij}$ : Move to the closest neighbouring city (default action if there is no task)
- $d_{ij}$ : Deliver the task using the shortest path

# Definitions of reward and probability transition tables

To compute the *Value iteration* of the *Markov Decision Process*, we need first to compute the reward table *R(s,a)* and the transition table *T(s,a,s')*.

Here are the definitions of both tables :

### The reward table *R(s,a)*

This definition is used in the definition of *R(s,a)* :

- $r(i,j)$ : the reward given by a task delivered from city *i* to city *j* (taken from $s=(i,t_i)$, where $t_i$ is task for city *j*)
- $cost(i,j)$ : the cost to travel from city *i* to city *j* ($distance * cost \ per \ km$)


$$R(s,a) =
\left\{
  \begin{array}{rcl}
    r(i,j) - cost(i,j) & \mbox{for} & a = d_{ij}\\
    - cost(i,j) & \mbox{for} & a = m_{ij}\\
  \end{array}\right.
$$

### The transition table *T(s,a,s')*

This definition is used in the definition of *T(s,a,s')* :

- $p(i,j)$ : the probability that in city *i* there is a task to city *j*
- $CN(i,j)$ : *j* is the closest neighbour of *i*

For T(s,a,s'), we have 3 cases :

- We deliver the task from city *i* to city *j* and city *k* is not the closest neighbour of city *j*
- We deliver the task from city *i* to city *j* and city *k* is the closest neighbour of city *j*
- We move from city *i* to city *j* (it does not matter if there is a task or not in city *i*) and city *k* is not the closest neighbour of city *j*
  - We move from city *i* to city *j* (it does not matter if there is a task or not in city *i*) and city *k* is the closest neighbour of city *j*
- All other cases


$$T(s(i,t_i),a,s'(j,t_j)) =
\left\{
  \begin{array}{rcl}
    p(i,j)*p(j,k) & \mbox{for} & a = d_{ij}, t_i \neq null, t_i \ is \ for \ city \ j, !CN(j,k)\\
    p(i,j)*(p(j,k)+(1-\sum_kp(j,k))) & \mbox{for} & a = d_{ij}, t_i \neq null, t_i \ is \ for \ city \ j, CN(j,k)\\
    p(j,k) & \mbox{for} & a = m_{ij}, CN(i,j), !CN(j,k)\\
    p(j,k)+(1-\sum_kp(j,k)) & \mbox{for} & a = m_{ij}, CN(i,j), CN(j,k)\\
    0 & & otherwise\\
  \end{array}\right.
$$

# Implementation

In the implementation of the reactive agent, we coded all matrices presented above and the algorithm presentend in the assignment. All matrices are represented by arrays in 2 or 3 dimensions in the Java code. About the *value iteration* algorithm, we simply implemented it with loops and the *good enough* condition for stopping loops is that the biggest difference between an element of two succesive *V(S)* should be smaller than $\epsilon=0.0001$.

  Each time the agent chooses an action, it watches the vector $Best(S)$ which gives the best action to take knowing the current state (its position and the presence of a task or not for a specific destination). If it decides to move (or because there is no task), it will move to the closest neighbouring city.

# Results
Here are the different graphs obtained running our implementation of the *value iteration* algorithm using *MDP*. For these, we have chosen different $\gamma$ values for reactive and random agents that run simultaneously. We can note that each agent has only one vehicle in our implementation.

For our tests, we used the following options on the map "*France*" with 4 agents described below.
```xml
<tasks number="10" rngSeed="3590420242192152424">
    <probability distribution="uniform" min="0.0" max="1.0" />
    <reward distribution="constant" policy="long-distances" min="1000" max="99999" />
    <weight distribution="constant" value="3" />
    <no-task distribution="uniform" min="0.2" max="0.4" />
  </tasks>
```
Agents :

- Vehicle 1 : random agent, $\gamma = 0.85$
- Vehicle 2 : reactive agent, $\gamma = 0.05$
- Vehicle 3 : reactive agent, $\gamma = 0.65$
- Vehicle 4 : reactive agent, $\gamma = 0.85$


\includegraphics[width=4cm]{img/1random3reactiveFranceproba02to04.png}

## Comments
We can observe on these different graphs (which represent the reward per km) that the random agent is in all cases worst than the reactive agents. Another observation is that the reward per km and the reward per task (both of them are strongly correlated) become quite constant in the time. It means that the agents will have a constant gain over time.

About the choice of $\gamma$, we can observe that it does not influence so much the reward. Nevertheless, we observe something strange : it happens relatively often that when the $\gamma$ is small, the reward is slitely better than when the $\gamma$ is bigger. It surprises us because when $\gamma$ is bigger, the algorithm considers more step in the future to give the best action to the agent. Agents with a small $\gamma$ will take every task because it only consider a close future. On the contrary, agents with a bigger $\gamma$ should consider further in the future and take better decisions for having a better reward. We can maybe explain this behavior with the fact that rewards are a bit huge compared to the cost of travelling from one city to another (even if the city is the closest neighbour).
