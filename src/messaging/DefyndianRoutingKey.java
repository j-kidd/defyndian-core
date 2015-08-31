package messaging;

public class DefyndianRoutingKey {

	private String producer;
	private DefyndianRoutingType routingType;
	private String extraRouting;
	
	public DefyndianRoutingKey(String producer, DefyndianRoutingType type, String extra){
		this.producer = producer;
		this.routingType = type;
		this.extraRouting = extra;
	}
	
	public String toString(){
		return producer +"."+ routingType +"."+ extraRouting;
	}
}
