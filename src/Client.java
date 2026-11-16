import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException {
        if(args.length != 2){
            System.out.println("Need two arguments: serverIP and serverPort");
            return;
        }

        Scanner scanner = new Scanner(System.in);
        boolean keepGoing = true;

        String serverIP = args[0];
        int serverPort = Integer.parseInt(args[1]);

        System.out.println("Type a command.\nList: ls\nDelete: del\nRename: rnm\nDownload: dwn\nUpload: up\nQuit: q\n");
        while (keepGoing) {
            System.out.print("> ");
            String command = scanner.nextLine();
            switch (command){
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
        System.out.println("list");
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
