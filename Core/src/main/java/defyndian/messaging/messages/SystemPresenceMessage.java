package defyndian.messaging.messages;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * This message is sent by a node when it starts to inform Station of itself
 * @author james
 *
 */
public class SystemPresenceMessage extends TimeStampedMessage {

	private final String nodeName;
	private final String nodeHost;
	
	@JsonCreator
	public SystemPresenceMessage(long epochSeconds, String name, String host) {
		super(epochSeconds);
		this.nodeName = name;
		this.nodeHost = host;
	}
	
	public String getNodeName(){
		return nodeName;
	}
	
	public String getNodeHost(){
		return nodeHost;
	}

}
