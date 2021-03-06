package defyndian.exception;

public class ConfigInitialisationException extends Exception {

	public ConfigInitialisationException(Exception e){
		super(e);
	}

	public ConfigInitialisationException(String msg, Exception e){
		super(msg, e);
	}

	public ConfigInitialisationException(String message){
		super(message);
	}

	public ConfigInitialisationException(Throwable cause) {
		super(cause);
	}
}
