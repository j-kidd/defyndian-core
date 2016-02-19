package defyndian.messaging;

import java.time.LocalDateTime;

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
	
	
	@JsonCreator
	public BasicDefyndianMessage( @JsonProperty String body) {
		this(LocalDateTime.now(), body);
	}
	
	public BasicDefyndianMessage(LocalDateTime datetime, String data) {
		message = data;
		timestamp = datetime;
	}
	
	public String getMessage(){
		return message;
	}
	
	@Override
	public LocalDateTime getTimestamp() {
		return timestamp;
	}
}
