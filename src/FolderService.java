import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class FolderService extends UnicastRemoteObject implements FolderInterface {
    private static MetadataInterface metadataService;

    public FolderService() throws RemoteException {
        super();
    }

    public String getChildren(String path) throws RemoteException{
        String children;
        try {
            children= metadataService.getChildren(path);
        } catch (Exception e) {
            return ("FolderService exception: "+ e.getMessage());
        }
        return "The folder " + path + " contains: " + children;

    }

    public String deleteFolder(String folderPath) throws RemoteException{
        List pathsToDelete = null;
        try {
            pathsToDelete = metadataService.deleteFolder(folderPath);
        } catch (Exception e){
            return ("FolderService exception: "+ e.getMessage());
        }

        System.out.println(pathsToDelete);

        pathsToDelete.forEach(path-> {
            try {
                System.out.println(path);
                Files.deleteIfExists(Paths.get("DB1/"+path));
                Files.deleteIfExists(Paths.get("DB2/"+path));
                Files.deleteIfExists(Paths.get("DB3/"+path));

            } catch (IOException e) {
                    throw new RuntimeException(e);
            }

        });
        return "Folder " + folderPath + " has been deleted";

    }

    public static void main(String args[]) {
        try {
            metadataService = (MetadataInterface) Naming.lookup("MetadataService");

            FolderService folderService = new FolderService();
            Naming.rebind("folderservice", folderService);
            System.out.println();
            System.out.println("FolderService bound in registry");
            System.out.println();
        } catch (Exception e) {
            System.out.println("FolderService exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
