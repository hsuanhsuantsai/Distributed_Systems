//Distributed System HW#4
//20170423
//Flora Tsai

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;


public class SuperNode {

	private static int superServerPort;					//server side for ABC
	//for every super node, it has a left node and a right node
	//e.g. B' left node is A; B's right node is C
	private static String left_name;					//host name of left node
	private static int leftPort;						//host port of left node
	private static String right_name;					//host name of right node
	private static int rightPort;						//host port of right node
	private static int subServerPort;					//server side for sub nodes A1, A2, B1, B2, C1, C2
	private static String label;						//A or B or C
	private static Map<Integer, Integer> table = new HashMap<Integer, Integer>();	//address table
	
	private static class MyServerThread implements Runnable {
		private Socket socket;
		private static int cur_member;	//current member
		
		public MyServerThread(Socket socket) {
			this.socket = socket;
		}
		
		public void run (){
			try (
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			){
				cur_member = Integer.parseInt(in.readLine());
				int port = Integer.parseInt(in.readLine());
				String command = in.readLine();
				if (command.equals("a"))
					//adding and changing agents are the same for map
					addAgent(cur_member, port);
				else if (command.equals("d"))
					deleteAgent(cur_member);
				else {
					String dest = in.readLine();
					String ret;
					if (dest.charAt(0) == label.charAt(0)) 
						ret = selfCheck(dest);
					else
						ret = lookUp(dest);
						
					out.println(ret);
					
				}
			}catch (Exception e) {
		
			}
		}
		
		public String lookUp(String s) {
			//left node
			try (
					Socket leftSocket = new Socket(left_name, leftPort);
					PrintWriter left_out = new PrintWriter(leftSocket.getOutputStream(), true);
					BufferedReader left_in = new BufferedReader(new InputStreamReader(leftSocket.getInputStream()));
				){
					left_out.println(s);
					String response = left_in.readLine();
					if (!response.equals("NULL"))
						return response;
			}catch (Exception e) {
				e.printStackTrace();
			}
			
			//right node
			try (
					Socket rightSocket = new Socket(right_name, rightPort);
					PrintWriter right_out = new PrintWriter(rightSocket.getOutputStream(), true);
					BufferedReader right_in = new BufferedReader(new InputStreamReader(rightSocket.getInputStream()));
				){
					right_out.println(s);
					String response = right_in.readLine();
					if (!response.equals("NULL"))
						return response;
			}catch (Exception e) {
				e.printStackTrace();
			}
			return "NULL";
		}
		
	}
	
	private static class SuperClient implements Runnable {
		private Socket socket;
		
		public SuperClient(Socket socket) {
			this.socket = socket;
		}
		
		public void run (){
			try(
					PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
					BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				){
					String input = in.readLine();
					System.out.println(input);
					String ret;
					if (input.charAt(0) == label.charAt(0)) 
						ret = selfCheck(input);
					else
						ret = "NULL";
					
					out.println(ret);
				}catch (Exception e) {
					System.err.println("SuperClient error");
					e.printStackTrace();
				}
		}
	}
	
	private static class SuperThread implements Runnable {
		
		public void run (){
			try (
					ServerSocket superServerSocket = new ServerSocket(superServerPort);
			){
				while(true) {
					try{
							Socket superClient = superServerSocket.accept();
							new Thread(new SuperClient(superClient)).start();
					}catch (Exception e) {
						
					}
				}
			}catch (Exception e) {
				
			}
				
		}
	}
	
	//check the table and whether the port is available
	public static String selfCheck(String input){
		int member = Integer.parseInt(input.substring(1));
		System.out.println(member);
		if (!table.containsKey(member)){
			System.out.println("No such key");
			return "NULL";
		}
				
		int port = table.get(member);
				
		//check the validity of the port number by launching a new socket
		try (
				Socket testSocket = new Socket("localhost", port);
			){}catch (Exception e) {
				//invalid port number
				deleteAgent(port);
				return "NULL";
			}
				
		return Integer.toString(port);
	}
	
	public static void addAgent(int member, int port) {
		table.put(member, port);
	}
	
	public static void deleteAgent(int member) {
		table.remove(member);
	}
	
	public static void main(String[] args) {
		if (args.length == 0) {
			System.err.println("Usage: java SuperNode <superServerPort> <subServerPort> <label>");
			System.exit(1);
		}

		superServerPort = Integer.parseInt(args[0]);
		subServerPort = Integer.parseInt(args[1]);
		label = args[2];
		
		//tell user this server host name
		try {
			InetAddress inetAddress = InetAddress.getLocalHost();
			System.out.println("Hostname: " + inetAddress.getHostName());
		}catch (Exception e) {
			System.err.println("No host: " + e.getMessage());
			System.exit(1);
		}
		
		try(
				ServerSocket subServerSocket = new ServerSocket(subServerPort);
				BufferedReader my_buffer = new BufferedReader(new InputStreamReader(System.in));
			){
				System.out.println("Host name of left node: ");
				left_name = my_buffer.readLine();
				System.out.println("Host port of left node: ");
				leftPort = Integer.parseInt(my_buffer.readLine());
				
				System.out.println("Host name of right node: ");
				right_name = my_buffer.readLine();
				System.out.println("Host port of right node: ");
				rightPort = Integer.parseInt(my_buffer.readLine());
				
				//deal with communications with the other two super nodes
				new Thread(new SuperThread()).start();
				
				//deal with connections between sub nodes
				while(true) {
					try {
							Socket socket = subServerSocket.accept();
							new Thread(new MyServerThread(socket)).start();
						}catch (Exception e) {
							e.printStackTrace();
						}
				}
		}catch (Exception e) {
				System.exit(1);
		}
	}

}
