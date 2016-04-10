package messaging;

import org.junit.Test;

import defyndian.messaging.DefyndianRoutingKey;
import defyndian.messaging.InvalidRoutingKeyException;

public class MessageTest {

	@Test
	public void createRoutingKeys() throws InvalidRoutingKeyException{
		new DefyndianRoutingKey("PRODUCER.#");
		new DefyndianRoutingKey("PRODUCER.*.extra");
		new DefyndianRoutingKey("PRODUCER.DEFAULT.extra");
		new DefyndianRoutingKey("PRODUCER.NOTIFICATION.*");
	}
}
