import java.net.*;
import java.io.*;
import java.util.*;

//Server port number 7734
public class Peer {

	private static String hostName; 
	private static String portNum;
	private static List<PeerRFC> rfcs = new ArrayList<PeerRFC>();
	private static String versionNumber = "P2P-CI/1.0";
	public static void setHostInfo(String hn, String pn) {
		hostName = hn;
		portNum = pn;
	}
	
	public static void setRfcs(String rfcString) {
		String[] rfcArr = rfcString.split("\\|");
		for(String s : rfcArr) {
			String n = s.split(",")[0];
			String t = s.split(",")[1];
			rfcs.add(new PeerRFC(t,n));
		}
	}
	
	public static String getHostName() {
		return hostName;
	}

	public static String getPortNum() {
		return portNum;
	}
	
	public static List<PeerRFC> getRfcs() {
		return rfcs;
	}
	
	public static String createAddRequest() {
		String req = "";
		for(PeerRFC r : rfcs) {
			req += ("ADD"+"\t"+r.rfcNum+"\t"+versionNumber+"\r\n");
			req += ("Host:\t"+hostName+"\r\n");
			req += ("Port:\t"+portNum+"\r\n");
			req += ("Title:\t"+r.title+"\r\n");
		}
		//System.out.println(req);
		return req;
	}
	
	public static String createLookupRequest(int rfcNum, String rfcTitle) {
		String req = "";
		req += ("LOOKUP"+"\tRFC "+rfcNum+"\t"+versionNumber+"\r\n");
		req += ("Host:\t"+hostName+"\r\n");
		req += ("Port:\t"+portNum+"\r\n");
		req += ("Title:\t"+rfcTitle+"\r\n");
		return req;
	}
	
	public static String createListRequest() {
		String req = "";
		req += ("LIST"+"\t"+versionNumber+"\r\n");
		req += ("Host:\t"+hostName+"\r\n");
		req += ("Port:\t"+portNum+"\r\n");
		return req;
	}
	
	public static String createExitRequest() {
		String req = "";
		req += ("EXIT"+"\t"+versionNumber+"\r\n");
		req += ("Host:\t"+hostName+"\r\n");
		req += ("Port:\t"+portNum+"\r\n");
		return req;
	}
	
	public static void main(String[] args) throws IOException {
		String serverName = "127.0.0.1";
		int port = 7734;
		
		Socket client;
		OutputStream outToServer;
		DataOutputStream out;
		InputStream inFromServer;
		DataInputStream in;
		
		//Get input values.
		String hostName = args[0];
		String portNum = args[1];
		/******************************************READ RFCs FROM TEXT FILE******************************************/
		BufferedReader br = new BufferedReader(new FileReader(args[2]));
		String rfcString = "";
		try {
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();

	        while (line != null) {
	            sb.append(line);
	            sb.append("\n");
	            line = br.readLine();
	        }
	        rfcString = sb.toString();
	    } finally {
	        br.close();
	    }
		/******************************************READ RFCs FROM TEXT FILE END******************************************/
		
		//Initialize client variables.
		setHostInfo(hostName, portNum);
		//Initialize RFCs for client.
		setRfcs(rfcString);
		try {
			System.out.println("Connecting to " + serverName + " on port " + port);// print statement
			
			/*************************************SERVER CONNECTION INITIALIZATION***************************************/
			client = new Socket(serverName, port);// Create a socket with a server.
			//System.out.println("Just connected to " + client.getRemoteSocketAddress());// gives server name i.e localhost/127.0.0.1:7738

			outToServer = client.getOutputStream();
			out = new DataOutputStream(outToServer);
			String req = createAddRequest();
			out.writeUTF(req);
			
			inFromServer = client.getInputStream();
			in = new DataInputStream(inFromServer);
			String response = in.readUTF();
			System.out.println("Server says\n" + response);
			in.close();
			client.close();
			/*************************************SERVER CONNECTION INITIALIZATION END***************************************/
			
			int option;
			Scanner sc;
			do {
				System.out.println("Enter option:\n 1. LOOKUP\n 2. LIST\n 3. CLOSE CONNECTION\n 4. EXIT");
				sc = new Scanner(System.in);
				option = sc.nextInt();
				switch(option) {
					case 1:
						System.out.println("Enter the RFC Number you want to lookup:");
						int rfcNum = sc.nextInt();
						System.out.println("Enter the RFC Title:");
						sc.nextLine();
						String rfcTitle = sc.nextLine();
						client = new Socket(serverName, port);
						outToServer = client.getOutputStream();
						out = new DataOutputStream(outToServer);
						String reqForLookup = createLookupRequest(rfcNum,rfcTitle);
						out.writeUTF(reqForLookup);
						//Read response
						inFromServer = client.getInputStream();
						in = new DataInputStream(inFromServer);
						String res= in.readUTF();
						System.out.println("Server says\n" + res);
						in.close();
						client.close();
						break;
					case 2:
						client = new Socket(serverName, port);
						outToServer = client.getOutputStream();
						out = new DataOutputStream(outToServer);
						String reqForList = createListRequest();
						out.writeUTF(reqForList);
						//Read response
						inFromServer = client.getInputStream();
						in = new DataInputStream(inFromServer);
						String resp= in.readUTF();
						System.out.println("Server says\n" + resp);
						in.close();
						client.close();
						break;
					case 3:
						client = new Socket(serverName, port);
						outToServer = client.getOutputStream();
						out = new DataOutputStream(outToServer);
						String reqForExit = createExitRequest();
						out.writeUTF(reqForExit);
						//Read response
						inFromServer = client.getInputStream();
						in = new DataInputStream(inFromServer);
						String respo = in.readUTF();
						System.out.println("Server says\n" + respo);
						in.close();
						client.close();
						break;
					default:
						System.out.println("Wrong Choice, try again");
				}
				if(option == 3) {
					System.out.println("Connection closed with server.");
					break;
				}
			}while(option != 4);
			sc.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

class PeerRFC{
	String title;
	String rfcNum;
	public PeerRFC(String t, String num) {
		title = t;
		rfcNum = num;
	}
}
