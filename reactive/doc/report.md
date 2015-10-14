# Introduction

In this programming exercise, we implement the Pickup and Delivery problem with reactive agents. These agents will decide to deliver or not a task in a city depending on an offline computation of a *Markov Decision Process*. This reinforcement learning permits the agents to know at each state what to do next so to maximise the benefit, which is a trade off between the reward given by the task and the cost of travelling.

In the next sections, we will define the representation of the states and actions, how we have built the different matrices that are necessary to have a reactive agent and make some remarks about our code. Finally, we will present different results given by our program.

# Representation of states and actions

## States

The state representation of the world is given by the tuples *S(i,$t_{ij}$), $\forall$ 0 $\leq$ i,j < N, i$\neq$j* where *N* is the number of cities, *i* represents a city and $t_{ij}$ represents a task to be delivered to city *j*. Note that $t_{ij}$ can be *null* when no task is available.

Here is the schema of the array of states used in the code, written in the form *i$\rightarrow t_{ij}$*:

----------------------------------------------------------------------------------------
City0$\rightarrow$City1 ... City0$\rightarrow$CityN City0$\rightarrow$null City1$\rightarrow$ City0 City1$\rightarrow$City2 ... CityN$\rightarrow$CityN-1 CityN$\rightarrow$null
-------------- ------------ --- ------------ ------------- ------------ --- ------------

## Actions
We have two possible actions which are :

- $m_{ij}$ : Move from city *i* to *j* where *j* is *i*'s nearest neighbour (default action if there is no task)
- $d_{ij}$ : Deliver the task from *i* to *j* using the shortest path

# Definitions of reward and probability transition tables

To compute the *Value iteration* of the *Markov Decision Process*, we need first to compute the reward table *R(s,a)* and the transition table *T(s,a,s')*:

### The reward table *R(s,a)*

To avoid an agent to be in a state $s(i,t_{ij}=null)$ with $a = d_{ij}$ which should be impossible, we set the reward for such combinations to be the less possible value: $- Double.MAX\_VALUE$. Here are the functions needed to build *R(s,a)* :

- $r(i,j)$ : the reward given by a task $t_{ij}$ delivered from city *i* to city *j* (from state $s=(i,t_{ij})$)
- $cost(i,j)$ : the cost to travel from city *i* to city *j* ($distance * cost \ per \ km$)


$$R(s,a) =
\left\{
  \begin{array}{rcl}
  	- cost(i,j) & \mbox{for} & a = m_{ij}\\
    r(i,j) - cost(i,j) & \mbox{for} & a = d_{ij}, t_{ij} \neq null\\
    - Double.MAX\_ VALUE & \mbox{for} & a = d_{ij}, t_{ij} = null\\
  \end{array}\right.
$$

### The transition table *T(s,a,s')*

Here are the functions needed to build *T(s,a,s')* :

- $p(i,j)$ : the probability that in city *i* there is a task $t_{ij}$
- $P(i)$ = $\sum_{j=0,j\neq i}^N$ $p(i,j)$ : the probability that there is a task in city *i*
- $CN(i,j)$ : true if *j* is the closest neighbour of *i*

The probability to arrive in the state $s'(i,t_{ij})$ is $p(i,j)$ for $t_{ij}\neq null$. When there is no task, i.e. $t_{ij} = null$, the probability is $(1-P(i))$. Like this, we have the property that $\sum_{s'} T(s,a,s') = 1$. $T(s,a,s')$ depends on each action:

- if $a = d_{ij}$, the only non-zero entries correspond to the states $s(i,t_{ij})$ and $s'(j,t_{jk})$.
- if $a = m_{ik}$, the only non-zero entries correspond to the states $s(i,t_{ij})$ and $s'(k,t_{kl})$ such that *k* is *i*'s nearest neighbour.

$$T(s(i,t_{ij}),a,s'(k,t_{kl})) =
\left\{
  \begin{array}{rcl}
    p(k,l) & \mbox{for} & a = d_{ij},j=k, t_{ij} \neq null, t_{kl} \neq null\\
    1 - P(k) & \mbox{for} & a = d_{ij},j=k, t_{ij} \neq null, t_{kl} = null\\
    p(k,l) & \mbox{for} & a = m_{ik}, CN(i,k), t_{kl} \neq null\\
    1 - P(k) & \mbox{for} & a = m_{ik}, CN(i,k),t_{kl} = null\\
    0 & & otherwise\\
  \end{array}\right.
$$

# Implementation

In the implementation of the reactive agent, we coded all matrices presented above and the algorithm presented in the assignment. All matrices are represented by arrays in 2 or 3 dimensions in the Java code. About the *value iteration* algorithm, we simply implemented it with loops and the *good enough* condition for stopping loops is that the biggest difference between an element of two successive *V(S)* should be smaller than $\epsilon=0.0001$.

The agent has two behaviours when arriving in a city *i*:

- If there is no task (i.e. $t_{ij}=null$), it goes to the nearest city to lose as less money as possible.
- If there is a task $t_{ij} \neq null$, the agent chooses the best action *a* by picking in the vector $Best(S)$ the element corresponding to its state $s(i,t_{ij})$. If $a = m_{ik}$, the agent doesn't take the task and moves to the nearest city *k*. If $a = d_{ij}$, the agent delivers to city *j*.

# Results
Here are the different graphs of reward with reactive agents having learned their optimal stategy using *MDP*. They show reactive agents with different $\gamma$ values and random agents run simultaneously. Note that each agent has only one vehicle in our implementation.

For our tests, we used the following options on the map "*France*" with 4 agents described below.
```xml
<tasks number="10" rngSeed="3590420242192152424">
    <probability distribution="uniform" min="0.0" max="1.0" />
    <reward distribution="constant" policy="long-distances" min="1000" max="99999" />
    <weight distribution="constant" value="3" />
    <no-task distribution="uniform" min="0.2" max="0.4" />
  </tasks>
```

In the following graphs representing *reward per km*, the blue line is always the random agent (85\% to take a task) and **TO COMPLETE**

\begin{figure}[h!]
   \begin{minipage}[b]{0.40\linewidth}
      \centering \includegraphics[scale=0.06]{img/1random3reactiveFranceproba02to04.png}
      \caption{\it V1 ($\gamma=0.05$), V2 ($\gamma=0.45$)}
   \end{minipage}\hfill
   \begin{minipage}[b]{0.48\linewidth}   
      \centering \includegraphics[scale=0.06]{img/1random3reactiveFranceproba02to04.png}
      \caption{\it V1 ($\gamma=0.05$), V2 ($\gamma=0.45$)}
   \end{minipage}
   \begin{minipage}[b]{0.40\linewidth}
      \centering \includegraphics[scale=0.06]{img/1random3reactiveFranceproba02to04.png}
      \caption{\it V1 ($\gamma=0.05$), V2 ($\gamma=0.45$)}
   \end{minipage}\hfill
   \begin{minipage}[b]{0.48\linewidth}   
      \centering \includegraphics[scale=0.06]{img/1random3reactiveFranceproba02to04.png}
      \caption{\it V1 ($\gamma=0.05$), V2 ($\gamma=0.45$)}
   \end{minipage}
\end{figure}

## Comments
We can observe on these different graphs (which represent the reward per km) that the random agent is in all cases worst than the reactive agents. Another observation is that the reward per km and the reward per task\footnote{You can observe values in the console during a run.} (both of them are strongly correlated) become quite constant in the time. It means that the agents will have a constant gain over time.

About the choice of $\gamma$, we can observe that it does not influence so much the reward. Indeed, we see that rewards are close to each other. Nevertheless, we observe something strange : it happens sometimes that when the $\gamma$ is small, the reward is slitely better than when the $\gamma$ is bigger. It surprises us because when $\gamma$ is bigger, the algorithm considers more step in the future to give the best action to the agent. Agents with a small $\gamma$ will take more tasks because it only considers a close future. We can maybe explain this behavior with the fact that rewards are a bit huge compared to the cost of travelling from one city to another (even if the city is the closest neighbour). We also observe that the ```Best(S)``` vectors for different $\gamma$ is often the same (or with 2-3 differences) and the randomness of task appearance makes that some agents with smaller $\gamma$ have a better reward than others with a bigger $\gamma$.
