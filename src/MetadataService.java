import java.io.*;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class MetadataService extends UnicastRemoteObject implements MetadataInterface
{
    private ReentrantLock mutex = new ReentrantLock();

    private static Map metadataTable = new HashMap<>();

    public MetadataService() throws RemoteException
    {
        super();
    }
    private void readTable(){
        try{
            if( new File("MetadataTable.txt").length()!=0){
                var ois = (new ObjectInputStream(new FileInputStream("MetadataTable.txt")));
                metadataTable = (Map) ois.readObject();
                ois.close();
            }

        } catch (Exception e) {
            System.out.println("MetadataService err: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void writeTable(){
        try{

            var oos = new ObjectOutputStream(new FileOutputStream("MetadataTable.txt"));
            oos.writeObject(metadataTable);
            oos.close();

        } catch (Exception e) {
            System.out.println("MetadataService err: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void canIRead (String path) throws Exception {
        mutex.lock();
        readTable();

        var locks=(List) metadataTable.get(path);
        if(locks==null) {
            mutex.unlock();
            throw new Exception("File doesn't exist");
        }

        if((boolean) locks.get(1)) {
            mutex.unlock();
            throw new Exception("File is being updated, try again");
        }

        int readers = (int) locks.get(0) +1;
        metadataTable.remove(path);
        metadataTable.put(path, List.of(readers,false));

        writeTable();
        mutex.unlock();
 }

    public void endRead (String path) throws RemoteException{
        mutex.lock();
        readTable();

        var locks=(List) metadataTable.get(path);
        int readers=(int)locks.get(0) -1;
        metadataTable.remove(path);
        metadataTable.put(path, List.of(readers,false));

        writeTable();
        mutex.unlock();
    }
    public void endWrite(String path) { //path già corretto
        try {
            mutex.lock();
            readTable();

            var locks= (List) metadataTable.get(path);
            int readers=(int)locks.get(0);
            metadataTable.remove(path);
            metadataTable.put(path, List.of(readers,false));

        } catch (Exception e) {
            System.out.println("MetadataService err: " + e.getMessage());
            e.printStackTrace();
        }
        writeTable();
        mutex.unlock();
    }


    public boolean createFile(String path) {
       try {
           mutex.lock();
           readTable();
           var locks= metadataTable.get(path.replace('/','-'));
           if (locks!=null) {
               mutex.unlock();
               return false;
           }

           metadataTable.put(path.replace('/','-'), List.of(0,true));

       } catch (Exception e) {
           System.out.println("MetadataService err: " + e.getMessage());
           e.printStackTrace();
        }
        writeTable();
        mutex.unlock();

        return true;
    }

    public void moveFile(String filePath, String newFilePath) throws Exception {
        mutex.lock();
        readTable();

        System.out.println("before move"+metadataTable);
        System.out.println(filePath);
        System.out.println(metadataTable.get(filePath));

        //controllo che nessuno sta scrivendo/leggendo su file di partenza
        var locks1= (List) metadataTable.get(filePath);
        var locks2= (List) metadataTable.get(newFilePath);

        if(locks1==null) {
            mutex.unlock();
            throw new Exception("File "+ filePath.replace('-','/') +" doesn't exist");
        }

        if(locks2!=null) {
            mutex.unlock();
            throw new Exception("File "+ newFilePath.replace('-','/') +" already exists");
        }

        int readers=(int) locks1.get(0);
        if((boolean) locks1.get(1) || readers != 0 ) {
            mutex.unlock();
            throw new Exception("File " + filePath.replace('-','/')+" is being updated or read, try again");
        }

        metadataTable.put(newFilePath, List.of(readers,true));
        metadataTable.remove(filePath);

        System.out.println("after move " + metadataTable);

        writeTable();
        mutex.unlock();
    }
    public void deleteFile(String filePath) throws Exception {
        mutex.lock();
        readTable();

        System.out.println("before delete"+metadataTable);

        var locks= (List) metadataTable.get(filePath.replace('/','-'));
        if(locks==null) {
            mutex.unlock();
            throw new Exception("File doesn't exist");

        }

        int readers=(int) locks.get(0);
        if((boolean) locks.get(1) || readers != 0) {
            mutex.unlock();
            throw new Exception("File is being updated or read, try again");
        }

        metadataTable.remove(filePath.replace('/','-'));
        System.out.println("after delete " + metadataTable);

        writeTable();
        mutex.unlock();
    }

    public List deleteFolder(String folderPath) throws Exception {
        mutex.lock();
        readTable();

        System.out.println("before delete"+metadataTable);
        var pathsToRemove= metadataTable.keySet().stream()
                .filter(path->path.toString().startsWith(folderPath.replace('/','-')))
                .toList();

        if (pathsToRemove.isEmpty()) {
            mutex.unlock();
            throw new Exception("Folder doesn't exist");
        }

        List locks;
        for (var path : pathsToRemove) {
            locks = (List) metadataTable.get(path);
            if((boolean) locks.get(1) || (int) locks.get(0) != 0) {
                mutex.unlock();
                throw new Exception("Folder is being updated or read, try later");
            }
        }

        metadataTable.keySet().removeIf(path->path.toString().startsWith(folderPath.replace('/','-')));
        System.out.println("after delete " + metadataTable);

        writeTable();
        mutex.unlock();

        return pathsToRemove;
    }

    public String getChildren(String folderPath) throws Exception {
        mutex.lock();
        readTable();

        var realFolderPath=folderPath.replace('/','-');

        var children=metadataTable.keySet().stream()
                .filter(path->path.toString().startsWith(realFolderPath)&& path.toString().length()>realFolderPath.length())
                .map(path->{
                    var pathWithoutFather=path.toString().replaceFirst(realFolderPath,"");
                    if(pathWithoutFather.toString().contains("-"))
                        return pathWithoutFather.substring(0,pathWithoutFather.toString().indexOf('-'));
                    return pathWithoutFather;
                })
                .collect(Collectors.toSet());


        if (children.toString().equals((String) "[]")) {
            mutex.unlock();
            throw new Exception("Folder doesn't exist");
        }

        writeTable();
        mutex.unlock();

        return children.toString();

    }

        public static void main(String[] args) {

        try {
            MetadataService metadataService = new MetadataService();
            // Bind the remote object's stub in the registry
            Naming.rebind("MetadataService", metadataService);
            System.out.println();
            System.out.println("MetadataService bound in registry");
            System.out.println();

        } catch (Exception e) {
            System.out.println("MetadataService err: " + e.getMessage());
            e.printStackTrace();
        }
    }
}