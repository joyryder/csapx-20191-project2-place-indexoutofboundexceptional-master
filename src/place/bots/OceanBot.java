package place.bots;

import place.PlaceBoardObservable;
import place.PlaceColor;
import place.PlaceException;
import place.PlaceTile;
import place.client.ptui.Run;
import place.network.NetworkClient;

import java.io.IOException;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/*
         ~^~^~^~^~^~^~^~^~^~^~^^~^~^~^~^~^~
           ~                            ~
                \   '    o      '
                /\ o       \  o
              >=)'>    '   /\ '
                \/   \   >=)'>        ~
                /    /\    \/
         ~         >=)'>   /     .
                     \/                   )
                     /                   (
                           ~          )   )
           }     ~              (    (   (
          {                      )    )   )
           }  }         .       (    (   (
          {  {               /^^^^^^^^^^^^jgs
         ^^^^^^^^^\         /
                   ^^^^^^^^^
 */
/**
 * A Rehash of a Rehash of the PTUI and FORBIDDEN bot that just makes everything blue
 *
 * @author Miguel Rosario
 */
public class OceanBot extends Run implements Observer {

    /**
     * Grabs the username of the player
     */
    private String username;

    /**
     * Grabs an observable board that can be updated.
     */
    private PlaceBoardObservable model;

    /**
     * creates a network client that is used to connect to a server
     */
    private NetworkClient serverConn;

    /**
     * Creates a boolean value, Running, true/false
     */
    private boolean Running;

    private synchronized boolean Running() {
        return this.Running;
    }

    /**
     * init function that calls to start the game  by taking the hostname
     * port number username class name and board and then sending all
     * of the information over to the network client.
     *
     * @throws Exception
     */
    @Override
    public void init() throws Exception {

        //Create a list of strings that holds the args passed in.
        List<String> parameters = super.getArguments();

        String hosting = parameters.get(0);
        int porting = Integer.parseInt(parameters.get(1));
        //Grabbing the hostname the port number and the username
        String host = parameters.get(0);

        //Grabbing the port
        int port = Integer.parseInt(parameters.get(1));

        //Grabbing the username
        this.username = "Ocean"
                + (Math.random() * 100);

        //Create a new observable board
        this.model = new PlaceBoardObservable();

        try {

            //Creates a new NetWorkClient that gets passed a host name
            // a port number, the username of the player, the classname and the model.
            this.serverConn = new NetworkClient(
                    host,
                    port,
                    this.username,
                    getClass().getSimpleName(),
                    this.model);
        }

        //Throws a PlaceException if theres an error and then closes the server.
        catch (PlaceException e) {
            System.exit(0);
            this.serverConn.close();
        }

        //If you get here then the server starts
        this.serverConn.start();

        //Add model to observer and update the board when possible.
        this.model.addObserver(this);

        //The game is now Running therefore the boolean is true.
        //Otherwise default false.
        this.Running = true;
    }

    /**
     * overriden start method that first displays the bird,
     * sends a server display method, and then run the game.
     * While the game is running it takes in the line of 3 inputted values
     * (Row col color) and splits/trims the line into single sets that get passed into
     * processors. if those values are between 0-15 it will process the board and send the information.
     * once that is done it sends the board back.
     *
     * EDITED SLIGHTLY to account for the bot that is running on this server.
     * Takes in the input almost the same but splits in while in a for loop
     * generating the moving randomly from the math class.
     *
     * @param in The scanner used for user input of moves.
     */
    @Override
    public void start(Scanner in) throws InterruptedException {
        //Start by displaying the board
        displayBoard();

        //A message to the user on how to input the proper variables.
        this.serverConn.displayMessage("Enter where you would like to make a move. (Row Col Color):");

        //Message telling the user how to exit
        this.serverConn.displayMessage("A player may exit the game by entering just -1");
        //While the game is Running
        while (this.Running() && this.serverConn.running()) {

            //Split the inputs into segments of 3
            String[] userInput = new String[3];

            //new command here that splits the array into 3 randomly generated variables.
            for (int i = 0; i < 3; i++) {
                userInput[0] = String.valueOf((int) (Math.random() * model.getDIM()));
                userInput[1] = String.valueOf((int) (Math.random() * model.getDIM()));
                userInput[2] = String.valueOf((int) (Math.floor(Math.random() * 13) + 10));
                    if(userInput[2].equals("14") || userInput[2].equals("15")){
                        userInput[2] = "13";
                    }

            }
            System.out.println(userInput[0] + " " + userInput[1] + " " + userInput[2]);

            //exit if the user inputs =1
            if (userInput[0].equals("-1")) {
                this.serverConn.displayMessage("Exit command has been read. Exiting PTUI.");
                this.Running = false;
            }

            //else proper input length detected try to process values.
            else {

                //try to process the inputted values
                try {

                    //the split string will be assigned to a row, col and color variable.
                    int row = Integer.parseInt(userInput[0]);
                    int col = Integer.parseInt(userInput[1]);
                    int color = Integer.parseInt(userInput[2]);

                    //making sure the color is between 0 and 15
                    if (color <= 15 && color >= 0) {
                        TimeUnit.SECONDS.sleep(1);

                        //If it is within these bounds,
                        //create a new tile with that specific color and then place it
                        PlaceTile newTile = new PlaceTile(
                                row,
                                col,
                                this.username,
                                PlaceColor.values()[color],
                                System.currentTimeMillis());

                        /*
                        BLACK 0, GRAY 1, SILVER 2, WHITE 3, MAROON 4, RED 5
                        OLIVE 6, YELLOW 7, GREEN 8, LIME 9, TEAL 10,
                        AQUA 11, NAVY 12, BLUE 13, PURPLE 14, FUCHSIA 15
                        */

                        //send the new tile to be processed.
                        this.serverConn.sendTile(newTile);
                    }

                } catch (NumberFormatException | IOException ignored) {
                }
            }//end else
        }//end while
    }//end start method


    /**
     * Override method to close the server connection
     *
     * @throws IOException exception
     */
    @Override
    public void stop() throws IOException {
        this.serverConn.close();
    }


    /**
     * Displays the updated board and shows a message.
     */
    private void refreshBoard() {
        displayBoard();
        this.serverConn.displayMessage("Enter where you would like to make a move. (Row Col Color):");
    }

    /**
     * calls the board refresher.
     *
     * @param o   object
     * @param arg argument
     */
    @Override
    public void update(Observable o, Object arg) {
        assert o == this.model;
        refreshBoard();
    }


    /**
     * Display the board
     */
    private void displayBoard() {
        System.out.println(this.model.getBoard());
    }

    /**
     * Main method that sends the args passed in (Host port username).
     *
     * @param args arguments.
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            //Hostname Portnumber ThreadCount
            System.err.println("HOSTNAME PORT #OFTHREADSYOUWANTSOYOUCANKILLSERVERS!!!!!????");
            System.exit(0);
        }
        Run.launch(OceanBot.class, args);
    }
}
