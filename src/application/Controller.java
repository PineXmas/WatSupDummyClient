package application;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class Controller {
	public Button btnSpawnClient;
	public Button btnDisconnectAll;
	public Label labelCountClients;
	public TextField txtServerPort;
	
	//client list
	public Integer countClients = 0;
	public ArrayList<Socket> listSockets = new ArrayList<>();
	public ArrayList<Thread> listThreads = new ArrayList<>();
	public ArrayList<Integer> listClientIDs = new ArrayList<>();
	
	public void onBtnSpawnClient_Click(ActionEvent event) {
		System.out.println("btnSpawnClient is clicked");
		
		try {
			++countClients;
			labelCountClients.setText(countClients.toString());
			
			//get port number
			Integer portNumber =Integer.valueOf(txtServerPort.getText());
			
			//start connection thread
			Socket clientSocket = new Socket();
			Integer clientID = listSockets.size()+1;
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					try {
						InetSocketAddress a = new InetSocketAddress("localhost", portNumber);
						System.out.println("Client " + clientID + " connecting to server...");
						clientSocket.connect(a);
						System.out.println("Client " + clientID + " connected to server: " + clientSocket.getInetAddress().toString());
						
						//send its names some times to the server
						int maxTrials = 100;
						String name = "[" + clientSocket.getLocalAddress().getHostName() + ":" + clientSocket.getLocalPort() + "]";
						OutputStream outputStream = clientSocket.getOutputStream();
						for (int i=0; i<maxTrials; i++) {
							String content = name + "(" + i + ")";
							byte[] buff = content.getBytes();
							outputStream.write(buff);
						}
					} catch (Exception e) {
						//e.printStackTrace();
						System.out.println(e.getMessage());
					}
					
				}
			});
			thread.start();
			
			//add to list
			listSockets.add(clientSocket);
			listThreads.add(thread);
			listClientIDs.add(listThreads.size());
		} catch (Exception e) {
			//e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}
	
	public void onBtnDisconnectAll_Click(ActionEvent event) {
		System.out.println("btnDisconnectAll is clicked");
		
		try {
			for (int i = 0; i < listClientIDs.size(); i++) {
				Integer id = listClientIDs.get(i);
				System.out.print("Client " + id + ": ");
				
				//interrupt thread
				Thread thread = listThreads.get(i);
				if (thread.isAlive()) {
					System.out.print("kill...");
					thread.interrupt();
					System.out.print("OK, ");
				}
				
				//close socket
				Socket socket = listSockets.get(i);
				if (socket.isConnected()) {
					System.out.print("close...");
					socket.close();
					System.out.print("OK, ");
				}
				
				System.out.println("done");
			}
			
			//empty all lists
			listThreads.clear();
			listSockets.clear();
			listClientIDs.clear();
			countClients = 0;
			labelCountClients.setText(countClients.toString());
		} catch (Exception e) {
			//e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}
}
