package defyndian.core;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;

import defyndian.messaging.DefyndianEnvelope;
import defyndian.messaging.DefyndianMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Publisher implements Runnable{

	private static final Logger logger = LoggerFactory.getLogger(Publisher.class);
	
	private Thread thread;
	private BlockingQueue<DefyndianEnvelope<? extends DefyndianMessage>> messageQueue;
	private Channel channel;
	private static final ObjectMapper objectMapper = new ObjectMapper();
	
	private boolean STOP;

	public Publisher(Channel channel){
		super();
		messageQueue = new LinkedBlockingQueue<>();
		this.channel = channel;
	}
	
	public Publisher(BlockingQueue<DefyndianEnvelope<? extends DefyndianMessage>> messageQueue, Channel channel){
		super();
		this.messageQueue = messageQueue;
		this.channel = channel;
	}

	public void publish(DefyndianEnvelope<? extends DefyndianMessage> envelope) throws InterruptedException {
		messageQueue.put(envelope);
	}

	/**
	 * Start this publisher in a separate thread, handled by the publisher
	 * This method has no effect on a running publisher
	 */
	public void start(String name){
		if( thread!=null && thread.isAlive() ){
			return;
		}
		else{
			thread = new Thread(this);
			thread.setName(name + " Publisher");
			thread.setDaemon(true);
			thread.start();
		}
	}
	
	public void setStop(){
		STOP = true;
	}
	
	@Override
	public void run(){
		logger.info("Publisher started");
		while( !STOP ){
			DefyndianEnvelope envelope;
			try {
				envelope = messageQueue.poll(5, TimeUnit.SECONDS);
			} catch (InterruptedException e1) {
				logger.warn("Interrupted while waiting for outbox messages");
				continue;
			}
			if( envelope==null ){
				continue;
			}
			try {
				logger.debug("Publishing message to [ " + envelope.getRoute().getExchange()+":"+envelope.getRoute().getRoutingKey()+" ]");
				channel.basicPublish(envelope.getRoute().getExchange(), envelope.getRoute().getRoutingKey().toString(), null, objectMapper.writeValueAsBytes(envelope));
			} catch (IOException e) {
				logger.error("Could not publish message: {}", envelope, e);
			}
		}
	}
}
