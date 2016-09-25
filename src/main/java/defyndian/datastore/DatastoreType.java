package defyndian.datastore;

/**
 * Created by james on 25/09/16.
 */
public enum DatastoreType {

    FILES(FileSerializationDataStore.class);

    private final Class<? extends DefyndianDatastore> datastoreClass;

    DatastoreType(Class<? extends DefyndianDatastore> datastoreClass){
        this.datastoreClass = datastoreClass;
    }

    public Class getDatastoreClass(){
        return datastoreClass;
    }
}
