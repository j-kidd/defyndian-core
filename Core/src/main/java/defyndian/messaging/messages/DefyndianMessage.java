package defyndian.messaging.messages;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="messageClass")
public interface DefyndianMessage {

	public long getTimestamp();

}
