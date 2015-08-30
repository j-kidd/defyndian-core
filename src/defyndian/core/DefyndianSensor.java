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
		while( !topShouldExit() ){
			if( sensorFired() ){
				createMessages();
				Thread.sleep(DELAY);
			}
		}

	}

}
