import java.io.*;
import java.nio.file.Files;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.rmi.*;
import javax.swing.*;
import java.util.ArrayList;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import jaco.mp3.player.MP3Player;

public class MusicPlayer {

    private static final int FRAMEX = 640, FRAMEY = 480, HALFX = 320, HALFY = 240;
    private static final Font buttonFont = new Font("Arial", Font.BOLD, 24);
    private static final Font uploadButtonFont = new Font("Arial", Font.BOLD, 12);
    private static final Font nowPlayingFont = new Font("Arial", Font.BOLD, 16);
    private static final Font playlistFont = new Font("Arial", Font.BOLD, 18);
    private static final String dataFolder = "songs/";
    private static ArrayList<String> playlist;
    private static int nowPlayingID;
    private static MP3Player player;
    private static JTable list;
    private static JScrollPane sp;
    private static JLabel image;
    private static JLabel ipl ;
    private static JTextField ip;
    private static JTextField port;
    private static JLabel portl;
    public static void getAlbumArt(FileInterface fi) throws Exception {
        //byte[] imagedata = fi.downloadImage(80);
        //ByteArrayInputStream bis = new ByteArrayInputStream(imagedata);
        // image = new JLabel();
        // image.setIcon(new ImageIcon(ImageIO.read(bis)));
        // image.setBounds(20, FRAMEY - 120, 80, 80);
    }

    public static void syncPlaylist(FileInterface fi) throws Exception {
        System.out.println("Syncing playlist from server");
        for (String s : fi.checkAvailableSongs()) {
            if (new File(dataFolder + s).exists())
                continue;
            byte[] filedata = fi.downloadSong(s);
            BufferedOutputStream output = new BufferedOutputStream(
                    new FileOutputStream("songs/" + new File(s).getName()));
            output.write(filedata, 0, filedata.length);
            output.flush();
            output.close();
        }
    }


    public static void startPlayer() throws Exception {
        player = new MP3Player();
        playlist = new ArrayList<String>();
        for (File fileEntry : new File(dataFolder).listFiles()) {
            player.addToPlayList(new File(dataFolder + fileEntry.getName()));
            playlist.add(fileEntry.getName());
        }
        nowPlayingID = 0;
        player.setRepeat(true);
        player.play();
    }


    public static void configurePlaylist(JFrame window) throws Exception {
        // convert playlist data to 2D array
        String[][] data = new String[playlist.size()][2];
        String[] cols = {"#", "Name"};
        for (int i = 0; i < playlist.size(); i++) {
            data[i][0] = String.valueOf(i + 1);
            data[i][1] = playlist.get(i);
        }
        // initialize and configure components, add them to window
        list = new JTable(data, cols);
        sp = new JScrollPane(list);
        list.setFont(playlistFont);
        list.setRowHeight(40);
        list.getColumnModel().getColumn(0).setMaxWidth(40);
        list.getColumnModel().getColumn(0).setHeaderValue("#");
        list.getColumnModel().getColumn(1).setHeaderValue("Name");
        list.setEnabled(false); // rows can't be clicked
        list.addRowSelectionInterval(nowPlayingID, nowPlayingID); // highlight current track
        sp.setBounds(10, 10, 620, 300);
        window.add(sp);
    }


    public static void main(String[] args) throws Exception {
        JFrame frame = new JFrame();
        String  ip = JOptionPane.showInputDialog("Enter IP Adress:", "127.0.0.1");
        String port = JOptionPane.showInputDialog("Enter Port Number:","2000");

        if (port!=""  || ip!="0") {
            // RMI connect to music server

            FileInterface fi = (FileInterface) Naming.lookup("rmi://" + ip + ":" + port + "/SFileImpl");
            // initialize window
            JFrame window = new JFrame("Music Player");
            // get album art and add it to the window
            // getAlbumArt(fi);
            window.setTitle("Music Player");
            // download new songs from server
            syncPlaylist(fi);
            // read files, add them to playlist, set now playing, start player
            startPlayer();
            // initialize and configure playlist, add to window
            configurePlaylist(window);

            // initialize now playing message and add to window
            JLabel nowPlayingLabel = new JLabel();
            nowPlayingLabel.setText("Now Playing: " + playlist.get(nowPlayingID));
            nowPlayingLabel.setFont(nowPlayingFont);
            nowPlayingLabel.setBounds(10, 330, 620, 20);
            window.add(nowPlayingLabel);

            // initialize play/pause button and add to window
            JButton playButton = new JButton("Play");
            playButton.setFont(buttonFont);
            playButton.setBounds(HALFX - 40, FRAMEY - 120, 80, 80);
            window.add(playButton);

            // initialize next button and add to window
            JButton nextButton = new JButton("Next");
            nextButton.setFont(buttonFont);
            nextButton.setBounds(HALFX + 40, FRAMEY - 120, 80, 80);
            window.add(nextButton);

            // initialize previous button and add to window
            JButton previousButton = new JButton("Previous");
            previousButton.setFont(buttonFont);
            previousButton.setBounds(HALFX - 120, FRAMEY - 120, 80, 80);
            window.add(previousButton);

            // initialize upload button and add to window
            JButton uploadButton = new JButton("Upload");
            uploadButton.setFont(uploadButtonFont);
            uploadButton.setBounds(FRAMEX - 140, FRAMEY - 115, 100, 30);
            window.add(uploadButton);

            // initialize delete button and add to window
            JButton deleteButton = new JButton("Delete");
            deleteButton.setFont(uploadButtonFont);
            deleteButton.setBounds(FRAMEX - 140, FRAMEY - 75, 100, 30);
            window.add(deleteButton);

            // initialize file chooser, add filter for .mp3 files
            JFileChooser uploadChooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter("MP3 Files", "mp3");
            uploadChooser.setFileFilter(filter);
            uploadChooser.setBounds(0, 0, 620, 460);

            // configure JFrame
            window.setSize(FRAMEX, FRAMEY);
            window.setResizable(true);
            window.setLayout(null);
            window.setLocationRelativeTo(null);
            window.setVisible(true);
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // action listeners for buttons
            playButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    // if currently playing, pause
                    if (!player.isPaused()) {
                        player.pause();
                        playButton.setText("pause");
                    }
                    // if currently paused, play
                    else {
                        player.play();
                        playButton.setText("Play");
                    }
                }
            });

            // listen for when next button is pressed
            nextButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    // if player was paused, set it to play
                    if (player.isPaused()) {
                        player.play();
                        playButton.setText("Play");
                    }
                    // go to next track
                    player.skipForward();
                    // if we're on last track, go to first
                    if (nowPlayingID == playlist.size() - 1)
                        nowPlayingID = 0;
                    else
                        nowPlayingID++;
                    // set now playing and playlist highlight
                    nowPlayingLabel.setText("Now Playing: " + playlist.get(nowPlayingID));
                    list.clearSelection();
                    list.addRowSelectionInterval(nowPlayingID, nowPlayingID);
                }
            });

            // listen for when previous button is pressed
            previousButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    // if player was paused, set it to play
                    if (player.isPaused()) {
                        player.play();
                        playButton.setText("Play");
                    }
                    // go to previous track
                    player.skipBackward();
                    // if we're on first track, go to last
                    if (nowPlayingID == 0)
                        nowPlayingID = playlist.size() - 1;
                    else
                        nowPlayingID--;
                    // set now playing and playlist highlight
                    nowPlayingLabel.setText("Now Playing: " + playlist.get(nowPlayingID));
                    list.clearSelection();
                    list.addRowSelectionInterval(nowPlayingID, nowPlayingID);
                }
            });

            // listen for when upload button is pressed
            uploadButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    // show file chooser window
                    int returnVal = uploadChooser.showOpenDialog(window);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = uploadChooser.getSelectedFile();
                        System.out.println("Uploading: " + file.getName() + ".");
                        try {
                            // request upload to server
                            fi.uploadSong(Files.readAllBytes(file.toPath()), file.getName());
                            // reconfigure playlist and restart player
                            player.stop();
                            player = null;
                            syncPlaylist(fi);
                            startPlayer();
                            playButton.setText("Play");
                            configurePlaylist(window);
                        } catch (Exception e) {
                            System.out.println("Client error uploading file: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
            });

            // listen for when delete button is pressed
            deleteButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    System.out.println("Deleting: " + playlist.get(nowPlayingID));
                    try {
                        // delete from client and request delete on server
                        new File(dataFolder + playlist.get(nowPlayingID)).delete();
                        fi.deleteSong(playlist.get(nowPlayingID));
                        // reconfigure playlist and restart player
                        player.stop();
                        player = null;
                        syncPlaylist(fi);
                        startPlayer();
                        playButton.setText("Play");
                        configurePlaylist(window);
                    } catch (Exception e) {
                        System.out.println("Client error deleting file: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
        }
        else {
            JOptionPane.showMessageDialog(frame, "In Valid Ip or Port",
                    "Swing Tester", JOptionPane.WARNING_MESSAGE);
        }
    }

} // if block
