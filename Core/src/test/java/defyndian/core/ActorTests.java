package defyndian.core;

import defyndian.config.DefyndianConfig;
import defyndian.datastore.DefyndianDatastore;
import defyndian.messaging.*;
import defyndian.messaging.messages.BasicDefyndianMessage;
import defyndian.messaging.messages.DefyndianMessage;
import defyndian.messaging.routing.DefyndianRoutingKey;
import defyndian.messaging.routing.DefyndianRoutingType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.TimeUnit;

@RunWith(MockitoJUnitRunner.class)
public class ActorTests {

	@Mock private Consumer consumer;
    @Mock private Publisher publisher;
    @Mock private DefyndianConfig config;
    @Mock private DefyndianDatastore datastore;

	@InjectMocks private TestableDefyndianActor actor;

	private final DefyndianRoutingKey testKey = new DefyndianRoutingKey("Station", DefyndianRoutingType.NOTIFICATION, "test");
	private final BasicDefyndianMessage testMessage = new BasicDefyndianMessage("Sample test message");
	private final DefyndianEnvelope testEnvelope = new DefyndianEnvelope(testKey, testMessage);
    private final DefyndianMessage unknownMessage = new DefyndianMessage(){

        @Override
        public long getTimestamp() {
            return 0;
        }
    };
    private final DefyndianEnvelope unknownEnvelope = new DefyndianEnvelope(testKey, unknownMessage);

    @Before
	public void setup() throws Exception {

	}
	
	@Test
	public void handleMessage_validMessage_shouldStoreMessage() throws Exception{
        when(consumer.poll(anyLong(), any(TimeUnit.class))).thenReturn(testEnvelope);
        actor.tryToProcessAMessage();
        assertThat(testMessage.equals(actor.getReceivedMessages().findFirst().get()), is(true));
	}

    @Test
    public void handleMessage_unknownMessageClass_handleException() throws Exception {
        when(consumer.poll(anyLong(), any(TimeUnit.class))).thenReturn(unknownEnvelope);
        actor.tryToProcessAMessage();
        assertThat(actor.getReceivedMessages().count(), is(0l));
    }
}
