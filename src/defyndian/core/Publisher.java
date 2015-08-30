package defyndian.core;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import messaging.DefyndianEnvelope;
import messaging.DefyndianMessage;

public class Publisher extends Thread{

	private BlockingQueue<DefyndianEnvelope> messageQueue;
	private Channel channel;
	private boolean STOP;
	private final Logger logger;
	
	public Publisher(BlockingQueue<DefyndianEnvelope> messageQueue, Channel channel, Logger logger){
		super();
		this.messageQueue = messageQueue;
		this.channel = channel;
		this.logger = logger;
	}
	
	public void setStop(){
		STOP = true;
	}
	
	@Override
	public void run(){
		super.run();
		while( !STOP ){
			DefyndianEnvelope envelope;
			try {
				envelope = messageQueue.poll(5, TimeUnit.SECONDS);
			} catch (InterruptedException e1) {
				logger.warn("Interrupted while waiting for outbox messages");
				continue;
			}
			try {
				channel.basicPublish(envelope.getRoute().getExchange(), envelope.getRoute().getRoutingKey(), null, envelope.getMessage().getMessageBody());
			} catch (IOException e) {
				logger.error("Could not publish message: " + new String(envelope.getMessage().getMessageBody()));
			}
		}
	}
}
