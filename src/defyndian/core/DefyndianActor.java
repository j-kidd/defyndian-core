package defyndian.core;

import exception.DefyndianDatabaseException;
import exception.DefyndianMQException;
import messaging.DefyndianEnvelope;
import messaging.DefyndianMessage;

public abstract class DefyndianActor extends DefyndianNode {

	private String queue;
	
	public DefyndianActor(String name) throws DefyndianMQException, DefyndianDatabaseException {
		super(name);
		setConsumer();
	}

	protected void tryToProcessAMessage() throws InterruptedException{
		logger.debug("Waiting on getting a message from inbox");
		DefyndianMessage message = getMessageFromInbox();
		if( message != null )  // Null is returned on timeout
			handleMessage(message);
	}
	
	protected abstract void handleMessage(DefyndianMessage message);
	
	@Override
	public void start() throws Exception {
		logger.info(getName() + " started");
		setup();
		while( !topShouldExit() ){
			try{
				tryToProcessAMessage();
			} catch (InterruptedException e){
				logger.error("Interrupted while getting message from inbox");
			} catch (Exception e){
				logger.error("Error while processing message, continuing", e);
			}
		}
	}

}
