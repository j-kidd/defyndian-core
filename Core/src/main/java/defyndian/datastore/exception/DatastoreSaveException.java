package defyndian.datastore.exception;

/**
 * Created by james on 25/09/16.
 */
public class DatastoreSaveException extends Exception{

    public DatastoreSaveException() {
    }

    public DatastoreSaveException(String message) {
        super(message);
    }

    public DatastoreSaveException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatastoreSaveException(Throwable cause) {
        super(cause);
    }

    public DatastoreSaveException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
