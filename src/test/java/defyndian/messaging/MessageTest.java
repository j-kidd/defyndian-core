package defyndian.messaging;

import org.junit.Test;

import defyndian.messaging.DefyndianRoutingKey;
import defyndian.messaging.InvalidRoutingKeyException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class MessageTest {

	private final String producer = "PRODUCER";

	private final DefyndianRoutingKey routingKeyA = new DefyndianRoutingKey(producer, DefyndianRoutingType.ALL, "*");
	private final DefyndianRoutingKey routingKeyB = new DefyndianRoutingKey(producer, DefyndianRoutingType.ALL, "extra");
	private final DefyndianRoutingKey routingKeyC = new DefyndianRoutingKey(producer, DefyndianRoutingType.DEFAULT, "extra");
	private final DefyndianRoutingKey routingKeyD = new DefyndianRoutingKey(producer, DefyndianRoutingType.NOTIFICATION, "*");


	@Test
	public void createRoutingKey_nonDefaultType_specificExtra() throws InvalidRoutingKeyException{
		assertThat(new DefyndianRoutingKey("PRODUCER.NOTIFICATION.*"), is(routingKeyD));
	}

	@Test
	public void createRoutingKeys_defaultType_specificExtra() throws InvalidRoutingKeyException{
		assertThat(new DefyndianRoutingKey("PRODUCER.DEFAULT.extra"), is(routingKeyC));
	}

	@Test
	public void createRoutingKeys_multimatch() throws InvalidRoutingKeyException{
		assertThat(new DefyndianRoutingKey("PRODUCER.#"), is(routingKeyA));
	}

	@Test
	public void createRoutingKeys_allTypes_specificExtra() throws InvalidRoutingKeyException{
		assertThat(new DefyndianRoutingKey("PRODUCER.*.extra"), is(routingKeyB));
	}


}
