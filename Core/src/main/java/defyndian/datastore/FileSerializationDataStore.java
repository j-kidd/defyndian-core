package defyndian.datastore;

import defyndian.datastore.exception.DatastoreLoadException;
import defyndian.datastore.exception.DatastoreSaveException;
import defyndian.datastore.exception.NoSuchDocumentException;

import javax.print.Doc;
import java.io.*;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by james on 25/09/16.
 */
public class FileSerializationDataStore<D extends Document> extends DefyndianDatastore<D> {

    private static final String NAME_DIGEST_ALGO = "SHA-1";

    private final File datastoreDirectory;
    private final Map<String, File> objectMap;

    public FileSerializationDataStore(String name) throws IOException {
        super(name);
        datastoreDirectory = new File(generateDirectoryName(getName()));
        if( !datastoreDirectory.exists()){
            Files.createDirectory(datastoreDirectory.toPath());
        }
        objectMap = Arrays  .stream(datastoreDirectory.listFiles())
                            .collect(Collectors.toMap(f -> f.getName(), Function.identity()));
    }

    @Override
    public String save(D doc) throws DatastoreSaveException {
        final String id = doc.id();
        final File documentFile = new File(datastoreDirectory, id);

        try( ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(documentFile)) ){
            objectOutputStream.writeObject(doc);
            objectOutputStream.close();
        } catch (IOException e) {
            throw new DatastoreSaveException("Couldn't save document " + doc, e);
        }
        return doc.id();
    }

    @Override
    public D load(String id) throws DatastoreLoadException{
        final File documentFile = new File(datastoreDirectory, id);
        final D document;
        try {
            final ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(documentFile));
            document = (D) objectInputStream.readObject();
        } catch (IOException e) {
            throw new NoSuchDocumentException("No document saved for ID " + id);
        } catch (ClassNotFoundException e){
            throw new NoSuchDocumentException("Could not find class for document with id " + id, e);
        }
        return document;
    }

    @Override
    public Collection<String> listIds() {
        return objectMap.keySet();
    }

    private static final String generateDirectoryName(String name){
        try {
            MessageDigest digest = MessageDigest.getInstance(NAME_DIGEST_ALGO);
            byte[] hash = digest.digest(name.getBytes());
            StringBuilder readAbleHash = new StringBuilder();
            for( byte b : hash ){
                readAbleHash.append(String.format("%02x", b));
            }
            return readAbleHash.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No " + NAME_DIGEST_ALGO + " algo available, shouldn't be possible");
        }

    }
}
