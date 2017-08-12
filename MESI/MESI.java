//Distributed Systems HW#6
//Flora Tsai

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MESI {

	private static ArrayList<Double> main_memory = new ArrayList<>();
	private static int n_exchange = 3;														//number of exchange
	private static int n_currency = 4;														//number of currency
	private static ArrayList<MESI_process> exchange_list= new ArrayList<MESI_process>();
	private static int M_copies = 0;														//how many M copies in the CPUs
	private static int E_copies = 0;														//how many E copies in the CPUs
	private static int S_copies = 0;														//how many S copies in the CPUs
	
	public static void print_state(int currency) {
		System.out.println("\nCache state of each exchange at this currency");
		for (int i=0; i<n_exchange; i++) {
			System.out.print("Exchange " + (i+1));
			int state = exchange_list.get(i).local_cache_state.get(currency);
			if (state == 1)
				System.out.println(": M");
			else if (state == 2)
				System.out.println(": E");
			else if (state == 3)
				System.out.println(": S");
			else if (state == 4)
				System.out.println(": I");
		}
	}
	
	public static void check_copies(int currency) {
		for (int i=0; i<n_exchange; i++) {
			int state = exchange_list.get(i).local_cache_state.get(currency);
			if (state == 1)
				M_copies++;
			else if (state == 2)
				E_copies++;
			else if (state == 3)
				S_copies++;
		}
	}
	
	//update state from E to S
	public static void E_update(int currency) {
		for (int i=0; i<n_exchange; i++) {
			MESI_process source = exchange_list.get(i);
			if (source.local_cache_state.get(currency) == 2) {
				System.out.println("Change source state to S");
				source.local_cache_state.set(currency, 3);
			}
		}
		E_copies = 0;
	}
	
	//update main memory and state from M to S
	public static void M_update(int currency) {
		for (int i=0; i<n_exchange; i++) {
			MESI_process source = exchange_list.get(i);
			if (source.local_cache_state.get(currency) == 1) {
				System.out.println("Update main memory");
				main_memory.set(currency, source.local_cache.get(currency));
				System.out.println("Change source state to S");
				source.local_cache_state.set(currency, 3);
			}
		}
		M_copies = 0;
	}
	
	//invalidate cache line by setting state from S to I
	public static void broadcast_invalidate(int currency){
		for (int i=0; i<n_exchange; i++) {
			MESI_process source = exchange_list.get(i);
			if (source.local_cache_state.get(currency) == 3) {
				System.out.println("Set the state from S to I");
				source.local_cache_state.set(currency, 4);
			}
		}
		S_copies = 0;
	}
	
	//used in write function
	public static void E_invalidate(int currency) {
		for (int i=0; i<n_exchange; i++) {
			MESI_process source = exchange_list.get(i);
			if (source.local_cache_state.get(currency) == 2) {
				System.out.println("Set the state from E to I");
				source.local_cache_state.set(currency, 4);
			}
		}
		E_copies = 0;
	}
	
	//used in write function (RWITM)
	public static void another_M_update(int currency) {
		for (int i=0; i<n_exchange; i++) {
			MESI_process source = exchange_list.get(i);
			if (source.local_cache_state.get(currency) == 1) {
				System.out.println("Update to main memory");
				//write back its copy to main memory
				main_memory.set(currency, source.local_cache.get(currency));
				System.out.println("Set the state from M to I");
				source.local_cache_state.set(currency, 4);
			}
		}
		M_copies = 0;
	}
	
	private static class MESI_process implements Runnable {
		private int process_numer;											//for print out purpose
		private ArrayList<Double> local_cache = new ArrayList<>();
		private ArrayList<Integer> local_cache_state = new ArrayList<>();
		private int RW;
		private int currency;
		private double value = -1;											//default = -1
		private boolean updated = false;
		
		public MESI_process(int number) {
			process_numer = number;
			for (int i=0; i<n_currency; i++) {
				local_cache.add(0.0);
				//default state is I
				local_cache_state.add(4);
			}
		}
		
		public double read(int currency) {
			int state = local_cache_state.get(currency);
			
			//M
			if (state == 1) {
				System.out.println("Exchange " + (process_numer + 1) + ": State M, read from local cache!");
				return local_cache.get(currency);
			}
			//E
			else if (state == 2) {
				System.out.println("Exchange " + (process_numer + 1) + ": State E, read from local cache!");
				return local_cache.get(currency);
			}
			//S
			else if (state == 3) {
				System.out.println("Exchange " + (process_numer + 1) + ": State S, read from local cache!");
				return local_cache.get(currency);
			}
			//I
			else if (state == 4) {
				System.out.print("Exchange " + (process_numer + 1) + ": State I, ");
				check_copies(currency);
				double value = -1;
				
				//no other copy in caches
				if ((M_copies == 0) && (E_copies == 0) && (S_copies == 0)) {
					System.out.println("read from main memory!");
					System.out.println("Exchange " + (process_numer + 1) + ": Value read to local cache, state is changed to E");
					value = main_memory.get(currency);
					local_cache.set(currency, value);
					local_cache_state.set(currency, 2);
				}
				
				//one cache has E copy
				else if (E_copies == 1){
					System.out.println("one cache has E copy! E-update and change state to S");
					//any copy is the same as that in main memory
					value = main_memory.get(currency);
					local_cache.set(currency, value);
					E_update(currency);
					local_cache_state.set(currency, 3);
					System.out.println("Exchange " + (process_numer + 1) + ": State set to S");
				}
				
				//several caches have S copies
				else if (S_copies > 1) {
					System.out.println("several caches have S copies! Cache the value and change state to S");
					//any copy is the same as that in main memory
					value = main_memory.get(currency);
					local_cache.set(currency, value);
					local_cache_state.set(currency, 3);
					System.out.println("Exchange " + (process_numer + 1) + ": State set to S");
				}
				
				//one cache has M copy
				else if (M_copies == 1){
					System.out.println("one cache has M copy! M-update and change state to S");
					M_update(currency);
					//M copy just updated main memory
					value = main_memory.get(currency);
					local_cache.set(currency, value);
					local_cache_state.set(currency, 3);
					System.out.println("Exchange " + (process_numer + 1) + ": State set to S");
				}
				
				return value;
			}
			
			//should not go to this region
			return -1;
		}
		
		public void write(int currency, double value) {
			int state = local_cache_state.get(currency);
			
			//M and E
			if (state == 1 || state == 2) {
				System.out.print("Exchange " + (process_numer + 1) + ": State ");
				System.out.print(state == 1? "M":"E");
				System.out.println(", write to local cache!");
				
				//update local cache
				local_cache.set(currency, value);
				//M set to M: unchanged; E set to M
				local_cache_state.set(currency, 1);
			}
			
			//S
			else if (state == 3) {
				System.out.println("Exchange " + (process_numer + 1) + ": State S, broadcast and invalidate other shared caches");
				broadcast_invalidate(currency);
				local_cache.set(currency, value);
				System.out.println("Exchange " + (process_numer + 1) + ": Local cache updated");
				local_cache_state.set(currency, 1);
				System.out.println("Exchange " + (process_numer + 1) + ": State set to M");
			}
			
			//I
			else if (state == 4) {
				System.out.print("Exchange " + (process_numer + 1) + ": State I, ");
				check_copies(currency);
				
				//no other copy in caches
				if ((M_copies == 0) && (E_copies == 0) && (S_copies == 0)) {
					local_cache.set(currency, value);
					System.out.println("Exchange " + (process_numer + 1) + ": Local cache updated");
					System.out.println("Exchange " + (process_numer + 1) + ": State set to M");
					local_cache_state.set(currency, 1);
				}
				
				//one in E copy or several S copies
				else if (E_copies != 0 && S_copies != 0) {
					System.out.println("Exchange " + (process_numer + 1) + ": Invalidate E and S states");
					E_invalidate(currency);
					broadcast_invalidate(currency);
					
					local_cache.set(currency, value);
					System.out.println("Exchange " + (process_numer + 1) + ": Local cache updated");
					//set state to M
					System.out.println("Exchange " + (process_numer + 1) + ": State set to M");
					local_cache_state.set(currency, 1);
				}
				
				//another copy in state M
				else if (M_copies != 0) {
					System.out.println("Exchange " + (process_numer + 1) + ": Update main memory from M cache");
					another_M_update(currency);
					local_cache.set(currency, value);
					System.out.println("Exchange " + (process_numer + 1) + ": Local cache updated");
					//set state to M
					System.out.println("Exchange " + (process_numer + 1) + ": State set to M");
					local_cache_state.set(currency, 1);
				}
			}
		}
		
		public void run (){
			while(true) {
				//hold on for update
				while(!updated) {
					try {
						TimeUnit.SECONDS.sleep(1);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
				
				String cur = null;
				if (currency == 0)
					cur = "EUR/USD";
				else if (currency == 1)
					cur = "USD/CAD";
				else if (currency == 2)
					cur = "USD/CHF";
				else if (currency == 3)
					cur = "GBP/USD";
				
				
				//read
				if (RW == 0) {
					value = read(currency);
					System.out.println("Exchange " + (process_numer + 1) + ": Read " + cur + " = " + value);
				}
				//write
				//if value is set, which means value != default value
				else if (value != -1) {					
					write(currency, value);
					System.out.println("Exchange " + (process_numer + 1) + ": Write " + cur + " = " + value);
					
				}
				
				updated = false;
			}
		}
	}
	
	public static void main(String[] args) {
		
		for (int i=0; i<n_currency; i++)
			main_memory.add(0.0);
		
		//EUR/USD
		main_memory.set(0, 1.05);
		//USD/CAD
		main_memory.set(1, 1.3);
		//USD/CHF
		main_memory.set(2, 0.99);
		//GBP/USD
		main_memory.set(3, 1.27);
		
		try {
			for (int i=0; i<n_exchange; i++) {
				exchange_list.add(new MESI_process(i));
				new Thread(exchange_list.get(i)).start();
			}
		}catch (Exception e) {
			System.err.println("MESI_process error");
		}
		
		try (
				BufferedReader my_buffer = new BufferedReader(new InputStreamReader(System.in));
			){
				System.out.println("Command form: <Exchange number>, <R/W>, <Currency number>, <Value>");
				while(true) {
					System.out.println("Enter command: ");
					String [] res = my_buffer.readLine().split(",");
					int exchange = Integer.parseInt(res[0]) - 1;
					int RW = res[1].equalsIgnoreCase("R")? 0:1;
					int currency = Integer.parseInt(res[2]);
					double value = Double.parseDouble(res[3]);
					MESI_process process = exchange_list.get(exchange);
					process.RW = RW;
					process.currency = currency;
					if (RW == 1)
						process.value = value;
					
					process.updated = true;
					try {
							TimeUnit.SECONDS.sleep(1);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
						
					print_state(currency);
				}
		}catch (Exception e) {
			e.printStackTrace();
		}

	}

}
