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
public class BasicDefyndianMessage implements DefyndianMessage {

	private final LocalDateTime timestamp;
	private final String message;
	
	public BasicDefyndianMessage(String body ) {
		this(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC), body);
	}
	
	@JsonCreator
	public BasicDefyndianMessage(@JsonProperty("timestamp") long epochSeconds, @JsonProperty("message") String data) {
		message = data;
		timestamp = LocalDateTime.ofEpochSecond(epochSeconds, 0, ZoneOffset.UTC);
	}
	
	public String getMessage(){
		return message;
	}
	
	public long getTimestamp() {
		return timestamp.toEpochSecond(ZoneOffset.UTC);
	}
	
	public String toString(){
		return "{ " + timestamp + " - " + message + " }";
	}
}
