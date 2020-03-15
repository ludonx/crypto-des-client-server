
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.*;




public class Client {

	// RSA encrypt/decrypt : les fonctions pour le chiffrement /dechiffrement en RSA 
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
		String address_serveur = "127.0.0.1";
		int port_con = 1254;

		String msg_end = "bye";
		String msg_init_session = "HELLO";
		String messageFin = "vide";
		int nbr_msg = 0;
		//int key_SDES = 0;
		String key_DES = "0000000000000000"; 

		// --------------------------------//

		try {
			socketClient = new Socket(address_serveur,port_con);
			String messageFromServer;
			String messageToServer;
			
			do {
				if(nbr_msg == 0){
					// envoie du message pour initialiser la session
					dataInputStream = new DataInputStream(socketClient.getInputStream());
					messageFromServer = new String (dataInputStream.readUTF());
					System.out.println("SERVER : " + messageFromServer);

					System.out.println("[... envoie du message["+msg_init_session+"] de debut de session ...]");
					messageToServer = msg_init_session;

					dataOutputStream = new DataOutputStream(socketClient.getOutputStream());
					dataOutputStream.writeUTF(messageToServer);
					dataOutputStream.flush();
					nbr_msg = nbr_msg + 1;

				}else if(nbr_msg == 1){
					// reception et sauvegarde de la clé publique du serveur
					dataInputStream = new DataInputStream(socketClient.getInputStream());
					messageFromServer = new String (dataInputStream.readUTF());
					System.out.println("SERVER : " + messageFromServer);

					System.out.println("[... sauvegarde de la clé plublique du server ...]");
    				Files.write(Paths.get(filePathToPublicKeyServer), messageFromServer.getBytes());
					
					//-------------------------//
					// je convertir la cles recu dans un nouveau format (PublicKey)
					/* je génére la clé publique. */
					X509EncodedKeySpec kspub = new X509EncodedKeySpec(read( filePathToPublicKeyServer ));
					KeyFactory kfpub = KeyFactory.getInstance("RSA");
					PublicKey pubRead = kfpub.generatePublic(kspub);
					//-------------------------//

					System.out.println("[... génération de la clé DES ...]");
					//--------------------- #SDES -----------------------//
					//key_SDES = 0b1010000010;
					//System.out.println("key_SDES        : " + " [ "+ Integer.toBinaryString(key_SDES)       +" ]  "+  key_SDES);
					
					//--------------------- #DES -----------------------//
					key_DES = "AABB09182736CCDD";
					System.out.println("key_DES        : " + " [ "+ key_DES +" ]  ");

					System.out.println("[... chiffrement de la clé DES en RSA avec la clé publique du serveur ...]");
					// chiffement de la clè DES en utilisant les clé publique que serveur
					// sachant que le server pourra la dechifrre uniquement avec sa cles priver
        			//SDES//byte[] secret = encrypt(pubRead, Integer.toBinaryString(key_SDES).getBytes());
					byte[] secret = encrypt(pubRead, key_DES.getBytes());

					//-------------------------------------------------//


					
					System.out.println("[... envoie de la clé secrète chiffré ...]");
					
					// Base64.getEncoder().encodeToString(secret); : permet de ne pas avoir de probleme lors de l'envoie de message avec de charactére spéciaux
					messageToServer = Base64.getEncoder().encodeToString(secret);
					
					// Envoie du message chiffré au serveur
					dataOutputStream = new DataOutputStream(socketClient.getOutputStream());
					dataOutputStream.writeUTF(messageToServer);
					dataOutputStream.flush();
					nbr_msg = nbr_msg + 1;

				}else{
					// si on est dans cette partie, tous les message doivent être crypter avec DES avant d'être envoyer
					// et tous les messages reçu doivent être decrypter et ceux en utilisant la clé génére plus haut
					dataInputStream = new DataInputStream(socketClient.getInputStream());
					messageFromServer = new String (dataInputStream.readUTF());

					//----- Affichage du message du serveur -------//
					System.out.println("SERVER : " + messageFromServer);
					// chiffrement avec #SDES
					//System.out.println("server SDES : " + SDES.decrypt(messageFromServer, SDES.getK1(key_SDES),SDES.getK2(key_SDES)));
					// chiffrement avec DES
					System.out.println("server DES : " + DES.decrypt(messageFromServer,key_DES));
					
					//----- -------------------------------- -------//

					System.out.print("> ");
					scanner = new Scanner(System.in);
					messageToServer = scanner.nextLine();
					// je garde une copie du message pour faire un test et savois si l'utilisateur veux interrompre l'échange ou pas
					messageFin = messageToServer;
					// #SDES
					//messageToServer = SDES.encrypt(messageToServer, SDES.getK1(key_SDES),SDES.getK2(key_SDES));
					// #DES
					messageToServer = DES.encrypt(messageToServer, key_DES);


					dataOutputStream = new DataOutputStream(socketClient.getOutputStream());
					dataOutputStream.writeUTF(messageToServer);
					dataOutputStream.flush();
					nbr_msg = nbr_msg + 1;
				}
				
				
			//} while(!messageFromServer.equals(msg_end)); // stop when server said bye
			} while(!messageFin.equals(msg_end)); // stop when client said bye
		
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
