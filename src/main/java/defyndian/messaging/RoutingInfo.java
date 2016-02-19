package defyndian.messaging;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RoutingInfo {

	private static final String DEFAULT_EXCHANGE = "";
	private final DefyndianRoutingKey routingKey;
	private final String exchange;
	
	@JsonCreator
	public RoutingInfo(@JsonProperty String exchange, @JsonProperty DefyndianRoutingKey routingKey) {
		this.routingKey = routingKey;
		this.exchange = exchange;
	}
	
	public RoutingInfo(DefyndianRoutingKey routingKey) {
		this.routingKey = routingKey;
		this.exchange = DEFAULT_EXCHANGE;
	}

	public DefyndianRoutingKey getRoutingKey() {
		return routingKey;
	}

	public String getExchange() {
		return exchange;
	}
	
	public static RoutingInfo getRoute(String exchange, DefyndianRoutingKey routingKey){
		String actualExchange = exchange==null ? DEFAULT_EXCHANGE : exchange;
		return new RoutingInfo(actualExchange, routingKey);
	}
	
}
