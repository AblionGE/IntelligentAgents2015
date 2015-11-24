# Introduction

In this assignment...

# Centralized Agent

Modifications from previous assignment

# Auction Agent

## Strategy

We already know the previous plan without the new task (*oldPlan*).

These are pseudo-code of methods :

```java
public long askPrice(Task t) {
  newPlan = computePlan(newTasksSet): // tasks already taken + the new one
  marginalCost = (newPlan.cost - oldPlan.cost);
  if (marginalCost < 0) { // The new plan costs less than the old one
    return -marginalCost - x*(-marginalCost); // x (0-1) to define - as taking the task costs less
    // than before, we accept the task for almost nothing (possibly 0)
    // We need to define x according to others behavior (bids)
    // If they bid really small (sometimes a lot less than the marginalCost)
    // x should be big
  } 
  
  // Question : to the same thing in both cases ?
  if (maxOthersGain >= ourGain) { // We want the task !
    // We compute how much each other agent wants to win and we try to bid a bit less
    // x between 0 and 1
    return marginalCost + bidsMean * x; // or bidMin ?
  } else { // We think we are winning / first auction
    return marginalCost + somethingRandomButSmall;
  }

}
```

```java
public void auctionResult(Task previous, int winner, Long[] bids) {

  if (winner == agent.id()) {
    // Eventually recompute the plan once to see if better than the one computed
    // in askPrice
    // vehiclePlans = computeSLS(vehicles, tasksList);
  }
  for (long bid : bids) {
    bidsMean = 0;
    bidsMean += abs(bid - marginalCost);
  }
  // Todo : mean or median ? (possibly not consider bids that are too high or to small)
  // Keep for each opponent a mean value ?

  // Consider a stupid plan for others ? our plan for the winner of this task ?
  // Goal : compute gain of the winner of this auction 
}
```

# Results

# Conclusion
