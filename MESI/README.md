# MESI protocol implementation
ref: https://www.scss.tcd.ie/Jeremy.Jones/vivio/caches/MESIHelp.htm
***
## Usage
1. How to compile: javac MESI.java
2. How to run: java MESI
3. User has to enter the desired command: < Exchange number >, < R/W >, < Currency number >, < Value >  
	* Exchange number: enter 1, 2, 3
	* R/W: enter R or W
	* Currency number:  
			EUR/USD -> 0  
			USD/CAD -> 1  
			USD/CHF -> 2  
			GBP/USD -> 3  
	* Value:  
			read command -> any value  
			write command -> desired value to be set
	* e.g. Exchange 1 receives EUR/USD and it stores this value into a0. (Assume value = 1.04)  
		Command should be: 1,W,0,1.04  
		Make sure using comma to separate parameters
4. local_cache_state table:  
		default: 4  
		Modified(M): 1  
		Exclusive(E): 2  
		Shared(S): 3  
		Invalid(I): 4  

## Notes
1. I use sleep() to make sure each read/write has been done before next user-input prompt shows up
2. After sleep(), state of each exchange at the specific currency is printed out
3. I set default number of exchanges to 3, you can change to any number in line 12 in the code  

## Running example (from MESI protocol at wikipedia):
	use EUR/USD as currency
	Request order: R1->W1->R3->W3->R1->R3->R2
	Command 		Expected output (E1-E2-E3)
	default			I-I-I
	R1: 1,R,0,-1		E-I-I
	W1: 1,W,0,1.09		M-I-I
	R3: 3,R,0,-1		S-I-S
	W3: 3,W,0,2		I-I-M
	R1: 1,R,0,-1		S-I-S
	R3: 3,R,0,-1		S-I-S
	R2: 2,R,0,-1		S-S-S
	

