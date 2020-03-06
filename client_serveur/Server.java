

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {

	public static void main(String[] args) throws IOException {
		DataOutputStream dataOutputStream = null;
		DataInputStream dataInputStream = null;		
		// Register service on port 1254
		ServerSocket serverSocket = null;
		Socket socketClient = null;
		Scanner scanner = null;
		String msg_end = "bye";
		
		try {
			serverSocket = new ServerSocket(1254);
			System.out.println("Server is running...");
			socketClient = serverSocket.accept();
			String messageFromClient;
			String messageToClient;
			dataOutputStream = new DataOutputStream(socketClient.getOutputStream());
			dataOutputStream.writeUTF("Hi There");
			do {			
				dataInputStream = new DataInputStream(socketClient.getInputStream());
				messageFromClient = new String (dataInputStream.readUTF());
				System.out.println("client : " + messageFromClient);
				System.out.print("> ");
				
				scanner = new Scanner(System.in);
				messageToClient = scanner.nextLine();
				dataOutputStream.writeUTF(messageToClient);
				dataOutputStream.flush();
				
				
			//} while(!messageToClient.equals(msg_end));//close when server said bye
			} while(!messageFromClient.equals(msg_end));//close when client said bye
			// Close the connection, but not the server socket
			System.out.print("[client said : bye] ");
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
			dataInputStream.close();
			scanner.close();
			socketClient.close();
			serverSocket.close();
			System.out.print("[Connection closed] ");
		}
	}

}
