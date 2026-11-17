import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException {


        optionMenu();
    }


    public static void optionMenu() {
        Scanner scanner = new Scanner(System.in);
        boolean keepGoing = true;
        System.out.println("Type a command.\nList: ls\nDelete: del\nRename: rnm\nDownload: dwn\nUpload: up\nQuit: q\n");
        while (keepGoing) {
            System.out.print("> ");
            String command = scanner.nextLine();
            switch (command) {
                case "ls":
                    list();
                    break;

                case "del":
                    delete();
                    break;

                case "rnm":
                    rename();
                    break;

                case "dwn":
                    download();
                    break;

                case "up":
                    upload();
                    break;

                case "q":
                    keepGoing = false;
                    System.out.println("You have quit");
                    break;

                default:
                    System.out.println("Invalid Command\n");
                    System.out.println("Type a command.\nList: ls\nDelete: del\nRename: rnm\nDownload: dwn\nUpload: up\nQuit: q");
            }
        }
    }

    public static void list(){
       File serverFileFolder = new File("ServerFiles");
        File[] files = serverFileFolder.listFiles();

        if (files != null){
            for (File file : files){
                if (file.isFile()){
                    System.out.println(file.getName());
                }
            }
        } else {
            System.out.println("Server files list is empty");
        }
    }

    public static void delete(){
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter file name to delete in the server: ");
        scanner.nextLine();
    }

    public static void rename(){
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter file name to rename in the server: ");
        scanner.nextLine();
    }

    public static void download(){
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter file name to download from server: ");
        scanner.nextLine();
    }

    public static void upload(){
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter file name to upload to server: ");
        scanner.nextLine();
    }
}
