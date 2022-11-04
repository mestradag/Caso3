package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientMain {
	
	public static final int PUERTO = 4030;
	public static final String SERVIDOR = "localhost";
	
	public static void main(String[] args) throws IOException {
		
		Socket socket = null;
		PrintWriter escritor = null;
		BufferedReader lector = null;
		System.out.println("Client...");
		try {
			// creates the socket in client side
			socket = new Socket (SERVIDOR, PUERTO) ;
			escritor = new PrintWriter(socket.getOutputStream(), true);
			lector = new BufferedReader (new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
				e.printStackTrace();
				System.exit(-1);
		}
		
		// creates object to read user's input
		BufferedReader stdIn = new BufferedReader (new InputStreamReader(System.in));

		// runs client thread
		ClientThread clientThread = new ClientThread(stdIn, lector, escritor);
		clientThread.start();
	}
}
