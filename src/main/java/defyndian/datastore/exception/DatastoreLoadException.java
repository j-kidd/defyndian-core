package defyndian.datastore.exception;

/**
 * Created by james on 25/09/16.
 */
public class DatastoreLoadException extends Exception {

    public DatastoreLoadException() {
    }

    public DatastoreLoadException(String message) {
        super(message);
    }

    public DatastoreLoadException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatastoreLoadException(Throwable cause) {
        super(cause);
    }

    public DatastoreLoadException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
