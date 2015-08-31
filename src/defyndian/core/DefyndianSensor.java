package defyndian.core;

import exception.DefyndianDatabaseException;
import exception.DefyndianMQException;

public abstract class DefyndianSensor extends DefyndianNode {

	private final Integer DELAY;
	
	public DefyndianSensor(String name, int delay) throws DefyndianMQException, DefyndianDatabaseException{
		super(name);
		setPublisher();
		DELAY = delay;
	}
	
	protected abstract boolean sensorFired();
	protected abstract void createMessages();
	
	@Override
	public final void start() throws Exception {
		logger.info(getName() + " started");
		setup();
		logger.info("Starting publisher");
		publisher.start();
		while( !topShouldExit() ){
			if( sensorFired() ){
				Thread.sleep(DELAY*1000);
				try{
					createMessages();
				} catch( Exception e ){
					logger.error("Error while creating messages", e);
				}
				Thread.sleep(DELAY*1000);
			}
		}
		shutdown();
	}

}
