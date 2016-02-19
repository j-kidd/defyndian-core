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
