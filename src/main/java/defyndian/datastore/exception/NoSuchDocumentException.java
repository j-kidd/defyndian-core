package defyndian.datastore.exception;

/**
 * Created by james on 25/09/16.
 */
public class NoSuchDocumentException extends DatastoreLoadException {

    public NoSuchDocumentException() {
    }

    public NoSuchDocumentException(String message) {
        super(message);
    }

    public NoSuchDocumentException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoSuchDocumentException(Throwable cause) {
        super(cause);
    }

    public NoSuchDocumentException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
