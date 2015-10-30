package defyndian.core;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import defyndian.messaging.DefyndianEnvelope;
import defyndian.messaging.DefyndianMessage;

public class Publisher extends Thread{

	private BlockingQueue<DefyndianEnvelope> messageQueue;
	private Channel channel;
	private boolean STOP;
	private final Logger logger;
	
	public Publisher(BlockingQueue<DefyndianEnvelope> messageQueue, Channel channel, Logger logger){
		super();
		this.setName("Publisher");
		setDaemon(true);
		this.messageQueue = messageQueue;
		this.channel = channel;
		this.logger = logger;
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
				channel.basicPublish(envelope.getRoute().getExchange(), envelope.getRoute().getRoutingKey().toString(), null, envelope.getMessage().toJSONString().getBytes());
			} catch (IOException e) {
				logger.error("Could not publish message: " + new String(envelope.getMessage().getMessageBody()));
			}
		}
	}
}
