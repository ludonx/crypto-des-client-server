

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

	public static void main(String[] args) throws IOException {
		
		System.out.println("S: Server is started");
		ServerSocket serverSocket = new ServerSocket(9999);
		
		System.out.println("S: Server is waiting for client request");
		Socket clientSocket = serverSocket.accept();
		PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
		BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

		System.out.println("S: Client Connected");
		
		String str = in.readLine();
		
		System.out.println("S: Client Data : "+ str);
		
		
		// String response = "[Bonjour client]";
		// out.write(response);
		// out.flush();
		// System.out.println("S: Response sent to client "+ str);
		
		out.close();
		serverSocket.close();
	}

}
