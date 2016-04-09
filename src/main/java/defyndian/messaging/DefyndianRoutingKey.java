package defyndian.messaging;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DefyndianRoutingKey {

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
		if( topics.length != 3 ){
			throw new InvalidRoutingKeyException(String.format("[ %s ] RoutingKeys must have three sections PRODUCER.TYPE.EXTRA", routingKey));
		}
		producer = topics[0];
		try{
			routingType = DefyndianRoutingType.valueOf(topics[1]);
		} catch( IllegalArgumentException e){
			throw new InvalidRoutingKeyException("RoutingType " + topics[1] + " did not match any known type");
		}
		extraRouting = topics[2];
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
	
	public String toString(){
		return producer +"."+ routingType +"."+ extraRouting;
	}
}
