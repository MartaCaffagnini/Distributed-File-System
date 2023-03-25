//remote interface
import java.io.IOException;
import java.rmi.*;
import java.util.List;

public interface MetadataInterface extends Remote
{

    boolean createFile(String x) throws IOException, ClassNotFoundException;

    String getChildren(String x) throws Exception;
    List deleteFolder(String x) throws Exception;

    void deleteFile(String filePath) throws Exception;
    void moveFile(String filePath, String newFilePath) throws Exception;
    void canIRead (String path) throws Exception;
    void endRead (String path)throws RemoteException;
    void endWrite (String path)throws RemoteException;




}