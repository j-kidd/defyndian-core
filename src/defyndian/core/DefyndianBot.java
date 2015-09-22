package defyndian.core;

import exception.DefyndianDatabaseException;
import exception.DefyndianMQException;

public class DefyndianBot extends DefyndianNode{

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

	@Override
	public void start() throws Exception {
		
	}

	
}
