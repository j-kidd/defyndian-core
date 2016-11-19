package defyndian.messaging.routing;

public enum DefyndianRoutingType {

	DIRECT,
	SYSTEM,
	NOTIFICATION,
	CALL,
	RESPONSE,
	ALL("*");
	
	private final String value;
	
	DefyndianRoutingType(String value){
		this.value = value;
	}

	DefyndianRoutingType() { this.value = name(); }

	public String getValue(){
		return value;
	}
	
	public static DefyndianRoutingType getType(String routingType){
		for( DefyndianRoutingType t : DefyndianRoutingType.values() ){
			if( routingType.equalsIgnoreCase(t.getValue()) )
				return t;
		}
		throw new IllegalArgumentException(routingType + " is not a DefyndianRoutingType");
	}
}
