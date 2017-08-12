//Distributed System HW#4
//20170423
//Flora Tsai

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


public class SubNode {
	
	private static String member;
	private static String hostName = "localhost";
	private static int superPort;
	private static int selfPort;

	private static class MyClientThread implements Runnable {
		
		public void run (){
			//add agent
			try(
					Socket socket = new Socket(hostName, superPort);
					PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				){
					out.println(member);
					out.println(selfPort);
					out.println("a");
			}catch (Exception e) {
				System.err.println("Add agent failed");
			}
			try (
					BufferedReader my_buffer = new BufferedReader(new InputStreamReader(System.in));
				){
					while(true) {
						System.out.println("Send a message(s)/ Delete agent(d):");
						String answer = my_buffer.readLine().toLowerCase();
						System.out.println("Connect to super node");
							try (
									Socket socket = new Socket(hostName, superPort);
									PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
									BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
									
								){
									out.println(member);
									out.println(selfPort);
	
									if (answer.equals("d")){
										out.println("d");
										System.out.println("Subnode closed");
										System.exit(0);
									}
									else{
										out.println("s");
										System.out.println("Enter destination: ");
										out.println(my_buffer.readLine());
										String response = in.readLine();
										//System.out.println(response);
										if (response.equals("NULL"))
											System.out.println("No such destination!");
										else {
											try (
													Socket msgSocket = new Socket("localhost", Integer.parseInt(response));
													PrintWriter msgOut = new PrintWriter(msgSocket.getOutputStream(), true);
												){
													System.out.println("Connection to sub node");
													
													String message = my_buffer.readLine();
													msgOut.println(message);
													
											}catch (Exception e) {
												//System.err.println("No such destination!");
												System.err.println("msgSocket error");
											}
										}
									}
									
							}catch (Exception e) {
								System.err.println("Suer-sub nodes connection error");
							}
						
				}
			}catch (Exception e) {
				System.err.println("STDIN error");
			}
		}
	}
	
	
	private static class SubThread implements Runnable {
		private Socket subSocket;
		
		public SubThread(Socket socket) {
			this.subSocket = socket;
		}
		
		public void run (){
			try (
					BufferedReader in = new BufferedReader(new InputStreamReader(subSocket.getInputStream()));
				){
					//System.out.println("Connection");
					String input = in.readLine();
					if (input != null)
						System.out.println("Received: " + input);
						
			}catch (Exception e) {
				System.err.println("SubThread error");
			}
		}
	}
	
	public static void main(String[] args) {
		if (args.length == 0) {
			System.err.println("Usage: java SubNode <member number> <super port number> <self port number>");
			System.exit(1);
		}
		
		member = args[0];
		superPort = Integer.parseInt(args[1]);
		selfPort = Integer.parseInt(args[2]);
		
		try (
				ServerSocket serverSocket = new ServerSocket(selfPort);
			){
				try {
						new Thread(new MyClientThread()).start();
				}catch (Exception e) {
					
				}
				
				while(true) {
					try {
						Socket socket = serverSocket.accept();
						new Thread(new SubThread(socket)).start();
					}catch (Exception e) {
						e.printStackTrace();
						System.exit(1);
					}
				}
		}catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

	}

}
