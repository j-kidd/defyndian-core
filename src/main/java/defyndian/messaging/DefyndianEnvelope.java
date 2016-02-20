package defyndian.messaging;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An Envelope wraps a DefyndianMessage in transit to provide routing information and info
 * on the type of the message contained
 * @author james
 *
 * @param <M> The type of the message contained
 */
public class DefyndianEnvelope<M extends DefyndianMessage> {

	private RoutingInfo route;
	private Class messageClass;
	private M message;
	
	@JsonCreator
	public DefyndianEnvelope(@JsonProperty("route") RoutingInfo route, @JsonProperty("message") M message){
		this.route = route;
		this.message = message;
		messageClass = message.getClass();
	}
	
	public DefyndianEnvelope(String exchange, DefyndianRoutingKey routingKey, M message){
		this(RoutingInfo.getRoute(exchange, routingKey), message);
	}
	
	public DefyndianEnvelope(DefyndianRoutingKey routingKey, M message){
		this(RoutingInfo.getRoute(null, routingKey), message);
	}
	
	public M getMessage(){
		return message;
	}
	
	public Class getMessageClass(){
		return messageClass;
	}
	
	public RoutingInfo getRoute(){
		return route;
	}
	
	public String toString(){
		return "[ " + route + " : " + message + " ]";
	}
	
}
