import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FileInterface extends Remote {
    String createFile(String path, String text) throws RemoteException;

    String readFile(String path) throws RemoteException;

    String appendOnFile(String path, String text) throws RemoteException;

    String moveFile(String path, String newPath) throws RemoteException;

    String renameFile(String path, String newName) throws RemoteException;
    String deleteFile(String path) throws RemoteException;

}


