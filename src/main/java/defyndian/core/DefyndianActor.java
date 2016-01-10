package defyndian.core;

import defyndian.exception.DefyndianDatabaseException;
import defyndian.exception.DefyndianMQException;
import defyndian.messaging.DefyndianEnvelope;
import defyndian.messaging.DefyndianMessage;

public abstract class DefyndianActor extends DefyndianNode {
	
	/**
	 * Creates a consumer in addition to DefyndianNode construction
	 * @param name Name of this actor
	 * @throws DefyndianMQException As thrown by DefyndianNode constructor
	 * @throws DefyndianDatabaseException As thrown by DefyndianNode constructor
	 */
	public DefyndianActor(String name) throws DefyndianMQException, DefyndianDatabaseException {
		super(name);
		try{
			setConsumer();
		} catch( Exception e){
			this.close();
			throw e;
		}
	}

	/**
	 * Waits on the inbox and calls handleMessage on each message as it enters the inbox
	 * @throws InterruptedException If no message is read from the inbox in the timeout period
	 */
	protected void tryToProcessAMessage() throws InterruptedException{
		DefyndianMessage message = getMessageFromInbox();
		if( message != null )  // Null is returned on timeout
			handleMessage(message);
	}
	
	/**
	 * Abstract method to provide actor specific processing logic
	 * @param message The DefyndianMessage to process
	 */
	protected abstract void handleMessage(DefyndianMessage message);
	
	/**
	 * General running loop for all actors, until either STOP is set or shouldExit returns true each 
	 * message in the inbox is put through handleMessage. Exceptions are caught within the loop
	 * to ensure continued running.
	 */
	@Override
	public final void start() throws Exception {
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
		close();
	}

}
