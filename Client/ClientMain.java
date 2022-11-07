package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientMain {
	
	//ClientMain ejecuta la aplicacion cliente

	public static final int PUERTO = 4030;
	public static final String SERVIDOR = "localhost";
	
	public static void main(String[] args) throws IOException {
		
		Scanner numClient = new Scanner(System.in);

    	System.out.println("Enter the number of clients:");

		int numClients = numClient.nextInt();
		System.out.println("The number of clientes is: " + numClients);

		System.out.println("Client...");
		try {
			// creates the socket in client side
			for(int i = 0; i < numClients; i++) {
				Socket socket = new Socket(SERVIDOR, PUERTO);
				PrintWriter escritor = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader lector = new BufferedReader(new InputStreamReader(socket.getInputStream()));

				// runs client thread
				ClientThread clientThread = new ClientThread(lector, escritor);
				clientThread.start();

				System.out.println("Client " + i + " connected");
			}
		} catch (IOException e) {
				e.printStackTrace();
				System.exit(-1);
		}
		
		// creates object to read user's input
		//BufferedReader stdIn = new BufferedReader (new InputStreamReader(System.in));

		// runs client thread
		//ClientThread clientThread = new ClientThread(stdIn, lector, escritor);
		//clientThread.start();

	}
}
