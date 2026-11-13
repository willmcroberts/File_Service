import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public static void main(String[] args) throws IOException {
        int port = 3000;
        int numThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);

        try (ServerSocket listenSocket = new ServerSocket(port)) {
            executorService.submit(new KeyboardMonitor(listenSocket));
            while (true) {
                Socket clientSocket = listenSocket.accept();
                executorService.submit(new ClientHandler((clientSocket)));
            }
        } catch (SocketException e){
            executorService.shutdown();
        }
    }

    public static class ClientHandler implements Runnable {
        private final Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        public void run() {
            System.out.println("client connected: " + clientSocket.getInetAddress());
            try {
                InputStream in = clientSocket.getInputStream();
                OutputStream out = clientSocket.getOutputStream();
                byte[] buffer = new byte[1024];
                int bytesRead;

                while ((bytesRead = in.read(buffer)) != -1) {
                    String message = new String(buffer, 0, bytesRead);
                    byte[] reply = message.toUpperCase().getBytes();
                    out.write(reply);
                }
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class KeyboardMonitor implements Runnable{
        private final ServerSocket listenSocket;

        public KeyboardMonitor(ServerSocket listenSocket) {
            this.listenSocket = listenSocket;
        }

        public void run() {
            Scanner keyboard = new Scanner(System.in);
            while (true){
                String input = keyboard.nextLine();
                if (input.equalsIgnoreCase("Q")){
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
