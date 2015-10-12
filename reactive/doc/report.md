# Introduction

In this programming exercise, we implement the Pickup and Delivery problem with reactive agents. These agents will decide to deliver or not a task in a city depending on an offline computation of a *Markov Decision Process*. This MDP permits the agents to know in each state what to do to maximise the benefit of each action according to the reward given by the task and the cost of going to a specific city.

In the next sections, we will define the representation of states and actions, then we will define of we build the different matrices that are necessary to have a reactive agent. Next, we will make some remarks about our code. Finally, we will present different results given by our program.

# Representation of states and actions

## States

The world perception of an agent is represented by a tuple *S(i,$t_i$)* where *i* is the current city of the agent and $t_i$ is a task to city from city *i*. We can note that $t_i$ can be *null*.

In our matrices, we need to represent the task $t_i$ by $n-1$ columns (or rows) because we need to express the destinations of the task. Here is the schema of our matrices :

----------------------------------------------------------------------------------------
City0$\rightarrow$City1 City0$\rightarrow$City2 ... City0$\rightarrow$CityN City1$\rightarrow$ City0 City1$\rightarrow$City2 ... Cityn$\rightarrow$CityN-1
-------------- ------------ --- ------------ ------------- ------------ --- ------------

## Actions
The two possible actions are :

- $move_{ij}$ : Move to the closest neighbouring city (default action if there is no task)
- $deliver_{ij}$ : Deliver the task using the shortest path

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
    r(i,j) - cost(i,j) & \mbox{for} & a = deliver_{ij}\\
    - cost(i,j) & \mbox{for} & a = move_{ij}\\
  \end{array}\right.
$$

### The transition table *T(s,a,s')*

This definition is used in the definition of *T(s,a,s')* :

- $p(i,j)$ : the probability that in city *i* there is a task to city *j*

For T(s,a,s'), we have 3 cases :

- We deliver the task from city *i* to city *j*
- We move from city *i* to city *j* (it does not matter if there is a task or not in city *i*)
- All other cases


$$T(s(i,t_i),a,s'(j,t_j)) =
\left\{
  \begin{array}{rcl}
    p(i,j)*p(j,k) & \mbox{for} & a = deliver_{ij}, t_i \neq null, t_i \ is \ for \ city \ j, !closestNeighbours(j,k)\\
    p(i,j)*(p(j,k)*p(noTask)) & \mbox{for} & a = deliver_{ij}, t_i \neq null, t_i \ is \ for \ city \ j, closestNeighbours(j,k)\\
    p(j,k) & \mbox{for} & a = move_{ij}, closestNeighbour(i,j), !closestNeighbours(j,k)\\
    p(j,k)*p(noTask) & \mbox{for} & a = move_{ij}, closestNeighbour(i,j), closestNeighbours(j,k)\\
    0 & & otherwise\\
  \end{array}\right.
$$

# Implementation

In the implementation of the reactive agent, we coded all matrices presented above and the algorithm presentend in the assignment. All matrices are represented by arrays in 2 or 3 dimensions in the Java code. About the *value iteration* algorithm, we simply implemented it with loops and the *good enough* condition for stopping loops is that the biggest difference between an element of two succesive *V(S)* should be smaller than $\epsilon=0.0001$.

# Results
Here are the different graphs obtained running our implementation of the *value iteration* algorithm using *MDP*. For these, we have chosen different $\gamma$ values for 3 agents running simultaneously. We can note that each agent has only one vehicle in our implementation.
