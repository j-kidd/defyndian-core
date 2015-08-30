package defyndian.core;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;

import messaging.DefyndianEnvelope;
import messaging.DefyndianMessage;

public class Consumer extends Thread{

	private BlockingQueue<DefyndianMessage> messageQueue;
	private Channel channel;
	private String queue;
	private boolean STOP;
	private final Logger logger;
	
	public Consumer(BlockingQueue<DefyndianMessage> messageQueue, Channel channel, String queue, Logger logger){
		this.messageQueue = messageQueue;
		this.channel = channel;
		this.queue = queue;
		this.logger = logger;
	}
	
	public void run(){
		while( !STOP ){
			DefyndianMessage message;
			try {
				GetResponse response = channel.basicGet(queue, false);
				message = DefyndianMessage.fromData(response.getBody());
				messageQueue.put(message);
			} catch( IOException | InterruptedException e){
				logger.error("Error while getting messages from queue [" + queue + "]");
			}
		}
	}
	
	public void setStop(){
		STOP = true;
	}
}
