import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException {
        if(args.length != 2){
            System.out.println("Need two arguments: serverIP and serverPort");
            return;
        }
        Scanner keyboard = new Scanner(System.in);
        String input = keyboard.nextLine();
        byte[] message = input.getBytes();

        String serverIP = args[0];
        int serverPort = Integer.parseInt(args[1]);

        // trigger a connect request to the server and establish the connection
        Socket mySocket = new Socket(serverIP, serverPort);
        InputStream in = mySocket.getInputStream();
        OutputStream out = mySocket.getOutputStream();
        out.write(message);
        // triggers the special signal to server so that it can get out of the while loop
        mySocket.shutdownOutput();

        byte[] buffer = new byte[1024];
        int bytesRead;

        while((bytesRead = in.read(buffer)) != -1){
            String reply = new String(buffer, 0, bytesRead);
            System.out.println(reply);
        }
        mySocket.close();
    }
}
