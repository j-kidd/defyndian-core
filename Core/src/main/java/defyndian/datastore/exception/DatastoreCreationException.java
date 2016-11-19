package defyndian.datastore.exception;

/**
 * Created by james on 25/09/16.
 */
public class DatastoreCreationException extends Exception {

    public DatastoreCreationException() {
    }

    public DatastoreCreationException(String message) {
        super(message);
    }

    public DatastoreCreationException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatastoreCreationException(Throwable cause) {
        super(cause);
    }

    public DatastoreCreationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
