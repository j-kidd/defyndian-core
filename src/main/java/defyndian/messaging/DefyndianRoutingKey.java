package defyndian.messaging;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DefyndianRoutingKey {

	private static final String MULTI_MATCH_CHAR = "#";
	
	private String producer;
	private DefyndianRoutingType routingType;
	private String extraRouting;
	
	@JsonCreator
	public DefyndianRoutingKey(	@JsonProperty("producer") String producer, 
								@JsonProperty("type") DefyndianRoutingType type,
								@JsonProperty("extra") String extra){
		this.producer = producer;
		this.routingType = type;
		this.extraRouting = extra;
	}
	
	public DefyndianRoutingKey(String routingKey) throws InvalidRoutingKeyException{
		String[] topics = routingKey.split("\\.");
		if( topics.length > 3 ){
			throw new InvalidRoutingKeyException("A DefyndianRoutingKey can have at most three sections");
		}
		else if( topics.length < 3 ){
			switch( topics.length ){
				case 0: {
					throw new InvalidRoutingKeyException("Empty Routing Key not valid");
				}
				case 1: {
					if( topics[0].equals(MULTI_MATCH_CHAR) ){
						producer = "";
						routingType = DefyndianRoutingType.ALL;
						extraRouting = "";
					}
					else {
						throw new InvalidRoutingKeyException("RoutingKeys must contain three sections, see docs");
					}
					break;
				}
				case 2: {
					if( topics[0].equals(MULTI_MATCH_CHAR) ){
						producer = "";
						routingType = DefyndianRoutingType.ALL;
						extraRouting = topics[1];
					}
					else if( topics[1].equals(MULTI_MATCH_CHAR) ){
						producer = topics[0];
						routingType = DefyndianRoutingType.ALL;
						extraRouting = "";
					}
					else{
						throw new InvalidRoutingKeyException("RoutingKeys must contain three sections (or wildcards), see docs");
					}
					break;
				}
				
			}
		}
		else{
			producer = topics[0];
			try{
				routingType = DefyndianRoutingType.getType(topics[1]);
			} catch( IllegalArgumentException e){
				throw new InvalidRoutingKeyException("RoutingType " + topics[1] + " did not match any known type");
			}
			extraRouting = topics[2];
		}
	}
	
	public static DefyndianRoutingKey getDefaultKey(String producer){
		return new DefyndianRoutingKey(producer, DefyndianRoutingType.DEFAULT, "");
	}
	
	public String getProducer(){
		return producer;
	}
	
	public DefyndianRoutingType getRoutingType(){
		return routingType;
	}
	
	public String getExtraRouting(){
		return extraRouting;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DefyndianRoutingKey that = (DefyndianRoutingKey) o;

		if (getProducer() != null ? !getProducer().equals(that.getProducer()) : that.getProducer() != null)
			return false;
		if (getRoutingType() != that.getRoutingType()) return false;
		return getExtraRouting() != null ? getExtraRouting().equals(that.getExtraRouting()) : that.getExtraRouting() == null;

	}

	@Override
	public int hashCode() {
		int result = getProducer() != null ? getProducer().hashCode() : 0;
		result = 31 * result + (getRoutingType() != null ? getRoutingType().hashCode() : 0);
		result = 31 * result + (getExtraRouting() != null ? getExtraRouting().hashCode() : 0);
		return result;
	}

	/**
	 * Returns the string representation of this routing key as used by AMQP (RabbitMQ)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		return producer +"."+ routingType +"."+ extraRouting;
	}
}
