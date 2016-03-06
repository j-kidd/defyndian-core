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
	
	public String toString(){
		return "{ " + getTimestamp() + " - " + message + " }";
	}
}
