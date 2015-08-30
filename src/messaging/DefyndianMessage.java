package messaging;

import java.util.Date;
import java.util.HashMap;
import java.util.Set;

public class DefyndianMessage {

	private Date creationTime;
	private byte[] body;
	private HashMap<String, String> extras;
	
	public DefyndianMessage(byte[] body){
		creationTime = new Date();
		this.body = body;
		extras = new HashMap<>();
	}
	
	public DefyndianMessage(byte[] body, HashMap<String, String> struct){
		this(body);
		extras = struct;
	}
	
	public static DefyndianMessage fromData(byte[] data){
		return new DefyndianMessage(data);
	}
	
	public Date getCreationTime(){
		return creationTime;
	}
	
	public byte[] getMessageBody(){
		return body;
	}
	
	public boolean hasExtras(){
		return ! extras.keySet().isEmpty();
	}
	
	public Set<String> getExtrasKeys(){
		return extras.keySet();
	}
	
	public String getExtra(String key){
		return extras.get(key);
	}
}
