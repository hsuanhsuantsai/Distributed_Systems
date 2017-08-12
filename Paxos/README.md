# Paxos algorithm implementation
Suppose we have exchanges A1, A2, B1, B2, B3, C1, C2 across the world.  
In such system, a new value EUR/USD is introduced by one of the exchanges.

***
## Assumptions
1. To simplify, I hardcode the port numbers of each exchange in Exchange class so that grader doesn't need to enter all the port numbers.
2. To simplify, run each exchange in the same machine. i.e. hostname = localhost
3. At launch time, if the acceptor, the proposer sends proposal to, has not been launched, we catch the exception and abandon the proposal.
4. The first process is the first proposer. Once the process gets a number different from the current value, it can launch a proposal to other processes.
5. Once an exchange gets a new value of EUR/USD, it becomes a proposer.
6. I use a random number generator in exchange to create a new value for proposal.

## Usage
* How to compile: javac Exchange.java
* How to run: java Exchange < member number > < number of exchanges in the system >

## Running example
In hw5, we have 7 processes, so member number is from 1 to 7.  
number of exchange is 7

A1: java Exchange 1 7  
A2: java Exchange 2 7  
B1: java Exchange 3 7  
B2: java Exchange 4 7  
B3: java Exchange 5 7  
C1: java Exchange 6 7  
C2: java Exchange 7 7  

Output shows agree/reject in prepare phase and if a proposal is committed, print out the current value.

