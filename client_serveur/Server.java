
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.*;

public class Server {
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

	// RSA generateAndSavePairKey : génération des clés publique et privé 
	//--------------------------------------------------------------------//
	public static void generateAndSavePairKey(String outpubFile, String outpvtFile) throws Exception {
		//---------------1. Génération du couple de clés
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
	    kpg.initialize(2048);
	    KeyPair kp = kpg.generateKeyPair();
	    Key pub = kp.getPublic();
	    Key pvt = kp.getPrivate();
		
		//---------------2. Sauvegarde du couple de clés au format binaire
		// FileOutputStream outpub = new FileOutputStream(outpubFile);
		// outpub.write(pub.getEncoded());
		// //outpub.write(Base64.getEncoder().encode(pub.getEncoded()));
		// outpub.close();

		FileOutputStream outpvt = new FileOutputStream(outpvtFile);
		outpvt.write(pvt.getEncoded());
		outpvt.close();

		//---------------2. Sauvegarde du couple de clés au format text afin de facilité l'envoie par la socket
		Base64.Encoder encoder = Base64.getEncoder();
 
		//String outFile =  +".txt";
		// Writer out = new FileWriter(outpvtFile);
		// out.write("-----BEGIN RSA PRIVATE KEY-----\n");
		// out.write(encoder.encodeToString(pvt.getEncoded()));
		// out.write("\n-----END RSA PRIVATE KEY-----\n");
		// out.close();

		//outFile =  +".txt";
		Writer out = new FileWriter(outpubFile);
		//out.write("-----BEGIN RSA PUBLIC KEY-----\n");
		out.write(encoder.encodeToString(pub.getEncoded()));
		//out.write("\n-----END RSA PUBLIC KEY-----\n");
		out.close();
	}
	//--------------------------------------------------------------------//
	public static void main(String[] args) throws Exception {

		System.out.println("Generating and  Saving the Key Pair [OK]");
		// --------------------------------//
		String outpubFile ="disk/server/publicKey.pub" ;
		String outpvtFile = "disk/server/privateKey.key";
		generateAndSavePairKey(outpubFile,outpvtFile);
	
		// --------------------------------//

		DataOutputStream dataOutputStream = null;
		DataInputStream dataInputStream = null;		
		// Register service on port 1254
		ServerSocket serverSocket = null;
		Socket socketClient = null;
		Scanner scanner = null;
		int port_con = 1254;

		// --------------------------------//
		String filePathToPublicKey = "disk/server/publicKey.pub" ;
		String filePathToPrivateKey = "disk/server/privateKey.key";
		
		// --------------------------------//
		String msg_end = "bye";
		String msg_init_session = "HELLO";
		int nbr_msg = 0;
		//int key_SDES = 0;
		String key_DES = "0000000000000000";
		// --------------------------------//
		try {
			// message pour initaliser la connection
			serverSocket = new ServerSocket(port_con);
			System.out.println("Server is running...");
			socketClient = serverSocket.accept();
			String messageFromClient;
			String messageToClient;
			dataOutputStream = new DataOutputStream(socketClient.getOutputStream());
			dataOutputStream.writeUTF("Hi There");

			do {		
				// les deux 1er messages ne sont pas crypter
				if(nbr_msg == 0 || nbr_msg == 1){
					dataInputStream = new DataInputStream(socketClient.getInputStream());
					messageFromClient = new String (dataInputStream.readUTF());
					System.out.println("CLIENT : " + messageFromClient);
				}else{ 
				// a partir d'ici tous les messages sont crypté donc on appliquer DES pour decrypter
					dataInputStream = new DataInputStream(socketClient.getInputStream());
					messageFromClient = new String (dataInputStream.readUTF());
					
					//----- Affichage du message du serveur -------//
					System.out.println("CLIENT : " + messageFromClient);
					// #SDES
					//System.out.println("client SDES: "+ SDES.decrypt(messageFromClient, SDES.getK1(key_SDES),SDES.getK2(key_SDES)));
					// #DES
					System.out.println("client DES: "+ DES.decrypt(messageFromClient, key_DES));
					//----- -------------------------------- -------//

				}

				
				if(messageFromClient.equals(msg_init_session)){
					// à la reception du message d'initialisation
					// on envoie la clé publique au client 
					System.out.println("[... envoie de ma clé publique ...]");
					messageToClient = readFile( filePathToPublicKey );

					dataOutputStream.writeUTF(messageToClient);
					dataOutputStream.flush();
					nbr_msg = nbr_msg + 1;
				}else if(nbr_msg == 1){
					// à la reception du 2eme message ( la clé DES chiffré avec RSA)
					// on dechiffre le message en utilisant la clès privé du serveur

					/* Read private key. */
					Path path = Paths.get(filePathToPrivateKey);
					byte[] bytes = Files.readAllBytes(path);
					
					/* Generate private key. */
					PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(bytes);
					KeyFactory kf = KeyFactory.getInstance("RSA");
					PrivateKey pvtRead = kf.generatePrivate(ks);

					byte[] message = Base64.getDecoder().decode(messageFromClient);
					
					byte[] recovered_message = decrypt(pvtRead,message);
					// #SDES
					//String key_SDES_str = new String(recovered_message, "UTF8");
					//key_SDES = Integer.parseInt(key_SDES_str,2);
					// #DES
					key_DES = new String(recovered_message, "UTF8");
		
					System.out.println("[... clé secrète chiffré reçu ...]");
					//System.out.println("key_SDES        : " + " [ "+ key_SDES_str +" ]  "+  key_SDES);
					System.out.println("key_DES        : " + " [ "+ key_DES +" ]  ");

					System.out.println("[... envoie de l'acquitement...]");
					messageToClient = "clé secrète chiffré reçu";
					// #SDES
					//messageToClient = SDES.encrypt(messageToClient, SDES.getK1(key_SDES),SDES.getK2(key_SDES));
					// #DES
					messageToClient = DES.encrypt(messageToClient, key_DES);
					
					dataOutputStream.writeUTF(messageToClient);
					dataOutputStream.flush();
					nbr_msg = nbr_msg + 1;

				}else{
					// si on est dans cette partie, tous les message doivent être crypter avec DES avant d'être envoyer
					System.out.print("> ");
					scanner = new Scanner(System.in);
					messageToClient = scanner.nextLine();
					// #SDES
					//messageToClient = SDES.encrypt(messageToClient, SDES.getK1(key_SDES),SDES.getK2(key_SDES));

					// #DES
					messageToClient = DES.encrypt(messageToClient, key_DES);

					dataOutputStream.writeUTF(messageToClient);
					dataOutputStream.flush();
					nbr_msg = nbr_msg + 1;
				}
				
				
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
