
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

public class Server {

	public static void main(String[] args) throws IOException {
		DataOutputStream dataOutputStream = null;
		DataInputStream dataInputStream = null;		
		// Register service on port 1254
		ServerSocket serverSocket = null;
		Socket socketClient = null;
		Scanner scanner = null;

		// --------------------------------//

		String filePathToPublicKey = "disk/server/publique.pub.pem";
		//filePathToPublicKey = "/home/ludovik/Bureau/ING3/crypto/crypto-des-client-server/client_serveur/disk/server/test.txt";
		// --------------------------------//
		String msg_end = "bye";
		int nbr_msg = 0;
		String msg_init_session = "HELLO";
		// --------------------------------//
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
				
				if(messageFromClient.equals(msg_init_session)){
					System.out.println("[... envoie de ma clé publique ...]");
					messageToClient = readFile( filePathToPublicKey );
				}else if(nbr_msg == 1){
					System.out.println("[... clé secrète chiffré reçu ...]");
					messageToClient = readFile( filePathToPublicKey );

				}else{
					System.out.print("> ");
					scanner = new Scanner(System.in);
					messageToClient = scanner.nextLine();
				}
				dataOutputStream.writeUTF(messageToClient);
				dataOutputStream.flush();
				nbr_msg = nbr_msg + 1;
				
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


	private static String readFile(String filePath) 
    {
        String content = "";
 
        try
        {
            content = new String ( Files.readAllBytes( Paths.get(filePath) ) );
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
 
        return content;
    }

}
