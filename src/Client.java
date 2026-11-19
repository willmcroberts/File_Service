
import java.io.*;
import java.net.Socket;
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
        DataInputStream in = new DataInputStream(mySocket.getInputStream());
        DataOutputStream out = new DataOutputStream(mySocket.getOutputStream());

        Scanner scanner = new Scanner(System.in);
        ExecutorService es = Executors.newFixedThreadPool(10);

        System.out.println("Type a command.\nList: ls\nDelete: del\nRename: rnm\nDownload: dwn\nUpload: up\nQuit: q\n");
        boolean keepGoing = true;
        try {
            while (keepGoing) {
                String clientToServerMessage = optionMenu(scanner);

                if ("q".equals(clientToServerMessage)) {
                    out.writeUTF("q");
                    out.flush();
                    keepGoing = false;
                } else if ("up".equals(clientToServerMessage)) {
                    UploadHandler upload = new UploadHandler(in, out, "ClientFiles/test_small.txt");
                    Thread uploadThread = new Thread(upload);
                    uploadThread.start();

                    try {
                        uploadThread.join();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                } else if (clientToServerMessage != null) {
                    out.writeUTF(clientToServerMessage);
                    out.flush();

                    String reply = in.readUTF();
                    System.out.println(reply);
                }
            }
        } finally {
            es.shutdown();
            mySocket.close();
        }
    }

    public static String optionMenu(Scanner scanner) {
        System.out.print("> ");
        String command = scanner.nextLine();

        switch (command) {
            case "ls":
            case "del":
            case "rnm":
            case "dwn":
            case "up":
            case "q":
                return command;

            default:
                System.out.println("Invalid Command\n");
                System.out.println("Type a command.\nList: ls\nDelete: del\nRename: rnm\nDownload: dwn\nUpload: up\nQuit: q");
                return null;
        }
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

    public static class UploadHandler implements Runnable {

        private final DataInputStream in;
        private final DataOutputStream out;
        private final String filePath;

        public UploadHandler(DataInputStream in, DataOutputStream out, String filePath) {
            this.in = in;
            this.out = out;
            this.filePath = filePath;
        }

        public void run() {
            File file = new File(filePath);

            if (!file.exists() || !file.isFile()) {
                System.out.println("File does not exist.");
                return;
            }

            try {
                out.writeUTF("up");
                out.flush();

                String response = in.readUTF();

                if (!response.equals("READY")) {
                    System.out.println(response);
                    return;
                }

                out.writeUTF(file.getName());
                out.writeLong(file.length());
                out.flush();

                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;

                    while ((bytesRead = fis.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }

                    out.flush();
                }

                response = in.readUTF();
                System.out.println(response);

            } catch (IOException e) {
                System.out.println("Upload failed: " + e.getMessage());
            }
        }
    }
}
