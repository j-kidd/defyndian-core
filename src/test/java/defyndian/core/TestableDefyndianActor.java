package defyndian.core;

import com.rabbitmq.client.Connection;
import defyndian.config.DefyndianConfig;
import defyndian.datastore.DefyndianDatastore;
import defyndian.datastore.exception.DatastoreCreationException;
import defyndian.exception.ConfigInitialisationException;
import defyndian.exception.DefyndianDatabaseException;
import defyndian.exception.DefyndianMQException;
import defyndian.messaging.BasicDefyndianMessage;
import defyndian.messaging.DefyndianMessage;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by james on 25/09/16.
 */
public class TestableDefyndianActor extends DefyndianActor{

    private final List<? super DefyndianMessage> receivedMessages;

    public TestableDefyndianActor
            (String name,
             DefyndianConfig config,
             Publisher publisher,
             Consumer consumer,
             DefyndianDatastore datastore){
        super(name, config, publisher, consumer, datastore);
        receivedMessages = new LinkedList<>();
    }

    @Override
    protected void handleMessage(BasicDefyndianMessage message) {
        receivedMessages.add(message);
    }

    public final Stream<? super DefyndianMessage> getReceivedMessages(){
        return receivedMessages.stream();
    }
}
