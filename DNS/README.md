# DNS implementation
Please see DNS.png  
A, B, and C are connected by a circular ring.  
Rules:  
	* A can communicate with B and C and vice versa.
	* A* can only communicate with A.
	* B* can only communicate with B.
	* C* can only communicate with C.

## Assumptions 
1. A, A1 and A2 work in the same machine; B, B1, B2 and B3 work in the same machine; C, C1 and C2 work in the same machine.
2. To simplify the question, set A, B and C in the same machine. (Thus, set hostName = localhost.)
## Usage
* How to compile: javac SuperNode.java  
						 javac SubNode.java

* How to run:  
   1. java SuperNode < superServerPort > < subServerPort > < label >  
			superServerPort: server side port number for the other supernodes  
			subServerPort: server side port number for its subnodes  
			label: enter A, B, or C
	2. java SubNode < member number > < super port number > < self port number >  
			member number: if A1, enter 1; if B2, enter 2  
			super port number: port number of its supernode  
			self port number: server side port number for other nodes to connect to this agent

## Running example
1. launch A:  
	java SuperNode 5000 8001 A  
	left node host name: localhost  
	left node port number: 7000  
	right node host name: localhost  
	right node port number: 6000  
2. launch B:  
	java SuperNode 6000 8002 B  
	left node host name: localhost  
	left node port number: 5000  
	right node host name: localhost  
	right node port number: 7000  
3. launch C:  
	java SuperNode 7000 8003 C  
	left node host name: localhost  
	left node port number: 6000  
	right node host name: localhost  
	right node port number: 5000  
4. launch A1:  
	java SubNode 1 8001 5001  
5. launch A2:  
	java SubNode 2 8001 5002  
6. launch B1:  
	java SubNode 1 8002 6001  
7. launch B2:  
	java SubNode 2 8002 6002  
8. launch B3:  
	java SubNode 3 8002 6003  
9. launch C1:  
	java SubNode 1 8003 7001  
10. launch C2:  
	java SubNode 2 8003 7002  

