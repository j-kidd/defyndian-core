package defyndian.messaging;

public enum DefyndianRoutingType {

	DEFAULT("DEFAULT"), DIRECT("DIRECT"), SYSTEM("SYSTEM"), NOTIFICATION("NOTIFICATION"), PRESENCE("PRESENCE"), ALL("*");
	
	private final String value;
	
	private DefyndianRoutingType(String value){
		this.value = value;
	}
	
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
