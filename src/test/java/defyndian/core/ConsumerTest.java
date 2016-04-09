package defyndian.core;

import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;

import defyndian.core.Consumer;
import defyndian.exception.DefyndianMQException;
import defyndian.messaging.BasicDefyndianMessage;
import defyndian.messaging.DefyndianEnvelope;
import defyndian.messaging.DefyndianMessage;
import defyndian.messaging.DefyndianRoutingKey;
import defyndian.messaging.InvalidRoutingKeyException;

public class ConsumerTest {

	private static final String consumerTag = "CONSUMER_TAG";
	private static final String exchange = "EXCHANGE";
	private static final String queue = "QUEUE";
	private static final String routingKeyA = "ROUTING_KEY_A";
	private static final String routingKeyB = "ROUTING_KEY_B";
	private static final ObjectMapper mapper = new ObjectMapper();
	
	private static DefyndianEnvelope<? extends DefyndianMessage> sampleEnvelopeA;
	private static DefyndianEnvelope<? extends DefyndianMessage> sampleEnvelopeB;
	
	private BlockingQueue<DefyndianEnvelope<? extends DefyndianMessage>> messageQueue;
	private static Channel channel;
	private Consumer consumer;
	
	@BeforeClass
	public static void setupMessages() throws InvalidRoutingKeyException{
		sampleEnvelopeA = new DefyndianEnvelope<BasicDefyndianMessage>(new DefyndianRoutingKey("TEST.DEFAULT.A"), new BasicDefyndianMessage("Sample Message A"));
		sampleEnvelopeB = new DefyndianEnvelope<BasicDefyndianMessage>(new DefyndianRoutingKey("TEST.DEFAULT.B"), new BasicDefyndianMessage("Sample Message B"));
		channel = mock(Channel.class);
	}
	
	@Before
	public void createConsumer() throws DefyndianMQException{
		messageQueue = new LinkedBlockingQueue<>();
		consumer = new Consumer(messageQueue, channel, exchange, queue, Arrays.asList(routingKeyA, routingKeyB));
	}
	
	@Test
	public void testStartCleanly() throws DefyndianMQException {
		consumer.start(consumerTag);
	}
	
	@Test
	public void sampleMessageIsDelivered() throws JsonProcessingException{
		consumer.handleDelivery(consumerTag, new Envelope(1, false, exchange, routingKeyA), null, mapper.writeValueAsBytes(sampleEnvelopeA));
		DefyndianEnvelope<? extends DefyndianMessage> envelope = messageQueue.poll();
		assert(envelope != null);
		assert(envelope.getMessage().equals(sampleEnvelopeA.getMessage()));
	}
}
