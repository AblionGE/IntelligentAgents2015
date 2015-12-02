# Introduction

In this assignment we have to implement an auction agent that must bid to acquire some tasks to deliver such that it has a maximum reward. During the auctions, a master proposes to each agent a task and they have to bid how much they want to be paid for delivering this task. The agent that proposes the smallest bid wins the task. Thus, our goal is to create an agent that maximizes its gain against other agents.

To reach this aim, we have at disposal the bids of others after the task is assigned to one agent, the probabilities of having a task in a city to another city. We don't know how much vehicles have the other agents, neither their size, their cost per km nor their start position.

In this report we will present first the modifications we made to the *centralized* agent to improve it. Then, we will present how our agent defines how much it has to bid to maximize its gain. Finally, we will show some results and make a small conclusion.

# Centralized Agent

For this assignment, we improved our *centralized* agent to allow us to have a smaller marginal cost. The improvements are :

- Use arrays instead of HashMaps for performance;
- In the *Stochastic Local Search* algorithm, keep the best solution found (not the last one);
- Change the initial solution : now we give each task to the closest vehicle (considering last position of the vehicle);
    - We still have only one task in a vehicle for this initial state.
    - This changes a lot the performance of the algorithm because we avoid visiting higher local minima.
- As we recompute the plan when a new task is proposed, we compute, at the end of the auctions, a second time the plan and we keep the best one.

# Auction Agent

The agent has to bid for each task that is proposed to him knowing the previous bids of other agents, the probability distribution of the tasks and its own configuration (vehicles, etc.).

## Strategy

Our strategy needs to compute the following informations :

- The probability distribution of having a task (for *Pickup* and *Delivery*) in a city (computed once during setup);
- The Expectation of the adversary's bids;
- The Variance of the adversary's bids;
- The marginal cost (the cost difference for taking the new task and for not having it).

#### Distribution of the tasks

For the distribution of tasks, we simply read the probability distribution that is given to us through the *Logist* platform and we normalize each matrix (we have one matrix for *Pickup* tasks and one for *Delivery* tasks).
We will use these probabilities to act differently if there is a higher probability to come back in a city later than a given threshold (defined as $\frac{1}{topology.size()-1}$).


#### Adversary's bids statistics

The expectation and variance of the adversary's bids are computed incrementally. We store them in two ways:

- general statistics of all the adversary's bids: the expectation $\mu$ and the variance $\sigma^2$;
- path statistics of the adversary's bids for a task on route from city *a* to city *b*: the expectation $\mu_{ab}$ and the variance $\sigma_{ab}^2$.

Three scenarios can occur:

- First bid: don't use any statistics;
- First time that a task from city *a* to city *b* comes up: we use the general statistics and compute $B_{min} = \mu - 3\sigma$;
- A task from city *a* to city *b* already came up: we use the path statistics and compute $B_{min} = \mu_{ab} - 3\sigma_{ab}$.

$B_{min}$ is a reasonable approximation of the lowest bid from the adversary.

#### The case of the first task

At the beginning of auctions, the marginal cost is always a bit high depending on position of our vehicles. We observed that if we do not take it, the problem is that the adversary will have more chance to have a marginal cost lower than ours in the future. Indeed, the next tasks have more chances to be close from his vehicles or the paths of them (while we are still waiting to have task close from our vehicles).

To fight against this problem, we accept to loose some money during the 3 first tasks proposed until we have won one. Our bid for having the task is simply the cost for delivering it with the cheapest\footnote{The vehicle with the smallest cost per km.} vehicle.

#### Final bid

With these informations, we can define the following strategy for bidding :

```java
// totalNbOfTasks is the number of task that were proposed for auctions
// takenTask is the number of task that are currently taken + the one that is bidding
if(totalNbOfTasks < 4 && takenTask == 1) {
  return (long) task.pickupCity.distanceTo(task.deliveryCity) * minCostPerKm;
}

// Bid depending on the probability of having a future task in the same cities than the current task
double pFuture = Math.floor(futurePickupTasksProba[task.deliveryCity.id] * 1000.0) / 1000.0;
double dFuture = Math.floor(futureDeliveryTasksProba[task.pickupCity.id] * 1000.0) / 1000.0;
double threshold = 1/(double)(topology.size()-1);
if(pFuture > threshold || dFuture > threshold) {
  return (long) Math.max(minBid, totalBidExpectation + (marginalCost - totalBidExpectation)/2);
}

return (long) Math.max(marginalCost, marginalCost + (minBid - marginalCost)/2);
```

where ```minBid``` is the minimal bid estimated from other agents using expectation and variance.

Thus, we have 3 different strategies : one for the beginning of the auctions, one if the probability to have later a task in the city is high (we can have a bid smaller than the marginal cost) and one for the general case where we never loose some money.

## Considered Strategies

We also thought about different strategies for bidding. Here are some ideas we tought with the reasons why we didn't keep them.

- Trying to compute the plan for other agents : it takes to much computations compared to the reliability of the results (we don't know how many vehicles, what vehicles, etc.);
- Considering the weight of tasks : it is already considered when we compute the plans and it is "useless" according to the others because we don't know what they have.

# Results

For evaluating our agent (```AuctionOeschgerSchaerAgent```), we had to implement other simple agents. We chose to have agents that have exactly the same *centralized* agent implementation (greedy agents have ```p=1``` in the *SLS* algorithm), but they have different bid strategies :

- ```AuctionRandomGreedyAgent``` : It bids its marginal cost + a random percentage of its marginal cost.
- ```AuctionBidLastAgent``` : It bids the maximum between its marginal cost and the last winning bid of the adversary.
- ```AuctionDummyAgent``` : It bids always 0.

On the last page you can find results of tournaments (figures \ref{auction} and \ref{auction2}) between all of these agents. You can also find in the zip file a directory with all configurations and histories for these tournaments. We can observe in the history files that our agent makes a low bid for the first tasks and that after it adapts itself to the adversary's bids. It allows it to win against simple agents. It will not probably be as good as here against other agents that are really smart.

We also observed (with agents of same quality) that in certain configurations (map, vehicles, etc.) be the company A or B gives the agent an advantage (or a disadvantage) according to the position of tasks. It is probably what happens in figure \ref{auction2} between our agent and the ```RandomGreedy``` one.

# Conclusion

In conclusion, we can say that it is difficult to find a good strategy for bidding. Indeed, to improve a strategy we have to test it against other agents. As the behaviors of other agents can be really different, it is impossible to have a general strategy to be better than each of them. To be really good, we need more informations about others agents (like number of vehicles, positions, capacities, etc.). Even with that, it is not sure to be unbeatable.

\newpage{}

# Tournament results

Here are two results from two different tournaments. Further informations can be found in the zip file in the *tournament* directory.

\begin{figure}[!h]
  \centering \includegraphics[scale=0.6]{img/results.png}
  \caption{\it Tournament between our 4 agents for map England with 2 vehicles for each company with capacity 30, cost per km of 5 and 20 tasks of weight 3.}
  \label{auction}
\end{figure}


\begin{figure}[!h]
  \centering \includegraphics[scale=0.64]{img/results2.png}
  \caption{\it Tournament between our 4 agents for map Netherlands with 3 vehicles for company A (with capacity 60, cost per km of 5) and 2 vehicles for company B (with capacity 90, cost per km 5) and 15 tasks of weights between 10 and 35 (uniform distribution).}
  \label{auction2}
\end{figure}
