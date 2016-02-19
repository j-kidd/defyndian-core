package defyndian.messaging;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DefyndianEnvelope<M extends DefyndianMessage> {

	private RoutingInfo route;
	private M message;
	
	@JsonCreator
	public DefyndianEnvelope(@JsonProperty RoutingInfo route, @JsonProperty M message){
		this.route = route;
		this.message = message;
	}
	
	public DefyndianEnvelope(String exchange, DefyndianRoutingKey routingKey, M message){
		this.route = RoutingInfo.getRoute(exchange, routingKey);
		this.message = message;
	}
	
	public DefyndianEnvelope(DefyndianRoutingKey routingKey, M message){
		this.route = RoutingInfo.getRoute(null, routingKey);
		this.message = message;
	}
	
	public M getMessage(){
		return message;
	}
	
	public RoutingInfo getRoute(){
		return route;
	}
	
	public String toString(){
		return "[ " + route + " : " + message + " ]";
	}
	
}
