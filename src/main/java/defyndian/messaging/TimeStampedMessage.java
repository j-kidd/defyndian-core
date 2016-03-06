package defyndian.messaging;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public abstract class TimeStampedMessage implements DefyndianMessage {

	private final LocalDateTime timestamp;

	public TimeStampedMessage(long epochSeconds) {
		timestamp = LocalDateTime.ofEpochSecond(epochSeconds, 0, ZoneOffset.UTC);

	}
	
	@Override
	public long getTimestamp() {
		return timestamp.toEpochSecond(ZoneOffset.UTC);
	}

}
