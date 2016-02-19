package defyndian.messaging;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public interface DefyndianMessage {

	public LocalDateTime getTimestamp();
}
