import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        boolean keepGoing = true;
        while (keepGoing) {
            String command = scanner.nextLine();
            switch (command){
                case "ls":
                    list();
                    break;

                case "del":
                    delete();
                    break;

                case "rnm":
                    rename();
                    break;

                case "dwn":
                    download();
                    break;

                case "up":
                    upload();
                    break;

                case "q":
                    quit();
                    keepGoing = false;
                    break;

                default:
                    System.out.println("Invalid Command");
            }
        }
    }

    public static void list(){
        System.out.println("list");
    }

    public static void delete(){
        System.out.println("delete");
    }

    public static void rename(){
        System.out.println("rename");
    }

    public static void download(){
        System.out.println("download");
    }

    public static void upload(){
        System.out.println("upload");
    }

    public static void quit(){
        System.out.println("quit");
    }

}
