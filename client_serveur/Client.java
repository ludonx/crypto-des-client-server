

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;


public class Client {

	public static void main(String[] args) throws UnknownHostException, IOException {
		String ip="localhost";
		int port = 9999; //0-1023 to 65535
		Socket clientSocket = new Socket(ip,port);
		PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
		BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

		String str_send = "Bonjour je suis le client";
		
		out.write(str_send);
		out.flush();
		
		//String str_recev = in.readLine();
		//System.out.println("C: Data from Server " + str_recev);
		
		out.close();
		clientSocket.close();

	}

}
