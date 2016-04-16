import java.net.*;
import java.io.*;
import java.util.*;

class PeerInfo {
	String hostName;
	int portNum;
}

class RFC {
	int num;
	String title;
	String hostName;
}

public class CentralServer {
	List<PeerInfo> peerList = new ArrayList<PeerInfo>();
	List<RFC> availableRFCs = new ArrayList<RFC>();
	private static ServerSocket serverSocket;
	private static Socket ClientSocket;
	public static void main(String[] args) {
		// A well defined port number.
		int port = 7734, clientId = 0;;
		try {
			serverSocket = new ServerSocket(port);
			//serverSocket.setSoTimeout(100000);// SO_TIMEOUT is the timeout that a read() call will block
			System.out.println("Server listening on port: "+ serverSocket.getLocalPort()+"...");
			while(true) {
				ClientSocket = serverSocket.accept();
				ChildThread ct = new ChildThread(ClientSocket);
				ct.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

class ChildThread extends Thread {
	DataInputStream in;
	Socket ClientSocket;
	public ChildThread(Socket cSocket) {
		ClientSocket = cSocket;
	}
	public void run() {
		while (true) {
			try {
				in = new DataInputStream(ClientSocket.getInputStream());
				System.out.println("Request received is this:\n"+in.readUTF());
				
			} catch (SocketTimeoutException s) {
				System.out.println("Socket timed out!");
				break;
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
		}
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