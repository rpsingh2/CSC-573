import java.net.*;
import java.io.*;
import java.util.*;

public class UploadServer extends Thread {
	String hostName, uploadPort, versionNumber, fileName;
	final String p2pVersion = "P2P-CI/1.0";
	private static ServerSocket uploadServerSocket;
	private static Socket ClientSocket;
	public UploadServer(String hostName, String uploadPort, String versionNumber, String fileName){
		this.hostName = hostName;
		this.uploadPort = uploadPort;
		this.versionNumber = versionNumber;
		this.fileName = fileName;
	}
	
	@Override
	public void run() {
		try {
			uploadServerSocket = new ServerSocket(Integer.parseInt(uploadPort));
			System.out.println("Server listening on port: "+ uploadServerSocket.getLocalPort()+"...");
			while(true) {
				ClientSocket = uploadServerSocket.accept();
				ChildProcess ct = new ChildProcess(ClientSocket, p2pVersion, fileName, hostName, uploadPort);
				ct.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

class ChildProcess extends Thread {
	DataInputStream in;
	DataOutputStream out;
	Socket ClientSocket;
	String p2pVersion;
	String fileName;
	String hostName;
	String uploadPort;
	public ChildProcess(Socket cSocket, String p2pVersion, String fileName, String hostName, String uploadPort) {
		ClientSocket = cSocket;
		this.p2pVersion = p2pVersion;
		this.fileName = fileName;
		this.hostName = hostName;
		this.uploadPort = uploadPort;
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
					String secondLine = req.split("\\n")[1];
					String[] wordsInFirstLine = firstLine.split("\\t");
					String ver = wordsInFirstLine[wordsInFirstLine.length-1].trim();
					int requestedRFC = Integer.parseInt(wordsInFirstLine[1].split(" ")[1].trim());
					String hName = secondLine.split("\\t")[1].trim();
					System.out.println("Req Version is this "+ver);
					if(ver.equals(p2pVersion)) {
						if(hName.equals(hostName)) {
							if(reqType.equals("GET")) {
								handleGetRequest(req, requestedRFC);
								break;
							} else {
								System.out.println("Error: Wrong request");
								handleBadRequest(req);
								break;
							}
						} else {
							System.out.println("Error: HostName mismatch");
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
	
	public void handleGetRequest(String req, int requestedRFC) throws IOException {
		Date date = new Date();
		/******************************************READ RFCs FROM TEXT FILE******************************************/
		BufferedReader br = null;
		String rfcString = "";
		try {
			br = new BufferedReader(new FileReader(fileName));
			
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();
	        while (line != null) {
	            sb.append(line);
	            sb.append("\n");
	            line = br.readLine();
	        }
	        rfcString = sb.toString();
	    }catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
	        br.close();
	    }
		/******************************************READ RFCs FROM TEXT FILE END******************************************/
		try {
			File file = new File(fileName);
			String res = "";
			res += p2pVersion+"\t200\tOK\r\n";
			res += "Date:\t"+date+"\r\n";
			res += "OS:\t"+System.getProperty("os.name")+"\r\n";
			res += "Last-Modified:\t"+new Date(file.lastModified())+"\r\n";
			res += "Content-Length:\t"+file.length()+"\r\n";
			res += "Content-Type: text/text\r\n";
			
			String[] rfcs = rfcString.split("\\n");
			for(String rfc: rfcs) {
				int rfcNum = Integer.parseInt(rfc.split(",")[0].split(" ")[1]);
				String rfcTitle = rfc.split(",")[1].trim();
				if(rfcNum == requestedRFC) {
					res += "RFC "+rfcNum+"\t"+rfcTitle+"\t"+hostName+"\t"+uploadPort+"\r\n";
				}
			}
			
			out.writeUTF(res);
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void handleBadRequest(String req) {
		try {
			String res = "";
			res += p2pVersion+"\t400\tBad Request\r\n";
			out.writeUTF(res);
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void handleVersionMismatch(String req) {
		try {
			String res = "";
			res += p2pVersion+"\t505\tP2P-CI Version Not Supported\r\n";
			out.writeUTF(res);
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	public void handleNotFound(String req) {
		try {
			String res = "";
			res += p2pVersion+"\t404\tNot Found\r\n";
			out.writeUTF(res);
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
}
