package defyndian.exception;

public class DefyndianMQException extends Exception {

	public DefyndianMQException(String message){
		super(message);
	}

	public DefyndianMQException(String message, Throwable e){
		super(message, e);
	}

	public DefyndianMQException(Throwable e){
		super(e);
	}
}
