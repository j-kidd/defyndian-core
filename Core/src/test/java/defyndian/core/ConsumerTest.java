package defyndian.core;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;

import defyndian.config.RabbitMQDetails;
import defyndian.exception.ConfigInitialisationException;
import defyndian.exception.DefyndianMQException;
import defyndian.messaging.messages.BasicDefyndianMessage;
import defyndian.messaging.DefyndianEnvelope;
import defyndian.messaging.messages.DefyndianMessage;
import defyndian.messaging.routing.DefyndianRoutingKey;
import defyndian.messaging.routing.InvalidRoutingKeyException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConsumerTest {

	private static final String consumerTag = "CONSUMER_TAG";
	private static final String exchange = "EXCHANGE";
	private static final String queue = "QUEUE";
	private static DefyndianRoutingKey routingKeyA; 
	private static DefyndianRoutingKey routingKeyB; 
	private static final ObjectMapper mapper = new ObjectMapper();
	
	private static DefyndianEnvelope<? extends DefyndianMessage> sampleEnvelopeA;
	private static DefyndianEnvelope<? extends DefyndianMessage> sampleEnvelopeB;

	@Mock private Channel channel;
	@Mock private RabbitMQDetails rabbitMQDetails;

    private Consumer consumer;
	
	@BeforeClass
	public static void setupMessages() throws InvalidRoutingKeyException{
		routingKeyA = new DefyndianRoutingKey("TEST.#");
		routingKeyB = new DefyndianRoutingKey("TEST.*.EXTRA");
		sampleEnvelopeA = new DefyndianEnvelope<BasicDefyndianMessage>(routingKeyA, new BasicDefyndianMessage("Sample Message A"));
		sampleEnvelopeB = new DefyndianEnvelope<BasicDefyndianMessage>(routingKeyB, new BasicDefyndianMessage("Sample Message B"));
	}
	
	@Before
	public void setup() throws DefyndianMQException, ConfigInitialisationException{
        MockitoAnnotations.initMocks(this);
        consumer = new Consumer(channel, rabbitMQDetails, Arrays.asList(routingKeyA, routingKeyB));
    }
	
	@Test
	public void testStartCleanly() throws Exception {
		consumer.start(consumerTag);
	}
	
	@Test
	public void handleDelivery_simpleMessage_deliveredSuccessfully() throws Exception {
		consumer.handleDelivery(
				consumerTag,
				new Envelope(1, false, exchange, routingKeyA.toString()),
				null,
				mapper.writeValueAsBytes(sampleEnvelopeA));
		DefyndianEnvelope<? extends DefyndianMessage> envelope = consumer.poll(1, TimeUnit.SECONDS);
		assert(envelope != null);
		assert(envelope.getMessage().equals(sampleEnvelopeA.getMessage()));
	}

}
