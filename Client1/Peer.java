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
		String[] rfcArr = rfcString.split("\\n");
		for(String s : rfcArr) {
			String n = s.split(",")[0];
			String t = s.split(",")[1].trim();
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
	
	public static String createDownloadRequest(int rfcNumber, String host, int hostPort) {
		String req = "";
		req += ("GET"+"\tRFC "+rfcNumber+"\t"+versionNumber+"\r\n");
		req += ("Host:\t"+host+"\r\n");
		req += ("OS:\t"+System.getProperty("os.name")+"\r\n");
		return req;
	}
	
	public static void writeRFCToFile(String req, String fileName){
		if(req.split("\\n")[0].split("\\t")[1].trim().equals("200")) {
			BufferedWriter writer = null;
	        try {
	            //create a temporary file
	            File rfcFile = new File(fileName);

	            writer = new BufferedWriter(new FileWriter(rfcFile, true));
	            String rfcInfo = req.split("\\n")[6];
	            String rfcNum = rfcInfo.split("\\t")[0].split(" ")[1];
	            String rfcTitle = rfcInfo.split("\\t")[1].trim();
	            writer.write("RFC "+rfcNum+", ");
	            writer.write(rfcTitle);
	        } catch (Exception e) {
	            e.printStackTrace();
	        } finally {
	            try {
	                // Close the writer regardless of what happens...
	                writer.close();
	            } catch (Exception e) {
	            }
	        }
		}
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
		System.out.println("Enter Host Name:");
		Scanner sc = new Scanner(System.in);
		String hostName = sc.nextLine();
		System.out.println("Enter Upload Port:");
		String portNum = sc.nextLine();
		System.out.println("Enter RFC file:");
		String rfcFile = sc.nextLine();

		/******************************************READ RFCs FROM TEXT FILE******************************************/
		BufferedReader br = new BufferedReader(new FileReader(rfcFile));
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
		//Start Upload Server
		UploadServer us = new UploadServer(hostName, portNum, versionNumber, rfcFile);
		us.start();
		
		
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
			System.out.println("==================================");
			System.out.println("Server says\n" + response);
			System.out.println("==================================");
			in.close();
			client.close();
			/*************************************SERVER CONNECTION INITIALIZATION END***************************************/
			
			int option;
			do {
				System.out.println("Enter option:\n 1. LOOKUP\n 2. LIST\n 3. CLOSE CONNECTION\n 4. SEND REQUEST TO PEER\n 5. EXIT");
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
						System.out.println("==================================");
						System.out.println("Server says\n" + res);
						System.out.println("==================================");
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
						System.out.println("==================================");
						System.out.println("Server says\n" + resp);
						System.out.println("==================================");
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
						System.out.println("==================================");
						System.out.println("Server says\n" + respo);
						System.out.println("==================================");
						in.close();
						client.close();
						break;
					case 4:
						System.out.println("Enter the RFC Number you want to download:");
						int rfcNumber = sc.nextInt();
						System.out.println("Enter the host Name you want to download from:");
						sc.nextLine();
						String host = sc.nextLine();
						System.out.println("Enter the host Port you want to download from:");
						//System.out.println(java.net.InetAddress.getLocalHost().getHostName());
						int hostPort = sc.nextInt();
						client = new Socket(serverName, hostPort);
						outToServer = client.getOutputStream();
						out = new DataOutputStream(outToServer);
						String reqForDownload = createDownloadRequest(rfcNumber, host, hostPort);
						out.writeUTF(reqForDownload);
						//Read response
						inFromServer = client.getInputStream();
						in = new DataInputStream(inFromServer);
						String respon= in.readUTF();
						System.out.println("Server says\n" + respon);
						writeRFCToFile(respon, rfcFile);
						in.close();
						client.close();
						break;
					case 5:
						break;
					default:
						System.out.println("Wrong Choice, try again");
				}
				if(option == 3) {
					System.out.println("Connection closed with server.");
					break;
				}
			}while(option != 5);
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
