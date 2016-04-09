package defyndian.messaging;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A DefyndianMessage is the primary method of communication between Nodes.
 * @author james
 *
 */
public class BasicDefyndianMessage extends TimeStampedMessage {

	private final String message;
	
	public BasicDefyndianMessage(String body ) {
		this(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC), body);
	}
	
	@JsonCreator
	public BasicDefyndianMessage(@JsonProperty("timestamp") long epochSeconds, @JsonProperty("message") String data) {
		super(epochSeconds);
		message = data;
	}
	
	public String getMessage(){
		return message;
	}
	
	@Override
	public boolean equals(Object other){
		if( other == this )
			return true;
		else if( other.getClass() != this.getClass() )
			return false;
		
		BasicDefyndianMessage otherMessage = (BasicDefyndianMessage) other;
		return message.equals(otherMessage.getMessage()) & getTimestamp() == otherMessage.getTimestamp();
	}
	
	@Override
	public int hashCode(){
		int result = 39;
		result = 17*result + (int)(getTimestamp()^(getTimestamp()>>32));
		result = 17*result + message.hashCode();
		return result;
	}
	
	public String toString(){
		return "{ " + getTimestamp() + " - " + message + " }";
	}
}
