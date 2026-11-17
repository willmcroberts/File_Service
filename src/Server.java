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

        } catch (SocketException e){
            executorService.shutdown();
        }
    }

    public static void listServerFiles() {
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
