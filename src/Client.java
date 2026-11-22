
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
        InputStream in = mySocket.getInputStream();
        OutputStream out = mySocket.getOutputStream();

        Scanner scanner = new Scanner(System.in);
        ExecutorService es = Executors.newFixedThreadPool(10);

        System.out.println("Type a command.\nList: ls\nRename: rnm\nDelete: del\nDownload: dwn\nUpload: up\nQuit: q\n");

        boolean keepGoing = true;

        try {
            while (keepGoing) {
                String clientToServerMessage = optionMenu(scanner);

                if (clientToServerMessage == null) {
                    continue;
                }

                if ("q".equals(clientToServerMessage)) {
                    sendMessage(out, "q");
                    keepGoing = false;

                } else if ("up".equals(clientToServerMessage)) {
                    System.out.print("Enter file name you would like to upload: ");
                    String fileName = "ClientFiles/" + scanner.nextLine();

                    es.submit(new UploadHandler(in, out, fileName));

                } else if ("dwn".equals(clientToServerMessage)) {
                    System.out.print("Enter file name you would like to download: ");
                    String fileName = scanner.nextLine();

                    sendMessage(out, "dwn?" + fileName);
                    receiveDownload(in, out);

                } else if ("rnm".equals(clientToServerMessage)) {
                    System.out.print("Enter file name you would like to rename: ");
                    String fileName = scanner.nextLine();

                    System.out.print("New file name: ");
                    String newFileName = scanner.nextLine();

                    sendMessage(out, "rnm?" + fileName + "?" + newFileName);

                    System.out.println("File renamed to " + newFileName);
                } else if ("ls".equals(clientToServerMessage)) {
                    sendMessage(out, "ls");

                    String reply = receiveMessage(in);
                    System.out.println(reply);
                } else if ("del".equals(clientToServerMessage)){
                    System.out.print("Enter file name you would like to delete: ");
                    String fileName = scanner.nextLine();

                    sendMessage(out, "del?" + fileName);

                    System.out.println(fileName + " deleted");
                }
            }
        } finally {
            es.shutdown();
            mySocket.close();
        }
    }

    public static void sendMessage(OutputStream out, String message) throws IOException {
        byte[] data = (message + "?").getBytes();
        out.write(data);
        out.flush();
    }

    public static String receiveMessage(InputStream in) throws IOException {
        byte[] buffer = new byte[1024];
        int bytesRead;
        StringBuilder message = new StringBuilder();

        while ((bytesRead = in.read(buffer)) != -1) {
            String received = new String(buffer, 0, bytesRead);

            int delimiter = received.indexOf('?');

            if (delimiter != -1) {
                message.append(received, 0, delimiter);
                break;
            }

            message.append(received);
        }

        if (message.isEmpty()) {
            return null;
        }

        return message.toString();
    }

    public static String optionMenu(Scanner scanner) {
        String command = scanner.nextLine();

        switch (command) {
            case "ls":
            case "rnm":
            case "del":
            case "dwn":
            case "up":
            case "q":
                return command;

            default:
                System.out.println("Invalid Command\n");
                System.out.println("Type a command.\nList: ls\nRename: rnm\nDelete: del\nDownload: dwn\nUpload: up\nQuit: q");
                return null;
        }
    }

    public static void receiveDownload(InputStream in, OutputStream out) throws IOException {
        final File clientFolder = new File("ClientFiles");

        String response = receiveMessage(in);

        if (!response.equals("READY")) {
            System.out.println(response);
            return;
        }

        String fileName = receiveMessage(in);
        long fileSize = Long.parseLong(receiveMessage(in));

        fileName = new File(fileName).getName();

        if (fileSize < 0) {
            System.out.println("Invalid file size.");
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
                int bytesToRead = (int) Math.min(buffer.length, remaining);

                int bytesRead = in.read(buffer, 0, bytesToRead);

                if (bytesRead == -1) {
                    throw new EOFException("File transfer interrupted.");
                }

                fileOut.write(buffer, 0, bytesRead);
                remaining -= bytesRead;
            }
        }

        System.out.println("Downloaded: " + fileName);

        sendMessage(out, "Download successful: " + fileName + "\n");
    }

    public static class UploadHandler implements Runnable {

        private final InputStream in;
        private final OutputStream out;
        private final String filePath;

        public UploadHandler(InputStream in, OutputStream out, String filePath) {
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
                sendMessage(out, "up");

                String response = receiveMessage(in);

                if (!response.equals("READY")) {
                    System.out.println(response);
                    return;
                }

                sendMessage(out, file.getName());
                sendMessage(out, String.valueOf(file.length()));

                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;

                    while ((bytesRead = fis.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }

                    out.flush();
                }

                response = receiveMessage(in);
                System.out.println(response);

            } catch (IOException e) {
                System.out.println("Upload failed: " + e.getMessage());
            }
        }
    }
}
