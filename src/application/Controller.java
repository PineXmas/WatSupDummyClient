package application;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import com.sun.javafx.image.ByteToBytePixelConverter;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class Controller {
	public Button btnSpawnClient;
	public Button btnDisconnectAll;
	public Label labelCountClients;
	public TextField txtServerPort;
	public CheckBox checkRandMsg;
	public TextField txtMinMsgs;
	public TextField txtMaxMsgs;
	public Button btnTest;
	public TextField txtServerHost;
	
	//client list
	public Integer countClients = 0;
	public ArrayList<Socket> listSockets = new ArrayList<>();
	public ArrayList<Thread> listThreads = new ArrayList<>();
	public ArrayList<Integer> listClientIDs = new ArrayList<>();
	
	//others
	static Random randomizer = new Random(System.currentTimeMillis());
	
	public void onBtnSpawnClient_Click(ActionEvent event) {
		System.out.println("btnSpawnClient is clicked");
		
		try {
			++countClients;
			labelCountClients.setText(countClients.toString());
			
			//get port number
			Integer portNumber =Integer.valueOf(txtServerPort.getText());
			
			//settings for random message sending
			boolean isSendRandMsg = checkRandMsg.isSelected();
			int minMsgs = Integer.valueOf(txtMinMsgs.getText());
			int maxMsgs = Integer.valueOf(txtMaxMsgs.getText());
			int randMsgs = randomizer.nextInt(maxMsgs - minMsgs + 1) + minMsgs;
			
			//start connection thread
			Socket clientSocket = new Socket();
			Integer clientID = listSockets.size()+1;
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					try {
						String hostName = txtServerHost.getText();
						InetSocketAddress a = new InetSocketAddress(hostName, portNumber);
						System.out.println("Client " + clientID + " connecting to server...");
						clientSocket.connect(a);
						System.out.println("Client " + clientID + " connected to server: " + clientSocket.getInetAddress().toString());
						
						//setup
						OutputStream outputStream = clientSocket.getOutputStream();
						String name = "[" + clientSocket.getLocalAddress().getHostName() + ":" + clientSocket.getLocalPort() + "]";
						
						
						if (isSendRandMsg) {
							System.out.println("Client " + name + " will sends " + randMsgs + " msgs in total");
							
							for (int i = 0; i < randMsgs; i++) {
								//prepare message
								int dataLength = randomizer.nextInt(20);
								String s = genRandomString(dataLength);
								int opcode = randomizer.nextInt();
								ByteBuffer buff = ByteBuffer.allocate(4+4+dataLength);
								buff.putInt(opcode).putInt(dataLength).put(s.getBytes());
								System.out.println("  client " + name + " sends: " + opcode + "|" + dataLength + "|" + s);
								
								//send message
								outputStream.write(buff.array());
								
								//sleep for some time
								int sleepMsecs = randomizer.nextInt(5001);
								System.out.println("  client " + name + " sleeps: " + ((float)sleepMsecs/1000.0f) + " secs");
								Thread.sleep(sleepMsecs);
							}
							
							System.out.println("Client " + name + " has done sending");
						} else {
							//send its names some times to the server
							int maxTrials = 100;
							for (int i=0; i<maxTrials; i++) {
								String content = name + "(" + i + ")";
								byte[] buff = content.getBytes();
								outputStream.write(buff);
							}
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

	public void onBtnTest_Click(ActionEvent event) {
		System.out.println("btnTest is clicked");
		
		int n = randomizer.nextInt(10) + 5;
		String s = genRandomString(n);
		System.out.println("n=" + n + ": " + s);
	}
	
	/**
	 * Generate random string of n chars
	 * @param n
	 * @return
	 */
	public static String genRandomString(int n) {
		String s = "";
		for (int i = 0; i < n; i++) {
			char c = (char)(randomizer.nextInt(122-97+1) + 97);
			s += c;
		}
		
		return s;
	}
}
