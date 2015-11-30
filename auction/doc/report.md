# Introduction

In this assignment we have to implement an auction agent that must bid to acquire some tasks to deliver such that it has a maximum reward. During the auctions, a master proposes to each agent a task and they have to bid how much they want to be paid for delivering this task. The agent that proposes the smallest bid wins the task. Thus, our goal is to create an agent that maximizes its gain against other agents.

To reach this aim, we have at disposal the bids of others after the task is assigned to one agent, the probabilities of having a task in a city to another city. We don't know how much vehicles have the other agents, neither their size, their cost per km nor their start position.

In this report we will present first the modifications we made to the *centralized* agent to improve it. Then, we will present how our agent define how much it has to bid to maximize its gain. Finally, we will show some results and make a small conclusion.

# Centralized Agent

For this assignment, we chose to improve our *centralied* agent ot allow us to have a smaller marginal cost. The improvement we made are :

- Use arrays instead of HashMaps for performance;
- In the *Stochastic Local Search* algorithm, keep the best solution found (not the last one);
- Change the initial solution : now we give each task to the closest vehicle (considering last position of the vehicle);
    - We still have only one task in a vehicle for this initial state.
    - This changes a lot the performance of the algorithm.
- As we recompute the plan when a new task is proposed, we compute, at the end of auctions, a second time the plan and we keep the best one.

# Auction Agent

The agent has to bid for each task that is proposed to him knowing the previous bids of other agents, the probability distribution of tasks and its own configuration (vehicles, etc.).

## Strategy

The strategy we chose needs to compute some informations :

- The probability distribution of having a task in a city (computed once during setup);
- The Expectation of others bids;
- The Variance of others bid;
- The marginal cost (the difference of cost between taking the task or not).

With these informations, we can define the following strategy for bidding :

```java
Math.max(marginalCost, minBid + (maxBid - minBid) * (1 - probaFuture));
```

where ```minBid``` and ```maxBid``` are the minimal and maximal bid estimated from other agents using expectation and variance and ```probaFuture``` is the probability to have a task in the pickup or delivery city of the task we are bidding on. We compute ```1 - probaFuture``` because best is the chance to have a task in a city of the current task, more we want the task, thus lower we will bid.

## Considered Strategies

We also thought about different strategies for bidding. Here are some ideas we tought with reasons why we didn't keep them.

- Trying to compute the plan for other agents : it takes to much computations compared to the reliability of the results (we don't know how many vehicles, what vehicles, etc.);
- Considering the weight of tasks : it is already considered when we compute the plans and it is "useless" according to the others because we don't know what they have.

# Results

For evaluating our agent, we had to implement other simple agents. We chose to have greedy agents that have exactly the same *centralized* agent implementation (except that there are greedy), but they have different bid strategies :

- ```AuctionGreedyAgent``` : It always bids its marginal cost.
- ```AuctionRandomGreedyAgent``` : It bids its marginal cost + a random percentage of its marginal cost.


# Conclusion
