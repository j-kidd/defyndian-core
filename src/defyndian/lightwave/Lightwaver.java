package defyndian.lightwave;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.Scanner;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import defyndian.lightwave.structure.Room;

public class Lightwaver {
	
	private static int TRANSMIT_PORT = 9760;
	private static final InetSocketAddress LIGHTWAVE_LINK_ADDRESS = new InetSocketAddress("LWLink", TRANSMIT_PORT);
	
	private LightwaveLayout layout;
	private DatagramSocket sendSocket;
	private int messageID;
	
	private static final Logger logger = Logger.getLogger("Lightwaver");
	
	public Lightwaver(LightwaveLayout layout) throws LightwaverException{
		BasicConfigurator.configure();
		this.layout = layout;
		try {
			initialiseSockets();
		} catch (SocketException e) {
			logger.error(e);
			throw new LightwaverException("Could not initialise sockets to LightwaveLink");
		}
		
		messageID = 0;
	}
	
	public void command(String roomName, String deviceName, boolean state) throws LightwaverException{
		Room room = layout.getRoom(roomName);
		int roomID = room.getID();
		int deviceID = room.getDevice(deviceName).getID();
		String commandWord = state ? "F1" : "F0";
		
		String commandString = String.format("!R%dR%d%s", roomID, deviceID, commandWord);
		logger.info("Sending Command: " + commandString);
		send(commandString);
	}
	
	private void send(String command) throws LightwaverException{
		try {
			logger.info("Connecting to LightwaveLink: " + LIGHTWAVE_LINK_ADDRESS);
			sendSocket.connect(LIGHTWAVE_LINK_ADDRESS);
			String actualCommand = getNextID().toString() + "," + command + "\n";
			DatagramPacket message = new DatagramPacket(actualCommand.getBytes(), actualCommand.length());
			logger.info("Sending: " + actualCommand);
			sendSocket.send(message);
			sendSocket.disconnect();
			
		} catch (IOException e) {
			throw new LightwaverException("Could not send message: " + command);
		}
		
	}

	private void initialiseSockets() throws SocketException{
		sendSocket = new DatagramSocket();
	}
	private Integer getNextID(){
		return messageID++;
	}
	
	public static void main(String...args){
		if( args.length < 3 ){
			System.err.println("Require: HOST USERNAME PASSWORD for database config");
			System.exit(1);
		}
		String host = args[0];
		String username = args[1];
		String password = args[2];
		MysqlDataSource datasource = new MysqlDataSource();
		datasource.setServerName(host);
		datasource.setUser(username);
		datasource.setPassword(password);
		datasource.setDatabaseName("lightwave");
		Scanner scanner = null;
		try{
			LightwaveLayout layout = LightwaveLayout.constructLightwaveLayout(datasource);
			Lightwaver lightwaver = new Lightwaver(layout);
			System.out.println(layout);
			String input;
			scanner = new Scanner(System.in);
			while( scanner.hasNextLine() ){
				input = scanner.nextLine();
				String[] tokens = input.split(" ");
				String room = tokens[0];
				String device = tokens[1];
				boolean command = tokens[2].trim().equals("ON");
				System.out.format("Turning %s [%s:%s]", tokens[2], room, device);
				lightwaver.command(room, device, command);
			}
		} catch( SQLException s){
			System.err.println("SQLException encountered: " + s);
		} catch (LightwaverException e) {
			System.err.println("LightwaverException: " + e);
		} finally{
			scanner.close();
		}
	}
}
