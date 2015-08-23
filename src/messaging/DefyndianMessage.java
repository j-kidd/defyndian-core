package messaging;

public interface DefyndianMessage {

	public RoutingInfo getRoutingInfo();
	public Object getMessageBody();
	public byte[] getMessageBytes();
}
