package defyndian.config;

import com.rabbitmq.client.ConnectionFactory;

import defyndian.exception.ConfigInitialisationException;
import defyndian.exception.DefyndianMQException;

public class RabbitMQDetails {

	private final String exchange;
	private final String queue;
	private final ConnectionFactory connectionFactory;
	
	public RabbitMQDetails(String exchange, String queue, ConnectionFactory factory) throws ConfigInitialisationException{
		if( exchange==null || queue==null || factory.getHost()==null || factory.getUsername()==null || factory.getPassword()==null)
			throw new ConfigInitialisationException(String.format(
					"Must specify exchange, queue and all connection parameters for RabbitMQ. {" +
							"exchange: %s," +
							"queue: %s," +
							"host: %s," +
							"username: %s," +
							"password: %s," +
							"}",
					exchange, queue, factory.getHost(), factory.getUsername(), factory.getPassword()
					));
		this.exchange = exchange;
		this.queue = queue;
		this.connectionFactory = factory;
	}
	
	public String getExchange(){
		return exchange;
	}
	
	public String getQueue(){
		return queue;
	}
	
	public ConnectionFactory getConnectionFactory(){
		return connectionFactory;
	}
}
