import java.rmi.Naming;
import java.util.Scanner;

public class Client {
    private static FileInterface fileService;
    private static FolderInterface folderService;

    public static void main(String args[]) {
        try {
            fileService= (FileInterface) Naming.lookup("fileservice");
            folderService= (FolderInterface) Naming.lookup("folderservice");

            String command = "";
            String output = "";
            System.out.println("\n\nread <path> per leggere un file\n" +
                    "create <path> <text> per creare un nuovo file\n" +
                            "delete <path> per eliminare un file\n" +
                    "move <oldPath> <newPath> per spostare un file\n"+
                    "append <path> <text> per aggiungere del testo al file\n"+
                    "rename <path> <name> per rinominare un file\n"+
                    "children <path> per visualizzare il contenuto di una cartella\n"+
                    "delete-folder <path> per eliminare una cartella\n"+
                    "exit per uscire\n");
            while (!command.equals("exit")) {
                Scanner scanner = new Scanner(System.in);
                output="";
                command="";
                command = scanner.next();
                switch (command) {
                    case "read":
                        output=fileService.readFile(scanner.next()); break;
                    case "create":
                        output=fileService.createFile(scanner.next(),scanner.nextLine().trim()); break;
                    case "delete":
                        output=fileService.deleteFile(scanner.next()); break;
                    case "move":
                        output=fileService.moveFile(scanner.next(),scanner.next() ); break;
                    case "append":
                        output=fileService.appendOnFile(scanner.next(),scanner.nextLine().trim()); break;
                    case "rename":
                        output=fileService.renameFile(scanner.next(),scanner.next() ); break;
                    case "children":
                        output= folderService.getChildren(scanner.next()); break;
                    case "delete-folder":
                        output= folderService.deleteFolder(scanner.next()); break;
                    case "exit":
                        break;
                    default:
                        output="Command not found"; break;

                }
                System.out.println("\n" + output + "\n");
            }
        } catch (Exception e) {
            System.out.println("Client exception: " + e.getMessage());
            e.printStackTrace();
        }
    }}