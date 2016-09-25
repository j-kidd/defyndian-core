package defyndian.core;

import java.util.Collection;

import com.rabbitmq.client.Connection;
import defyndian.datastore.exception.DatastoreCreationException;
import defyndian.exception.ConfigInitialisationException;
import defyndian.exception.DefyndianDatabaseException;
import defyndian.exception.DefyndianMQException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DefyndianSensor<T> extends DefyndianNode {

	private static final Logger logger = LoggerFactory.getLogger(DefyndianSensor.class);

	private final Integer DELAY;
	
	/**
	 * Creates a published in addition to DefyndianNode construction
	 * @param name Name of this Sensor
	 * @param delay Seconds between sensor checks
	 * @throws DefyndianMQException As thrown by DefyndianNode constructor
	 * @throws DefyndianDatabaseException As thrown by DefyndianNode constructor
	 */
	public DefyndianSensor(String name, int delay, Connection connection) throws DefyndianMQException, ConfigInitialisationException, DatastoreCreationException {
		super(name, connection);
		DELAY = delay;
	}
	
	/**
	 * Method to check the 'sensor' this node represents, all objects in the returned
	 * collection are passed to createMessages
	 * @return A collection of objects to pass to createMessages
	 */
	protected abstract Collection<T> sensorFired();
	
	/**
	 * Method to allow messages to be put in outbox, the given Collection
	 * is the collection returned by the last call to sensorFired()
	 */
	protected abstract void createMessages(Collection<T> sensorInfo);
	
	/**
	 * General running loop for all sensors, until STOP is set or shouldExit is true 
	 * sensorFired is checked every DELAY seconds and if true then createMessages is called
	 */
	@Override
	public final void start() throws Exception {
		logger.info(getName() + " started");
		setup();
		logger.info("Starting publisher");
		publisher.start(getName());;
		while( !topShouldExit() ){
			Collection<T> sensorInfo = sensorFired();
			if( ! sensorInfo.isEmpty() ){
				try{
					createMessages(sensorInfo);
					Thread.sleep(DELAY*1000);
				} catch( Exception e ){
					logger.error("Error while creating messages", e);
				}
				Thread.sleep(DELAY*1000);
			}
		}
		close();
	}

}
