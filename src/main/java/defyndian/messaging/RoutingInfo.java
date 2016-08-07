package defyndian.messaging;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RoutingInfo {

	private static final String DEFAULT_EXCHANGE = "";
	private final DefyndianRoutingKey routingKey;
	private final String exchange;
	
	@JsonCreator
	public RoutingInfo(@JsonProperty("exchange") String exchange, @JsonProperty("routingKey") DefyndianRoutingKey routingKey) {
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		RoutingInfo that = (RoutingInfo) o;

		if (getRoutingKey() != null ? !getRoutingKey().equals(that.getRoutingKey()) : that.getRoutingKey() != null)
			return false;
		return getExchange() != null ? getExchange().equals(that.getExchange()) : that.getExchange() == null;

	}

	@Override
	public int hashCode() {
		int result = getRoutingKey() != null ? getRoutingKey().hashCode() : 0;
		result = 31 * result + (getExchange() != null ? getExchange().hashCode() : 0);
		return result;
	}

	public String toString(){
		return "{ " + exchange + " " + routingKey + " }";
	}
	
}
