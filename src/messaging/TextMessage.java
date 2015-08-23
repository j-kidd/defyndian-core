package messaging;

public class TextMessage implements DefyndianMessage{

	private final RoutingInfo routingInfo;
	private final String message;
	
	public TextMessage(String message, RoutingInfo routingInfo){
		this.routingInfo = routingInfo;
		this.message = message;
	}
	
	public TextMessage(String message, String routingKey){
		this.routingInfo = new RoutingInfo(routingKey);
		this.message = message;
	}

	@Override
	public Object getMessageBody(){
		return message;
	}
	
	@Override
	public byte[] getMessageBytes() {
		return message.getBytes();
	}

	@Override
	public RoutingInfo getRoutingInfo() {
		return routingInfo;
	}
}
