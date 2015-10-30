package defyndian.messaging;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;


public class DefyndianMessage {

	private static final ObjectMapper objectMapper = new ObjectMapper();
	
	public static final String BODY_KEY = "body";
	public static final String CREATION_TIME_KEY = "created";
	
	private Date messageStamped;
	private ObjectNode root;
	
	protected DefyndianMessage(byte[] body) throws JsonProcessingException, IOException {
		root = (ObjectNode)objectMapper.readTree(body);
		JsonNode creationTime = root.path(CREATION_TIME_KEY);
		if( creationTime.isMissingNode() )
			root.put(CREATION_TIME_KEY, new Date().toInstant().getEpochSecond());
		
		messageStamped = Date.from(Instant.ofEpochMilli(root.path(CREATION_TIME_KEY).asLong()));
		
	}
	
	protected DefyndianMessage(String body) throws JsonProcessingException, IOException{
		this(body.getBytes());
	}
	
	protected DefyndianMessage(byte[] body, HashMap<String, String> struct) throws JsonProcessingException, IOException {
		this(body);
		for( String key : struct.keySet() ){
			root.put(key, struct.get(key));
		}
	}
	
	public static DefyndianMessage fromJSONData(byte[] data) throws JsonProcessingException, IOException {
		return new DefyndianMessage(data);
	}
	
	public static DefyndianMessage withBody(String data) throws JsonProcessingException, IOException{
		return new DefyndianMessage(data);
	}
	
	public String toJSONString(){
		return root.toString();
	}
	
	public Date getCreationTime(){
		return messageStamped;
	}
	
	public String getMessageBody(){
		JsonNode body = root.path(BODY_KEY);
		if( body.isMissingNode() )
			return null;
		else
			return body.asText();
	}
	
}
