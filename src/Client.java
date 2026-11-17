import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Need 2 arguments: serverIP and serverPort");
            return;
        }

        String serverIP = args[0];
        int serverPort = Integer.parseInt(args[1]);

        Socket mySocket = new Socket(serverIP, serverPort);
        InputStream in = mySocket.getInputStream();
        OutputStream out = mySocket.getOutputStream();


        optionMenu(mySocket, in, out);
    }


    public static void optionMenu(Socket mySocket, InputStream in, OutputStream out) {
        Scanner scanner = new Scanner(System.in);
        boolean keepGoing = true;
        System.out.println("Type a command.\nList: ls\nDelete: del\nRename: rnm\nDownload: dwn\nUpload: up\nQuit: q\n");
        while (keepGoing) {
            System.out.print("> ");
            String command = scanner.nextLine();
            switch (command) {
                case "ls":
                    list(mySocket, in, out);
                    break;

                case "del":
                    delete(mySocket, in, out);
                    break;

                case "rnm":
                    rename(mySocket, in, out);
                    break;

                case "dwn":
                    download(mySocket, in, out);
                    break;

                case "up":
                    upload(mySocket, in, out);
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

    public static void list(Socket mySocket, InputStream in, OutputStream out){
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

    public static void delete(Socket mySocket, InputStream in, OutputStream out){
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter file name to delete in the server: ");
        scanner.nextLine();
    }

    public static void rename(Socket mySocket, InputStream in, OutputStream out){
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter file name to rename in the server: ");
        scanner.nextLine();
    }

    public static void download(Socket mySocket, InputStream in, OutputStream out){
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter file name to download from server: ");
        scanner.nextLine();
    }

    public static void upload(Socket mySocket, InputStream in, OutputStream out){
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter file name to upload to server: ");
        scanner.nextLine();
    }
}
