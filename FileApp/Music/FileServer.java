import java.rmi.*;

public class FileServer {

    public static void main(String argv[]) {
        System.out.println("Starting music server..............");

        try {
            System.out.println("Starting a new file interface");
            FileInterface fi = new SFileImpl("MusicServer");
            Naming.rebind("rmi://127.0.0.1:2000/SFileImpl", fi);
        } catch (Exception e) {
            System.out.println("MusicServer: " + e.getMessage() +" connection error from FileServer.java");
            e.printStackTrace();
        }
    }
}