
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
        ExecutorService es = Executors.newFixedThreadPool(10);

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
                es.submit(new UploadHandler(listen))
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

    public static class UploadHandler implements Runnable{
        private ServerSocket listenSocket;
        private String filePath = "ClientFiles/test_small.txt";

        public UploadHandler(ServerSocket listenSocket) {
            this.listenSocket = listenSocket;
        }

        public void run(){
            try (Socket socket = new Socket("localhost", 3000);
                FileInputStream fis = new FileInputStream(filePath);
                OutputStream ops = socket.getOutputStream()){

                byte[] buffer = new byte[1024];
                int bytesRead;

                while ((bytesRead = fis.read(buffer)) != -1) {
                    ops.write(buffer, 0, bytesRead);
                }

                ops.close();
                System.out.println("File Uploaded Successfully");

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }
}
