
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.*;




public class Client {

	// RSA encrypt/decrypt
	//--------------------------------------------------------------------//
	static byte[] encrypt(PublicKey key, byte[] plaintext) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException
	{
	    Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");   
	    cipher.init(Cipher.ENCRYPT_MODE, key);  
	    return cipher.doFinal(plaintext);
	}

	static byte[] decrypt(PrivateKey key, byte[] ciphertext) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException
	{
	    Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");   
	    cipher.init(Cipher.DECRYPT_MODE, key);  
	    return cipher.doFinal(ciphertext);
	}

	//--------------------------------------------------------------------//
	
	public static void main(String[] args) throws UnknownHostException, IOException {
		// Open your connection to a server, at port 1254
		Socket socketClient = null;
		DataInputStream dataInputStream = null;
		DataOutputStream dataOutputStream = null;
		Scanner scanner = null;

		// --------------------------------//
		String filePathToPublicKeyServer = "disk/client/server_publique.pub";

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
					
					//-------------------------//
					// je convertir la cles recu dans un nouveau format (PublicKey)
					/* Generate public key. */
					X509EncodedKeySpec kspub = new X509EncodedKeySpec(read( filePathToPublicKeyServer ));
					KeyFactory kfpub = KeyFactory.getInstance("RSA");
					PublicKey pubRead = kfpub.generatePublic(kspub);
					System.out.println(pubRead);
					//-------------------------//

					System.out.println("[... génération de la clé DES ...]");
					int key = 0b1010000010;
					System.out.println("key        : " + " [ "+ Integer.toBinaryString(key)       +" ]  "+  key);
					
					System.out.println("[... chiffrement de la clé DES en RSA avec la clé publique du serveur ...]");
					// sachant que le server pourra la dechifrre uniquement avec sa cles priver
        			byte[] secret = encrypt(pubRead, Integer.toBinaryString(key).getBytes());
					System.out.println("[... envoie de la clé secrète chiffré ...]");
					String msg =  secret.toString();
					System.out.println(msg);
					//messageToServer = SDES.encrypt(msg, SDES.getK1(key),SDES.getK2(key));
					messageToServer = msg;

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

	private static byte[] read(String path) {
        try {
            return Base64.getDecoder().decode(Files.readAllBytes(new File(path).toPath()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read data from file: " + path, e);
        }
    }   

}
