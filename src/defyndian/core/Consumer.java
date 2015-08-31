package defyndian.core;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.GetResponse;

import exception.DefyndianMQException;
import messaging.DefyndianEnvelope;
import messaging.DefyndianMessage;

public class Consumer extends com.rabbitmq.client.DefaultConsumer{

	private static final String KEY_SEPARATOR = ",";
	private BlockingQueue<DefyndianMessage> messageQueue;
	private String exchange;
	private String queue;
	private final Logger logger;
	
	public Consumer(	BlockingQueue<DefyndianMessage> messageQueue, 
						Channel channel, 
						String exchange, 
						String queue,
						String routingKeys,
						Logger logger) throws DefyndianMQException{
		super(channel);
		this.messageQueue = messageQueue;
		this.exchange = exchange;
		this.queue = queue;
		this.logger = logger;
		initialiseQueue(routingKeys);
		logger.info("Consumer created: " + exchange + " " + queue);
	}
	
	public void start(String consumerTag) throws DefyndianMQException{
		try {
			logger.info("Beginning to consume " + consumerTag + " " + queue);
			getChannel().basicConsume(queue, true, consumerTag, this);
		} catch (IOException e) {
			throw new DefyndianMQException("Could not begin consuming");
		}
	}
	
	@Override
	public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body){
		try{
			DefyndianMessage message = DefyndianMessage.fromJSONData(body);
			try{
				logger.debug(new String(body));
				logger.debug(message.getExtrasKeys());
				messageQueue.put(message);
			} catch( Exception e){
				logger.error("Error while handling message", e);
			}
		} catch( Exception e ){
			logger.error("Error while getting messages from queue [" + queue + "]", e);
		}
		
	}
	
	private void initialiseQueue(String routingKeys) throws DefyndianMQException{
		logger.info("Consumer declaring exchange/queue [" + exchange + "/" + queue + "]");
		try{
			getChannel().exchangeDeclare(exchange, "topic", true);
			getChannel().queueDeclare(queue, true, false, false, null);
			if( routingKeys==null )
				throw new DefyndianMQException("Must specify routing keys to bind for this queue");
			String[] routingKeysToBind = routingKeys.split(KEY_SEPARATOR);
			for( String key : routingKeysToBind ){
				logger.info("Binding queue - ["+exchange + ":" + key + "] -> " + queue);
				getChannel().queueBind(queue, exchange, key);
			}
		} catch( IOException e ){
			logger.error("Error declaring exchange/queue", e);
		}
	}
}
