
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.*;

public class Server {
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

	public static void generateAndSavePairKey(String outpubFile, String outpvtFile) throws Exception {
		//---------------1. Generating a Key Pair
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
	    kpg.initialize(2048);
	    KeyPair kp = kpg.generateKeyPair();
	    Key pub = kp.getPublic();
	    Key pvt = kp.getPrivate();
		
		//---------------2. Saving the Keys in Binary Format
		
		// FileOutputStream outpub = new FileOutputStream(outpubFile);
		// outpub.write(pub.getEncoded());
		// //outpub.write(Base64.getEncoder().encode(pub.getEncoded()));
		// outpub.close();

		FileOutputStream outpvt = new FileOutputStream(outpvtFile);
		outpvt.write(pvt.getEncoded());
		outpvt.close();

		//---------------3. Saving the Keys in Text Format
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

		// --------------------------------//

		String filePathToPublicKey = "disk/server/publicKey.pub" ;
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
				//int key = 0b1010000010;
				//System.out.println("client : " + SDES.decrypt(messageFromClient, SDES.getK1(key),SDES.getK2(key)));
				
				if(messageFromClient.equals(msg_init_session)){
					System.out.println("[... envoie de ma clé publique ...]");
					// Path pathpub = Paths.get("disk/server/publicKey.pub.pem");
					//byte[] bytespub = read("disk/server/publicKey.pub.pem");
					// byte[] bytespub = Files.readAllBytes(pathpub);
					// /* Generate public key. */
					// X509EncodedKeySpec kspub = new X509EncodedKeySpec(read( filePathToPublicKey ));
					// KeyFactory kfpub = KeyFactory.getInstance("RSA");
					// PublicKey pubRead = kfpub.generatePublic(kspub);
					// System.out.println(pubRead);
					messageToClient = readFile( filePathToPublicKey );
				}else if(nbr_msg == 1){
					System.out.println("[... clé secrète chiffré reçu ...]");
					messageToClient = "clé secrète chiffré reçu";

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

	private static byte[] read(String path) {
        try {
            return Base64.getDecoder().decode(Files.readAllBytes(new File(path).toPath()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read data from file: " + path, e);
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
