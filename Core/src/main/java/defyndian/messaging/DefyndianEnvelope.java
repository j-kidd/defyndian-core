package defyndian.messaging;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import defyndian.messaging.messages.DefyndianMessage;
import defyndian.messaging.routing.DefyndianRoutingKey;
import defyndian.messaging.routing.RoutingInfo;

/**
 * An Envelope wraps a DefyndianMessage in transit to provide routing information and info
 * on the type of the message contained
 * @author james
 *
 * @param <M> The type of the message contained
 */
public class DefyndianEnvelope<M extends DefyndianMessage> {

	private final RoutingInfo route;
	private final Class<? extends DefyndianMessage> messageClass;
	private final M message;
	
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
	
	public Class<? extends DefyndianMessage> getMessageClass(){
		return messageClass;
	}
	
	public RoutingInfo getRoute(){
		return route;
	}

	@Override
	public int hashCode(){
		int result = 17;
		final int a = 37;

		result = a*result + (route.hashCode());
		result = a*result + (message.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object o){
		if( o==this )
			return true;
		if( o==null || ! o.getClass().equals(this.getClass()) )
			return false;

		DefyndianEnvelope<? extends DefyndianMessage> other = (DefyndianEnvelope) o;
		return this.getMessage().equals(other.getMessage()) && this.getRoute().equals(other.getRoute());
	}

	public String toString(){
		return "[ " + route + " : " + message + " ]";
	}
	
}
