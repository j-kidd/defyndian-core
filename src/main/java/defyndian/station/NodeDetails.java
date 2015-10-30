package defyndian.station;

import java.net.InetAddress;

public class NodeDetails {

	private int id;
	private String name;
	private String description;
	private InetAddress host;
	
	
	public NodeDetails(int id, String name, String description, InetAddress host) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.host = host;
	}


	public int getID() {
		return id;
	}


	public String getName() {
		return name;
	}


	public String getDescription() {
		return description;
	}
	
	public InetAddress getHost(){
		return host;
	}
	
	
}
