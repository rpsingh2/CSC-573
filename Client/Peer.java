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
	
	public static void main(String[] args) throws IOException {
		String serverName = "127.0.0.1";
		int port = 7734;
		//Get input values.
		String hostName = args[0];
		String portNum = args[1];
		String rfcString = args[2];
		//Initialize client variables.
		setHostInfo(hostName, portNum);
		//Initialize RFCs for client.
		setRfcs(rfcString);
		try {
			System.out.println("Connecting to " + serverName + " on port " + port);// print statement
			Socket client = new Socket(serverName, port);// Create a socket with a server.
			//System.out.println("Just connected to " + client.getRemoteSocketAddress());// gives server name i.e localhost/127.0.0.1:7738

			OutputStream outToServer = client.getOutputStream();
			DataOutputStream out = new DataOutputStream(outToServer);
			//out.writeUTF("Hello from " + client.getLocalSocketAddress());// Hello from 127.0.0.1:54617
			String req = createAddRequest();
			out.writeUTF(req);
			
			InputStream inFromServer = client.getInputStream();
			DataInputStream in = new DataInputStream(inFromServer);
			String response = in.readUTF();
			System.out.println("Server says\n" + response);
			in.close();
			client.close();
			
			System.out.println("Enter option:\n 1. LOOKUP\n 2. LIST");
			Scanner sc = new Scanner(System.in);
			int option = sc.nextInt();
			sc.close();
			switch(option) {
				case 1:
					break;
				case 2:
					break;
				case 3:
					break;
				default:
					
			}
			
			
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
