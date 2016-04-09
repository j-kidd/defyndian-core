package defyndian.core;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import defyndian.messaging.DefyndianEnvelope;
import defyndian.messaging.DefyndianMessage;
import defyndian.messaging.BasicDefyndianMessage;

public class Publisher implements Runnable{

	private static final Logger logger = LogManager.getLogger();
	
	private BlockingQueue<DefyndianEnvelope<? extends DefyndianMessage>> messageQueue;
	private Channel channel;
	private static final ObjectMapper objectMapper = new ObjectMapper();
	
	private boolean STOP;
	
	
	public Publisher(BlockingQueue<DefyndianEnvelope<? extends DefyndianMessage>> messageQueue, Channel channel){
		super();
		this.messageQueue = messageQueue;
		this.channel = channel;
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
				logger.error("Could not publish message: " + envelope);
				logger.debug("Failed publish - " + e, e);
			}
		}
	}
}
