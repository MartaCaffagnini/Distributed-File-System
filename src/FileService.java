import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class FileService extends UnicastRemoteObject implements FileInterface {

    private static MetadataInterface metadataService;

    public FileService() throws RemoteException
    {
        super();
    }


    public String createFile(String path, String text) throws RemoteException {

        try {
            if (!metadataService.createFile(path)) return "File already exists";
            Files.write(Paths.get("DB1/"+path.replace('/','-')), text.getBytes());
            Files.write(Paths.get("DB2/"+path.replace('/','-')), text.getBytes());
            Files.write(Paths.get("DB3/"+path.replace('/','-')), text.getBytes());
            metadataService.endWrite(path.replace('/','-'));

        } catch (Exception e) {
            System.out.println("FileService exception: "+ e.getMessage());
            e.printStackTrace();
        }

        return "File "+ path + " has been created";
    }
    public String readFile(String path) throws RemoteException {
        String realPath = path.replace('/','-');
        try {
            metadataService.canIRead(realPath);
        } catch (Exception e) {
            return ("FileService exception: "+ e.getMessage());
        };

        String text="";
        try {
            text = Files.readString(Path.of("DB1/"+ realPath));
        } catch (Exception e) {
            System.out.println("FileService exception: "+ e.getMessage());
            e.printStackTrace();
        }
        metadataService.endRead(realPath);
        return text;
    }

    public String appendOnFile(String path, String text) throws RemoteException {
        try {
            String oldText = readFile(path);

            if(oldText.equals("FileService exception: File doesn't exist")) return oldText;
            if(oldText.equals("FileService exception: File is being updated, try again")) return oldText;

            String result=deleteFile(path);
            if(result.equals("FileService exception: File doesn't exist")) return result;
            if(result.equals("FileService exception: File is being updated or read, try again")) return result;

            result=createFile(path, oldText+"\n"+text);
            if(result.equals("FileService exception: File already exists")) return result;

        } catch (Exception e) {
            System.out.println("FileService exception: "+ e.getMessage());
            e.printStackTrace();
        }
        return "File " + path + " updated";

    }

    public String moveFile(String path, String newPath) throws RemoteException {
        var realPath= path.replace('/','-');
        var realNewPath=newPath.replace('/','-');

        try{
            metadataService.moveFile(realPath, realNewPath);

            Files.move(Paths.get("DB1/"+realPath), Paths.get("DB1/"+realNewPath));

            Files.move(Paths.get("DB2/"+realPath), Paths.get("DB2/"+realNewPath));

            Files.move(Paths.get("DB3/"+realPath), Paths.get("DB3/"+realNewPath));

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            return ("FileService exception: "+ e.getMessage());
        }
        metadataService.endWrite(realNewPath);
        return "File " + path + " has been moved to " + newPath;
    }

    public String renameFile(String path, String newName) throws RemoteException {

        try{
            var oldName = path.substring(path.lastIndexOf('/') + 1);
            String result = moveFile(path, path.replace(oldName,newName));
            if(result.equals("FileService exception: File "+ path + " doesn't exist")) return result;
            if(result.equals("FileService exception: File "+ path.replace(oldName,newName) + " already exists")) return result;
            if(result.equals("FileService exception: File "+ path + " is being updated or read, try again")) return result;


        } catch (IOException e) {
            e.printStackTrace();
        }
        return "File " + path + " has been renamed";
    }

   public String deleteFile(String path) throws RemoteException {
       try {
           metadataService.deleteFile(path);
           Files.deleteIfExists(Paths.get("DB1/"+path.replace('/', '-')));
           Files.deleteIfExists(Paths.get("DB2/"+path.replace('/', '-')));
           Files.deleteIfExists(Paths.get("DB3/"+path.replace('/', '-')));
       } catch (IOException e) {
           throw new RuntimeException(e);
       } catch (Exception e){
           return ("FileService exception: "+ e.getMessage());
       }

       return "File " + path + " has been deleted";
   }


    public static void main(String args[]){
        try {
            metadataService = (MetadataInterface)Naming.lookup("MetadataService");

            FileService fileService = new FileService();
            // Bind the remote object's stub in the registry
            Naming.rebind("fileservice", fileService);
            System.out.println();
            System.out.println("FileService bound in registry");
            System.out.println();
        } catch (Exception e) {
            System.out.println("FileService exception: "+ e.getMessage());
            e.printStackTrace();
        }
    }

}

