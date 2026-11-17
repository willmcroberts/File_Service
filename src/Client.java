
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

        System.out.println("Type a command.\nList: ls\nDelete: del\nRename: rnm\nDownload: dwn\nUpload: up\nQuit: q\n");
        boolean keepGoing = true;

        while (keepGoing) {
            String clientToServerMessage = optionMenu();

            if ("q".equals(clientToServerMessage)) {
                keepGoing = false;
            } else if (clientToServerMessage != null) {
                out.write(clientToServerMessage.getBytes());
                out.flush();

                byte[] buffer = new byte[1024];
                int bytesRead = in.read(buffer);

                if (bytesRead != -1) {
                    String reply = new String(buffer, 0, bytesRead);
                    System.out.println(reply);
                }
            }
        }
        mySocket.close();
    }

    public static String optionMenu() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("> ");
        String command = scanner.nextLine();

        switch (command) {
            case "ls":
                return command;
            case "del":
                return command;
            case "rnm":
                return command;
            case "dwn":
                return command;
            case "up":
                return command;
            case "q":
                System.out.println("You have quit");
                return command;
            default:
                System.out.println("Invalid Command\n");
                System.out.println("Type a command.\nList: ls\nDelete: del\nRename: rnm\nDownload: dwn\nUpload: up\nQuit: q");
        }
        return null;
    }

    public static void delete() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter file name to delete in the server: ");
        scanner.nextLine();
    }

    public static void rename() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter file name to rename in the server: ");
        scanner.nextLine();
    }

    public static void download() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter file name to download from server: ");
        scanner.nextLine();
    }

    public static void upload() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter file name to upload to server: ");
        scanner.nextLine();
    }
}
