package defyndian.core;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.rabbitmq.client.BasicProperties;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rabbitmq.client.Channel;

import defyndian.exception.DefyndianMQException;
import defyndian.messaging.BasicDefyndianMessage;
import defyndian.messaging.DefyndianEnvelope;
import defyndian.messaging.DefyndianMessage;
import defyndian.messaging.DefyndianRoutingKey;
import defyndian.messaging.InvalidRoutingKeyException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PublisherTest {

	private static DefyndianEnvelope<? extends DefyndianMessage> sampleEnvelopeA;
	
	private BlockingQueue<DefyndianEnvelope<? extends DefyndianMessage>> messageQueue;
	@Mock
	private static Channel channel;
	private Publisher publisher;
	
	@BeforeClass
	public static void setupMessages() throws InvalidRoutingKeyException, IOException{
		sampleEnvelopeA = new DefyndianEnvelope<BasicDefyndianMessage>(new DefyndianRoutingKey("TEST.DEFAULT.A"), new BasicDefyndianMessage("Sample Message A"));
	}
	
	@Before
	public void setup() throws Exception{
		messageQueue = new LinkedBlockingQueue<>();
		publisher = new Publisher(messageQueue, channel);
	}
	
	@Test
	public void publishEnvelope_WaitForSend_ShouldSendToChannel() throws Exception{
		publisher.publish(sampleEnvelopeA);
		Thread t = new Thread(publisher);
		t.start();
		Thread.sleep(500);	// Allow the publisher a chance to start up
		publisher.setStop();
		t.join();
		verify(channel).basicPublish(anyString(), anyString(), anyObject(), any(byte[].class));
		assert(messageQueue.poll()==null);
	}
}
