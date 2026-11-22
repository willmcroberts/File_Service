
import java.io.*;
import java.net.*;
import java.util.Objects;
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

                executorService.submit(new ClientHandler(clientSocket));
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

            try (InputStream in = clientSocket.getInputStream();
                 OutputStream out = clientSocket.getOutputStream()) {

                while (true) {

                    String command = receiveMessage(in);

                    if (command == null) {
                        return;
                    }

                    System.out.println("Received: " + command);

                    switch (command) {

                        case "up":
                            receiveUpload(in, out);
                            break;

                        case "ls":
                            sendMessage(out, listServerFiles());
                            break;

                        case "dwn":
                            String fileName = receiveMessage(in);
                            sendDownload(in, out, fileName);
                            break;

                        case "rnm":
                            String oldFileName = receiveMessage(in);
                            String newFileName = receiveMessage(in);

                            renameFile(oldFileName, newFileName);
                            break;

                        case "del":
                            String deleteFileName = receiveMessage(in);

                            deleteFile(deleteFileName);
                            break;
                        case "q":
                            return;

                        default:
                            sendMessage(out, "Invalid command.");
                            break;
                    }
                }

            } catch (EOFException e) {
                System.out.println("Client disconnected.");
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void receiveUpload(InputStream in,
                                   OutputStream out) throws IOException {

            sendMessage(out, "READY");

            String fileName = receiveMessage(in);
            long fileSize = Long.parseLong(Objects.requireNonNull(receiveMessage(in)));

            assert fileName != null;
            fileName = new File(fileName).getName();

            if (fileSize < 0) {
                sendMessage(out, "Invalid file size.");
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

            sendMessage(out, "Upload successful: " + fileName + '\n');
        }

        private void sendDownload(InputStream in,
                                  OutputStream out,
                                  String fileName) throws IOException {

            fileName = new File(fileName).getName();

            File file = new File(serverFolder, fileName);

            if (!file.exists() || !file.isFile()) {
                sendMessage(out, "File does not exist.\n");
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
                System.out.println("File does not exist.\n");
                return;
            }

            if (newFile.exists()) {
                System.out.println("A file with that name already exists.\n");
                return;
            }

            if (oldFile.renameTo(newFile)) {
                System.out.println("Renamed: " + oldFileName + " to " + newFileName);
            } else {
                System.out.println("Rename failed.");
            }
        }

        private void deleteFile(String deleteFileName){
            deleteFileName = new File(deleteFileName).getName();

            File deleteFile = new File(serverFolder, deleteFileName);

            if (!deleteFile.exists()){
                System.out.println("File does not exist.\n");
                return;
            }

            if (deleteFile.delete()){
                System.out.println("Deleted: " + deleteFileName);
            }
        }
    }

    public static void sendMessage(OutputStream out,
                                   String message) throws IOException {

        byte[] data = (message + "?").getBytes();

        out.write(data);
        out.flush();

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

                System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);

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
}
