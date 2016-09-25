package defyndian.datastore;

import defyndian.datastore.exception.DatastoreLoadException;
import defyndian.datastore.exception.DatastoreSaveException;
import defyndian.datastore.exception.NoSuchDocumentException;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by james on 25/09/16.
 */
public class FileSerializationDataStore extends DefyndianDatastore<Serializable> {

    private static final String NAME_DIGEST_ALGO = "SHA-1";

    private final File datastoreDirectory;
    private final AtomicInteger id;
    private static final Charset charset = Charset.forName("UTF-8");

    public FileSerializationDataStore(String name){
        super(name);
        datastoreDirectory = new File(generateDirectoryName(getName()));
        if( !datastoreDirectory.exists() ||
            !datastoreDirectory.isDirectory()){
            throw new IllegalArgumentException("Datastore directory must exist");
        }
        id = new AtomicInteger(0);
    }

    @Override
    public int save(Serializable doc) throws DatastoreSaveException {
        final int docId = id.getAndIncrement();
        final File documentFile = new File(datastoreDirectory, Integer.toString(docId));

        try( ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(documentFile)) ){
            objectOutputStream.writeObject(doc);
            objectOutputStream.close();
        } catch (IOException e) {
            throw new DatastoreSaveException("Couldn't save document " + doc, e);
        }
        return docId;
    }

    @Override
    public Serializable load(int id) throws DatastoreLoadException{
        final File documentFile = new File(datastoreDirectory, documentFileNameForId(id));
        final Serializable document;
        try {
            final ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(documentFile));
            document = (Serializable) objectInputStream.readObject();
        } catch (IOException e) {
            throw new NoSuchDocumentException("No document saved for ID " + id);
        } catch (ClassNotFoundException e){
            throw new NoSuchDocumentException("Could not find class for document with id " + id, e);
        }
        return document;
    }

    private static String documentFileNameForId(int id){
        return Integer.toString(id);
    }

    private static int idFromDocumentFileName(String fileName){
        return Integer.valueOf(fileName);
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
