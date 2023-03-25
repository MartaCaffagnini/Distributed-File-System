import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FolderInterface extends Remote {
    String getChildren(String path) throws RemoteException;

    String deleteFolder(String path) throws RemoteException;

}
