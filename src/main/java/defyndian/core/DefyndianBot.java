package defyndian.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import defyndian.exception.ConfigInitialisationException;
import defyndian.exception.DefyndianDatabaseException;
import defyndian.exception.DefyndianMQException;
import defyndian.messaging.DefyndianEnvelope;
import defyndian.messaging.DefyndianMessage;
import defyndian.messaging.BasicDefyndianMessage;

/**
 * A DefyndianBot represents a Node which has both publish and consume capabilities
 * it processes an incoming message and produces a collection of messages for publishing
 * as a result
 * @author james
 *
 */
public abstract class DefyndianBot extends DefyndianNode{

	private static final String MESSAGE_HANDLER_METHOD_NAME = null;

	/**
	 * DefyndianNode with both a publisher and consumer
	 * @param name Name of this bot
	 * @throws DefyndianMQException If there is an AMQPException while connecting to the broker
	 * @throws DefyndianDatabaseException If there is an error connecting to the database
	 */
	public DefyndianBot(String name) throws DefyndianMQException, DefyndianDatabaseException, ConfigInitialisationException{
		super(name);
		setPublisher();
		setConsumer(config.getRabbitMQDetails());
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
	
	/**
	 * Starts the bot, setup is done then while exit isn't signalled, 
	 * each message produced by tryToProcessAMessage is placed in the outbox
	 * @throws Exception If setup throws an exception
	 */
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
