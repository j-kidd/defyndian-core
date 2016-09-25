package defyndian.datastore;

import defyndian.datastore.exception.DatastoreCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by james on 25/09/16.
 */
public class DatastoreBuilder {

    private static final Logger logger = LoggerFactory.getLogger(DatastoreBuilder.class);
    private static final DatastoreType DEFAULT_DATASTORE = DatastoreType.FILES;

    public static final DefyndianDatastore newDatastore(String type, String name) throws DatastoreCreationException{
        DatastoreType datastoreType;
        try {
            datastoreType = DatastoreType.valueOf(type);
        } catch (IllegalArgumentException e){
            logger.error("No such datastore type {}. Using default type {}", type, DEFAULT_DATASTORE);
            datastoreType = DEFAULT_DATASTORE;
        }
        try {
            return (DefyndianDatastore) datastoreType.getDatastoreClass().getConstructor(String.class).newInstance(name);
        } catch (InstantiationException e) {
            logger.error("Datastore class {} is an abstract class. Must be a fully implemented datastore", datastoreType.getDatastoreClass(), e);
            throw new IllegalArgumentException("Only implemented classes can be used as datastore types");
        } catch (IllegalAccessException | NoSuchMethodException e) {
            logger.error("Datastore class {} has no accessible constuctor which takes a name");
            throw new IllegalArgumentException("Datastore types must have a constructor which takes a string");
        } catch (InvocationTargetException e) {
            logger.error("Error while creating new datastore of type {}", datastoreType, e);
            throw new DatastoreCreationException("Couldn't create datastore", e);
        }
    }
}
