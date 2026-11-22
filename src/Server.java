import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class Server {
    public static void main(String[] args) throws IOException {

        int port = 3000;
        int numThreads = 10;

        ExecutorService executorService =
                Executors.newFixedThreadPool(numThreads);

        try (ServerSocket listenSocket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = listenSocket.accept();

                System.out.println("Client connected!");

                executorService.submit(new ClientHandler(clientSocket)
                );
            }

        } catch (SocketException e) {
            System.out.println("Server socket closed.");
        } finally {
            executorService.shutdown();
        }
    }

    public static class ClientHandler implements Runnable {

        private final Socket clientSocket;
        private final File serverFolder = new File("ServerFiles");

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        public void run() {

            try (DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                 DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())) {
                while (true) {

                    String command = in.readUTF();

                    System.out.println("Received: " + command);

                    switch (command) {

                        case "up":
                            receiveUpload(in, out);
                            break;

                        case "ls":
                            out.writeUTF(listServerFiles());
                            out.flush();
                            break;

                        case "dn":
                            Client.receiveDownload(in, out);
                            System.out.println("case");
                            break;

                        case "q":
                            return;

                        default:
                            out.writeUTF("Invalid command.");
                            out.flush();
                            break;
                    }
                }

            } catch (EOFException e) {
                System.out.println("Client disconnected.");
            } catch (IOException e) {
                System.out.println("Client connection error: " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void receiveUpload(DataInputStream in,
                                   DataOutputStream out) throws IOException {

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

            if (!serverFolder.exists()) {
                serverFolder.mkdirs();
            }

            File destination = new File(serverFolder, fileName);

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

            System.out.println("Uploaded: " + fileName);

            sendMessage(out, "Upload successful: " + fileName);
        }

        private void sendDownload(InputStream in,
                                  OutputStream out,
                                  String fileName) throws IOException {

            fileName = new File(fileName).getName();

            File file = new File(serverFolder, fileName);

            if (!file.exists() || !file.isFile()) {
                sendMessage(out, "File does not exist.");
                return;
            }

            sendMessage(out, "READY");
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

            String response = receiveMessage(in);
            System.out.println(response);
        }

        private void renameFile(String oldFileName,
                                String newFileName) {

            oldFileName = new File(oldFileName).getName();
            newFileName = new File(newFileName).getName();

            File oldFile = new File(serverFolder, oldFileName);
            File newFile = new File(serverFolder, newFileName);

            if (!oldFile.exists()) {
                System.out.println("File does not exist.");
                return;
            }

            if (newFile.exists()) {
                System.out.println("A file with that name already exists.");
                return;
            }

            if (oldFile.renameTo(newFile)) {
                System.out.println("Renamed: " + oldFileName + " to " + newFileName);
            } else {
                System.out.println("Rename failed.");
            }
        }
    }

    public static String sendMessage(OutputStream out,
                                     String message) throws IOException {

        byte[] data = (message + "?").getBytes();

        out.write(data);
        out.flush();

        return message;
    }

    public static String receiveMessage(InputStream in) throws IOException {

        byte[] buffer = new byte[1024];
        int index = 0;
        int nextByte;

        while ((nextByte = in.read()) != -1) {

            if (nextByte == '?') {
                return new String(buffer, 0, index);
            }

            if (index == buffer.length) {
                byte[] newBuffer = new byte[buffer.length * 2];

                for (int i = 0; i < buffer.length; i++) {
                    newBuffer[i] = buffer[i];
                }

                buffer = newBuffer;
            }

            buffer[index] = (byte) nextByte;
            index++;
        }

        if (index == 0) {
            return null;
        }

        throw new EOFException("Message was not terminated.");
    }

    public static String listServerFiles() {
        File serverFolder = new File("ServerFiles");
        File[] files = serverFolder.listFiles();

        StringBuilder fileList = new StringBuilder();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    fileList.append(file.getName()).append("\n");
                }
            }
        }

        return fileList.toString();
    }

    public static class DownloadHandler implements Runnable{
        private final DataInputStream in;
        private final DataOutputStream out;
        private final String filePath;

        public DownloadHandler(DataInputStream in, DataOutputStream out, String filePath) {
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
                out.writeUTF("dn");
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