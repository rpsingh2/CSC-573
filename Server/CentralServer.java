import java.net.*;
import java.io.*;
import java.util.*;

class PeerInfo {
	String hostName;
	int portNum;
	PeerInfo(String hostName, int portNum) {
		this.hostName = hostName;
		this.portNum = portNum;
	}
}

class RFC {
	int num;
	String title;
	String hostName;
	RFC(int num, String title, String hostName) {
		this.num = num;
		this.hostName = hostName;
		this.title = title;
	}
}

public class CentralServer {
	static List<PeerInfo> peerList = new ArrayList<PeerInfo>();
	static List<RFC> availableRFCs = new ArrayList<RFC>();
	private static ServerSocket serverSocket;
	private static Socket ClientSocket;
	public static void main(String[] args) {
		// A well defined port number.
		final int port = 7734;
		try {
			serverSocket = new ServerSocket(port);
			//serverSocket.setSoTimeout(100000);// SO_TIMEOUT is the timeout that a read() call will block
			System.out.println("Server listening on port: "+ serverSocket.getLocalPort()+"...");
			while(true) {
				ClientSocket = serverSocket.accept();
				ChildThread ct = new ChildThread(ClientSocket, peerList, availableRFCs);
				ct.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

class ChildThread extends Thread {
	List<PeerInfo> peerList;
	List<RFC> availableRFCs;
	DataInputStream in;
	Socket ClientSocket;
	DataOutputStream out;
	String version;
//	out.writeUTF("Thank you for connecting to " + server.getLocalSocketAddress() + "\nGoodbye!");
	public ChildThread(Socket cSocket, List<PeerInfo> pl, List<RFC> aRFCs) {
		ClientSocket = cSocket;
		peerList = pl;
		availableRFCs = aRFCs;
	}
	public void run() {
		try {
			in = new DataInputStream(ClientSocket.getInputStream());
			out = new DataOutputStream(ClientSocket.getOutputStream());
			while (true) {
				try {
					
					String req = in.readUTF();
					System.out.println("Request received is this:\n"+req);
					String reqType = req.split("\\t")[0];

					if(reqType.equals("ADD")) {
						handleNewClient(req);
						break;
					} else if(reqType.equals("LOOKUP")) {
						
					} else if(reqType.equals("LIST")) {
						
					} else if(reqType.equals("EXIT")) {
						
					} else {
						System.out.println("200");
					}
				} catch (SocketTimeoutException s) {
					System.out.println("Socket timed out!");
					break;
				} catch (EOFException eof) {
					System.out.println("End of file reached!");
					break;
				} catch (IOException e) {
					e.printStackTrace();
					break;
				}
			}
			out.close();
			in.close();
			ClientSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void handleNewClient(String req) {
		String[] lines = req.split("\\n");
		String hostName = lines[1].split("\\t")[1].trim();
		version = lines[0].split("\\t")[2].trim();
		int portNum = Integer.parseInt(lines[2].split("\\t")[1].trim());
		peerList.add(new PeerInfo(hostName,portNum));
		int count = lines.length/4;
		List<RFC> clientRFCs = new ArrayList<RFC>();
		for(int i = 0; i < count; i++) {
			String line1 = lines[i*4];
			String line4 = lines[i*4+3];
			int num = Integer.parseInt(line1.split("\\t")[1].split(" ")[1].trim());
			String title = line4.split("\\t")[1].trim();
			availableRFCs.add(new RFC(num, title, hostName));
			clientRFCs.add(new RFC(num, title, hostName));
		}
		
		try {
			String res = createAddResponse(clientRFCs, portNum);
			out.writeUTF(res);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		for(int i = 0; i < availableRFCs.size();i++) {
//			if(i == 0) {
//				System.out.println(peerList.get(i).hostName+" "+peerList.get(i).portNum);
//			}
//			
//			System.out.println(availableRFCs.get(i).num+" "+availableRFCs.get(i).hostName+" "+availableRFCs.get(i).title);
//		}
		
	}
	
	public String createAddResponse(List<RFC> clientRFCs, int portNum) {
		String res = "";
		res += version+"\t200\tOK\r\n";
		for(int i = 0; i < clientRFCs.size(); i++) {
			RFC cRFC = clientRFCs.get(i);
			res += "RFC "+cRFC.num+"\t"+cRFC.title+"\t"+cRFC.hostName+"\t"+portNum+"\r\n";
		}
		return res;
	}
}

//class MasterThread extends Thread {
//	private ServerSocket serverSocket;
//
//	/******Server Initialization************/
//	
//	public MasterThread(int port) throws IOException {
//		serverSocket = new ServerSocket(port);
//		serverSocket.setSoTimeout(100000);// SO_TIMEOUT is the timeout that a read() call will block
//	}
//	
//	/***************************************/
//	
//	public void run() {
//		
//		while (true) {
//			try {
//				System.out.println("Waiting for client on port " + serverSocket.getLocalPort() + "...");
//				Socket server = serverSocket.accept();// Listen to connection requests from client.
//
//				System.out.println("Just connected to " + server.getRemoteSocketAddress());// Once a client connects
//				DataInputStream in = new DataInputStream(server.getInputStream());
//				System.out.println("Client upload port number is "+in.readInt());
//				DataOutputStream out = new DataOutputStream(server.getOutputStream());
//				out.writeUTF("Thank you for connecting to " + server.getLocalSocketAddress() + "\nGoodbye!");
//				server.close();
//			} catch (SocketTimeoutException s) {
//				System.out.println("Socket timed out!");
//				break;
//			} catch (IOException e) {
//				e.printStackTrace();
//				break;
//			}
//		}
//	}
//}