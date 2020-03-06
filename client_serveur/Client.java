

import java.io.*;
import java.net.*;
import java.util.*;


public class Client {

	public static void main(String[] args) throws UnknownHostException, IOException {
		// Open your connection to a server, at port 1254
		Socket socketClient = null;
		DataInputStream dataInputStream = null;
		DataOutputStream dataOutputStream = null;
		Scanner scanner = null;
		String msg_end = "bye";
		try {
			socketClient = new Socket("127.0.0.1",1254);
			String messageFromServer;
			String messageToServer;
			do {
				dataInputStream = new DataInputStream(socketClient.getInputStream());
				messageFromServer = new String (dataInputStream.readUTF());
				System.out.println("server : " + messageFromServer);
				// When done, just close the connection and exit
				System.out.print("> ");
				scanner = new Scanner(System.in);
				messageToServer = scanner.nextLine();
				dataOutputStream = new DataOutputStream(socketClient.getOutputStream());
				dataOutputStream.writeUTF(messageToServer);
				dataOutputStream.flush();
			//} while(!messageFromServer.equals(msg_end)); // stop when server said bye
			} while(!messageToServer.equals(msg_end)); // stop when client said bye
		
		// Managing exception
		} catch (ConnectException e) {
			System.err.println("Error: "+e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			System.err.println("Error: "+e.getMessage());
			e.printStackTrace();
		
		
		
		
		// Closing socket
		} finally {
			dataOutputStream.close();
			scanner.close();
			dataInputStream.close();
			socketClient.close();
			System.out.print("[Connection closed] ");
		}
	}

}
