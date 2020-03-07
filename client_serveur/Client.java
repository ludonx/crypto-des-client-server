

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;


public class Client {

	public static void main(String[] args) throws UnknownHostException, IOException {
		// Open your connection to a server, at port 1254
		Socket socketClient = null;
		DataInputStream dataInputStream = null;
		DataOutputStream dataOutputStream = null;
		Scanner scanner = null;

		// --------------------------------//
		String filePathToPublicKeyServer = "disk/client/server_publique.pub.pem";

		// --------------------------------//

		String msg_end = "bye";
		int nbr_msg = 0;
		String msg_init_session = "HELLO";
		try {
			socketClient = new Socket("127.0.0.1",1254);
			String messageFromServer;
			String messageToServer;
			
			do {
				// When done, just close the connection and exit
				if(nbr_msg == 0){
					dataInputStream = new DataInputStream(socketClient.getInputStream());
					messageFromServer = new String (dataInputStream.readUTF());
					System.out.println("server : " + messageFromServer);

					System.out.println("[... envoie du message["+msg_init_session+"] de debut de session ...]");
					messageToServer = msg_init_session;
				}else if(nbr_msg == 1){
					dataInputStream = new DataInputStream(socketClient.getInputStream());
					messageFromServer = new String (dataInputStream.readUTF());
					System.out.println("server : " + messageFromServer);

					System.out.println("[... sauvegarde de la clé plublique du server ...]");
    				Files.write(Paths.get(filePathToPublicKeyServer), messageFromServer.getBytes());

					System.out.println("[... génération de la clé DES ...]");
					System.out.println("[... chiffrement de la clé DES en RSA avec la clé publique du serveur ...]");
					System.out.println("[... envoie de la clé secrète chiffré ...]");
					messageToServer = "voici ma clés secrète chiffré...";
				}else{
					dataInputStream = new DataInputStream(socketClient.getInputStream());
					messageFromServer = new String (dataInputStream.readUTF());
					System.out.println("server : " + messageFromServer);
					System.out.print("> ");
					scanner = new Scanner(System.in);
					messageToServer = scanner.nextLine();
				}
				
				dataOutputStream = new DataOutputStream(socketClient.getOutputStream());
				dataOutputStream.writeUTF(messageToServer);
				dataOutputStream.flush();
				nbr_msg = nbr_msg + 1;
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
