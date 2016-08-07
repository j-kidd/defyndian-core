package defyndian.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import defyndian.config.RabbitMQDetails;
import defyndian.exception.ConfigInitialisationException;
import defyndian.exception.DefyndianMQException;
import defyndian.messaging.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.Connection;
import java.util.Collections;
import java.util.concurrent.LinkedBlockingQueue;

import static org.mockito.Mockito.*;

/**
 * Created by James on 07/08/2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class DefyndianNodeTest {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String exchange = "TEST-EXCHANGE";
    private static final String queue = "TEST-QUEUE";

    @Mock private DefyndianNode node;

    @Mock private Channel channel;
    @Mock private RabbitMQDetails rabbitMQDetails;
    private Publisher publisher;
    private Consumer consumer;

    @Before
    public void setup() throws DefyndianMQException, ConfigInitialisationException, InterruptedException {
        when(rabbitMQDetails.getExchange()).thenReturn(exchange);
        when(rabbitMQDetails.getQueue()).thenReturn(queue);

        publisher = new Publisher(channel);
        consumer = new Consumer(channel, rabbitMQDetails, Collections.emptyList());
        node.consumer = consumer;
        node.publisher = publisher;

    }

    @Test
    public void publish_standardMessage_channelShouldPublishGivenEnvelope() throws Exception {
        doCallRealMethod().when(node).publish(any(DefyndianEnvelope.class));
        publisher.start("TEST-PUBLISHER");
        DefyndianMessage message = new BasicDefyndianMessage("TEST-MESSAGE");
        DefyndianEnvelope<DefyndianMessage> envelope = new DefyndianEnvelope<>(exchange, DefyndianRoutingKey.getDefaultKey("TEST"), message);
        node.publish(envelope);

        ArgumentCaptor<byte[]> messageCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(channel, timeout(5000)).basicPublish(eq(exchange), anyString(), anyObject(), messageCaptor.capture());
        assertEquals("Message sent on channel does not match", new String(messageCaptor.getValue()), mapper.writeValueAsString(envelope));
    }

    @Test
    public void getMessageFromInbox_basicMessagesFromConsumer_shouldReceiveMessageFromConsumer() throws DefyndianMQException, JsonProcessingException, InterruptedException {
        when(node.consume()).thenCallRealMethod();
        final DefyndianEnvelope<BasicDefyndianMessage> envelope1 = new DefyndianEnvelope<>(DefyndianRoutingKey.getDefaultKey("TEST"), new BasicDefyndianMessage("envelope1"));
        final DefyndianEnvelope<BasicDefyndianMessage> envelope2 = new DefyndianEnvelope<>(DefyndianRoutingKey.getDefaultKey("TEST"), new BasicDefyndianMessage("envelope2"));
        final DefyndianEnvelope<BasicDefyndianMessage> envelope3 = new DefyndianEnvelope<>(DefyndianRoutingKey.getDefaultKey("TEST"), new BasicDefyndianMessage("envelope3"));

        consumer.handleDelivery("TEST", null, null, mapper.writeValueAsBytes(envelope1));
        consumer.handleDelivery("TEST", null, null, mapper.writeValueAsBytes(envelope2));
        consumer.handleDelivery("TEST", null, null, mapper.writeValueAsBytes(envelope3));

        final DefyndianEnvelope<? extends DefyndianMessage> received1;
        final DefyndianEnvelope<? extends DefyndianMessage> received2;
        final DefyndianEnvelope<? extends DefyndianMessage> received3;

        received1 = node.consume();
        received2 = node.consume();
        received3 = node.consume();

        assertEquals(envelope1, received1);
        assertEquals(envelope2, received2);
        assertEquals(envelope3, received3);

    }
}
