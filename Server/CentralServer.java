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
	String version = "P2P-CI/1.0";
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
					String firstLine = req.split("\\n")[0];
					String[] wordsInFirstLine = firstLine.split("\\t");
					String ver = wordsInFirstLine[wordsInFirstLine.length-1].trim();
					System.out.println("Req Version is this "+ver);
					if(ver.equals(version)) {
						if(reqType.equals("ADD")) {
							handleNewClient(req);
							break;
						} else if(reqType.equals("LOOKUP")) {
							handleLookUpRequest(req);
							break;
						} else if(reqType.equals("LIST")) {
							handleListRequest(req);
							break;
						} else if(reqType.equals("EXIT")) {
							handleExitRequest(req);
							break;
						} else {
							System.out.println("Error: Wrong request");
							handleBadRequest(req);
							break;
						}
					} else {
						System.out.println("Error: Version mismatch");
						handleVersionMismatch(req);
						break;
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
	
	@SuppressWarnings("unused")
	public void handleNewClient(String req) {
		try {
			String[] lines = req.split("\\n");
			String hostName = lines[1].split("\\t")[1].trim();
			String version = lines[0].split("\\t")[2].trim();
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
			String res = createAddResponse(clientRFCs, portNum);
			out.writeUTF(res);
			
			System.out.println("\n***************************************Available RFCs:***************************************\n");
			for(int i = 0; i < availableRFCs.size(); i++) {
				System.out.println(availableRFCs.get(i).num+" "+availableRFCs.get(i).hostName+" "+availableRFCs.get(i).title);
			}
			System.out.println("\n***************************************Available Peers:***************************************\n");
			for(int i = 0; i < peerList.size(); i++) {
				System.out.println(peerList.get(i).hostName+" "+peerList.get(i).portNum);
			}
			System.out.println("\n**********************************************************************************************\n");
			
		}catch (ArrayIndexOutOfBoundsException e1) {
			handleBadRequest(req);
			e1.printStackTrace();
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unused")
	public void handleLookUpRequest(String req) {
		try {
			String[] lines = req.split("\\n");
			String hostName = lines[1].split("\\t")[1].trim();
			String version = lines[0].split("\\t")[2].trim();
			int num = Integer.parseInt(lines[0].split("\\t")[1].split(" ")[1].trim());
			int portNum = Integer.parseInt(lines[2].split("\\t")[1].trim());
			String title = lines[3].split("\\t")[1].trim();
			
			List<String> hostNames = new ArrayList<String>();
			List<PeerInfo> hostNameWithPort = new ArrayList<PeerInfo>();
			for(int i = 0; i < availableRFCs.size(); i++){
				if(availableRFCs.get(i).num == num && availableRFCs.get(i).title.equals(title)) {
					hostNames.add(availableRFCs.get(i).hostName);
				}
			}
			for(int i = 0; i < hostNames.size(); i++) {
				for(int j = 0; j < peerList.size(); j++) {
					if(hostNames.get(i).equals(peerList.get(j).hostName)) {
						hostNameWithPort.add(new PeerInfo(peerList.get(j).hostName,peerList.get(j).portNum));
					}
				}
			}
			if(hostNameWithPort.size() == 0) {
				handleNotFound(req);
				System.out.println("No such RFC found in the system");
			} else {
				String res = createLookUpResponse(hostNameWithPort, num, title);
				out.writeUTF(res);
			}
			
		}catch (ArrayIndexOutOfBoundsException e1) {
			handleBadRequest(req);
			e1.printStackTrace();
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unused")
	public void handleListRequest(String req) {
		try {
			String[] lines = req.split("\\n");
			String hostName = lines[1].split("\\t")[1].trim();
			String version = lines[0].split("\\t")[1].trim();
			int portNum = Integer.parseInt(lines[2].split("\\t")[1].trim());
			String res = createListResponse();
			out.writeUTF(res);
			
		}catch (ArrayIndexOutOfBoundsException e1) {
			handleBadRequest(req);
			e1.printStackTrace();
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unused")
	public void handleExitRequest(String req) {
		try {
			String[] lines = req.split("\\n");
			String hostName = lines[1].split("\\t")[1].trim();
			String version = lines[0].split("\\t")[1].trim();
			int portNum = Integer.parseInt(lines[2].split("\\t")[1].trim());
			for(int i = 0; i < availableRFCs.size(); i++) {
				RFC rfc = availableRFCs.get(i);
				if(rfc.hostName.equals(hostName)) {
					availableRFCs.remove(i);
					i = 0;
				}
			}
			for(int i = 0; i < peerList.size(); i++) {
				PeerInfo pi = peerList.get(i);
				if(pi.hostName.equals(hostName) && pi.portNum == portNum) {
					peerList.remove(i);
					i = 0;
				}
			}
			String res = createExitResponse();
			out.writeUTF(res);
			
			System.out.println("\n***************************************Available RFCs:***************************************\n");
			for(int i = 0; i < availableRFCs.size(); i++) {
				System.out.println(availableRFCs.get(i).num+" "+availableRFCs.get(i).hostName+" "+availableRFCs.get(i).title);
			}
			System.out.println("\n***************************************Available Peers:***************************************\n");
			for(int i = 0; i < peerList.size(); i++) {
				System.out.println(peerList.get(i).hostName+" "+peerList.get(i).portNum);
			}
			System.out.println("\n**********************************************************************************************\n");
			
		}catch (ArrayIndexOutOfBoundsException e1) {
			handleBadRequest(req);
			e1.printStackTrace();
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	
	public String createLookUpResponse(List<PeerInfo> hostNameWithPort, int rfcNum, String rfcTitle) {
		String res = "";
		res += version+"\t200\tOK\r\n";
		for(int i = 0; i < hostNameWithPort.size(); i++) {
			res += "RFC "+rfcNum+"\t"+rfcTitle+"\t"+hostNameWithPort.get(i).hostName+"\t"+hostNameWithPort.get(i).portNum+"\r\n";
		}
		return res;
	}
	
	public String createListResponse() {
		String res = "";
		res += version+"\t200\tOK\r\n";
		for(int i = 0; i < availableRFCs.size(); i++) {
			RFC rfc = availableRFCs.get(i);
			res += "RFC "+rfc.num+"\t"+rfc.title+"\t"+rfc.hostName+"\t"+"\r\n";
		}
		return res;
	}
	
	public String createExitResponse() {
		String res = "";
		res += version+"\t200\tOK\r\n";
		return res;
	}
	
	public void handleBadRequest(String req) {
		try {
			String res = "";
			res += version+"\t400\tBad Request\r\n";
			out.writeUTF(res);
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void handleVersionMismatch(String req) {
		try {
			String res = "";
			res += version+"\t505\tP2P-CI Version Not Supported\r\n";
			out.writeUTF(res);
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	public void handleNotFound(String req) {
		try {
			String res = "";
			res += version+"\t404\tNot Found\r\n";
			out.writeUTF(res);
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
}