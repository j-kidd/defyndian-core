package messaging;

public class RoutingInfo {

	private static final String DEFAULT_EXCHANGE = "";
	private final String routingKey;
	private final String exchange;
	
	public RoutingInfo(String routingKey, String exchange) {
		this.routingKey = routingKey;
		this.exchange = exchange;
	}
	
	public RoutingInfo(String routingKey) {
		this.routingKey = routingKey;
		this.exchange = DEFAULT_EXCHANGE;
	}

	public String getRoutingKey() {
		return routingKey;
	}

	public String getExchange() {
		return exchange;
	}
	
	
	
}
