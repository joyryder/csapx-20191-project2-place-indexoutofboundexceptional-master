package place.server;

import place.PlaceBoard;
import place.PlaceException;
import place.PlaceTile;
import place.network.PlaceRequest;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

/**
 * The Place server is run on the command line as:
 *
 * $ java PlaceServer port DIM
 *
 * Where port is the port number of the host and DIM is the square dimension
 * of the board.
 *
 * @author Sean Strout @ RIT CS
 * @author Miguel Rosario
 * @author Joey Saltalamacchia
 * @author Shrif Rai
 */

public class PlaceServer implements Closeable {

    private ServerSocket server;
    private Map<String, ClientThreads> clients;
    private static int dim;
    private static int port;
    private PlaceBoard board;
    private boolean running = true;

    private PlaceServer(int port) throws PlaceException {
        try {
            this.server = new ServerSocket(port);
            clients = new HashMap<>();
            board = new PlaceBoard(dim);


        } catch (IOException e) {
            throw new PlaceException(e);
        }

    }

    /**
     * Method that starts the running of the server and creates a thread with no username
     */
    private void run() {
        while (running) {
            while (running) {
                try {
                    new Thread(new ClientThreads(server.accept(), "", this)).start();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception ignored) {
                }
            }
        }
    }


        /**
         * Method to close the server when called.
         */
    public void close(){
        //close the server
        try {
            this.server.close();
        }
        //yes
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Getter for the board
     * @return board
     */
    public PlaceBoard getBoard() {
        return board;
    }


    /**
     * Method for a thread (user) to log into the server.
     * Checks the threads to ensure 2 users with the same name
     * cannot join the server.
     * @param username user
     * @param client the client thread trying to join
     * @return boolean
     */
    public synchronized boolean login(String username, ClientThreads client) throws IOException {

        System.out.println("login");
        //Fail message to output if reached
        String fail = "Login error. There is a user with the name \"" + username + "\" already";

        //For each key value of the client, check if
        if(clients.containsKey(username))
            try {
                //Send a new serverError
                PlaceRequest<?> serverError = new PlaceRequest<>(PlaceRequest.RequestType.ERROR,
                        fail);
                client.getOutput().writeObject(serverError);
                client.getOutput().flush();
                System.out.println(serverError + (username));

                return false;
            } catch (IOException ignored){}
        //Put the client user and output stream
        clients.put(username,client);

        //State that they connected properly
        System.err.println("Connection Success.");

        //Display how many people are connected to the server
        System.err.println("There are " + clients.size() + " users connected to the server.");

        //Display the names of the users
        System.err.println("Client list: " + clients.keySet());
        try {
            client.getOutput().writeObject(new PlaceRequest<>(PlaceRequest.RequestType.LOGIN_SUCCESS, "Welcome " + username));
            client.getOutput().flush();
        }
        catch (IOException ignored) {

        }
        return true;
    }

    /**
     * removes the user from the list and notifies the server
     *
     * @param username the user signing out
     */
    public void signOut(String username)
    {
        //remove the user from the client list
        this.clients.remove(username);

        //state that the user diconnected properly
        System.err.println(username + " disconnected from server.");

        //Display how many people are connected to the server
        System.err.println("There are " + clients.size() + " users connected to the server.");

        //Display the names of the users
        System.err.println("Client list: " + clients.keySet());
    }

    /**
     * When called it updates the logins of the user and removes the person who left.
     * Throws a "error" - red - message that shows who logged off.
     * @param username user
     */
    public synchronized void updateLogins(String username){
        //Remove the user who left
        try {
            clients.get(username).close();
            clients.remove(username);
            System.err.println(username + " logged off." + clients.keySet());

        }
        catch (IOException e){}
    }

    /**
     * Method to update the tile that was moved/changed
     * a tile has a time for the timestamp that gets updated based on when
     * the input occurs.
     *
     * once the time is stamped, set the board with the tile.
     *
     * output a success message and then send the tile.
     * @param tile tile
     */
    public synchronized void tileUpdate(PlaceTile tile){
        tile.setTime(getTime());
        board.setTile(tile);
        System.out.println("The tile has been updated.");
        sendTile(tile);
    }

    /**
     * Method that gets the time for the timestamps
     * @return long
     */
    private static long getTime(){
        return System.currentTimeMillis();
    }

    /**
     * Method that sends the tile to the server if its a valid move.
     * @param tile tile
     */
    private void sendTile(PlaceTile tile) {
        //For each username send the message
        clients.keySet().forEach(username -> {
            System.out.println("Sending " + tile + " to " + username);
            // try to move the tile.
            try {
                //write the object with the new tile that has been changed
                clients.get(username).getOutput().writeObject(new PlaceRequest<>
                        (PlaceRequest.RequestType.TILE_CHANGED, tile));

                //flushed
                    clients.get(username).getOutput().flush();

            } catch (IOException e) {
            e.printStackTrace();
        }
        });
    }


    /**
     * The main method starts the server and spawns client threads each time a new
     * client connects.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // if the args aren't correct throw a fit
        if(args.length != 2) {
            System.err.println("Please run the server as:");
            System.err.println("$ java PlaceGUI host port username");
            System.exit(0);
        }

        //Taking the values from the passed args.
        port = Integer.parseInt(args[0]);
        dim = Integer.parseInt(args[1]);

        //try to make a server
        try ( PlaceServer server = new PlaceServer(port) ) {
            System.out.println("The server is now running!");
            server.run();
        }
        //otherwise error
        catch (PlaceException ignored) {
        }

    }
}
