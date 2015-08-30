package defyndian.core;

import exception.DefyndianDatabaseException;
import exception.DefyndianMQException;
import messaging.DefyndianEnvelope;
import messaging.DefyndianMessage;

public abstract class DefyndianActor extends DefyndianNode {

	private String queue;
	
	public DefyndianActor(String name) throws DefyndianMQException, DefyndianDatabaseException {
		super(name);
		queue = name;
		setConsumer(queue);
	}
	
	public DefyndianActor(String name, String queue) throws DefyndianMQException, DefyndianDatabaseException{
		super(name);
		this.queue = queue;
		setConsumer(queue);
	}

	protected void tryToProcessAMessage() throws InterruptedException{
		DefyndianMessage message = getMessageFromInbox();
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
			}
		}
	}

}
