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

/**
 * A DefyndianMessage represents the various types of message sent between DefyndianNodes.
 * The data is JSON structured with a timestamp and a few set keys
 * @author james
 *
 */
public class DefyndianMessage {

	private static final ObjectMapper objectMapper = new ObjectMapper();
	
	public static final String BODY_KEY = "body";
	public static final String CREATION_TIME_KEY = "created";
	
	private Date messageStamped;
	private ObjectNode root;
	
	/**
	 * Create a message with this set json structure
	 * @param rootNode
	 */
	protected DefyndianMessage(ObjectNode rootNode){
		root = rootNode;
		JsonNode creationTime = root.path(CREATION_TIME_KEY);
		if( creationTime.isMissingNode() )
			root.put(CREATION_TIME_KEY, new Date().toInstant().getEpochSecond());
		
		messageStamped = Date.from(Instant.ofEpochMilli(root.path(CREATION_TIME_KEY).asLong()));
	}
	
	/**
	 * Parse this message from the given byte string (as received in AMQP)
	 * @param jsonString A string of json to form this message
	 * @throws JsonProcessingException If the given string was not json
	 * @throws IOException
	 */
	protected DefyndianMessage(byte[] jsonString) throws JsonProcessingException, IOException {
		this((ObjectNode)objectMapper.readTree(jsonString));
	}
	
	/**
	 * Same as byte[] but for a string
	 * @param body
	 * @throws JsonProcessingException
	 * @throws IOException
	 */
	protected DefyndianMessage(String body) throws JsonProcessingException, IOException{
		this(body.getBytes());
	}
	
	/**
	 * Define a new message from bytes but with set overriding key values too
	 * @param body
	 * @param struct
	 * @throws JsonProcessingException
	 * @throws IOException
	 */
	protected DefyndianMessage(byte[] body, HashMap<String, String> struct) throws JsonProcessingException, IOException {
		this(body);
		for( String key : struct.keySet() ){
			root.put(key, struct.get(key));
		}
	}
	
	/**
	 * Static accessors, wrapper method to create a new message with the given data
	 * as body
	 * @param data
	 * @return
	 * @throws JsonProcessingException
	 * @throws IOException
	 */
	public static DefyndianMessage fromJSONData(byte[] data) throws JsonProcessingException, IOException {
		ObjectNode r = objectMapper.createObjectNode();
		r.put(BODY_KEY, data);
		return new DefyndianMessage(r);
	}
	
	/**
	 * Same as bytes but for a string
	 * @param data
	 * @return
	 * @throws JsonProcessingException
	 * @throws IOException
	 */
	public static DefyndianMessage withBody(String data) throws JsonProcessingException, IOException{
		ObjectNode r = objectMapper.createObjectNode();
		r.put(BODY_KEY, data);
		
		return new DefyndianMessage(r);
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
