package structure;

public class Device {

	private int id;
	private String name;
	
	public Device(int id, String name){
		this.id = id;
		this.name = name;
	}
	
	public int getID(){
		return id;
	}
	
	public String toString(){
		return String.format("[%d] %s", id, name);
	}
}
