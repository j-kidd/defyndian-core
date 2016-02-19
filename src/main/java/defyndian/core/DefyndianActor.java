package defyndian.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import defyndian.exception.DefyndianDatabaseException;
import defyndian.exception.DefyndianMQException;
import defyndian.messaging.BasicDefyndianMessage;
import defyndian.messaging.DefyndianEnvelope;

public abstract class DefyndianActor extends DefyndianNode {
	
	private static final String MESSAGE_HANDLER_METHOD_NAME = "handleMessage";

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
		DefyndianEnvelope envelope = getMessageFromInbox();
		if( envelope != null ){  // Null is returned on timeout
			try{
				Method messageHandler = this.getClass().getDeclaredMethod(MESSAGE_HANDLER_METHOD_NAME,
																	envelope.getMessageClass());
				messageHandler.setAccessible(true);
				messageHandler.invoke(this, envelope.getMessage());
			} catch( NoSuchMethodException e){
				logger.warn("No message handler for message type: " + envelope.getMessageClass());
			} catch (IllegalAccessException e) {
				logger.error("Couldn't call handler method for message (Illegal access): " + envelope.getMessageClass());
			} catch (IllegalArgumentException e) {
				logger.error("Couldn't call handler method for message (Illegal argument): " + envelope.getMessageClass());
			} catch (InvocationTargetException e) {
				logger.error("Couldn't call handler method for message (Invocation Target): " + envelope.getMessageClass());
			}
		}
	}
	
	/**
	 * Generic message handler function for BasicDefyndianMessage, other functions
	 * would have the same name but for different message classes. Handling of 
	 * the basic message type is required.
	 * @param message The DefyndianMessage to process
	 */
	protected abstract void handleMessage(BasicDefyndianMessage message);
	
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
