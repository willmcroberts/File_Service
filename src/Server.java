import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static java.lang.System.out;

public class Server {
    public static void main(String[] args) throws IOException {
        int port = 3000;
        int numThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);

        try (ServerSocket listenSocket = new ServerSocket(port)) {
            Socket clientSocket = listenSocket.accept();

            InputStream in = clientSocket.getInputStream();
            OutputStream socketOut = clientSocket.getOutputStream();

            byte[] buffer = new byte[1024];
            int bytesRead;

            // Where the server reads and replies
            while ((bytesRead = in.read(buffer)) != -1) {
                String message = new String(buffer, 0, bytesRead).trim();
                out.println(message);

                if ("ls".equals(message)) {
                    String replyString = listServerFiles();
                    byte[] reply = replyString.getBytes();
                    socketOut.write(reply);
                    socketOut.flush();
                } else {
                    socketOut.write("We'll get here when we get here.".getBytes());
                    socketOut.flush();
                }
            }

            clientSocket.close();

        } catch (SocketException e) {
            executorService.shutdown();
        }
    }

    public static String listServerFiles() {
        File serverFileFolder = new File("ServerFiles");
        File[] files = serverFileFolder.listFiles();

        StringBuilder fileList = new StringBuilder();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    fileList.append(file.getName()).append("\n");
                }
            }
        } else {
            out.println("Server files list is empty");
            return "";
        }
        return fileList.toString();
    }

    public static class ClientHandler implements Runnable {
        private final Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        public void run() {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class KeyboardMonitor implements Runnable {
        private final ServerSocket listenSocket;

        public KeyboardMonitor(ServerSocket listenSocket) {
            this.listenSocket = listenSocket;
        }

        public void run() {
            Scanner keyboard = new Scanner(System.in);
            while (true) {
                String input = keyboard.nextLine();
                if (input.equalsIgnoreCase("Q")) {
                    try {
                        listenSocket.close();
                        return;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
