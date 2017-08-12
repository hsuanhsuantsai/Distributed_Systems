//Distributed Systems HW#5 
//Flora Tsai

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class Exchange {

	private static int member;				//id of this exchange in the system
	private static int n;					//number of exchanges in the system
	private static double cur_value = 0.5;	//current EUR/USD value
	
	//acceptor side
	private static int rec_x = 0;			//the highest agreed sequence number
	private static double rec_value = 0;	//value along with the highest agreed sequence number
	private static ArrayList<Double> accepted_values = new ArrayList<>();	//store previously accepted values
	
	private static class ProposerThread implements Runnable {
		//proposer side
		private static int seq = 0;				//sequence number
		private static double value = 0;		//value in proposal
		private static int total = 0;			//total number of valid responding exchanges
		private static double agree_ex = 0;		//number of exchanges who agree with the proposal
		private static int temp_seq = 0;		//highest seq number of received proposal among acceptors
		private static double temp_value = 0;	//corresponding value of temp_seq
		
		public void run (){
			while(true) {
				//reset counters
				agree_ex = 0;
				total = 0;
				
				try {
					TimeUnit.SECONDS.sleep(10);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				
				while (value == 0)
					value = ThreadLocalRandom.current().nextInt(10)/10.0;
				//System.out.println("value = " + value);
				if (value != cur_value) {
					seq++;
					System.out.println("seq = " + seq);
					
					//send proposal to each member
					System.out.println("Send proposal");
					for (int i = 1; i <= n; i++) {
						//exchange does not send proposal to itself
						if (i == member)
							continue;
						
						try (
								Socket socket = new Socket("localhost", 8000+i);
								PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
								BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
							){
								out.println("proposal");
								out.println(seq + "," + value);
								String reply = in.readLine();
								String [] proposal = in.readLine().split(",");
								
								System.out.println(reply);
								if (reply.equals("agree")) {
									agree_ex++;
									int proposal_seq = Integer.parseInt(proposal[0]);
									if (proposal_seq > temp_seq){
										temp_seq = proposal_seq;
										temp_value = Double.parseDouble(proposal[1]);
									}
								}
								total++;
						}catch (Exception e) {
							//acceptor fails to reply 
							System.err.println("Member " + i + " is not available.");
							total++;
						}
					}
					//System.out.println(agree_ex + ";" + total);
					//if a majority of acceptors agree, send accept request
					if (agree_ex/total > 0.5) {
						//reset counter
						agree_ex = 0;
					
						if (temp_value != 0)
							value = temp_value;
						
						System.out.println("Send accept request");
						for (int i = 1; i <= n && agree_ex/total <= 0.5; i++) {
							if (i == member)
								continue;
							try (
									Socket socket = new Socket("localhost", 8000+i);
									PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
									BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
								){
									out.println("accept request");
									out.println(seq + "," + value);
									
									String reply = in.readLine();
									System.out.println(reply);
									if (reply.equals("accept")) 
										agree_ex++;
									
							}catch (Exception e) {
								//acceptor fails to reply 
								System.err.println("Accept " + i + " request error");
							}
						}
						
						//Commit stage
						if (agree_ex/total > 0.5){
							System.out.println("Commit stage");
							cur_value = value;
							
							//send commit msg to acceptors
							for (int i = 1; i <= n; i++) {
								if (i == member)
									continue;
								try (
										Socket socket = new Socket("localhost", 8000+i);
										PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
									){
										out.println("Committed");
								}catch (Exception e) {
									System.err.println("Commit " + i + " error");
								}
							}
							
							System.out.println("Current value updated: " + cur_value);
						}
					}
				}
			}
		}
	}
	
	
	private static class AcceptorThread implements Runnable {
		
		private Socket socket;
		
		public AcceptorThread(Socket socket) {
			this.socket = socket;
		}
		
		public void run (){
			try (
					PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
					BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				){
					String input = in.readLine();
					if (input.equals("proposal")) {
						String [] proposal = in.readLine().split(",");
						int pro_seq = Integer.parseInt(proposal[0]);
						double pro_value = Double.parseDouble(proposal[1]);
						
						//first proposal
						if (rec_x == 0 || pro_seq > rec_x){
							out.println("agree");
							rec_x = pro_seq;
							rec_value = pro_value;
							accepted_values.add(pro_value);
						}
						
						else 
							out.println("reject");
						
						out.println(rec_x + "," + rec_value);
					}
					
					else if (input.equals("accept request")) {
						String [] proposal = in.readLine().split(",");
						int pro_seq = Integer.parseInt(proposal[0]);
						double pro_value = Double.parseDouble(proposal[1]);
						
						if (accepted_values.contains(pro_value) && pro_seq == rec_x) {
							out.println("accept");
							rec_value = pro_value;
						}
						else
							out.println("reject");
					}
					
					else if (input.equals("Committed")) {
						//clear accepted values table for next paxos
						accepted_values.clear();
						cur_value = rec_value;
						System.out.println("Current value updated: " + cur_value);
					}
						
			}catch (Exception e) {
				System.err.println("AcceptorThread error");
			}
		}
	}
	
	public static void main(String[] args) {
		if (args.length == 0) {
			System.err.println("Usage: java Exchange <member number> <number of exchanges in the system>");
			System.exit(1);
		}

		member = Integer.parseInt(args[0]);
		n = Integer.parseInt(args[1]);
		
		try (
				ServerSocket serverSocket = new ServerSocket(8000+member);
			){
			try {
				new Thread(new ProposerThread()).start();
			}catch (Exception e) {
				System.err.println("ProposerThread error");
			}
				while(true) {
					try {
						Socket socket = serverSocket.accept();
						new Thread(new AcceptorThread(socket)).start();
					}catch (Exception e) {
						System.err.println("AcceptorThread error");
						System.exit(1);
					}
				}
				
		}catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
