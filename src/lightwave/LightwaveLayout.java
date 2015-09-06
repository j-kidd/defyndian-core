package lightwave;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import structure.Device;
import structure.Room;

public class LightwaveLayout {

	private static final String DEVICE_ID_STRING = "deviceID";
	private static final String ROOM_ID_STRING = "roomid";
	private static final String DEVICE_NAME_STRING = "deviceName";
	private static final String ROOM_NAME_STRING = "roomName";
	
	private int nextDeviceID = 0;
	
	private HashMap<String, Room> rooms;
	
	public LightwaveLayout(){
		rooms = new HashMap<>();
	}
	
	public Room getRoom(String name){
		return rooms.get(name);
	}
	
	/**
	 * Adds a device with the given name to the specified room, this assumes the ids don't matter, and 
	 * will be generated
	 * @param deviceName
	 * @param roomName
	 * @return true if the device was inserted, false if not
	 */
	public boolean add(String deviceName, String roomName){
		if( rooms.containsKey(roomName) ){
			return rooms.get(roomName).add(deviceName);
		}
		else{
			rooms.put(roomName, new Room(getNextID(), roomName));
			return rooms.get(roomName).add(deviceName);
		}
	}
	
	/**
	 * Similar to add but this method ensures the ids of the device and room are as
	 * specified else fail
	 * @param deviceID
	 * @param deviceName
	 * @param roomID
	 * @param roomName
	 * @return
	 */
	public boolean add(int deviceID, String deviceName, int roomID, String roomName){
		Room room;
		if( rooms.containsKey(roomName) ){
			room = rooms.get(roomName);
			if( room.getID()!=roomID )
				return false;
		}
		else{
			rooms.put(roomName, new Room(roomID, roomName));
			room = rooms.get(roomName);
		}
		
		return room.add(deviceID, deviceName);
	}
	
	public static LightwaveLayout constructLightwaveLayout(MysqlDataSource datasource) throws SQLException{
		Connection conn = datasource.getConnection();
		LightwaveLayout layout = new LightwaveLayout();
		
		PreparedStatement statement = conn.prepareCall("call lightwave.getLightwaveLayout");
		statement.execute();
		ResultSet results = statement.getResultSet();
		while( results.next() ){
			int deviceID = results.getInt(DEVICE_ID_STRING);
			int roomID = results.getInt(ROOM_ID_STRING);
			String deviceName = results.getString(DEVICE_NAME_STRING);
			String roomName = results.getString(ROOM_NAME_STRING);
			layout.add(deviceID, deviceName, roomID, roomName);
		}
		return layout;
	}
	
	private int getNextID(){
		return nextDeviceID++;
	}
	
	public String toString(){
		StringBuilder builder = new StringBuilder();
		for( Room r : rooms.values()){
			builder.append(r);
			builder.append(System.lineSeparator());
			for( Device d : r.iterateDevices()){
				builder.append("\t");
				builder.append(d);
				builder.append(System.lineSeparator());
			}
			builder.append(System.lineSeparator());
		}
		return builder.toString();
	}
}
