package defyndian.core;

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

    /**
     * Creates a consumer in addition to DefyndianNode construction
     *
     * @param name Name of this actor
     * @throws DefyndianMQException       As thrown by DefyndianNode constructor
     * @throws DefyndianDatabaseException As thrown by DefyndianNode constructor
     */
    public TestableDefyndianActor(String name) throws DefyndianMQException, DefyndianDatabaseException, ConfigInitialisationException {
        super(name);
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
