package defyndian.core;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import defyndian.config.RabbitMQDetails;
import defyndian.exception.DefyndianMQException;
import defyndian.messaging.DefyndianEnvelope;
import defyndian.messaging.DefyndianMessage;
import defyndian.messaging.DefyndianRoutingKey;
import defyndian.messaging.DefyndianRoutingType;

/**
 * A Consumer run Asynchronously for each Sensor; it manages the inbox by
 * reading messages from the broker and providing them to the Node.
 * No direct interaction with the consumer is required, the Node class
 * manages the lifecycle.
 * @author james
 *
 */
public class Consumer extends DefaultConsumer{
	
	private static final Logger logger = LogManager.getLogger();
	private static final ObjectMapper objectMapper = new ObjectMapper();
	private final String directConsumerRoutingKey;
	
	private BlockingQueue<DefyndianEnvelope<? extends DefyndianMessage>> messageQueue;
	private String exchange;
	private String queue;
	
	/**
	 * Fully specified constructor for a new consumer
	 * @param messageQueue The Queue messages are placed in after being retrieved over AMQP
	 * @param channel The AMQP channel to use
	 * @param exchange The Exchange to make bindings with
	 * @param queue The Queue to consume on
	 * @param routingKeys The routingkeys to bind the queue with
	 * @param logger The Logger to use
	 * @throws DefyndianMQException If an exception occurs during setup of the AMQP connection
	 */
	public Consumer(	BlockingQueue<DefyndianEnvelope<? extends DefyndianMessage>> messageQueue, 
						Channel channel, 
						String name, 
						RabbitMQDetails details,
						Collection<DefyndianRoutingKey> routingKeys) throws DefyndianMQException{
		super(channel);
		directConsumerRoutingKey = String.format("*."+DefyndianRoutingType.DIRECT+".%s", name);
		this.messageQueue = messageQueue;
		this.exchange = details.getExchange();
		this.queue = details.getQueue();
		initialiseQueue(routingKeys);
		logger.info("Consumer created: " + exchange + " " + queue);
	}
	
	/**
	 * Start the consumer, this starts the consumption of messages and the consumer
	 * will begin placing messages in the inbox
	 * @param consumerTag The AMQP consumer tag, that the broker recognises this consumer by
	 * @throws DefyndianMQException If an exception prevents consumption
	 */
	public void start(String consumerTag) throws DefyndianMQException{
		try {
			logger.info("Beginning to consume " + consumerTag + " " + queue);
			getChannel().basicConsume(queue, true, consumerTag, this);
		} catch (IOException e) {
			throw new DefyndianMQException("Could not begin consuming");
		}
	}
	
	/**
	 * Called when a message is received, the DefyndianEnvelope is mapped from the JSON and placed in the 
	 * message queue
	 */
	@Override
	public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body){
		try{
			DefyndianEnvelope<? extends DefyndianMessage> defyndianEnvelope = objectMapper.readValue(body, DefyndianEnvelope.class);
			try{
				logger.debug(new String(body));
				messageQueue.put(defyndianEnvelope);
			} catch( Exception e){
				logger.error("Error while handling message", e);
			}
		} catch( Exception e ){
			logger.error("Error while getting messages from queue [" + queue + "]", e);
		}
		
	}
	
	/**
	 * Sets up the queue and exchange with any bindings specified
	 * @param routingKeys The RoutingKeys to bind to the given queue
	 * @throws DefyndianMQException If no routing keys are specified
	 */
	private void initialiseQueue(Collection<DefyndianRoutingKey> routingKeys) throws DefyndianMQException{
		logger.info("Consumer declaring exchange/queue [" + exchange + "/" + queue + "]");
		try{
			getChannel().exchangeDeclare(exchange, "topic", true);
			getChannel().queueDeclare(queue, true, false, false, null);
			if( routingKeys==null )
				throw new DefyndianMQException("Must specify routing keys to bind for this queue");
			else if( routingKeys.isEmpty() ){
				logger.warn("No Routing Keys specified for queue, will only receive on existing bindings");
			}
			for( DefyndianRoutingKey key : routingKeys ){
				logger.info("Binding queue - ["+exchange + ":" + key + "] -> " + queue);
				getChannel().queueBind(queue, exchange, key.toString());
			}
			getChannel().queueBind(queue, exchange, directConsumerRoutingKey);
			getChannel().queueBind(queue, exchange, new DefyndianRoutingKey("Station", DefyndianRoutingType.ALL, "*").toString());
		} catch( IOException e ){
			logger.error("Error declaring exchange/queue", e);
		}
	}
}
