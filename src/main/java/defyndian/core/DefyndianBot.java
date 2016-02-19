package defyndian.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import defyndian.exception.DefyndianDatabaseException;
import defyndian.exception.DefyndianMQException;
import defyndian.messaging.DefyndianEnvelope;
import defyndian.messaging.DefyndianMessage;
import defyndian.messaging.BasicDefyndianMessage;

public abstract class DefyndianBot extends DefyndianNode{

	private static final String MESSAGE_HANDLER_METHOD_NAME = null;

	/**
	 * DefyndianNode with both a publisher and consumer
	 * @param name
	 * @throws DefyndianMQException
	 * @throws DefyndianDatabaseException
	 */
	public DefyndianBot(String name) throws DefyndianMQException, DefyndianDatabaseException {
		super(name);
		try{
			setPublisher();
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
	protected Collection<DefyndianEnvelope> tryToProcessAMessage() throws InterruptedException{
		DefyndianEnvelope envelope = getMessageFromInbox();

		if( envelope != null ){  // Null is returned on timeout
			try{
				Method messageHandler = envelope.getMessageClass().getMethod(MESSAGE_HANDLER_METHOD_NAME, envelope.getMessageClass());
				return (Collection<DefyndianEnvelope>) messageHandler.invoke(envelope.getMessage());
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
		return Collections.emptyList();
	}
	
	/**
	* Abstract method to provide actor specific processing logic
	* @param message The DefyndianMessage to process
	*/
	protected abstract Collection<DefyndianEnvelope> handleMessage(DefyndianMessage message);
	
	@Override
	public void start() throws Exception {
		logger.info(getName() + " started");
		setup();
		while( !topShouldExit() ){
			try{
				for( DefyndianEnvelope e : tryToProcessAMessage() ){
					putMessageInOutbox(e);
				}
			} catch (InterruptedException e){
				logger.error("Interrupted while getting message from inbox");
			} catch (Exception e){
				logger.error("Error while processing message, continuing", e);
			}
		}
		close();
	}

	
}
