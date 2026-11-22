
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
                    System.out.print("Enter file name you would like to upload: ");
                    String fileName = "ClientFiles/" + scanner.nextLine();

                    es.submit(new UploadHandler(in, out, fileName));

                } else if ("dwn".equals(clientToServerMessage)) {
                    System.out.print("Enter file name you would like to download: ");
                    String fileName = "ServerFiles/" + scanner.nextLine();
                    // THIS WON'T WORK WHEN USING A DIFFERENT COMPUTER FOR THE SERVER
                    // NEED TO SEND DATA OVER IN BYTES AND SUBMIT TASK THERE INSTEAD.
                    es.submit(new Server.DownloadHandler(in, out, fileName));

                } else if ("rnm".equals(clientToServerMessage)) {
                    System.out.print("Enter file name you would like to rename: ");
                    String fileName = "ServerFiles/" + scanner.nextLine();
                    System.out.println("New file name: " + scanner.nextLine());
                    String newFileName = scanner.nextLine();

                } else if ("ls".equals(clientToServerMessage)) {
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

    public static void receiveDownload(DataInputStream in, DataOutputStream out) throws IOException {
        final File clientFolder = new File("ClientFiles");


        out.writeUTF("READY");
        out.flush();

        String fileName = in.readUTF();
        long fileSize = in.readLong();

        fileName = new File(fileName).getName();

        if (fileSize < 0) {
            out.writeUTF("Invalid file size.");
            out.flush();
            return;
        }

        if (!clientFolder.exists()) {
            clientFolder.mkdirs();
        }

        File destination = new File(clientFolder, fileName);

        try (FileOutputStream fileOut = new FileOutputStream(destination)) {

            byte[] buffer = new byte[1024];
            long remaining = fileSize;

            while (remaining > 0) {

                int bytesToRead =
                        (int) Math.min(buffer.length, remaining);

                int bytesRead = in.read(buffer, 0, bytesToRead);

                if (bytesRead == -1) {
                    throw new EOFException("File transfer interrupted.");
                }

                fileOut.write(buffer, 0, bytesRead);

                remaining -= bytesRead;
            }
        }

        System.out.println("Downloaded: " + fileName);

        out.writeUTF("Download successful: " + fileName);
        out.flush();
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
                System.out.println("File does not exist.\n");
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
