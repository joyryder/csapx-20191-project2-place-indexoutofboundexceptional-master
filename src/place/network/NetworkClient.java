package place.network;

import place.PlaceBoard;
import place.PlaceBoardObservable;
import place.PlaceException;
import place.PlaceTile;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Network client for place that handles the moving, message displays
 * waiting functionality, and run checking.
 * @author Joey Saltalamacchia
 * @author Shrif Rai
 * @author Miguel Rosario
 */
public class NetworkClient {

    /**The set wait time for the user to wait in between each turn*/
    private final static int WAIT = 500;

    /**New board object*/
    private PlaceBoardObservable board;

    /**New socket serverConn*/
    private Socket serverConn;

    /**User In*/
    private ObjectInputStream in;

    /**User Out*/
    private ObjectOutputStream out;

    /**the message that gets displayed to the user*/
    private String displayMessage;

    /**the time to wait*/
    private boolean coolDown;

    /**boolean value to see if the game is still running*/
    private boolean running;

    /**
     * Sends a value to notify if the game is running or not
     * @return boolean
     */
    public boolean running() { return this.running; }

    /**
     * A method that turns the running boolean false to notify the game
     * is no longer running
     */
    public void stop() { this.running = false; }


    /**
     * Create a new network client with the given args.
     * once that args have been passed succesfully, the game will
     * then commence.
     * @param host hostname
     * @param port port number
     * @param username username
     * @param className className (used for indicating what is happening when like in the Bees lab)
     * @param board a board
     * @throws PlaceException placeException
     */
    public NetworkClient(String host,
                         int port,
                         String username,
                         String className,
                         PlaceBoardObservable board) throws PlaceException {
        try {
            //Display the class being used.
            this.displayMessage = "*" + className + "*: ";

            //new socket server connection
            this.serverConn = new Socket(host, port);

            //instantiating board
            this.board = board;

            //outputs the serverconn
            this.out = new ObjectOutputStream(serverConn.getOutputStream());
            out.flush();

            //inputs serverconn info
            this.in = new ObjectInputStream(serverConn.getInputStream());

            out.writeUnshared(new PlaceRequest<>(PlaceRequest.RequestType.LOGIN, username));
            PlaceRequest<?> response = (PlaceRequest<?>) in.readUnshared();

            //Attempts to log onto the server
            switch (response.getType()) {
                //if it does then it will display a success message.
                case LOGIN_SUCCESS:
                    displayMessage("Joined the server under alias: \"" + response.getData() + "\".");
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

            PlaceRequest<?> boardResponse = (PlaceRequest<?>) in.readUnshared();

            //If there is not request for the board being transmitted then the board
            //has not been received and will fail
            if (boardResponse.getType() != PlaceRequest.RequestType.BOARD)
                throw new PlaceException("Board not received.");
            else
            //else the board will be created and the game will now be running
                this.board.createBoard((PlaceBoard) boardResponse.getData());
            this.running = true;
        } catch (IOException | ClassNotFoundException | PlaceException e) {
            System.exit(0);
            throw new PlaceException(e);
        }
    }

    /**
     * start method that creates a thread via a lambda and runs
     * immediately unless theres an IO exception. IF it can run
     * then it will start.
     */
    public void start()
    {
        new Thread(() -> {
            try {
                run();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Method that makes a place request and sends the input of the user over to the board
     * to make a move and the server will then display it
     * @throws IOException e
     */
    private void run() throws IOException {
        //while running
        while(this.running()) {
            try {
                //try to make a place request that sends the input in
                //unshared: Reads an "unshared" object from the ObjectInputStream.  This method is
                //          * identical to readObject, except that it prevents subsequent calls to
                //          * readObject and readUnshared from returning additional references to the
                //          * deserialized instance obtained via this call.
                PlaceRequest<?> request = ( PlaceRequest<?> ) this.in.readUnshared();

                //once request sent
                //switch statement that takes in a type
                switch(request.getType()) {
                    //if the TILE_CHANGED, then make a move and display it with the tile
                    case TILE_CHANGED:
                        moveDisplay( (PlaceTile) request.getData() );
                        break;

                    //if theres an error, display an error by string and send it.
                    case ERROR:
                        error( (String) request.getData() );
                        break;
                    default:
                        cantConnect();
                }
            }
            catch(IOException | ClassNotFoundException e) {
                disconnected();

                this.stop();
            }
        }
        this.close();
    }

    /**
     * method that sends the tile to be placed.
     * If the cooldown is still in place (.5s) then the user will get a wait message.
     * once the cooldown is over the user can proceed to place another tile that will be sent
     * via writeUnshared.
     * @param tile tile
     * @throws IOException e
     */
    public synchronized void sendTile(PlaceTile tile) throws IOException {
        //if there is a cooldown, display error message that you can't make a move yet
        if (this.coolDown)
            displayErrorMessage("Wait 0.5s before making a move.");
        //else send a thread that's created in a lambda to send the tile if there is not a cooldown.
        else {
            this.out.writeUnshared(new PlaceRequest<>(PlaceRequest.RequestType.CHANGE_TILE, tile));
            out.flush();
            new Thread(() -> {
                try {
                    coolDown();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    /**
     * A method that forces the threads (player moves) to wait a set amount of time.
     * (Currently 5s, but subject to change)
     * @throws InterruptedException IE
     */
    private void coolDown() throws InterruptedException {
        this.coolDown = true;
        Thread.sleep(WAIT);
        this.coolDown = false;
    }


    /**
     * update the board with the new move of the tile.
     * @param tile tile
     */
    private void moveDisplay(PlaceTile tile) {
        this.board.moveDisplay(tile);
    }

    /**
     * Displays an error message and the error that the server threw.
     * @param error err
     */
    private void error(String error) {
        displayErrorMessage("Server responded with error message: \"" + error + "\"");
        this.stop();
    }

    /**
     * If the user can't connect to the server, disconnect.
     */
    private void cantConnect() {
        displayErrorMessage("Can't connect to server.");
        this.stop();
    }

    /**
     * when disconnect is called, displays an error message that the connection
     * to the server disconnected.
     */
    private void disconnected() {
        displayErrorMessage("Lost connection to server.");
    }

    /**
     * displays a nice regular message that lets you know that you're doing okay.
     * @param msg msg
     */
    public void displayMessage(String msg) {
        System.out.println(displayMessage + msg);
    }

    /**
     * produces a nice red message to let you know that you screwed up somewhere.
     * @param msg error
     */
    public void displayErrorMessage(String msg) {
        System.err.println(displayMessage + msg);
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

}