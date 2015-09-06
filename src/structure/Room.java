package structure;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

public class Room {

	private int id;
	private String name;
	
	private int nextRoomID = 0;
	private HashMap<String, Device> devices;
	
	public Room(int id, String name){
		this.id = id;
		this.name = name;
		devices = new HashMap<>();
	}
	
	public Device getDevice(String deviceName){
		return devices.get(deviceName);
	}
	
	public int getID(){
		return id;
	}
	
	public boolean add( int deviceID, String deviceName ){
		Device device = devices.get(deviceName);
		
		if( device!=null ){
			return device.getID()==deviceID;
		}
		else{
			devices.put(deviceName, new Device(deviceID, deviceName));
			return true;
		}
	}
	
	public boolean add( String deviceName ){
		if( devices.containsKey(deviceName) ){
			return false;
		}
		else{
			devices.put(deviceName, new Device(getNextID(), deviceName));
			return true;
		}
	}
	
	public Collection<Device> iterateDevices(){
		return devices.values();
	}
	
	public String toString(){
		return String.format("[%d] %s", id, name);
	}
	
	private int getNextID(){
		return nextRoomID++;
	}
}
