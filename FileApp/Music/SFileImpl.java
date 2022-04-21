import java.io.*;
import java.net.URL;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
public class SFileImpl extends UnicastRemoteObject implements FileInterface {

    private String name;
    private final String dataFolder = "songs/";

    public SFileImpl(String s) throws RemoteException {
        super();
        name = s;
    }


    public byte[] downloadSong(String fileName) {
        try {
            File file = new File(dataFolder + fileName);
            byte[] buffer = new byte[(int) file.length()];
            BufferedInputStream input = new BufferedInputStream(new FileInputStream(dataFolder + fileName));
            System.out.println(super.getClientHost() + " has requested for file '" + fileName + "'");
            input.read(buffer, 0, buffer.length);
            input.close();
            return (buffer);
        } catch (Exception e) {
            System.out.println("SFileImpl download error: " + e.getMessage());
            e.printStackTrace();
            return (null);
        }
    }


    public void uploadSong(byte[] content, String fileName) {
        try {
            File file = new File(fileName);
            BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(dataFolder + file));
            System.out.println(super.getClientHost() + " has uploaded file '" + fileName + "'");
            output.write(content);
            output.close();
        } catch (Exception e) {
            System.out.println("SFileImpl upload error: " + e.getMessage());
            e.printStackTrace();
            return;
        }
    }


    public void deleteSong(String fileName) {
        try {
            System.out.println("Deleting: " + fileName);
            new File(dataFolder + fileName).delete();
        } catch (Exception e) {
            System.out.println("SFileImpl delete error: " + e.getMessage());
            e.printStackTrace();
            return;
        }
    }


    public ArrayList<String> checkAvailableSongs() {
        ArrayList<String> list = new ArrayList<String>();
        try {
            for (File fileEntry : new File(dataFolder).listFiles()) {
                list.add(fileEntry.getName());
            }
        } catch (Exception e) {
            System.out.println("SFileImpl check available songs error: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }


    public byte[] downloadImage(int size) {
        try {
            BufferedImage image = ImageIO
                    .read(new URL("https://loremflickr.com/" + size + "/" + size + "/post+malone"));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", baos);
            byte[] buffer = baos.toByteArray();
            return (buffer);
        } catch (Exception e) {
            System.out.println("SFileImpl image download error: " + e.getMessage());
            e.printStackTrace();
            return (null);
        }
    }
}