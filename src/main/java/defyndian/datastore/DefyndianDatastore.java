package defyndian.datastore;

import defyndian.datastore.exception.DatastoreLoadException;
import defyndian.datastore.exception.DatastoreSaveException;

/**
 * Created by james on 25/09/16.
 *
 * A datastore is used by nodes to persist data beyond restarts/failures etc
 * This top level interface is totally generic and is intended to allow maximum
 * flexibility
 */
public abstract class DefyndianDatastore<D> {

    private final String name;

    public DefyndianDatastore(String name){
        this.name = name;
    };

    public abstract int save(D doc) throws DatastoreSaveException;
    public abstract D load(int id) throws DatastoreLoadException;

    protected final String getName(){
        return name;
    }

}
