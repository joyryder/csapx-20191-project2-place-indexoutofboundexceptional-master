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


/**
 * ##          ##
 *                                                 ##      ##         )  )
 *                                               ##############
 *                                             ####  ######  ####
 *                                           ######################
 *                                           ##  ##############  ##     )   )
 *                                           ##  ##          ##  ##
 *                                                 ####  ####
 *
 *
 *
 *                                                         ##
 *                                                       ##
 *                                                         ##
 *                                                           ##
 *                                                         ##
 *                                                       ##
 *                                                         ##
 *
 * a bot that draws a space invader
 * (only works on an 8x8 board)
 *
 * author Joseph Saltalamacchia
 */
public class ALIENS extends Run implements Observer {

    //monitorts the current row the bot is on
    private int row = 0;
    //monitors the current column the bot is on
    private int col = 0;

    /**
     * Grabs the username of the player
     */
    private String username;

    private String hosting;
    private int porting;
    private PlaceColor currentColor = PlaceColor.BLACK;
    private boolean isBlack = true;
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
    public synchronized void init() throws Exception {

        //Create a list of strings that holds the args passed in.
        List<String> parameters = super.getArguments();

        this.hosting = parameters.get(0);
        this.porting = Integer.parseInt(parameters.get(1));

        //Grabbing the username
        this.username = "ALIENS" + (Math.random() * 100);

        //Create a new observable board
        this.model = new PlaceBoardObservable();

        try {

            //Creates a new NetWorkClient that gets passed a host name
            // a port number, the username of the player, the classname and the model.
            this.serverConn = new NetworkClient(
                    this.hosting,
                    this.porting,
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
     * @param in The scanner used for user input of moves.
     */
    @Override
    public synchronized void start(Scanner in) throws InterruptedException {
       //the bot will only try to place things if the board is an 8x8
        if(this.model.getDIM() == 8) {
           //Start by displaying the board
           try {
               init();
           } catch (Exception e) {
               e.printStackTrace();
           }

           displayBoard();

           //A message to the user on how to input the proper variables.
           this.serverConn.displayMessage("Enter where you would like to make a move. (Row Col Color):");

           //Message telling the user how to exit
           this.serverConn.displayMessage("A player may exit the game by entering just -1");
           //While the game is Running
           while (this.Running() && this.serverConn.running()) {

               //Split the inputs into segments of 3
               String[] userInput = new String[3]; // = in.nextLine().trim().split(" ");



                   userInput[0] = String.valueOf(row);
                   userInput[1] = String.valueOf(col);

               //checks any location that's supposed to be black, and sets it's color to black, otherwise set the color to green
                   if ((this.row == 1 && this.col == 0) || (this.row == 3 && this.col == 0) || (this.row == 4 && this.col == 0) ||
                           (this.row == 5 && this.col == 0) ||(this.row == 6 && this.col == 0) || (this.row == 2&& this.col == 1) ||
                           (this.row == 3 && this.col == 1) || (this.row == 4 && this.col == 1) || (this.row == 5 && this.col == 1) ||
                           (this.row == 0 && this.col == 2) || (this.row == 1 && this.col == 2) || (this.row == 0 && this.col == 1) ||
                           (this.row == 6 && this.col == 2) || (this.row == 7 && this.col == 2) || (this.row == 0 && this.col == 3) ||
                           (this.row == 2 && this.col == 3) || (this.row == 5 && this.col == 3) || (this.row == 7 && this.col == 3) ||
                           (this.row == 0 && this.col == 4) || (this.row == 1 && this.col == 4) || (this.row == 6 && this.col == 4) ||
                           (this.row == 7 && this.col == 4) || (this.row == 1 && this.col == 6) || (this.row == 3 && this.col == 6) ||
                           (this.row == 4 && this.col == 6) || (this.row == 6 && this.col == 6) || (this.row == 1 && this.col == 7) ||
                           (this.row == 6 && this.col == 7) || (this.row == 2 && this.col == 0) || (this.row == 7 && this.col == 1)) {
                       this.currentColor = PlaceColor.BLACK;
                   }
                   else
                   {
                       this.currentColor = PlaceColor.GREEN;
                   }

                   this.row++;
                   if(this.row == model.getDIM()) {
                       this.col++;
                       this.row = 0;
                   }
                   if (this.col == model.getDIM())
                   {
                       this.col = 0;
                   }


                   userInput[2] = this.currentColor.toString();

               System.out.println(row);
               System.out.println(col);



               System.out.println(userInput[0] + " " + userInput[1] + " " + userInput[2]);

               //exit if the user inputs =1
               if (userInput[0].equals("-1")) {
                   this.serverConn.displayMessage("Exit command has been read. Exiting PTUI.");
                   this.Running = false;
               }

               //If the length of the array is not 3 (Proper amount), then display error message.
               else if (userInput.length != 3) {
                   this.serverConn.displayErrorMessage("To make a move you must enter 3 numbers.\n" +
                           "(Row Col Color[0-15])");
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

                       //else display an error message stating the color choices.
                       else {
                           this.serverConn.displayErrorMessage("Please enter a color value between 0-15.");
                           this.serverConn.displayErrorMessage("\nBLACK 0, GRAY 1, SILVER 2, WHITE 3, MAROON 4, RED 5\n" +
                                   "OLIVE 6, YELLOW 7, GREEN 8, LIME 9, TEAL 10,\n" +
                                   "AQUA 11, NAVY 12, BLUE 13, PURPLE 14, FUCHSIA 15");
                       }
                   } catch (NumberFormatException e) {
                       this.serverConn.displayErrorMessage("Please only enter integer numbers numberformat");
                   } catch (IOException ignored) {
                   }
               }//end else
           }//end while
       }//end if
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
            System.err.println("HOSTNAME PORT ");
            System.exit(0);
        }
        Run.launch(ALIENS.class, args);
    }
}
