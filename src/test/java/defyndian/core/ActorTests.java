package defyndian.core;

import com.rabbitmq.client.ConnectionFactory;
import defyndian.config.DefyndianConfig;
import defyndian.config.RabbitMQDetails;
import defyndian.messaging.DefyndianEnvelope;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import defyndian.messaging.BasicDefyndianMessage;
import defyndian.messaging.DefyndianRoutingKey;
import defyndian.messaging.DefyndianRoutingType;

import java.util.concurrent.TimeUnit;

@RunWith(MockitoJUnitRunner.class)
public class ActorTests {

    //@Mock private RabbitMQDetails testRabbitMQDetails;
	//@Mock private DefyndianConfig config;
	@Mock private Consumer consumer;

	@InjectMocks private TestableDefyndianActor actor;

	private final DefyndianRoutingKey testKey = new DefyndianRoutingKey("Station", DefyndianRoutingType.NOTIFICATION, "test");
	private final BasicDefyndianMessage testMessage = new BasicDefyndianMessage("Sample test message");
	private final DefyndianEnvelope testEnvelope = new DefyndianEnvelope(testKey, testMessage);

    @Before
	public void setup() throws Exception {
        when(consumer.poll(anyLong(), any(TimeUnit.class))).thenReturn(testEnvelope);
	}
	
	@Test
	public void handleMessage_validMessage_shouldPrintMessage() throws Exception{
        actor.start();
        assertThat(testMessage.equals(actor.getReceivedMessages().findFirst().get()), is(true));
	}
}
