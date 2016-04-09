package defyndian.core;

import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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

public class PublisherTest {

	private static DefyndianEnvelope<? extends DefyndianMessage> sampleEnvelopeA;
	
	private BlockingQueue<DefyndianEnvelope<? extends DefyndianMessage>> messageQueue;
	private static Channel channel;
	private Publisher publisher;
	
	@BeforeClass
	public static void setupMessages() throws InvalidRoutingKeyException, IOException{
		sampleEnvelopeA = new DefyndianEnvelope<BasicDefyndianMessage>(new DefyndianRoutingKey("TEST.DEFAULT.A"), new BasicDefyndianMessage("Sample Message A"));
		channel = mock(Channel.class);
	}
	
	@Before
	public void createPublisher() throws DefyndianMQException{
		messageQueue = new LinkedBlockingQueue<>();
		publisher = new Publisher(messageQueue, channel);
	}
	
	@Test
	public void sampleMessageIsPublished() throws JsonProcessingException, InterruptedException{
		messageQueue.add(sampleEnvelopeA);
		Thread t = new Thread(publisher);
		t.start();
		Thread.sleep(500);	// Allow the publisher a chance to start up
		publisher.setStop();
		t.join();
		assert(messageQueue.poll()==null);
	}
}
