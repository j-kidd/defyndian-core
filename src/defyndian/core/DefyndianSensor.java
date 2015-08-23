package defyndian.core;

import exception.DefyndianDatabaseException;
import exception.DefyndianMQException;

public abstract class DefyndianSensor extends DefyndianNode {

	private final Integer DELAY;
	
	public DefyndianSensor(String name, int delay) throws DefyndianMQException, DefyndianDatabaseException{
		super(name);
		DELAY = delay;
	}
	
	protected void setup(){
		logger.info("No setup specified, using default");
	}
	
	protected boolean shouldExit(){
		return false;
	}
	
	protected abstract boolean sensorFired();
	protected abstract void createMessages();
	
	@Override
	public final void start() throws Exception {
		logger.info(getName() + " started");
		setup();
		while( !shouldExit() ){
			if( sensorFired() ){
				createMessages();
				Thread.sleep(DELAY);
			}
		}

	}
	

}
