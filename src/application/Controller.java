package application;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Random;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
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
	
	//message types
	public RadioButton radioRandomMsg;
	public RadioButton radioLoginMsg;
	public RadioButton radioLogoutMsg;
	public RadioButton radioLoginSuccessMsg;
	public RadioButton radioListRoomsMsg;
	public RadioButton radioListRoomsRespMsg;
	public RadioButton radioListUsersRespMsg;
	public RadioButton radioJoinMsg;
	public RadioButton radioLeaveMsg;
	public RadioButton radioSendRoomMsgMsg;
	public RadioButton radioTellRoomMsgMsg;
	public RadioButton radioSendPrivateMsgMsg;
	public RadioButton radioTellPrivateMsgMsg;
	public RadioButton radioErrorUnknownMsg;
	public RadioButton radioErrorNameExistsMsg;
	public RadioButton radioErrorTooUsersMsg;
	public RadioButton radioErrorTooRoomsMsg;
	public RadioButton radioErrorKickedOutMsg;
	
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
								WSMessage msg;
								if (radioRandomMsg.isSelected()) {
									msg = genRandomMsg();
								} else if (radioLoginMsg.isSelected()) {
									msg = genLoginMsg();
								} else if (radioLogoutMsg.isSelected()) {
									msg = genLogoutMsg();
								} else if (radioLoginSuccessMsg.isSelected()) {
									msg = genLoginSuccessMsg();
								} else if (radioListRoomsMsg.isSelected()) {
									msg = genListRoomsMsg();
								} else if (radioListRoomsRespMsg.isSelected()) {
									msg = genListRoomsRespMsg();
								} else if (radioListUsersRespMsg.isSelected()) {
									msg = genListUsersRespMsg();
								} else if (radioJoinMsg.isSelected()) {
									msg = genJoinRoomMsg();
								} else if (radioLeaveMsg.isSelected()) {
									msg = genLeaveRoomMsg();
								} else if (radioSendPrivateMsgMsg.isSelected()) {
									msg = genSendPrivateMsgMsg();
								} else if (radioTellPrivateMsgMsg.isSelected()) {
									msg = genTellPrivateMsgMsg();
								} else if (radioSendRoomMsgMsg.isSelected()) {
									msg = genSendRoomMsgMsg();
								} else if (radioTellRoomMsgMsg.isSelected()) {
									msg = genTellRoomMsgMsg();
								} else if (radioErrorUnknownMsg.isSelected()) {
									msg = genErrorUnknownMsg();
								} else if (radioErrorNameExistsMsg.isSelected()) {
									msg = genErrorNameExistsMsg();
								} else if (radioErrorTooUsersMsg.isSelected()) {
									msg = genErrorTooManyUsersMsg();
								} else if (radioErrorTooRoomsMsg.isSelected()) {
									msg = genErrorTooManyRoomsMsg();
								} else if (radioErrorKickedOutMsg.isSelected()) {
									msg = genErrorKickedOutMsg();
								} else {
									System.out.println("No message type is selected. Sending skipped");
									continue;
								}
								
								
								System.out.println("  client " + name + " sends: " + msg.toString());
								
								//send message
								outputStream.write(msg.msgBytes);
								
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
	
	public WSMessage genRandomMsg() {

		int dataLength = randomizer.nextInt(20);
		String s = genRandomString(dataLength);
		int opcode = randomizer.nextInt();
		ByteBuffer buff = ByteBuffer.allocate(4+4+dataLength);
		buff.putInt(opcode).putInt(dataLength).put(s.getBytes());
		
		return new WSMUnknown(opcode, dataLength, buff.array(), null);
	}
	
	public WSMessage genLoginMsg() {

		int nameLength = randomizer.nextInt(WSSettings._LABEL_SIZE);
		String s = genRandomString(nameLength);
		
		return new WSMLogin(s);
	}
	
	public WSMessage genLogoutMsg() {
		
		return new WSMLogout();
	}
	
	public WSMessage genLoginSuccessMsg() {
		
		return new WSMLoginSuccess();
	}
	
	public WSMessage genListRoomsMsg() {
		
		return new WSMListRooms();
	}
	
	public WSMessage genJoinRoomMsg() {
		int nameLength = randomizer.nextInt(WSSettings._LABEL_SIZE);
		String roomName = genRandomString(nameLength);
		return new WSMJoinRoom(roomName);
	}
	
	public WSMessage genLeaveRoomMsg() {
		int nameLength = randomizer.nextInt(WSSettings._LABEL_SIZE);
		String roomName = genRandomString(nameLength);
		return new WSMLeaveRoom(roomName);
	}
	
	public WSMessage genListRoomsRespMsg() {
		int nRooms = randomizer.nextInt(10);
		String[] arrRoomNames = new String[nRooms];
		for (int i = 0; i < nRooms; i++) {
			int nameLength = randomizer.nextInt(WSSettings._LABEL_SIZE);
			String roomName = genRandomString(nameLength);
			arrRoomNames[i] = roomName;
		}
		return new WSMListRoomsResp(arrRoomNames);
	}
	
	public WSMessage genListUsersRespMsg() {
		int nameLength = randomizer.nextInt(WSSettings._LABEL_SIZE);
		String roomName = genRandomString(nameLength);
		
		int nRooms = randomizer.nextInt(10);
		String[] arrUserNames = new String[nRooms];
		for (int i = 0; i < nRooms; i++) {
			nameLength = randomizer.nextInt(WSSettings._LABEL_SIZE);
			String name = genRandomString(nameLength);
			arrUserNames[i] = name;
		}
		
		return new WSMListUsersResp(roomName, arrUserNames);
	}
	
	public WSMessage genSendRoomMsgMsg() {
		int n = randomizer.nextInt(WSSettings._LABEL_SIZE);
		String roomName = genRandomString(n);
		n = randomizer.nextInt(WSSettings._LABEL_SIZE * 4);
		String content  = genRandomString(n);
		return new WSMSendRoomMsg(roomName, content);
	}

	public WSMessage genTellRoomMsgMsg() {
		int n = randomizer.nextInt(WSSettings._LABEL_SIZE);
		String roomName = genRandomString(n);
		n = randomizer.nextInt(WSSettings._LABEL_SIZE * 4);
		String userName = genRandomString(n);
		n = randomizer.nextInt(WSSettings._LABEL_SIZE * 4);
		String content  = genRandomString(n);
		return new WSMTellRoomMsg(userName, roomName, content);
	}
	
	public WSMessage genSendPrivateMsgMsg() {
		int n = randomizer.nextInt(WSSettings._LABEL_SIZE);
		String name = genRandomString(n);
		n = randomizer.nextInt(WSSettings._LABEL_SIZE * 4);
		String content  = genRandomString(n);
		return new WSMSendPrivateMsg(name, content);
	}
	
	public WSMessage genTellPrivateMsgMsg() {
		int n = randomizer.nextInt(WSSettings._LABEL_SIZE);
		String name = genRandomString(n);
		n = randomizer.nextInt(WSSettings._LABEL_SIZE * 4);
		String content  = genRandomString(n);
		return new WSMTellPrivateMsg(name, content);
	}
	
	public WSMessage genErrorUnknownMsg() {
		return new WSMError(WSMCode.ERR_UNKNOWN);
	}
	
	public WSMessage genErrorNameExistsMsg() {
		
		return new WSMError(WSMCode.ERR_NAME_EXISTS);
	}
	
	public WSMessage genErrorTooManyUsersMsg() {
		
		return new WSMError(WSMCode.ERR_TOO_MANY_USERS);
	}
	
	public WSMessage genErrorTooManyRoomsMsg() {
		
		return new WSMError(WSMCode.ERR_TOO_MANY_ROOMS);
	}
	
	public WSMessage genErrorKickedOutMsg() {

		return new WSMError(WSMCode.ERR_KICKED_OUT);
	}
}
