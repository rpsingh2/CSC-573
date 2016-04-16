import java.net.*;
import java.io.*;
import java.util.*;

//Server port number 7734
public class Peer {

	private static String hostName; 
	private static int portNum;
	private static List<Integer> rfcs = new ArrayList<Integer>();
	public static void setHostInfo(String hn, int pn) {
		hostName = hn;
		portNum = pn;
	}
	
	public static void setRfcs(String rfcString) {
		String[] rfcArr = rfcString.split(",");
		for(String s : rfcArr) {
			rfcs.add(Integer.parseInt(s));
		}
	}
	
	public static String getHostName() {
		return hostName;
	}

	public static int getPortNum() {
		return portNum;
	}
	
	public static List<Integer> getRfcs() {
		return rfcs;
	}
	
	public static void main(String[] args) {
		String serverName = "127.0.0.1";
		int port = 7734;
		//Get input values.
		String hostName = args[0];
		int portNum = Integer.parseInt(args[1]);
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
			out.writeInt(getPortNum());
			
			InputStream inFromServer = client.getInputStream();
			DataInputStream in = new DataInputStream(inFromServer);
			System.out.println("Server says " + in.readUTF());// Thank you for connecting to 127.0.0.1:7734
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
