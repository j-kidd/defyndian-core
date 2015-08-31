package messaging;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class DefyndianMessage {

	public static final String BODY_KEY = "body";
	public static final String CREATION_TIME_KEY = "created";
	
	private static final JSONParser parser = new JSONParser();
	private JSONObject extras;
	
	protected DefyndianMessage(byte[] body) throws ParseException{
		extras = (JSONObject) parser.parse(new String(body));
		long timeMillis = (long) extras.get(CREATION_TIME_KEY);
		extras.put(CREATION_TIME_KEY, Date.from(Instant.ofEpochMilli(timeMillis)));
	}
	
	protected DefyndianMessage(String body){
		extras = new JSONObject();
		extras.put(BODY_KEY, body);
		extras.put(CREATION_TIME_KEY, System.currentTimeMillis());
	}
	
	protected DefyndianMessage(byte[] body, HashMap<String, String> struct) throws ParseException{
		this(body);
		extras.putAll(struct);
	}
	
	public static DefyndianMessage fromJSONData(byte[] data) throws ParseException{
		return new DefyndianMessage(data);
	}
	
	public static DefyndianMessage withBody(String data){
		return new DefyndianMessage(data);
	}
	
	public String toJSONString(){
		return extras.toJSONString();
	}
	
	
	
	public Date getCreationTime(){
		Object body = extras.get(CREATION_TIME_KEY);
		if( body!=null )
			return (Date)body;
		else
			return null;
	}
	
	public String getMessageBody(){
		Object body = extras.get(BODY_KEY);
		if( body!=null )
			return body.toString();
		else
			return null;
	}
	
	public Set<String> getExtrasKeys(){
		return extras.keySet();
	}
	
	public String getExtra(String key){
		return extras.get(key).toString();
	}
}
