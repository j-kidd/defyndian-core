package defyndian.core;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import messaging.DefyndianMessage;

public class Publisher {

	private BlockingQueue<DefyndianMessage> messageQueue;
	private Channel channel;
	private boolean STOP;
	private Logger logger;
	
	public Publisher(BlockingQueue<DefyndianMessage> messageQueue, Channel channel, Logger logger){
		this.messageQueue = messageQueue;
		this.channel = channel;
	}
	
	public void setStop(){
		STOP = true;
	}
	
	public void run(){
		while( !STOP ){
			DefyndianMessage message;
			try {
				message = messageQueue.poll(5, TimeUnit.SECONDS);
			} catch (InterruptedException e1) {
				logger.warn("Interrupted while waiting for outbox messages");
				continue;
			}
			try {
				channel.basicPublish(message.getRoutingInfo().getExchange(), message.getRoutingInfo().getRoutingKey(), null, message.getMessageBytes());
			} catch (IOException e) {
				logger.error("Could not publish message: " + message.getMessageBody().toString());
			}
		}
	}
}
