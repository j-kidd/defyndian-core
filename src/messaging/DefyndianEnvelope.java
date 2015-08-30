package messaging;

public class DefyndianEnvelope {

	private RoutingInfo route;
	private DefyndianMessage message;
	
	public DefyndianEnvelope(RoutingInfo route, DefyndianMessage message){
		this.route = route;
		this.message = message;
	}
	
	public DefyndianEnvelope(String exchange, String routingKey, DefyndianMessage message){
		this.route = RoutingInfo.getRoute(exchange, routingKey);
		this.message = message;
	}
	
	public DefyndianEnvelope(String routingKey, DefyndianMessage message){
		this.route = RoutingInfo.getRoute(null, routingKey);
		this.message = message;
	}
	
	public DefyndianMessage getMessage(){
		return message;
	}
	
	public RoutingInfo getRoute(){
		return route;
	}
	
}
