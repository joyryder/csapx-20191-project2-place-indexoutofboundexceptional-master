package place.bots;

import place.PlaceColor;
import place.PlaceException;
import place.PlaceTile;
import place.network.PlaceRequest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

/**
 * erases endlessly
 *
 * @author Shrif Rai
 */
public class eraser extends Thread
{
    /**New socket serverConn*/
    private Socket serverConn;

    /**User In*/
    private ObjectInputStream in;

    /**User Out*/
    private ObjectOutputStream out;
    String hostName, username;
    int row, col,port,color;


    public static void main(String[] args) throws InterruptedException {
        if (args.length != 2) {
            System.out.println("Usage: java PlaceGUI host port");
            System.exit(-1);
        }
        ArrayList<Thread> threadList = new ArrayList<>();
        int x = 0;
        //only accepts 9 bots at a time
        for (int j = 0; j < 7; j++)
        {

            eraser t1 = new eraser(0, j, 0, args[0], Integer.parseInt(args[1]), "eraser" + x);
            t1.start();
            threadList.add(t1);
            x++;

        }
        System.out.println(threadList.size());
        System.out.println("reached");
        //Thread.sleep(50000);
        for (Thread g: threadList) {
            try {
                g.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public eraser(int row, int col, int color, String host, int port, String username) {
        this.row = row;
        this.col = col;
        this.color = color;
        this.port = port;
        this.hostName = host;
        this.username = username;
    }


    @Override
    public void run()
    {
        //new socket server connection
        try {
            this.serverConn = new Socket(hostName, port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //outputs the serverconn
        try {
            this.out = new ObjectOutputStream(serverConn.getOutputStream());
            out.flush();

            //inputs serverconn info
            this.in = new ObjectInputStream(serverConn.getInputStream());
            execute();
            this.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public synchronized void execute() {
        try {
            out.writeUnshared(new PlaceRequest<>(PlaceRequest.RequestType.LOGIN, username));
            PlaceRequest<?> response = (PlaceRequest<?>) in.readUnshared();

            //Attempts to log onto the server
            switch (response.getType()) {
                //if it does then it will display a success message.
                case LOGIN_SUCCESS:
                    System.out.println("Joined the server under alias: \"" + response.getData() + "\".");
                    break;
                //otherwise it will show a fail message
                case ERROR:
                    displayErrorMessage("Failed to join server.");
                    displayErrorMessage("Server response: " + response.getData() + ".");
                    this.close();
                    throw new PlaceException("Unable to join.");
                    //base case is a fail case
                default:
                    displayErrorMessage("Bad response. Disconnecting.");
                    this.close();
                    throw new PlaceException("Unable to join.");
            }
            while(true) {
                while (col < 10) {
                    while (row < 10) {
                        this.out.writeUnshared(new PlaceRequest<>(PlaceRequest.RequestType.CHANGE_TILE, new PlaceTile(col, row, username, PlaceColor.values()[this.color], System.currentTimeMillis())));
                        out.flush();
                        row++;
                    }
                    row = 0;
                    col+=2;
                }
                col = 0;
            }
        } catch (IOException | ClassNotFoundException | PlaceException e) {
            e.printStackTrace();
        }
    }
    /**
     * Close all of the threads/server connections
     * @throws IOException IOE
     */
    public void close() throws IOException {
        this.serverConn.close();
        this.in.close();
        this.out.close();
    }

    private void displayErrorMessage(String s) {
        System.err.println(s);
    }
}
